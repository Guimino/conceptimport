/**
 *
 */
package org.openmrs.module.conceptimport.service;

import java.util.List;

import org.openmrs.Concept;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.conceptimport.dao.HibernateConceptDictionaryDAO;
import org.openmrs.module.conceptimport.exception.EntityExistisException;

/**
 * @author Guimino Neves
 *
 */

public interface ImportGeneralDrugConceptService extends OpenmrsService {

	void setConceptDictionaryDAO(HibernateConceptDictionaryDAO hibernateConceptDictionary);

	void setDbSessionManager(DbSessionManager dbSessionManager);

	void createConcepts(List<Concept> codedConcepts) throws EntityExistisException;
}
