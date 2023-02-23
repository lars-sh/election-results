package de.larssh.election.germany.schleswigholstein.local.file;

import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.ss.util.SheetUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellType;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STTotalsRowFunction;

import de.larssh.election.germany.schleswigholstein.Color;
import de.larssh.election.germany.schleswigholstein.District;
import de.larssh.election.germany.schleswigholstein.Keys;
import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.local.LocalElectionResult;
import de.larssh.election.germany.schleswigholstein.local.LocalNomination;
import de.larssh.election.germany.schleswigholstein.local.LocalNominationResult;
import de.larssh.election.germany.schleswigholstein.local.LocalNominationResultType;
import de.larssh.election.germany.schleswigholstein.local.LocalPollingStation;
import de.larssh.utils.OptionalInts;
import de.larssh.utils.annotations.PackagePrivate;
import de.larssh.utils.io.Resources;
import de.larssh.utils.text.Strings;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.experimental.UtilityClass;

/**
 * This class contains helper methods to write an Excel spreadsheet (XLSX) with
 * metrics of an election result.
 *
 * <p>
 * TODO: Information rows
 */
@UtilityClass
public class MetricsFiles {
	/**
	 * Formats and writes {@code result} to {@code outputStream}.
	 *
	 * @param result       the election result to to write
	 * @param outputStream the metrics file output stream
	 * @throws IOException on IO error
	 */
	public static void write(final LocalElectionResult result, final OutputStream outputStream) throws IOException {
		new MetricsFileWriter(result, outputStream).write();
	}

	/**
	 * This class writes metrics of a {@link LocalElectionResult} to an Excel
	 * spreadsheet (XLSX).
	 */
	private static class MetricsFileWriter {
		private static final String DATA_FORMAT_PERCENTAGE = "0.0%";

		private static final String DATA_FORMAT_PERCENTAGE_WITH_SIGN = "\\+0.0%;\\-0.0%;0.0%";

