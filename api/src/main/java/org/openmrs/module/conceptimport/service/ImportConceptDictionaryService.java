/**
 *
 */
package org.openmrs.module.conceptimport.service;

import java.util.List;

import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.conceptimport.dao.DbSessionManager;
import org.openmrs.module.conceptimport.dao.HibernateConceptDictionaryDAO;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Guimino Neves
 *
 */
@Transactional
public interface ImportConceptDictionaryService extends OpenmrsService {

	void setConceptService(final ConceptService conceptService);

	void setConceptDictionaryDAO(HibernateConceptDictionaryDAO hibernateConceptDictionary);

	void setDbSessionManager(DbSessionManager dbSessionManager);

	void createPharmaceuticalFormConcepts(List<Concept> codedConcepts);

}
