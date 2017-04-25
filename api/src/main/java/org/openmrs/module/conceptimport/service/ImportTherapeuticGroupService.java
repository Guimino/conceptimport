/**
 *
 */
package org.openmrs.module.conceptimport.service;

import java.util.List;

import org.openmrs.Concept;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.conceptimport.dao.HibernateConceptDictionaryDAO;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Guimino Neves
 *
 */
@Transactional
public interface ImportTherapeuticGroupService extends OpenmrsService {

	void setConceptDictionaryDAO(HibernateConceptDictionaryDAO hibernateConceptDictionary);

	void setDbSessionManager(DbSessionManager dbSessionManager);

	void createConcepts(List<Concept> codedConcepts);

	String getQuestionConceptNamePT();

	String getQuestionConceptNameEN();
}
