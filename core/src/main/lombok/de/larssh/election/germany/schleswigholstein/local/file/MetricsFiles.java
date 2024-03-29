package de.larssh.election.germany.schleswigholstein.local.file;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.summingInt;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
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

import de.larssh.election.germany.schleswigholstein.Ballot;
import de.larssh.election.germany.schleswigholstein.Color;
import de.larssh.election.germany.schleswigholstein.District;
import de.larssh.election.germany.schleswigholstein.Keys;
import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.local.LocalBallot;
import de.larssh.election.germany.schleswigholstein.local.LocalElectionResult;
import de.larssh.election.germany.schleswigholstein.local.LocalNomination;
import de.larssh.election.germany.schleswigholstein.local.LocalNominationResult;
import de.larssh.election.germany.schleswigholstein.local.LocalNominationResultType;
import de.larssh.election.germany.schleswigholstein.local.LocalPollingStation;
import de.larssh.utils.OptionalInts;
import de.larssh.utils.annotations.PackagePrivate;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.experimental.UtilityClass;

/**
 * This class contains helper methods to write an Excel spreadsheet (XLSX) with
 * metrics of an election result.
 */
@UtilityClass
@SuppressWarnings("PMD.ExcessiveImports")
@SuppressFBWarnings(value = "OI_OPTIONAL_ISSUES_PRIMITIVE_VARIANT_PREFERRED",
		justification = "using generic Optional<Number> functionality")
public class MetricsFiles {
	/**
	 * Formats and writes {@code result} to {@code outputStream}.
	 *
	 * @param result       the election result to to write
	 * @param outputStream the metrics file output stream
	 * @param extended     if {@code true} additional metrics on ballot basis are
	 *                     included
	 * @throws IOException on IO error
	 */
	public static void write(final LocalElectionResult result, final OutputStream outputStream, final boolean extended)
			throws IOException {
		new MetricsFileWriter(result, outputStream, extended).write();
	}

	/**
	 * This class writes metrics of a {@link LocalElectionResult} to an Excel
	 * spreadsheet (XLSX).
	 */
	@SuppressWarnings({ "checkstyle:MultipleStringLiterals", "PMD.AvoidDuplicateLiterals" })
	private static class MetricsFileWriter {
		/**
		 * Width of the auto filter control in Excel, calculated using the difference of
		 * a cell without and with auto filter
		 */
		private static final int AUTO_FILTER_WIDTH = 563;

		/**
		 * Width of one character
		 */
		private static final int CHARACTER_WIDTH = 256;

		/**
		 * The maximum width of a column
		 */
		private static final int COLUMN_MAX_WIDTH = 255 * CHARACTER_WIDTH;

		/**
		 * Excel data format for percentage values
		 */
		private static final String DATA_FORMAT_PERCENTAGE = "0.0%";

		/**
		 * Excel data format for differences between two percentage values, showing the
		 * plus sign in case of a positive value
		 */
		private static final String DATA_FORMAT_PERCENTAGE_WITH_SIGN = "\\+0.0%;\\-0.0%;0.0%";

