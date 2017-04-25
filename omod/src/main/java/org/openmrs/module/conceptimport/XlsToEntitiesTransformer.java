/**
 *
 */
package org.openmrs.module.conceptimport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.module.conceptimport.util.Row;
import org.springframework.stereotype.Component;

/**
 * @author Guimino Neves
 *
 */
@Component
public class XlsToEntitiesTransformer {

	static final String LOCALE_PT = "pt";

	public List<Concept> toEntitiesFarmaceuticalFormConcepts(final File file) throws IOException {

		final List<Concept> concepts = new ArrayList<Concept>();

		final Set<String> loadedRows = new TreeSet<String>();

		if (file.isFile() && file.exists()) {
			final FileInputStream fileInputStream = new FileInputStream(file);
			final HSSFWorkbook workbook = new HSSFWorkbook(fileInputStream);

			final HSSFSheet sheet = workbook.getSheetAt(0);

			final int totalRows = sheet.getLastRowNum();

			final Locale localPT = new Locale(XlsToEntitiesTransformer.LOCALE_PT);

			for (int i = 3; i < totalRows; i++) {
				final HSSFRow row = sheet.getRow(i);

				final String name = Normalizer.normalize(row.getCell(1).getStringCellValue(), Normalizer.Form.NFD)
						.replaceAll("[^\\p{ASCII}]", "").toUpperCase().trim();
				final String shortName = Normalizer.normalize(row.getCell(2).getStringCellValue(), Normalizer.Form.NFD)
						.replaceAll("[^\\p{ASCII}]", "").toUpperCase().trim();

				if (!loadedRows.contains(name + shortName)) {
					final Concept concept = new Concept();
					final ConceptName conceptName = new ConceptName(name, localPT);
					final ConceptName shortDesignation = new ConceptName(shortName, localPT);

					concept.setNames(Arrays.asList(conceptName, shortDesignation));
					concepts.add(concept);
					loadedRows.add(name + shortName);
				}
			}

			fileInputStream.close();
			workbook.close();
		}
		return concepts;
	}

	public List<Concept> toEntitiesTherapeuticGroupConcepts(final File file) throws IOException {

		final List<Concept> concepts = new ArrayList<Concept>();

		final Set<String> loadedRows = new TreeSet<String>();

		if (file.isFile() && file.exists()) {
			final FileInputStream fileInputStream = new FileInputStream(file);
			final HSSFWorkbook workbook = new HSSFWorkbook(fileInputStream);

			final HSSFSheet sheet = workbook.getSheetAt(0);

			final int totalRows = sheet.getLastRowNum();

			final Locale localPT = new Locale(XlsToEntitiesTransformer.LOCALE_PT);

			for (int i = 4; i < totalRows; i++) {
				final HSSFRow row = sheet.getRow(i);

				final String name = Normalizer.normalize(row.getCell(1).getStringCellValue(), Normalizer.Form.NFD)
						.replaceAll("[^\\p{ASCII}]", "").toUpperCase().trim();

				if (!loadedRows.contains(name)) {
					final Concept concept = new Concept();
					final ConceptName conceptName = new ConceptName(name, localPT);

					concept.setNames(Arrays.asList(conceptName));
					concepts.add(concept);
					loadedRows.add(name);
				}
			}

			fileInputStream.close();
			workbook.close();
		}
		return concepts;
	}