		private static final NumberFormat DECIMAL_FORMAT
				= new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.ROOT));
		static {
			DECIMAL_FORMAT.setMaximumFractionDigits(Integer.MAX_VALUE);
		}

		private static Row appendRow(final Sheet sheet) {
			return CellUtil.getRow(sheet.getLastRowNum() + 1, sheet);
		}

		private static XSSFTable createTable(final Sheet sheet, final String name) {
			final XSSFTable table = ((XSSFSheet) sheet).createTable(new AreaReference(new CellReference(0, 0),
					new CellReference(sheet.getLastRowNum(), CellUtil.getRow(0, sheet).getLastCellNum() - 1),
					SpreadsheetVersion.EXCEL2007));
			table.setName(name);
			table.setDisplayName(name);
			table.setStyleName("TableStyleLight1");
			table.getCTTable().addNewAutoFilter();
			return table;
		}

		private static String getDisplayValue(final LocalNominationResultType type) {
			if (type == LocalNominationResultType.DIRECT) {
				return "Direkt";
			}
			if (type == LocalNominationResultType.DIRECT_DRAW) {
				return "Los";
			}
			if (type == LocalNominationResultType.DIRECT_BALANCE_SEAT) {
				return "Mehrsitz";
			}
			if (type == LocalNominationResultType.LIST) {
				return "Liste";
			}
			if (type == LocalNominationResultType.LIST_DRAW) {
				return "Los";
			}
			if (type == LocalNominationResultType.LIST_OVERHANG_SEAT) {
				return "Überhang";
			}
			return "";
		}

		/**
		 * Loads a resource from a folder next to this class. The file name is build by
		 * concatenating the class name, a dash and {@code fileNameSuffix}.
		 *
		 * @param fileNameSuffix the file name's suffix
		 * @return the resource file content
		 */
		@SuppressFBWarnings(value = { "EXS_EXCEPTION_SOFTENING_NO_CONSTRAINTS", "PATH_TRAVERSAL_IN" },
				justification = "IOExceptions in here are not expected to be related to user input or behavior "
						+ "and fileNameSuffix is expected to be a constant value within this class.")
		private static String loadResourceRelativeToClass(final String fileNameSuffix) {
			final Class<?> clazz = MethodHandles.lookup().lookupClass();
			final String fileName = clazz.getSimpleName() + "-" + fileNameSuffix;
			final Path path = Resources.getResourceRelativeTo(clazz, Paths.get(fileName)).get();

			try {
				return new String(Files.readAllBytes(path), Strings.DEFAULT_CHARSET);
			} catch (final IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		private static void setCellValue(final Cell cell, final Number value) {
			final CTCell ctCell = ((XSSFCell) cell).getCTCell();
			ctCell.setT(STCellType.N);
			ctCell.setV(DECIMAL_FORMAT.format(value));
		}

		/**
		 * Election Result to write
		 *
		 * @return the election result to write
		 */
		LocalElectionResult result;

		/**
		 * Metrics file output stream
		 *
		 * @return the metrics file output stream
		 */
		OutputStream outputStream;

		Map<String, CellStyle> cellStyles = new HashMap<>();

		String dataFormatSainteLague;

		public MetricsFileWriter(final LocalElectionResult result, final OutputStream outputStream) {
			this.result = result;
			this.outputStream = outputStream;

			dataFormatSainteLague = result.getSainteLagueScale() == 0
					? "0"
					: String.format("0.%0" + result.getSainteLagueScale() + "d", 0);
		}

		@SuppressWarnings("resource")
		private <T> void appendCell(final Row row,
				final Optional<Party> party,
				final BiConsumer<Cell, T> setValue,
				final Optional<String> dataFormat,
				final Optional<T> value) {
			final Cell cell = CellUtil.getCell(row, Math.max(0, row.getLastCellNum()));
			getCellStyle(row.getSheet().getWorkbook(), party, dataFormat).ifPresent(cell::setCellStyle);
			if (value.isPresent()) {
				setValue.accept(cell, value.get());
			}
		}

		private void appendFormula(final Row row, final Optional<Party> party, final String value) {
			appendCell(row, party, Cell::setCellFormula, Optional.empty(), Optional.of(value));
		}

		private void appendFormula(final Row row,
				final Optional<Party> party,
				final String dataFormat,
				final String value) {
			appendCell(row, party, Cell::setCellFormula, Optional.of(dataFormat), Optional.of(value));
		}

		private void appendNumber(final Row row,
				final Optional<Party> cellStyle,
				final Optional<? extends Number> value) {
			appendCell(row, cellStyle, MetricsFileWriter::setCellValue, Optional.empty(), value);
		}

		private void appendString(final Row row, final Optional<Party> party, final String value) {
			appendCell(row, party, Cell::setCellValue, Optional.empty(), Optional.of(value));
		}

		private void appendStrings(final Row row, final String... values) {
			for (final String value : values) {
				appendString(row, Optional.empty(), value);
			}
		}

		private Optional<CellStyle> getCellStyle(final Workbook workbook,
				final Optional<Party> party,
				final Optional<String> dataFormat) {
			if (!party.isPresent() && !dataFormat.isPresent()) {
				return Optional.empty();
			}

			final String key = Keys.escape(party.map(Party::getKey).orElse(""), "TODO", "TODO", dataFormat.orElse(""));
			return Optional.of(cellStyles.computeIfAbsent(key, k -> {
				final CellStyle cellStyle = workbook.createCellStyle();

				party.ifPresent(p -> {
					cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

					final Color backgroundColor = p.getBackgroundColor();
					cellStyle.setFillForegroundColor(new XSSFColor(new byte[] {
							(byte) backgroundColor.getAlphaAsByte(),
							(byte) backgroundColor.getRedAsByte(),
							(byte) backgroundColor.getGreenAsByte(),
							(byte) backgroundColor.getBlueAsByte() }));

					final Color fontColor = p.getFontColor();
					final XSSFFont font = (XSSFFont) workbook.createFont();
					font.setColor(new XSSFColor(new byte[] {
							(byte) fontColor.getAlphaAsByte(),
							(byte) fontColor.getRedAsByte(),
							(byte) fontColor.getGreenAsByte(),
							(byte) fontColor.getBlueAsByte() }));
					cellStyle.setFont(font);
				});

				dataFormat.ifPresent(f -> cellStyle.setDataFormat(workbook.createDataFormat().getFormat(f)));
				return cellStyle;
			}));
		}

		/**
		 * Formats and writes {@link #result} to {@link #writer}.
		 *
		 * @throws IOException on IO error
		 */
		@PackagePrivate
		void write() throws IOException {
			try (XSSFWorkbook workbook = new XSSFWorkbook()) {
				workbook.setCellFormulaValidation(false);

				writeOverview(workbook.createSheet("Übersicht"));
				writeParties(workbook.createSheet("Gruppierungen"));
				writeBlockVotes(workbook.createSheet("Blockstimmen"));
				writeNominations(workbook.createSheet("Kandidierende"));

				// Auto Size Columns
				workbook.sheetIterator().forEachRemaining(sheet -> {
					final int numberOfColumns = sheet.getRow(0).getLastCellNum();
					for (int columnIndex = 0; columnIndex < numberOfColumns; columnIndex += 1) {
						sheet.autoSizeColumn(columnIndex);

						// Add the width of the auto filter
						double widthOfHeader = SheetUtil.getColumnWidth(sheet, columnIndex, false, 0, 0);
						if (widthOfHeader != -1) {
							// The maximum column width for an individual cell is 255 characters
							widthOfHeader = Math.min(widthOfHeader, 255);

							// Difference of a cell without and with auto filter in Excel
							widthOfHeader += 2.28515625;

							final int intWidth = (int) Math.round(256 * widthOfHeader);
							if (intWidth > sheet.getColumnWidth(columnIndex)) {
								sheet.setColumnWidth(columnIndex, intWidth);
							}
						}
					}
				});

				workbook.write(outputStream);
			}
		}

		private void writeOverview(final Sheet sheet) {
			// Header
			appendStrings(appendRow(sheet),
					"Wahlbezirk",
					"Wahlberechtigte",
					"Stimmzettel",
					"Wahlbeteiligung",
					"ausgezählt",
					"ausgezählt %",
					"ungültig");

			// Content
			for (final LocalPollingStation pollingStation : result.getElection().getPollingStations()) {
				writeOverviewOfDistrict(appendRow(sheet), pollingStation);
			}

			// Table
			sheet.createFreezePane(1, 0);
			final XSSFTable table = createTable(sheet, "Übersicht");
			table.getCTTable().getTableStyleInfo().setShowFirstColumn(true);
			table.getCTTable().getTableStyleInfo().setShowRowStripes(true);

			// Totals Row
			table.setDataRowCount(table.getDataRowCount() + 1);
			writeOverviewTotalsRow(appendRow(sheet), table.getCTTable());
		}

		private void writeOverviewOfDistrict(final Row row, final District<?> district) {
			// Wahlbezirk
			appendStrings(row, district.getName());

			// Wahlberechtigte
			appendNumber(row,
					Optional.empty(),
					OptionalInts.boxed(result.getElection().getNumberOfEligibleVoters(district)));

			// Stimmzettel
			appendNumber(row, Optional.empty(), OptionalInts.boxed(result.getNumberOfAllBallots(district)));

			// Wahlbeteiligung
			appendFormula(row,
					Optional.empty(),
					DATA_FORMAT_PERCENTAGE,
					"Übersicht[Stimmzettel] / Übersicht[Wahlberechtigte]");

			// ausgezählt
			appendNumber(row, Optional.empty(), Optional.of(result.getBallots(district).size()));

			// ausgezählt %
			appendFormula(row,
					Optional.empty(),
					DATA_FORMAT_PERCENTAGE,
					"Übersicht[ausgezählt] / Übersicht[Stimmzettel]");

			// ungültig
			appendNumber(row, Optional.empty(), Optional.of(result.getNumberOfInvalidBallots(district)));
		}

		private void writeOverviewTotalsRow(final Row row, final CTTable ctTable) {
			ctTable.setTotalsRowCount(1);
			final Iterator<CTTableColumn> columns = ctTable.getTableColumns().getTableColumnList().iterator();

			// Wahlbezirk
			columns.next().setTotalsRowLabel("Gesamt");
			appendStrings(row, "Gesamt");

			// Wahlberechtigte
			columns.next().setTotalsRowFunction(STTotalsRowFunction.CUSTOM);
			appendFormula(row, Optional.empty(), "SUM(Übersicht[Wahlberechtigte])");

			// Stimmzettel
			columns.next().setTotalsRowFunction(STTotalsRowFunction.CUSTOM);
			appendFormula(row, Optional.empty(), "SUM(Übersicht[Stimmzettel])");

			// Wahlbeteiligung
			columns.next().setTotalsRowFunction(STTotalsRowFunction.CUSTOM);
			appendFormula(row,
					Optional.empty(),
					DATA_FORMAT_PERCENTAGE,
					"Übersicht[[#Totals],[Stimmzettel]] / Übersicht[[#Totals],[Wahlberechtigte]]");

			// ausgezählt
			columns.next().setTotalsRowFunction(STTotalsRowFunction.CUSTOM);
			appendFormula(row, Optional.empty(), "SUM(Übersicht[ausgezählt])");

			// ausgezählt %
			columns.next().setTotalsRowFunction(STTotalsRowFunction.CUSTOM);
			appendFormula(row,
					Optional.empty(),
					DATA_FORMAT_PERCENTAGE,
					"Übersicht[[#Totals],[ausgezählt]] / Übersicht[[#Totals],[Stimmzettel]]");

			// ungültig
			columns.next().setTotalsRowFunction(STTotalsRowFunction.CUSTOM);
			appendFormula(row, Optional.empty(), "SUM(Übersicht[ungültig])");
		}

		private void writeParties(final Sheet sheet) {
			final Row row = appendRow(sheet);
			appendStrings(row, "Gruppierung");
			for (final LocalPollingStation pollingStation : result.getElection().getPollingStations()) {
				appendStrings(row, pollingStation.getName(), pollingStation.getName() + " %");
			}
			appendStrings(row, "Gesamt", "Gesamt %", "Sitze");

			// Content
			for (final Party party : result.getPartyResults().keySet()) {
				writeParty(appendRow(sheet), party);
			}

			// Table
			sheet.createFreezePane(1, 0);
			final XSSFTable table = createTable(sheet, "Gruppierungen");
			table.getCTTable().getTableStyleInfo().setShowFirstColumn(true);
		}

		private void writeParty(final Row row, final Party party) {
			final Optional<Party> optionalParty = Optional.of(party);

			// Gruppierung
			appendString(row, optionalParty, party.getShortName());

			for (final LocalPollingStation pollingStation : result.getElection().getPollingStations()) {
				// Stimmen
				appendNumber(row,
						optionalParty,
						Optional.of(result.filterByDistrict(pollingStation)
								.getPartyResults()
								.get(party)
								.getNumberOfVotes()));

				// Stimmen %
				appendFormula(row,
						optionalParty,
						DATA_FORMAT_PERCENTAGE,
						"Gruppierungen[[#This Row],["
								+ pollingStation.getName()
								+ "]] / SUM(Gruppierungen["
								+ pollingStation.getName()
								+ "])");
			}

			// Gesamt
			appendFormula(row,
					optionalParty,
					result.getElection()
							.getPollingStations()
							.stream()
							.map(LocalPollingStation::getName)
							.collect(joining("] + Gruppierungen[", "Gruppierungen[", "]")));

			// Gesamt %
			appendFormula(row,
					optionalParty,
					DATA_FORMAT_PERCENTAGE,
					"Gruppierungen[[#This Row],[Gesamt]] / SUM(Gruppierungen[Gesamt])");

			// Sitze
			appendFormula(row,
					optionalParty,
					"COUNTIFS(Kandidierende[Gruppierung], Gruppierungen[[#This Row],[Gruppierung]], Kandidierende[Mandat], \"<>\")");
		}

		private void writeBlockVotes(final Sheet sheet) {
			// Header
			final Row row = appendRow(sheet);
			appendStrings(row, "Gruppierung");
			for (final LocalPollingStation pollingStation : result.getElection().getPollingStations()) {
				appendStrings(row,
						pollingStation.getName(),
						pollingStation.getName() + " %",
						pollingStation.getName() + " ±%");
			}
			appendStrings(row, "Gesamt", "Gesamt %", "Gesamt ±%");

			// Content
			for (final Party party : result.getPartyResults().keySet()) {
				writeBlockVotesOfParty(appendRow(sheet), party);
			}

			// Table
			sheet.createFreezePane(1, 0);
			final XSSFTable table = createTable(sheet, "Blockstimmen");
			table.getCTTable().getTableStyleInfo().setShowFirstColumn(true);
		}

		private void writeBlockVotesOfParty(final Row row, final Party party) {
			final Optional<Party> optionalParty = Optional.of(party);

			// Gruppierung
			appendString(row, optionalParty, party.getShortName());

			for (final LocalPollingStation pollingStation : result.getElection().getPollingStations()) {
				// Stimmen
				appendNumber(row,
						optionalParty,
						Optional.of(result.filterByDistrict(pollingStation)
								.getPartyResults()
								.get(party)
								.getNumberOfBlockVotings()));

				// Stimmen %
				appendFormula(row,
						optionalParty,
						DATA_FORMAT_PERCENTAGE,
						"Blockstimmen[[#This Row],["
								+ pollingStation.getName()
								+ "]] / SUM(Blockstimmen["
								+ pollingStation.getName()
								+ "])");

				// Stimmen ±%
				appendFormula(row,
						optionalParty,
						DATA_FORMAT_PERCENTAGE_WITH_SIGN,
						"Blockstimmen["
								+ pollingStation.getName()
								+ " %] - INDEX(Gruppierungen["
								+ pollingStation.getName()
								+ " %], MATCH(Blockstimmen[Gruppierung], Gruppierungen[Gruppierung], 0))");
			}

			// Gesamt
			appendFormula(row,
					optionalParty,
					result.getElection()
							.getPollingStations()
							.stream()
							.map(LocalPollingStation::getName)
							.collect(joining("] + Blockstimmen[", "Blockstimmen[", "]")));

			// Gesamt %
			appendFormula(row,
					optionalParty,
					DATA_FORMAT_PERCENTAGE,
					"Blockstimmen[[#This Row],[Gesamt]] / SUM(Blockstimmen[Gesamt])");

			// Gesamt ±%
			appendFormula(row,
					optionalParty,
					DATA_FORMAT_PERCENTAGE_WITH_SIGN,
					"Blockstimmen[Gesamt %] - INDEX(Gruppierungen[Gesamt %], MATCH(Blockstimmen[Gruppierung], Gruppierungen[Gruppierung], 0))");
		}

		private void writeNominations(final Sheet sheet) {
			// Header
			final Row row = appendRow(sheet);
			appendStrings(row, "#", "Gruppierung", "Nachname", "Vorname");
			for (final LocalPollingStation pollingStation : result.getElection().getPollingStations()) {
				appendStrings(row, pollingStation.getName());
			}
			appendStrings(row, "Gesamt", "Sainte-Laguë-Wert", "Mandat");

			// Content
			for (final LocalNominationResult nominationResult : result.getNominationResults().values()) {
				writeNomination(appendRow(sheet), nominationResult);
			}

			// Table
			sheet.createFreezePane(4, 0);
			createTable(sheet, "Kandidierende");
		}

		private void writeNomination(final Row row, final LocalNominationResult nominationResult) {
			final LocalNomination nomination = nominationResult.getNomination();
			final Optional<Party> party = nomination.getParty();

			// #
			appendNumber(row,
					party,
					nomination.getParty()
							.map(p -> new ArrayList<>(result.getElection().getNominations(p)).indexOf(nomination) + 1));

			// Gruppierung
			appendCell(row, party, Cell::setCellValue, Optional.empty(), party.map(Party::getShortName));

			// Nachname
			appendString(row, party, nomination.getPerson().getFamilyName());

			// Vorname
			appendString(row, party, nomination.getPerson().getGivenName());

			for (final LocalPollingStation pollingStation : result.getElection().getPollingStations()) {
				// Stimmen
				appendNumber(row,
						party,
						Optional.of(result.filterByDistrict(pollingStation)
								.getNominationResults()
								.get(nomination)
								.getNumberOfVotes()));
			}

			// Gesamt
			appendFormula(row,
					party,
					result.getElection()
							.getPollingStations()
							.stream()
							.map(LocalPollingStation::getName)
							.collect(joining("] + Kandidierende[", "Kandidierende[", "]")));

			// Sainte-Laguë-Wert
			appendCell(row,
					party,
					MetricsFileWriter::setCellValue,
					Optional.of(dataFormatSainteLague),
					nominationResult.getSainteLagueValue());

			// Mandat
			appendString(row, party, getDisplayValue(nominationResult.getType()));
		}
	}
}
