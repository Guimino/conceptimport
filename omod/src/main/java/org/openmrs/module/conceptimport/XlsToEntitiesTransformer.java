/**
 *
 */
package org.openmrs.module.conceptimport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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

	public List<Concept> toEntitiesFarmaceuticalFormConcepts(final File file) throws IOException {

		final List<Concept> concepts = new ArrayList<Concept>();

		if (file.isFile() && file.exists()) {
			final FileInputStream fileInputStream = new FileInputStream(file);
			final HSSFWorkbook workbook = new HSSFWorkbook(fileInputStream);

			final HSSFSheet sheet = workbook.getSheetAt(0);

			final int totalRows = sheet.getLastRowNum();

			final Locale localPT = new Locale("pt", "PT");

			for (int i = 4; i < totalRows; i++) {
				final HSSFRow row = sheet.getRow(i);

				final Concept concept = new Concept();
				final ConceptName conceptName = new ConceptName(row.getCell(1).getStringCellValue(), localPT);
				final ConceptName shortDesignation = new ConceptName(row.getCell(2).getStringCellValue(), localPT);

				concept.setNames(Arrays.asList(conceptName, shortDesignation));

				concepts.add(concept);
			}

			fileInputStream.close();
			workbook.close();
		}

		return concepts;
	}
}