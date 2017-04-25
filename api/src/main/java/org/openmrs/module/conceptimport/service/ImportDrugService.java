/**
 *
 */
package org.openmrs.module.conceptimport.service;

import java.util.List;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.conceptimport.dao.HibernateConceptDictionaryDAO;
import org.openmrs.module.conceptimport.exception.ConceptImportBusinessException;
import org.openmrs.module.conceptimport.util.Row;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Guimino Neves
 *
 */
@Transactional
public interface ImportDrugService extends OpenmrsService {

	void setConceptDictionaryDAO(HibernateConceptDictionaryDAO hibernateConceptDictionary);

	void setDbSessionManager(DbSessionManager dbSessionManager);

	void load(List<Row> rows) throws ConceptImportBusinessException;
}