	public List<Concept> toEntitiesGeneralDrugsConcepts(final File file) throws IOException {

		final List<Concept> concepts = new ArrayList<Concept>();

		if (file.isFile() && file.exists()) {
			final FileInputStream fileInputStream = new FileInputStream(file);
			final HSSFWorkbook workbook = new HSSFWorkbook(fileInputStream);

			final HSSFSheet sheet = workbook.getSheetAt(0);

			final int totalRows = sheet.getLastRowNum();

			final Locale localPT = new Locale(XlsToEntitiesTransformer.LOCALE_PT);

			for (int i = 6; i < totalRows; i++) {
				final HSSFRow row = sheet.getRow(i);

				final String firstcell = row.getCell(0).getStringCellValue().trim();

				if (firstcell.equals("NEW_GENERIC_CONCEPT_NAME")) {

					final String designationPT = Normalizer
							.normalize(row.getCell(1).getStringCellValue(), Normalizer.Form.NFD)
							.replaceAll("[^\\p{ASCII}]", "").toUpperCase().trim();

					final String str = Normalizer.normalize(row.getCell(2).getStringCellValue(), Normalizer.Form.NFD)
							.replaceAll("[^\\p{ASCII}]", "").toUpperCase().trim();

					final String designationEN = str.split(",")[0].trim();

					final Concept concept = new Concept();
					final ConceptName conceptNamePT = new ConceptName(designationPT, localPT);
					final ConceptName conceptNameEN = new ConceptName(designationEN, Locale.ENGLISH);

					concept.setNames(Arrays.asList(conceptNamePT, conceptNameEN));
					concepts.add(concept);

				}
			}

			fileInputStream.close();
			workbook.close();
		}

		return concepts;
	}

	public List<Concept> toEntitiesDosingUnitConcept(final File file) throws IOException {

		final List<String> NAMES_TO_SKIP = Arrays.asList("1", "SEM DOSAGEM");
		final List<Concept> concepts = new ArrayList<Concept>();

		final Set<String> loadedRows = new TreeSet<String>();

		if (file.isFile() && file.exists()) {
			final FileInputStream fileInputStream = new FileInputStream(file);
			final HSSFWorkbook workbook = new HSSFWorkbook(fileInputStream);

			final HSSFSheet sheet = workbook.getSheetAt(0);

			final int totalRows = sheet.getLastRowNum();

			final Locale localPT = new Locale(XlsToEntitiesTransformer.LOCALE_PT);

			for (int i = 1; i < totalRows; i++) {
				final HSSFRow row = sheet.getRow(i);

				final String designation = Normalizer
						.normalize(row.getCell(0).getStringCellValue(), Normalizer.Form.NFD)
						.replaceAll("[^\\p{ASCII}]", "").toUpperCase().trim();

				if (!loadedRows.contains(designation) && !NAMES_TO_SKIP.contains(designation)) {
					final Concept concept = new Concept();
					final ConceptName conceptNamePT = new ConceptName(designation, localPT);
					final ConceptName conceptNameEN = new ConceptName(designation, Locale.ENGLISH);

					concept.setNames(Arrays.asList(conceptNamePT, conceptNameEN));
					concepts.add(concept);
					System.out.println(designation);

					loadedRows.add(designation);
				}
			}

			fileInputStream.close();
			workbook.close();
		}

		return concepts;
	}

	public List<Row> loadDrugRows(final File file) throws IOException {

		final List<Row> rows = new ArrayList<Row>();

		final Set<String> loadedRows = new TreeSet<String>();

		if (file.isFile() && file.exists()) {
			final FileInputStream fileInputStream = new FileInputStream(file);
			final HSSFWorkbook workbook = new HSSFWorkbook(fileInputStream);

			final HSSFSheet sheet = workbook.getSheetAt(0);

			final int totalRows = sheet.getLastRowNum();

			for (int i = 6; i < totalRows; i++) {
				final HSSFRow row = sheet.getRow(i);

				final Row currentRow = new Row(row.getCell(0).getStringCellValue(), row.getCell(1).getStringCellValue(),
						row.getCell(2).getStringCellValue(), row.getCell(3).getStringCellValue(),
						row.getCell(4).getStringCellValue(), row.getCell(5).getStringCellValue(),
						row.getCell(8).getStringCellValue(),
						Double.valueOf(row.getCell(13).getNumericCellValue()).intValue(),
						Double.valueOf(row.getCell(14).getNumericCellValue()).intValue(),
						Double.valueOf(row.getCell(15).getNumericCellValue()).intValue());

				if (!loadedRows.contains(currentRow.getFnm())) {

					rows.add(currentRow);
					loadedRows.add(currentRow.getFnm());
				}
			}

			fileInputStream.close();
			workbook.close();
		}

		return rows;
	}
}