		/**
		 * Excel data format for any number with any number of decimal places
		 */
		private static final ThreadLocal<NumberFormat> DECIMAL_FORMAT = ThreadLocal.withInitial(() -> {
			final DecimalFormat format = new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.ROOT));
			format.setMaximumFractionDigits(Integer.MAX_VALUE);
			return format;
		});

		/**
		 * Appends a row to {@code sheet} below the currently last row.
		 *
		 * @param sheet the sheet to modify
		 * @return the created row
		 */
		private static Row appendRow(final Sheet sheet) {
			return CellUtil.getRow(sheet.getLastRowNum() + 1, sheet);
		}

		/**
		 * Creates a table with auto filter, default style and {@ode name}, spanning all
		 * curently available cells.
		 *
		 * @param sheet the sheet to modify
		 * @param name  the table name
		 * @return the created table
		 */
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

		/**
		 * Converts a {@link LocalNominationResultType} to a display value.
		 *
		 * @param type the type to convert
		 * @return the type's display value
		 */
		private static String getDisplayValue(final LocalNominationResultType type) {
			if (type == LocalNominationResultType.DIRECT) {
				return "Direkt";
			}
			if (type == LocalNominationResultType.DIRECT_BALANCE_SEAT) {
				return "Mehrsitz";
			}
			if (type == LocalNominationResultType.DIRECT_DRAW_LIST) {
				return "Direktlos mit Liste";
			}
			if (type == LocalNominationResultType.LIST) {
				return "Liste";
			}
			if (type == LocalNominationResultType.LIST_OVERHANG_SEAT) {
				return "Überhang";
			}
			if (type == LocalNominationResultType.DIRECT_DRAW) {
				return "Direktlos";
			}
			if (type == LocalNominationResultType.LIST_DRAW) {
				return "Listenlos";
			}
			return "";
		}

		/**
		 * Sets a numeric {@code value} for {@code cell}.
		 *
		 * <p>
		 * This method allows setting e.g. precise {@link java.math.BigDecimal} values,
		 * but does not permit infinite and {@code NaN} values.
		 *
		 * @param cell  the cell to modify
		 * @param value the value to set
		 */
		private static void setCellValue(final Cell cell, final Number value) {
			final CTCell ctCell = ((XSSFCell) cell).getCTCell();
			ctCell.setT(STCellType.N);
			ctCell.setV(DECIMAL_FORMAT.get().format(value));
		}

		/**
		 * Election Result to create metrics of
		 *
		 * @return the election result to create metrics of
		 */
		LocalElectionResult result;

		/**
		 * Metrics file output stream
		 *
		 * @return the metrics file output stream
		 */
		OutputStream outputStream;

		/**
		 * Adds additional metrics on ballot basis
		 *
		 * @return {@code true} if additional metrics on ballot basis are included, else
		 *         {@code false}
		 */
		boolean extended;

		/**
		 * Cache to simplify reusing {@link CellStyle} instances
		 */
		Map<String, CellStyle> cellStyleCache = new HashMap<>();

		/**
		 * The Excel data format for Sainte Laguë values, decimal places being derived
		 * from {@link LocalElectionResult#getSainteLagueScale()}.
		 */
		String dataFormatSainteLague;

		/**
		 * This class writes metrics of a {@link LocalElectionResult} to an Excel
		 * spreadsheet (XLSX).
		 *
		 * @param result       the {@link LocalElectionResult} to create metrics of
		 * @param outputStream the {@link OutputStream} to write to
		 * @param extended     if {@code true} additional metrics on ballot basis are
		 *                     included
		 */
		@PackagePrivate
		MetricsFileWriter(final LocalElectionResult result, final OutputStream outputStream, final boolean extended) {
			this.result = result;
			this.outputStream = outputStream;
			this.extended = extended;

			dataFormatSainteLague = result.getSainteLagueScale() == 0
					? "0"
					: String.format("0.%0" + result.getSainteLagueScale() + "d", 0);
		}

		/**
		 * Appends a cell to {@code row} after the currently last cell.
		 *
		 * @param <T>        the value's data type
		 * @param row        the row to modify
		 * @param party      the {@link Party} to be used for coloring or empty
		 * @param dataFormat the Excel data format to set or empty
		 * @param setValue   a method to set the value to the created cell
		 * @param value      the value to set or empty
		 * @return the created cell
		 */
		@SuppressWarnings({ "checkstyle:SuppressWarnings", "resource" })
		private <T> Cell appendCell(final Row row,
				final Optional<Party> party,
				final Optional<String> dataFormat,
				final BiConsumer<Cell, T> setValue,
				final Optional<T> value) {
			final Cell cell = CellUtil.getCell(row, Math.max(0, row.getLastCellNum()));
			getCellStyle(row.getSheet().getWorkbook(), party, dataFormat).ifPresent(cell::setCellStyle);
			if (value.isPresent()) {
				setValue.accept(cell, value.get());
			}
			return cell;
		}

		/**
		 * Appends a cell with {@code formula} to {@code row} after the currently last
		 * cell.
		 *
		 * @param row     the row to modify
		 * @param party   the {@link Party} to be used for coloring or empty
		 * @param formula the formula to set
		 * @return the created cell
		 */
		private Cell appendFormula(final Row row, final Optional<Party> party, final String formula) {
			return appendCell(row, party, Optional.empty(), Cell::setCellFormula, Optional.of(formula));
		}

		/**
		 * Appends a cell with {@code formula} to {@code row} after the currently last
		 * cell.
		 *
		 * @param row        the row to modify
		 * @param party      the {@link Party} to be used for coloring or empty
		 * @param dataFormat the Excel data format to set or empty
		 * @param formula    the formula to set
		 * @return the created cell
		 */
		private Cell appendFormula(final Row row,
				final Optional<Party> party,
				final String dataFormat,
				final String formula) {
			return appendCell(row, party, Optional.of(dataFormat), Cell::setCellFormula, Optional.of(formula));
		}

		/**
		 * Appends a cell with numeric {@code value} to {@code row} after the currently
		 * last cell.
		 *
		 * @param row   the row to modify
		 * @param party the {@link Party} to be used for coloring or empty
		 * @param value the value to set or empty
		 * @return the created cell
		 */
		private Cell appendNumber(final Row row, final Optional<Party> party, final Optional<? extends Number> value) {
			return appendCell(row, party, Optional.empty(), MetricsFileWriter::setCellValue, value);
		}

		/**
		 * Appends a cell with string {@code value} to {@code row} after the currently
		 * last cell.
		 *
		 * @param row   the row to modify
		 * @param party the {@link Party} to be used for coloring or empty
		 * @param value the value to set or empty
		 * @return the created cell
		 */
		private Cell appendString(final Row row, final Optional<Party> party, final String value) {
			return appendCell(row, party, Optional.empty(), Cell::setCellValue, Optional.of(value));
		}

		/**
		 * Appends cells with string {@code values} to {@code row} after the currently
		 * last cell.
		 *
		 * @param row    the row to modify
		 * @param values the values to set
		 */
		private void appendStrings(final Row row, final String... values) {
			for (final String value : values) {
				appendString(row, Optional.empty(), value);
			}
		}

		/**
		 * Determines if the votes of {@code ballot} contain at least one nomination
		 * relating {@code party}.
		 *
		 * @param ballot the ballot to search in
		 * @param party  the party to look for
		 * @return {@code true} if {@code ballot} contains at least one nomination of
		 *         {@code party}, else {@code false}
		 */
		private static boolean containsParty(final Ballot<?> ballot, final Party party) {
			return ballot.getNominations()
					.stream()
					.anyMatch(nomination -> nomination.getParty().map(party::equals).orElse(Boolean.FALSE));
		}

		/**
		 * Returns a {@link CellStyle} instance with the coloring of {@code party} and
		 * {@code dataFormat}. Cell styles are cached and reused.
		 *
		 * @param workbook   the current workbook
		 * @param party      the {@link Party} to be used for coloring or empty
		 * @param dataFormat the Excel data format to set or empty
		 * @return the {@link CellStyle} instance with the coloring of {@code party} and
		 *         {@code dataFormat}
		 */
		private Optional<CellStyle> getCellStyle(final Workbook workbook,
				final Optional<Party> party,
				final Optional<String> dataFormat) {
			if (!party.isPresent() && !dataFormat.isPresent()) {
				return Optional.empty();
			}

			final String key = Keys.escape(party.map(Party::getKey).orElse(""), ", ", dataFormat.orElse(""), "");
			return Optional.of(cellStyleCache.computeIfAbsent(key, k -> {
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
				if (extended) {
					writeVotes(workbook.createSheet("Stimmen"));
					writeBallots(workbook.createSheet("Stimmzettel"));
				}

				// Auto Size Columns
				workbook.sheetIterator().forEachRemaining(sheet -> {
					final int numberOfColumns = sheet.getRow(0).getLastCellNum();
					for (int columnIndex = 0; columnIndex < numberOfColumns; columnIndex += 1) {
						sheet.autoSizeColumn(columnIndex);

						// Add the width of the auto filter
						final double widthOfHeader = SheetUtil.getColumnWidth(sheet, columnIndex, false, 0, 0);
						if (widthOfHeader != -1) {
							final int intWidth = (int) Math.round(
									Math.min(CHARACTER_WIDTH * widthOfHeader + AUTO_FILTER_WIDTH, COLUMN_MAX_WIDTH));
							if (intWidth > sheet.getColumnWidth(columnIndex)) {
								sheet.setColumnWidth(columnIndex, intWidth);
							}
						}
					}
				});

				workbook.write(outputStream);
			}
		}

		/**
		 * Adds the sheet "Übersicht"
		 *
		 * @param sheet the sheet to write to
		 */
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

		/**
		 * Adds a row with district metrics to the sheet "Übersicht"
		 *
		 * @param row      the row to write to
		 * @param district the district
		 */
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
					"MIN(1, Übersicht[Stimmzettel] / Übersicht[Wahlberechtigte])");

			// ausgezählt
			appendNumber(row, Optional.empty(), Optional.of(result.getBallots(district).size()));

			// ausgezählt %
			appendFormula(row,
					Optional.empty(),
					DATA_FORMAT_PERCENTAGE,
					"MIN(1, Übersicht[ausgezählt] / Übersicht[Stimmzettel])");

			// ungültig
			appendNumber(row, Optional.empty(), Optional.of(result.getNumberOfInvalidBallots(district)));
		}

		/**
		 * Adds a total row to the sheet "Übersicht"
		 *
		 * @param row   the row to write to
		 * @param table the Excel table to update
		 */
		private void writeOverviewTotalsRow(final Row row, final CTTable table) {
			table.setTotalsRowCount(1);
			final Iterator<CTTableColumn> columns = table.getTableColumns().getTableColumnList().iterator();

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
					"MIN(1, Übersicht[[#Totals],[Stimmzettel]] / Übersicht[[#Totals],[Wahlberechtigte]])");

			// ausgezählt
			columns.next().setTotalsRowFunction(STTotalsRowFunction.CUSTOM);
			appendFormula(row, Optional.empty(), "SUM(Übersicht[ausgezählt])");

			// ausgezählt %
			columns.next().setTotalsRowFunction(STTotalsRowFunction.CUSTOM);
			appendFormula(row,
					Optional.empty(),
					DATA_FORMAT_PERCENTAGE,
					"MIN(1, Übersicht[[#Totals],[ausgezählt]] / Übersicht[[#Totals],[Stimmzettel]])");

			// ungültig
			columns.next().setTotalsRowFunction(STTotalsRowFunction.CUSTOM);
			appendFormula(row, Optional.empty(), "SUM(Übersicht[ungültig])");
		}

		/**
		 * Adds the sheet "Gruppierungen"
		 *
		 * @param sheet the sheet to write to
		 */
		private void writeParties(final Sheet sheet) {
			// Header
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

		/**
		 * Adds a row with party metrics to the sheet "Gruppierungen"
		 *
		 * @param row   the row to write to
		 * @param party the party
		 */
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
					"COUNTIF(Kandidierende[Gruppierung], Gruppierungen[[#This Row],[Gruppierung]]) - COUNTIFS(Kandidierende[Gruppierung], Gruppierungen[[#This Row],[Gruppierung]], Kandidierende[Mandat], \"\")");
		}

		/**
		 * Adds the sheet "Blockstimmen"
		 *
		 * @param sheet the sheet to write to
		 */
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

		/**
		 * Adds a row with block vote metrics of {@code party} to the sheet
		 * "Blockstimmen"
		 *
		 * @param row   the row to write to
		 * @param party the party
		 */
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

		/**
		 * Adds the sheet "Kandidierende"
		 *
		 * @param sheet the sheet to write to
		 */
		private void writeNominations(final Sheet sheet) {
			// Header
			final Row row = appendRow(sheet);
			appendStrings(row, "#", "Gruppierung", "Nachname", "Vorname");
			final int freezeColumn = row.getLastCellNum();
			for (final LocalPollingStation pollingStation : result.getElection().getPollingStations()) {
				appendStrings(row, pollingStation.getName());
			}
			appendStrings(row, "Gesamt", "Sainte-Laguë-Wert", "Mandat");

			// Content
			for (final LocalNominationResult nominationResult : result.getNominationResults().values()) {
				writeNomination(appendRow(sheet), nominationResult);
			}

			// Table
			sheet.createFreezePane(freezeColumn, 0);
			createTable(sheet, "Kandidierende");
		}

		/**
		 * Adds a row with nomination metrics to the sheet "Kandidierende"
		 *
		 * @param row              the row to write to
		 * @param nominationResult the nomination's results
		 */
		private void writeNomination(final Row row, final LocalNominationResult nominationResult) {
			final LocalNomination nomination = nominationResult.getNomination();
			final Optional<Party> party = nomination.getParty();

			// #
			appendNumber(row, party, OptionalInts.boxed(nomination.getListPosition()));

			// Gruppierung
			appendCell(row, party, Optional.empty(), Cell::setCellValue, party.map(Party::getShortName));

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
					Optional.of(dataFormatSainteLague),
					MetricsFileWriter::setCellValue,
					nominationResult.getSainteLagueValue());

			// Mandat
			appendString(row, party, getDisplayValue(nominationResult.getType()));
		}

		/**
		 * Adds a row with vote metrics of a {@code party} to the sheet "Stimmen"
		 *
		 * @param row   the row to write to
		 * @param party the party
		 */
		private void writeVote(final Row row, final Party party) {
			appendString(row, Optional.of(party), party.getShortName());
			for (final Party column : result.getPartyResults().keySet()) {
				final long numberOfBallots = result.getBallots()
						.stream()
						.filter(ballot -> containsParty(ballot, party))
						.flatMap(ballot -> ballot.getNominations().stream())
						.filter(ballot -> ballot.getParty().map(column::equals).orElse(Boolean.FALSE))
						.count();
				appendNumber(row, Optional.of(party), Optional.of(numberOfBallots));
			}
			for (final LocalNomination nomination : result.getNominationResults().keySet()) {
				if (nomination.isDirectNomination()) {
					final long numberOfBallots = result.getBallots()
							.stream()
							.filter(ballot -> ballot.getNominations().contains(nomination)
									&& containsParty(ballot, party))
							.count();
					appendNumber(row, Optional.of(party), Optional.of(numberOfBallots));
				}
			}
		}

		/**
		 * Adds a row with vote metrics of a {@code nomination} to the sheet "Stimmen"
		 *
		 * @param row        the row to write to
		 * @param nomination the nomination
		 */
		private void writeVote(final Row row, final LocalNomination nomination) {
			appendString(row,
					nomination.getParty(),
					String.format("%s, %s",
							nomination.getPerson().getFamilyName(),
							nomination.getPerson().getGivenName()));
			for (final Party party : result.getPartyResults().keySet()) {
				final long numberOfBallots = result.getBallots()
						.stream()
						.filter(ballot -> ballot.getNominations().contains(nomination))
						.flatMap(ballot -> ballot.getNominations().stream())
						.filter(ballot -> ballot.getParty().map(party::equals).orElse(Boolean.FALSE))
						.count();
				appendNumber(row, nomination.getParty(), Optional.of(numberOfBallots));
			}
			for (final LocalNomination column : result.getNominationResults().keySet()) {
				if (column.isDirectNomination()) {
					final long numberOfBallots = result.getBallots()
							.stream()
							.filter(ballot -> ballot.getNominations().contains(nomination)
									&& ballot.getNominations().contains(column))
							.count();
					appendNumber(row, nomination.getParty(), Optional.of(numberOfBallots));
				}
			}
		}

		/**
		 * Adds the sheet "Stimmen"
		 *
		 * @param sheet the sheet to write to
		 */
		private void writeVotes(final Sheet sheet) {
			// Header
			final Row row = appendRow(sheet);
			appendStrings(row, "Stimmen");
			for (final Party party : result.getPartyResults().keySet()) {
				appendString(row, Optional.empty(), party.getShortName());
			}
			for (final LocalNomination nomination : result.getNominationResults().keySet()) {
				if (nomination.isDirectNomination()) {
					appendString(row,
							Optional.empty(),
							String.format("%s, %s",
									nomination.getPerson().getFamilyName(),
									nomination.getPerson().getGivenName()));
				}
			}

			// Content
			for (final Party party : result.getPartyResults().keySet()) {
				writeVote(appendRow(sheet), party);
			}
			for (final LocalNomination nomination : result.getNominationResults().keySet()) {
				if (nomination.isDirectNomination()) {
					writeVote(appendRow(sheet), nomination);
				}
			}

			// Table
			sheet.createFreezePane(1, 1);
			createTable(sheet, "Stimmen");
		}

		/**
		 * Adds a row with ballot metrics to the sheet "Stimmzettel"
		 *
		 * @param row    the row to write to
		 * @param ballot the ballot to add
		 * @param count  the number of times this ballot was voted
		 */
		private void writeBallot(final Row row, final Set<LocalNomination> ballot, final Integer count) {
			for (final LocalNomination nomination : result.getNominationResults().keySet()) {
				if (nomination.isDirectNomination()) {
					appendNumber(row, nomination.getParty(), Optional.of(ballot.contains(nomination) ? count : 0));
				}
			}
		}

		/**
		 * Adds the sheet "Stimmzettel"
		 *
		 * @param sheet the sheet to write to
		 */
		private void writeBallots(final Sheet sheet) {
			// Header
			final Row row = appendRow(sheet);
			for (final LocalNomination nomination : result.getNominationResults().keySet()) {
				if (nomination.isDirectNomination()) {
					appendString(row,
							nomination.getParty(),
							String.format("%s, %s",
									nomination.getPerson().getFamilyName(),
									nomination.getPerson().getGivenName()));
				}
			}

			// Content
			final Map<Set<LocalNomination>, Integer> ballots = result.getBallots()
					.stream()
					.filter(LocalBallot::isValid)
					.collect(groupingBy(LocalBallot::getNominations, summingInt(ballot -> 1)));
			ballots.entrySet()
					.stream()
					.sorted(Comparator.comparing(Entry::getValue))
					.forEach(entry -> writeBallot(appendRow(sheet), entry.getKey(), entry.getValue()));

			// Table
			sheet.createFreezePane(0, 1);
			final XSSFTable table = createTable(sheet, "Stimmzettel");

			// Totals Row
			table.setDataRowCount(table.getDataRowCount() + 1);
			writeBallotsTotalsRow(appendRow(sheet), table.getCTTable());
		}

		/**
		 * Adds a total row to the sheet "Stimmzettel"
		 *
		 * @param row   the row to write to
		 * @param table the Excel table to update
		 */
		private void writeBallotsTotalsRow(final Row row, final CTTable table) {
			table.setTotalsRowCount(1);
			final Iterator<CTTableColumn> columns = table.getTableColumns().getTableColumnList().iterator();

			for (final LocalNomination nomination : result.getNominationResults().keySet()) {
				if (nomination.isDirectNomination()) {
					columns.next().setTotalsRowFunction(STTotalsRowFunction.CUSTOM);
					appendFormula(row,
							nomination.getParty(),
							String.format("SUBTOTAL(9, Stimmzettel[%s, %s])",
									nomination.getPerson().getFamilyName(),
									nomination.getPerson().getGivenName()));
				}
			}
		}
	}
}
