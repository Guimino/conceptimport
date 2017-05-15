/**
 *
 */
package org.openmrs.module.conceptimport.service;

import java.util.List;

import org.openmrs.Concept;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.conceptimport.dao.HibernateConceptDictionaryDAO;

/**
 * @author Guimino Neves
 *
 */

public interface ImportConceptService extends OpenmrsService {

	void setConceptDictionaryDAO(HibernateConceptDictionaryDAO hibernateConceptDictionary);

	void load(List<List<Concept>> codedConcepts);
}
