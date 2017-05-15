/**
 *
 */
package org.openmrs.module.conceptimport.dao;

import java.util.Map;
import java.util.NoSuchElementException;

import org.hibernate.SessionFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.Drug;
import org.openmrs.api.ConceptNameType;
import org.openmrs.module.conceptimport.exception.ConceptImportBusinessException;

/**
 * @author Guimino Neves
 *
 */
public interface HibernateConceptDictionaryDAO {

	void setSessionFactory(SessionFactory sessionFactory);

	SessionFactory getSessionFactory();

	ConceptClass findConceptClassByName(final String className) throws NoSuchElementException;

	ConceptDatatype findConceptDataTypeByName(final String className) throws NoSuchElementException;

	boolean hasConceptAnswered(final Concept question, final Concept answer);

	Concept findConceptByConceptNameAndClassName(final ConceptName conceptName, final String className)
			throws NoSuchElementException;

	Concept findConceptByConceptNameAndClassNameAndNameType(final ConceptName conceptName, String className,
			ConceptNameType conceptNameType) throws NoSuchElementException;

	Map<String, Concept> findConceptsByClass(String className);

	Drug findDrugByFnmCode(final String fnmCode) throws ConceptImportBusinessException;

	Concept findConceptByName(final String name);

}
