/**
 *
 */
package org.openmrs.module.conceptimport.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.Drug;
import org.openmrs.api.ConceptNameType;
import org.openmrs.module.conceptimport.exception.ConceptImportBusinessException;
import org.openmrs.module.conceptimport.exception.EntityNotFoundException;
import org.openmrs.module.conceptimport.service.ImportConceptConstants;
import org.openmrs.module.pharmacyapi.api.model.DrugItem;

/**
 * @author Guimino Neves
 *
 */
public class HibernateConceptDictionaryDAOImpl implements HibernateConceptDictionaryDAO {

	private SessionFactory sessionFactory;

	@Override
	public void setSessionFactory(final SessionFactory sessionFactory) {

		this.sessionFactory = sessionFactory;
	}

	@Override
	public ConceptClass findConceptClassByName(final String className) throws NoSuchElementException {

		final String hql = "select conceptClass from ConceptClass as conceptClass where conceptClass.name =:className";

		final Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
		query.setParameter("className", className);

		return (ConceptClass) query.list().get(0);
	}

	@Override
	public ConceptDatatype findConceptDataTypeByName(final String className) throws NoSuchElementException {

		final String hql = "select conceptDataType from ConceptDatatype as conceptDataType where conceptDataType.name =:className";

		final Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
		query.setParameter("className", className);

		return (ConceptDatatype) query.list().get(0);
	}

	@Override
	public boolean hasConceptAnswered(final Concept question, final Concept answer) {

		if ((answer == null) || (answer.getConceptId() == null)) {
			return false;
		}

		final String hql = "select conceptAnswer from ConceptAnswer conceptAnswer "
				+ " where conceptAnswer.concept.conceptId = :questionId "
				+ " and conceptAnswer.answerConcept.conceptId = :answerId";

		final Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
		query.setParameter("questionId", question.getConceptId());
		query.setParameter("answerId", answer.getConceptId());

		return !query.list().isEmpty();
	}

	@Override
	public Concept findConceptByConceptNameAndClassName(final ConceptName conceptName, final String className)
			throws NoSuchElementException {

		final String hql = "select concept from Concept as concept " + " join concept.names as names "
				+ " join concept.conceptClass as classe " + " where names.conceptNameType = 'FULLY_SPECIFIED' "
				+ " and classe.name = :className " + " and upper(names.name) = :conceptName";

		final Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
		query.setParameter("conceptName", conceptName.getName().toUpperCase());
		query.setParameter("className", className);

		final Object uniqueResult = query.uniqueResult();

		if (uniqueResult == null) {
			throw new NoSuchElementException();
		}

		return (Concept) uniqueResult;
	}

	@Override
	public SessionFactory getSessionFactory() {
		return this.sessionFactory;
	}

	@Override
	public Concept findConceptByConceptNameAndClassNameAndNameType(final ConceptName conceptName,
			final String className, final ConceptNameType conceptNameType) throws NoSuchElementException {
		final String hql = "select concept from Concept as concept " + " join concept.names as names "
				+ " join concept.conceptClass as classe " + " where names.conceptNameType = '"
				+ conceptNameType.toString() + "'" + " and classe.name = :className "
				+ " and upper(names.name) = :conceptName";

		final Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
		query.setParameter("conceptName", conceptName.getName().toUpperCase());
		query.setParameter("className", className);

		final Object uniqueResult = query.uniqueResult();

		if (uniqueResult == null) {
			throw new NoSuchElementException();
		}

		return (Concept) uniqueResult;

	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Concept> findConceptsByClass(final String className) {

		final Map<String, Concept> results = new HashMap<String, Concept>();

		final Query query = this.sessionFactory.getCurrentSession()
				.createQuery(" select c from Concept c where c.conceptClass.name =:name")
				.setParameter("name", className);

		final List<Concept> concepts = query.list();

		for (final Concept concept : concepts) {

			results.put(concept.getName(new Locale(ImportConceptConstants.LOCALE_PT)).getName(), concept);
		}

		return results;
	}

	@Override
	public Drug findDrugByFnmCode(final String fnmCode) throws ConceptImportBusinessException {

		final Query query = this.sessionFactory.getCurrentSession()
				.createSQLQuery("select * from phm_drug_items where fnm_code = :fnmCode").addEntity(DrugItem.class)
				.setParameter("fnmCode", fnmCode);

		final DrugItem drugItem = (DrugItem) query.uniqueResult();

		if (drugItem == null) {
			throw new EntityNotFoundException(
					"Entity " + Drug.class + " was not found for the parameters fnmCode = " + fnmCode);
		}

		System.out.println("FOUND DRUG: " + drugItem);

		return drugItem.getDrug();
	}

	@Override
	public Concept findConceptByName(final String name) {

		final String hql = "select concept from Concept as concept join concept.names as names "
				+ " where  names.conceptNameType = 'FULLY_SPECIFIED' and upper(names.name) = :name";

		final Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
		query.setParameter("name", name.toUpperCase());

		return (Concept) query.uniqueResult();

	}

}
