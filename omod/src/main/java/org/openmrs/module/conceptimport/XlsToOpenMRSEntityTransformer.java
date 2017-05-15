/**
 *
 */
package org.openmrs.module.conceptimport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.api.ConceptNameType;
import org.openmrs.module.conceptimport.util.Row;
import org.springframework.stereotype.Component;

/**
 * @author Guimino Neves
 *
 */
@Component
public class XlsToOpenMRSEntityTransformer {

	static final Locale LOCALE_PT;
	static {
		LOCALE_PT = new Locale("pt");
	}

	public List<List<Concept>> toEntitiesQuestionCodedConcept(final File file) throws IOException {

		final List<List<Concept>> result = new ArrayList<List<Concept>>();

		if (file.isFile() && file.exists()) {

			final FileInputStream fileInputStream = new FileInputStream(file);
			final HSSFWorkbook workbook = new HSSFWorkbook(fileInputStream);

			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {

				final List<Concept> concepts = new ArrayList<Concept>();

				final HSSFSheet sheet = workbook.getSheetAt(i);
				final int totalRows = sheet.getLastRowNum();

				concepts.add(this.instatientConcept(sheet.getRow(2)));

				for (int j = 6; j < totalRows; j++) {
					concepts.add(this.instatientConcept(sheet.getRow(j)));
				}
				result.add(concepts);
			}

			fileInputStream.close();
			workbook.close();
		}
		return result;
	}

	public List<Row> toEntitiesDrug(final File file) throws IOException {

		final List<Row> rows = new ArrayList<Row>();

		final Set<String> loadedRows = new TreeSet<String>();

		if (file.isFile() && file.exists()) {
			final FileInputStream fileInputStream = new FileInputStream(file);
			final HSSFWorkbook workbook = new HSSFWorkbook(fileInputStream);

			final HSSFSheet sheet = workbook.getSheetAt(0);

			final int totalRows = sheet.getLastRowNum();

			for (int i = 6; i < totalRows; i++) {
				final HSSFRow row = sheet.getRow(i);

				final Row currentRow = new Row(this.normalize(row.getCell(0).getStringCellValue()),
						this.normalize(row.getCell(1).getStringCellValue()),
						this.normalize(row.getCell(2).getStringCellValue()),
						this.normalize(row.getCell(4).getStringCellValue()),
						this.normalize(row.getCell(5).getStringCellValue()).toLowerCase(),
						this.normalize(row.getCell(7).getStringCellValue()),
						this.normalize(row.getCell(8).getStringCellValue()));

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

	private Concept instatientConcept(final HSSFRow row) {

		final String namePT = row.getCell(0).getStringCellValue().trim();
		final String namePTShort = row.getCell(1).getStringCellValue().trim();
		final String nameEN = row.getCell(2).getStringCellValue().trim();
		final String nameENShort = row.getCell(3).getStringCellValue().trim();

		if ((namePT == null) || (namePT.length() < 2)) {

			throw new IllegalArgumentException("Invalid Concept Name " + namePT + " -> line " + row.getRowNum());
		}

		final List<ConceptName> conceptNames = new ArrayList<ConceptName>();

		conceptNames.add(this.instatietConceptName(namePT, XlsToOpenMRSEntityTransformer.LOCALE_PT, true,
				ConceptNameType.FULLY_SPECIFIED));

		if ((namePTShort != null) && (namePTShort.length() > 1)) {
			conceptNames.add(this.instatietConceptName(namePTShort, XlsToOpenMRSEntityTransformer.LOCALE_PT, false,
					ConceptNameType.SHORT));
		}

		if ((nameEN != null) && (nameEN.length() > 1)) {
			conceptNames.add(this.instatietConceptName(nameEN, Locale.ENGLISH, true, ConceptNameType.FULLY_SPECIFIED));
		}

		if ((nameENShort != null) && (nameENShort.length() > 1)) {
			conceptNames.add(this.instatietConceptName(nameENShort, Locale.ENGLISH, false, ConceptNameType.SHORT));
		}

		final Concept concept = new Concept();
		concept.setNames(conceptNames);

		return concept;
	}

	private ConceptName instatietConceptName(final String name, final Locale locale, final boolean localePreferred,
			final ConceptNameType conceptNameType) {

		final ConceptName conceptName = new ConceptName(this.normalize(name), locale);
		conceptName.setLocalePreferred(localePreferred);
		conceptName.setConceptNameType(conceptNameType);

		return conceptName;
	}

	private String normalize(final String toNormalize) {
		return Normalizer.normalize(toNormalize, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toUpperCase()
				.trim();
	}

}