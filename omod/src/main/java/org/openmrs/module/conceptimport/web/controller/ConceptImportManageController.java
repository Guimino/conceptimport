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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.module.conceptimport.XlsToEntitiesTransformer;
import org.openmrs.module.conceptimport.service.ImportConceptDictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
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
	private ImportConceptDictionaryService importConceptDictionaryService;

	@Autowired
	private XlsToEntitiesTransformer xLSToEntitiesTransformer;

	@RequestMapping(value = "/module/conceptimport/manage", method = RequestMethod.GET)
	public void manage(final ModelMap model) {
		model.addAttribute("user", Context.getAuthenticatedUser());
	}

	@RequestMapping(value = "/module/conceptimport/manage", method = RequestMethod.POST)
	public void onSubmit(@RequestParam("xlsFile") final MultipartFile multipart) throws IOException {

		final File xlsFile = new File(multipart.getOriginalFilename());
		multipart.transferTo(xlsFile);

		final List<Concept> concepts = this.xLSToEntitiesTransformer.toEntitiesFarmaceuticalFormConcepts(xlsFile);

		this.importConceptDictionaryService.createPharmaceuticalFormConcepts(concepts);
	}
}
