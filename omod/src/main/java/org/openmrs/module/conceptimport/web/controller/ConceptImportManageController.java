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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.module.conceptimport.XlsToEntitiesTransformer;
import org.openmrs.module.conceptimport.service.ImportGeneralDrugConceptService;
import org.openmrs.module.conceptimport.service.ImportPharmaceuticalFormService;
import org.openmrs.module.conceptimport.service.ImportTherapeuticGroupService;
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
public class ConceptImportManageController extends BaseOpenmrsObject {

	protected final Log log = LogFactory.getLog(this.getClass());

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
		List<Concept> concepts = new ArrayList<Concept>();

		// TODO, this is a choice that will came from a combobox select
		final String option = "3";

		if (option.equals("1")) {

			concepts = this.xLSToEntitiesTransformer.toEntitiesFarmaceuticalFormConcepts(xlsFile);
			final ImportPharmaceuticalFormService importPharmaceuticalFormService = Context
					.getService(ImportPharmaceuticalFormService.class);
			importPharmaceuticalFormService.createConcepts(concepts);

		} else if (option.equals("2")) {

			concepts = this.xLSToEntitiesTransformer.toEntitiesTherapeuticGroupConcepts(xlsFile);
			final ImportTherapeuticGroupService importTherapeuticGroupService = Context
					.getService(ImportTherapeuticGroupService.class);
			importTherapeuticGroupService.createConcepts(concepts);
		} else if (option.equals("3")) {

			concepts = this.xLSToEntitiesTransformer.toEntitiesGeneralDrugsConcepts(xlsFile);
			final ImportGeneralDrugConceptService importGeneralDrugConceptService = Context
					.getService(ImportGeneralDrugConceptService.class);

			importGeneralDrugConceptService.createConcepts(concepts);
		}
	}

	@Override
	public Integer getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setId(final Integer id) {
		// TODO Auto-generated method stub

	}
}
