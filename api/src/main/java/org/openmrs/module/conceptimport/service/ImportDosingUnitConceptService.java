/**
 *
 */
package org.openmrs.module.conceptimport.service;

import java.util.List;

import org.openmrs.Concept;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.conceptimport.dao.HibernateConceptDictionaryDAO;
import org.openmrs.module.conceptimport.exception.ConceptImportBusinessException;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Guimino Neves
 *
 */
@Transactional
public interface ImportDosingUnitConceptService extends OpenmrsService {

	void setConceptDictionaryDAO(HibernateConceptDictionaryDAO hibernateConceptDictionary);

	void setDbSessionManager(DbSessionManager dbSessionManager);

	void load(List<Concept> concepts) throws ConceptImportBusinessException;

	String getQuestionConceptNamePT();

	String getQuestionConceptNameEN();
}
