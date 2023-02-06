package de.larssh.election.germany.schleswigholstein.local.file;

import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STTotalsRowFunction;

import de.larssh.election.germany.schleswigholstein.Color;
import de.larssh.election.germany.schleswigholstein.District;
import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.local.LocalElectionResult;
import de.larssh.election.germany.schleswigholstein.local.LocalNomination;
import de.larssh.election.germany.schleswigholstein.local.LocalNominationResult;
import de.larssh.election.germany.schleswigholstein.local.LocalNominationResultType;
import de.larssh.election.germany.schleswigholstein.local.LocalPollingStation;
import de.larssh.utils.annotations.PackagePrivate;
import de.larssh.utils.io.Resources;
import de.larssh.utils.text.Strings;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.experimental.UtilityClass;

/**
 * This class contains helper methods to write an Excel spreadsheet (XLSX) with
 * metrics of an election result.
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
	@RequiredArgsConstructor
	private static class MetricsFileWriter {
		private static Cell getCell(final Row row, final int columnIndex, final Optional<CellStyle> cellStyle) {
			final Cell cell = CellUtil.getCell(row, columnIndex);
			cellStyle.ifPresent(cell::setCellStyle);
			return cell;
		}

		private static Optional<String> getDisplayValue(final LocalNominationResultType type) {
			if (type == LocalNominationResultType.DIRECT) {
				return Optional.of("Direkt");
			}
			if (type == LocalNominationResultType.DIRECT_DRAW) {
				return Optional.of("Los");
			}
			if (type == LocalNominationResultType.DIRECT_BALANCE_SEAT) {
				return Optional.of("Mehrsitz");
			}
			if (type == LocalNominationResultType.LIST) {
				return Optional.of("Liste");
			}
			if (type == LocalNominationResultType.LIST_DRAW) {
				return Optional.of("Los");
			}
			if (type == LocalNominationResultType.LIST_OVERHANG_SEAT) {
				return Optional.of("Überhang");
			}
			return Optional.empty();
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

		Map<Party, CellStyle> cellStyleParties = new HashMap<>();

		@NonFinal
		@Nullable
		CellStyle cellStylePercentage = null;

		@NonFinal
		@Nullable
		CellStyle cellStylePercentageWithSign = null;

		@NonFinal
		@Nullable
		CellStyle cellStyleSainteLagueValue = null;

		/**
		 * Formats and writes {@link #result} to {@link #writer}.
		 *
		 * @throws IOException on IO error
		 */
		@PackagePrivate
		void write() throws IOException {
			try (XSSFWorkbook workbook = new XSSFWorkbook()) {
				workbook.setCellFormulaValidation(false);

				for (final Party party : result.getPartyResults().keySet()) {
					final CellStyle partyCellStyle = workbook.createCellStyle();
					partyCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

					final Color backgroundColor = party.getBackgroundColor();
					partyCellStyle.setFillForegroundColor(new XSSFColor(new byte[] {
							(byte) backgroundColor.getAlphaAsByte(),
							(byte) backgroundColor.getRedAsByte(),
							(byte) backgroundColor.getGreenAsByte(),
							(byte) backgroundColor.getBlueAsByte() }));

					final Color fontColor = party.getFontColor();
					final XSSFFont font = workbook.createFont();
					font.setColor(new XSSFColor(new byte[] {
							(byte) fontColor.getAlphaAsByte(),
							(byte) fontColor.getRedAsByte(),
							(byte) fontColor.getGreenAsByte(),
							(byte) fontColor.getBlueAsByte() }));
					partyCellStyle.setFont(font);

					cellStyleParties.put(party, partyCellStyle);
				}

				final CellStyle cellStylePercentage = workbook.createCellStyle();
				cellStylePercentage.setDataFormat(workbook.createDataFormat().getFormat("0.0%"));
				this.cellStylePercentage = cellStylePercentage;

				final CellStyle cellStylePercentageWithSign = workbook.createCellStyle();
				cellStylePercentageWithSign
						.setDataFormat(workbook.createDataFormat().getFormat("\\+0.0%;\\-0.0%;0.0%"));
				this.cellStylePercentageWithSign = cellStylePercentageWithSign;

				final CellStyle cellStyleSainteLagueValue = workbook.createCellStyle();
				cellStyleSainteLagueValue.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
				this.cellStyleSainteLagueValue = cellStyleSainteLagueValue;

				writeOverview(workbook.createSheet("Übersicht"));
				writeParties(workbook.createSheet("Gruppierungen"));
				writeBlockVotes(workbook.createSheet("Blockstimmen"));
				writeNominations(workbook.createSheet("Kandidierende"));
				// TODO: writeBallotsByGroups(workbook.createSheet("Verteilung nach G."));
				// TODO: writeBallotsByNominations(workbook.createSheet("Verteilung nach K."));

				workbook.sheetIterator().forEachRemaining(sheet -> {
					final int numberOfColumns = sheet.getRow(0).getLastCellNum();
					for (int columnIndex = 0; columnIndex < numberOfColumns; columnIndex += 1) {
						sheet.autoSizeColumn(columnIndex, true);
					}
				});

				workbook.write(outputStream);
			}
		}

		private void createHeader(final Row row, final String... values) {
			int columnIndex = 0;
			for (final String value : values) {
				if (!value.isEmpty()) {
					CellUtil.getCell(row, columnIndex).setCellValue(value);
				}
				columnIndex += 1;
			}
		}

		private void writeOverview(final Sheet sheet) {
			sheet.createFreezePane(1, 0);
			sheet.setDefaultColumnStyle(3, cellStylePercentage); // Wahlbeteiligung
			sheet.setDefaultColumnStyle(5, cellStylePercentage); // ausgezählt

			int rowIndex = 0;
			createHeader(CellUtil.getRow(rowIndex, sheet), //
					"Wahlbezirk",
					"Wahlberechtigte",
					"Stimmzettel",
					"Wahlbeteiligung",
					"ausgezählt",
					"ausgezählt %",
					"ungültig");

			for (final LocalPollingStation pollingStation : result.getElection().getPollingStations()) {
				rowIndex += 1;
				writeOverviewOfDistrict(CellUtil.getRow(rowIndex, sheet), pollingStation.getName(), pollingStation);
			}

			final XSSFTable table = ((XSSFSheet) sheet).createTable(new AreaReference(new CellReference(0, 0),
					new CellReference(rowIndex + 1, CellUtil.getRow(0, sheet).getLastCellNum() - 1),
					SpreadsheetVersion.EXCEL2007));
			table.setName("Übersicht");
			table.setDisplayName("Übersicht");
			table.setStyleName("TableStyleLight1");
			table.getCTTable().addNewAutoFilter();
			table.getCTTable().getTableStyleInfo().setShowFirstColumn(true);
			table.getCTTable().getTableStyleInfo().setShowRowStripes(true);

			rowIndex += 1;
			writeOverviewTotalRow(CellUtil.getRow(rowIndex, sheet), table.getCTTable());
		}

		private void writeOverviewOfDistrict(final Row row, final String name, final District<?> district) {
			// Wahlbezirk
			int columnIndex = 0;
			CellUtil.getCell(row, columnIndex).setCellValue(name);

			// Wahlberechtigte
			columnIndex += 1;
			result.getElection()
					.getNumberOfEligibleVoters(district)
					.ifPresent(CellUtil.getCell(row, columnIndex)::setCellValue);

			// Stimmzettel
			columnIndex += 1;
			result.getNumberOfAllBallots(district).ifPresent(CellUtil.getCell(row, columnIndex)::setCellValue);

			// Wahlbeteiligung
			columnIndex += 1;
			CellUtil.getCell(row, columnIndex).setCellFormula("Übersicht[Stimmzettel] / Übersicht[Wahlberechtigte]");

			// ausgezählt
			columnIndex += 1;
			CellUtil.getCell(row, columnIndex).setCellValue(result.getBallots(district).size());

			// ausgezählt %
			columnIndex += 1;
			CellUtil.getCell(row, columnIndex).setCellFormula("Übersicht[ausgezählt] / Übersicht[Stimmzettel]");

			// ungültig
			columnIndex += 1;
			CellUtil.getCell(row, columnIndex).setCellValue(result.getNumberOfInvalidBallots(district));
		}

		private void writeOverviewTotalRow(final Row row, final CTTable ctTable) {
			ctTable.setTotalsRowCount(1);
			final List<CTTableColumn> columns = ctTable.getTableColumns().getTableColumnList();

			// Wahlbezirk
			int columnIndex = 0;
			columns.get(columnIndex).setTotalsRowLabel("Gesamt");
			CellUtil.getCell(row, columnIndex).setCellValue("Gesamt");

			// Wahlberechtigte
			columnIndex += 1;
			columns.get(columnIndex).setTotalsRowFunction(STTotalsRowFunction.CUSTOM);
			CellUtil.getCell(row, columnIndex)
					.setCellFormula("SUM(Übersicht[" + columns.get(columnIndex).getName() + "])");

			// Stimmzettel
			columnIndex += 1;
			columns.get(columnIndex).setTotalsRowFunction(STTotalsRowFunction.CUSTOM);
			CellUtil.getCell(row, columnIndex)
					.setCellFormula("SUM(Übersicht[" + columns.get(columnIndex).getName() + "])");

			// Wahlbeteiligung
			columnIndex += 1;
			columns.get(columnIndex).setTotalsRowFunction(STTotalsRowFunction.CUSTOM);
			CellUtil.getCell(row, columnIndex)
					.setCellFormula("Übersicht[[#Totals],[Stimmzettel]] / Übersicht[[#Totals],[Wahlberechtigte]]");

			// ausgezählt
			columnIndex += 1;
			columns.get(columnIndex).setTotalsRowFunction(STTotalsRowFunction.CUSTOM);
			CellUtil.getCell(row, columnIndex)
					.setCellFormula("SUM(Übersicht[" + columns.get(columnIndex).getName() + "])");

			// ausgezählt %
			columnIndex += 1;
			columns.get(columnIndex).setTotalsRowFunction(STTotalsRowFunction.CUSTOM);
			CellUtil.getCell(row, columnIndex)
					.setCellFormula("Übersicht[[#Totals],[ausgezählt]] / Übersicht[[#Totals],[Stimmzettel]]");

			// ungültig
			columnIndex += 1;
			columns.get(columnIndex).setTotalsRowFunction(STTotalsRowFunction.CUSTOM);
			CellUtil.getCell(row, columnIndex)
					.setCellFormula("SUM(Übersicht[" + columns.get(columnIndex).getName() + "])");
		}

		private void writeParties(final Sheet sheet) {
			sheet.createFreezePane(1, 0);
			final int max = 2 + 2 * result.getElection().getPollingStations().size();
			for (int i = 2; i <= max; i += 2) {
				sheet.setDefaultColumnStyle(i, cellStylePercentage); // TODO
			}

			// TODO: Information rows

			int rowIndex = 0;
			final List<String> headers = new ArrayList<>();
			headers.add("Gruppierung");
			for (final LocalPollingStation pollingStation : result.getElection().getPollingStations()) {
				headers.add(pollingStation.getName());
				headers.add(pollingStation.getName() + " %");
			}
			headers.add("Gesamt");
			headers.add("Gesamt %");
			headers.add("Sitze");
			createHeader(CellUtil.getRow(rowIndex, sheet), headers.toArray(new String[0]));

			for (final Party party : result.getPartyResults().keySet()) {
				rowIndex += 1;
				writeParty(CellUtil.getRow(rowIndex, sheet), party);
			}

			final XSSFTable table = ((XSSFSheet) sheet).createTable(new AreaReference(new CellReference(0, 0),
					new CellReference(rowIndex, CellUtil.getRow(0, sheet).getLastCellNum() - 1),
					SpreadsheetVersion.EXCEL2007));
			table.setName("Gruppierungen");
			table.setDisplayName("Gruppierungen");
			table.setStyleName("TableStyleLight1");
			table.getCTTable().addNewAutoFilter();
			table.getCTTable().getTableStyleInfo().setShowFirstColumn(true);
		}

		private void writeParty(final Row row, final Party party) {
			final Optional<CellStyle> cellStyle = Optional.of(cellStyleParties.get(party));

			// Gruppierung
			int columnIndex = 0;
			getCell(row, columnIndex, cellStyle).setCellValue(party.getShortName());

			for (final LocalPollingStation pollingStation : result.getElection().getPollingStations()) {
				// Stimmen
				columnIndex += 1;
				getCell(row, columnIndex, cellStyle).setCellValue(
						result.filterByDistrict(pollingStation).getPartyResults().get(party).getNumberOfVotes());

				// Stimmen %
				columnIndex += 1;
				getCell(row, columnIndex, cellStyle).setCellFormula("Gruppierungen[[#This Row],["
						+ pollingStation.getName()
						+ "]] / SUM(Gruppierungen["
						+ pollingStation.getName()
						+ "])");
			}

			// Gesamt
			columnIndex += 1;
			getCell(row, columnIndex, cellStyle).setCellFormula(result.getElection()
					.getPollingStations()
					.stream()
					.map(LocalPollingStation::getName)
					.collect(joining("] + Gruppierungen[", "Gruppierungen[", "]")));

			// Gesamt %
			columnIndex += 1;
			getCell(row, columnIndex, cellStyle)
					.setCellFormula("Gruppierungen[[#This Row],[Gesamt]] / SUM(Gruppierungen[Gesamt])");

			// Sitze
			// TODO: Need to implement the seats table first
		}

		private void writeBlockVotes(final Sheet sheet) {
			sheet.createFreezePane(1, 0);
			final int max = 2 + 3 * result.getElection().getPollingStations().size();
			for (int i = 2; i <= max; i += 3) {
				sheet.setDefaultColumnStyle(i, cellStylePercentage); // TODO
				sheet.setDefaultColumnStyle(i + 1, cellStylePercentageWithSign); // TODO
			}

			// TODO: Information rows

			int rowIndex = 0;
			final List<String> headers = new ArrayList<>();
			headers.add("Gruppierung");
			for (final LocalPollingStation pollingStation : result.getElection().getPollingStations()) {
				headers.add(pollingStation.getName());
				headers.add(pollingStation.getName() + " %");
				headers.add(pollingStation.getName() + " +-%");
			}
			headers.add("Gesamt");
			headers.add("Gesamt %");
			headers.add("Gesamt +-%");
			createHeader(CellUtil.getRow(rowIndex, sheet), headers.toArray(new String[0]));

			for (final Party party : result.getPartyResults().keySet()) {
				rowIndex += 1;
				writeBlockVotesOfParty(CellUtil.getRow(rowIndex, sheet), party);
			}

			final XSSFTable table = ((XSSFSheet) sheet).createTable(new AreaReference(new CellReference(0, 0),
					new CellReference(rowIndex, CellUtil.getRow(0, sheet).getLastCellNum() - 1),
					SpreadsheetVersion.EXCEL2007));
			table.setName("Blockstimmen");
			table.setDisplayName("Blockstimmen");
			table.setStyleName("TableStyleLight1");
			table.getCTTable().addNewAutoFilter();
			table.getCTTable().getTableStyleInfo().setShowFirstColumn(true);
		}

		private void writeBlockVotesOfParty(final Row row, final Party party) {
			final Optional<CellStyle> cellStyle = Optional.of(cellStyleParties.get(party));

			// Gruppierung
			int columnIndex = 0;
			getCell(row, columnIndex, cellStyle).setCellValue(party.getShortName());

			for (final LocalPollingStation pollingStation : result.getElection().getPollingStations()) {
				// Stimmen
				columnIndex += 1;
				getCell(row, columnIndex, cellStyle).setCellValue(
						result.filterByDistrict(pollingStation).getPartyResults().get(party).getNumberOfBlockVotings());

				// Stimmen %
				columnIndex += 1;
				getCell(row, columnIndex, cellStyle).setCellFormula("Blockstimmen[[#This Row],["
						+ pollingStation.getName()
						+ "]] / SUM(Blockstimmen["
						+ pollingStation.getName()
						+ "])");

				// Stimmen +-%
				columnIndex += 1;
				getCell(row, columnIndex, cellStyle).setCellFormula("Blockstimmen["
						+ pollingStation.getName()
						+ " %] - INDEX(Gruppierungen["
						+ pollingStation.getName()
						+ " %], MATCH(Blockstimmen[Gruppierung], Gruppierungen[Gruppierung], 0))");
			}

			// Gesamt
			columnIndex += 1;
			getCell(row, columnIndex, cellStyle).setCellFormula(result.getElection()
					.getPollingStations()
					.stream()
					.map(LocalPollingStation::getName)
					.collect(joining("] + Blockstimmen[", "Blockstimmen[", "]")));

			// Gesamt %
			columnIndex += 1;
			getCell(row, columnIndex, cellStyle)
					.setCellFormula("Blockstimmen[[#This Row],[Gesamt]] / SUM(Blockstimmen[Gesamt])");

			// Gesamt +-%
			columnIndex += 1;
			getCell(row, columnIndex, cellStyle).setCellFormula(
					"Blockstimmen[Gesamt %] - INDEX(Gruppierungen[Gesamt %], MATCH(Blockstimmen[Gruppierung], Gruppierungen[Gruppierung], 0))");
		}

		private void writeNominations(final Sheet sheet) {
			sheet.createFreezePane(4, 0);
			sheet.setDefaultColumnStyle(5 + result.getElection().getPollingStations().size(),
					cellStyleSainteLagueValue); // TODO: Sainte-Laguë-Wert

			// TODO: Information rows

			int rowIndex = 0;
			final List<String> headers = new ArrayList<>();
			headers.add("#");
			headers.add("Gruppierung");
			headers.add("Nachname");
			headers.add("Vorname");
			for (final LocalPollingStation pollingStation : result.getElection().getPollingStations()) {
				headers.add(pollingStation.getName());
			}
			headers.add("Gesamt");
			headers.add("Sainte-Laguë-Wert");
			headers.add("Mandat");
			createHeader(CellUtil.getRow(rowIndex, sheet), headers.toArray(new String[0]));

			for (final LocalNominationResult nominationResult : result.getNominationResults().values()) {
				rowIndex += 1;
				writeNomination(CellUtil.getRow(rowIndex, sheet), nominationResult);
			}

			final XSSFTable table = ((XSSFSheet) sheet).createTable(new AreaReference(new CellReference(0, 0),
					new CellReference(rowIndex, CellUtil.getRow(0, sheet).getLastCellNum() - 1),
					SpreadsheetVersion.EXCEL2007));
			table.setName("Kandidierende");
			table.setDisplayName("Kandidierende");
			table.setStyleName("TableStyleLight1");
			table.getCTTable().addNewAutoFilter();
		}

		private void writeNomination(final Row row, final LocalNominationResult nominationResult) {
			final LocalNomination nomination = nominationResult.getNomination();
			final Optional<CellStyle> cellStyle = nomination.getParty().map(cellStyleParties::get);

			// #
			int columnIndex = 0;
			nomination.getParty()
					.map(party -> new ArrayList<>(result.getElection().getNominations(party)).indexOf(nomination) + 1)
					.ifPresent(getCell(row, columnIndex, cellStyle)::setCellValue);

			// Gruppierung
			columnIndex += 1;
			nomination.getParty()
					.map(Party::getShortName)
					.ifPresent(getCell(row, columnIndex, cellStyle)::setCellValue);

			// Nachname
			columnIndex += 1;
			getCell(row, columnIndex, cellStyle).setCellValue(nomination.getPerson().getFamilyName());

			// Vorname
			columnIndex += 1;
			getCell(row, columnIndex, cellStyle).setCellValue(nomination.getPerson().getGivenName());

			for (final LocalPollingStation pollingStation : result.getElection().getPollingStations()) {
				// Stimmen
				columnIndex += 1;
				getCell(row, columnIndex, cellStyle).setCellValue(result.filterByDistrict(pollingStation)
						.getNominationResults()
						.get(nomination)
						.getNumberOfVotes());
			}

			// Gesamt
			columnIndex += 1;
			getCell(row, columnIndex, cellStyle).setCellFormula(result.getElection()
					.getPollingStations()
					.stream()
					.map(LocalPollingStation::getName)
					.collect(joining("] + Kandidierende[", "Kandidierende[", "]")));

			// Sainte-Laguë-Wert
			columnIndex += 1;
			nominationResult.getSainteLagueValue()
					.map(BigDecimal::doubleValue) // TODO
					.ifPresent(getCell(row, columnIndex, cellStyle)::setCellValue);

			// Mandat
			columnIndex += 1;
			getDisplayValue(nominationResult.getType()).ifPresent(getCell(row, columnIndex, cellStyle)::setCellValue);
		}
	}
}
