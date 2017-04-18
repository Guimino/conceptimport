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
public interface ImportTherapeuticGroupService extends OpenmrsService {

	static final String CONCEPT_CLASS_NAME_MISC = "Misc";
	static final String CONCEPT_CLASS_NAME_QUESTION = "Question";

	static final String CONCEPT_DATA_TYPE_NA = "N/A";
	static final String CONCEPT_DATA_TYPE_CODED = "Coded";

	static final String CONCEPT_QUESTION_NAME_THERAPEUTIC_GROUP_PT = "GRUPO TERAPEUTICO";
	static final String CONCEPT_QUESTION_NAME_THERAPEUTIC_GROUP_EN = "THERAPEUTIC GROUP";

	void setConceptDictionaryDAO(HibernateConceptDictionaryDAO hibernateConceptDictionary);

	void setDbSessionManager(DbSessionManager dbSessionManager);

	void createConcepts(List<Concept> codedConcepts);

	String getQuestionConceptNamePT();

	String getQuestionConceptNameEN();
}
