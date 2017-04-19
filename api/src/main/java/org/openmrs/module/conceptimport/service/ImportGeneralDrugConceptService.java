/**
 *
 */
package org.openmrs.module.conceptimport.service;

import java.util.List;

import org.openmrs.Concept;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.conceptimport.dao.DbSessionManager;
import org.openmrs.module.conceptimport.dao.HibernateConceptDictionaryDAO;

/**
 * @author Guimino Neves
 *
 */

public interface ImportGeneralDrugConceptService extends OpenmrsService {

	static final String CONCEPT_CLASS_NAME_DRUG = "Drug";
	static final String CONCEPT_DATA_TYPE_NA = "N/A";

	static final String LOCALE_PT = "pt";

	void setConceptDictionaryDAO(HibernateConceptDictionaryDAO hibernateConceptDictionary);

	void setDbSessionManager(DbSessionManager dbSessionManager);

	void createConcepts(List<Concept> codedConcepts);
}
