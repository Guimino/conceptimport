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

		final Set<String> loadedDesignationsPT = new TreeSet<String>();
		final Set<String> loadedDesignationsEN = new TreeSet<String>();

		if (file.isFile() && file.exists()) {
			final FileInputStream fileInputStream = new FileInputStream(file);
			final HSSFWorkbook workbook = new HSSFWorkbook(fileInputStream);

			final HSSFSheet sheet = workbook.getSheetAt(0);

			final int totalRows = sheet.getLastRowNum();

			final Locale localPT = new Locale(XlsToEntitiesTransformer.LOCALE_PT);

			for (int i = 6; i < totalRows; i++) {
				final HSSFRow row = sheet.getRow(i);

				final boolean isRowGroup = row.getCell(0).getStringCellValue().trim()
						.equals("NEW_GENERIC_CONCEPT_NAME");

				if (isRowGroup) {
					final String designationPT = Normalizer
							.normalize(row.getCell(1).getStringCellValue(), Normalizer.Form.NFD)
							.replaceAll("[^\\p{ASCII}]", "").toUpperCase().trim();

					final String designationEN = Normalizer
							.normalize(row.getCell(2).getStringCellValue(), Normalizer.Form.NFD)
							.replaceAll("[^\\p{ASCII}]", "").toUpperCase().trim().split(",")[0].trim();

					if (!loadedDesignationsPT.contains(designationPT)
							&& !loadedDesignationsEN.contains(designationEN)) {
						final Concept concept = new Concept();
						final ConceptName conceptNamePT = new ConceptName(designationPT, localPT);
						final ConceptName conceptNameEN = new ConceptName(designationEN, Locale.ENGLISH);

						concept.setNames(Arrays.asList(conceptNamePT, conceptNameEN));
						concepts.add(concept);

						loadedDesignationsPT.add(designationPT);
						loadedDesignationsEN.add(designationEN);
					}
				}
			}

			fileInputStream.close();
			workbook.close();
		}
		return concepts;
	}

	public static void main(final String[] args) {
		final String Str = "";

		final String designationEN = Normalizer
				.normalize(Str, Normalizer.Form.NFD)
				.replaceAll("[^\\p{ASCII}]", "").toUpperCase().trim().split(",")[0].trim();


		System.out.println(designationEN);
	}
}