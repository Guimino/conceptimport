/**
 *
 */
package org.openmrs.module.conceptimport.service;

import java.util.List;

import org.openmrs.Concept;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.conceptimport.dao.DbSessionManager;
import org.openmrs.module.conceptimport.dao.HibernateConceptDictionaryDAO;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Guimino Neves
 *
 */
@Transactional
public interface ImportPharmaceuticalFormService extends OpenmrsService {

	static final String CONCEPT_CLASS_NAME_MISC = "Misc";
	static final String CONCEPT_CLASS_NAME_QUESTION = "Question";

	static final String CONCEPT_DATA_TYPE_NA = "N/A";
	static final String CONCEPT_DATA_TYPE_CODED = "Coded";

	static final String PHARMACEUTICAL_FORM_NAME_PT = "FORMA FARMACEUTICA";
	static final String PHARMACEUTICAL_FORM_NAME_EN = "PHARMACEUTICAL FORM";

	void setConceptDictionaryDAO(HibernateConceptDictionaryDAO hibernateConceptDictionary);

	void setDbSessionManager(DbSessionManager dbSessionManager);

	void createConcepts(List<Concept> codedConcepts);

	String getQuestionConceptNamePT();

	String getQuestionConceptNameEN();
}
