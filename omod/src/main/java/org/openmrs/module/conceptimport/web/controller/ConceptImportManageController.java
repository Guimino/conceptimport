/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.conceptimport.web.controller;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.module.conceptimport.OptionImport;
import org.openmrs.module.conceptimport.XlsToOpenMRSEntityTransformer;
import org.openmrs.module.conceptimport.exception.ConceptImportBusinessException;
import org.openmrs.module.conceptimport.service.ImportConceptService;
import org.openmrs.module.conceptimport.service.ImportDrugService;
import org.openmrs.module.conceptimport.util.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * The main controller.
 */
@Controller
public class ConceptImportManageController {

	protected final Log log = LogFactory.getLog(this.getClass());

	@Autowired
	private XlsToOpenMRSEntityTransformer xlsToOpenMRSEntityTransformer;

	@Autowired
	private ImportConceptService importConceptService;

	@Autowired
	private ImportDrugService importDrugService;

	@RequestMapping(value = "/module/conceptimport/manage", method = RequestMethod.GET)
	public void manage(final ModelMap model) {
		model.addAttribute("user", Context.getAuthenticatedUser());
	}

	@RequestMapping(value = "/module/conceptimport/manage", method = RequestMethod.POST)
	public void onSubmit(@RequestParam("xlsFile") final MultipartFile multipart,
			@RequestParam("optionImport") final OptionImport optionImport) throws IOException {

		final File xlsFile = new File(multipart.getOriginalFilename());
		multipart.transferTo(xlsFile);

		final long startTime = System.nanoTime();

		if ((optionImport == null)) {
			throw new IllegalArgumentException("Select Option to import");
		}

		if (optionImport.equals(OptionImport.LOAD_CONCEPTS)) {

			final List<List<Concept>> entities = this.xlsToOpenMRSEntityTransformer
					.toEntitiesQuestionCodedConcept(xlsFile);

			this.importConceptService.load(entities);

		} else if (optionImport.equals(OptionImport.LOAD_DRUGS)) {

			final List<Row> drugs = this.xlsToOpenMRSEntityTransformer.toEntitiesDrug(xlsFile);

			try {
				this.importDrugService.load(drugs);
			} catch (final ConceptImportBusinessException e) {
				e.printStackTrace();
			}
		}
		final long stopTime = System.nanoTime();

		final NumberFormat formatter = new DecimalFormat("#0.00000");

		this.log.info(" Time spent on execution: " + formatter.format((stopTime - startTime) / 1000d) + " seconds");
	}

	@ModelAttribute("optionsToImport")
	public List<OptionImport> getOptionsToImport() {

		return Arrays.asList(OptionImport.values());
	}
}
