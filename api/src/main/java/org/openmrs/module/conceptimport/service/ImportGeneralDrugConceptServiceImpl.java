/**
 *
 */
package org.openmrs.module.conceptimport.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.NonUniqueObjectException;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.DuplicateConceptNameException;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.conceptimport.dao.DbSessionManager;
import org.openmrs.module.conceptimport.dao.HibernateConceptDictionaryDAO;
import org.openmrs.module.conceptimport.exception.EntityExistisException;
import org.openmrs.module.conceptimport.exception.EntityNotExistsException;

/**
 * @author Guimino Neves
 *
 */

public class ImportGeneralDrugConceptServiceImpl extends BaseOpenmrsService implements ImportGeneralDrugConceptService {

	protected final Log log = LogFactory.getLog(this.getClass());

	private HibernateConceptDictionaryDAO conceptDictionaryDAO;

	private DbSessionManager dbSessionManager;

	@Override
	public void setConceptDictionaryDAO(final HibernateConceptDictionaryDAO hibernateConceptDictionary) {

		this.conceptDictionaryDAO = hibernateConceptDictionary;
	}

	@Override
	public void setDbSessionManager(final DbSessionManager dbSessionManager) {
		this.dbSessionManager = dbSessionManager;
	}

	@Override
	public void createConcepts(final List<Concept> codedConcepts) {

		final ConceptService conceptService = Context.getConceptService();

		this.log.info("-- Begin Importing General Drugs Concepts... --");

		this.dbSessionManager.setManualFlushMode();

		final List<Concept> conceptToBeCreated = new ArrayList<Concept>();

		for (final Concept concept : codedConcepts) {

			try {

				conceptToBeCreated.add(this.setConceptAttributes(conceptService, concept));

			} catch (final DuplicateConceptNameException e) {

				this.log.error("Concept : " + concept, e);
			}

			catch (final EntityExistisException e) {

				this.log.error("Concept : " + concept, e);
			}

			catch (final IllegalArgumentException e) {

				this.log.error("Concept : " + concept, e);
			}
		}

		this.batchCreateConcepts(conceptToBeCreated);

		this.log.info("--End Importing General Drugs Concepts! --");
	}

	private void batchCreateConcepts(final List<Concept> concepts) {

		final int count = 1;
		for (final Concept concept : concepts) {

			try {
				this.conceptDictionaryDAO.getSessionFactory().getCurrentSession().saveOrUpdate(concept);

				this.log.info("Created Concept --> " + concept + " -> [" + count + "]");

			} catch (final NonUniqueObjectException e) {

				// TODO: Having some strange Exceptions
				this.log.error("Creating concept with name: " + concept + " -> " + e.getMessage());
			}
		}
	}

	/**
	 * Creates the Concept which will represent General Term for Drugs
	 *
	 * @param conceptService
	 *
	 * @return
	 */
	private Concept setConceptAttributes(final ConceptService conceptService, final Concept concept) {

		final Collection<ConceptName> namesToBeCreated = this.getNamesToBeCreated(concept);

		return this.setAttributes(conceptService, namesToBeCreated);
	}

	private Concept checkDuplications(final ConceptService conceptService,
			final Collection<ConceptName> namesToBeCreated) {
		Concept found = null;

		final StringBuilder sb = new StringBuilder();
		for (final ConceptName conceptName : namesToBeCreated) {

			try {

				final String name = conceptName.getName();
				sb.append(name + " \t");

				found = this.findConceptByName(conceptService, name);

			} catch (final NoSuchElementException e) {
			}
		}

		if (found == null) {
			throw new EntityNotExistsException(" Concept Not Found  name(s) " + sb.toString());
		}

		return found;
	}

	private Concept setAttributes(final ConceptService conceptService, final Collection<ConceptName> namesToBeCreated) {

		final ConceptClass conceptClassDrug = this.conceptDictionaryDAO
				.findConceptClassByName(ImportGeneralDrugConceptService.CONCEPT_CLASS_NAME_DRUG);
		final ConceptDatatype conceptDatatypeNA = this.conceptDictionaryDAO
				.findConceptDataTypeByName(ImportGeneralDrugConceptService.CONCEPT_DATA_TYPE_NA);

		Concept found = null;
		try {

			found = this.checkDuplications(conceptService, namesToBeCreated);

		} catch (final EntityNotExistsException e) {
		}

		if (found != null) {

			if (!found.getConceptClass().equals(conceptClassDrug)) {

				this.log.error("Concept With name " + found.getName().getName()
						+ " Exists and does not belong to Drug Class. Belongs to Class ["
						+ found.getConceptClass().getName() + "]");

				throw new IllegalArgumentException(
						" Concept :" + found + " does not belong to Drug Class. This belongs to class ["
								+ found.getConceptClass().getName() + "]");
			}

			if (found.isRetired()) {

				found.setRetired(false);
				return found;
			}

			throw new EntityExistisException(found);

		} else {

			boolean shouldSave = false;

			for (final ConceptName conceptName : namesToBeCreated) {
				conceptName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
				conceptName.setUuid(UUID.randomUUID().toString());

				shouldSave = true;
			}

			if (shouldSave) {

				final Concept newConcept = new Concept();
				newConcept.setConceptClass(conceptClassDrug);
				newConcept.setDatatype(conceptDatatypeNA);
				newConcept.setNames(namesToBeCreated);

				try {
					conceptService.saveConcept(newConcept);

					this.log.info("Created Concept --> " + newConcept);

					return newConcept;

				} catch (final DuplicateConceptNameException e) {

					this.log.error(
							" Found Used Concept Name: " + newConcept.getName().getName() + " Error:" + e.getMessage());
				}
			}
			throw new DuplicateConceptNameException();
		}
	}

	private Collection<ConceptName> getNamesToBeCreated(final Concept concept) {

		final ConceptName firstName = this.getConceptName(concept.getNames(),
				new Locale(ImportGeneralDrugConceptService.LOCALE_PT));
		final ConceptName secondName = this.getConceptName(concept.getNames(), Locale.ENGLISH);

		final Collection<ConceptName> namesToCreate = new HashSet<ConceptName>();

		if (firstName != null) {
			namesToCreate.add(firstName);
		}

		if (secondName != null) {
			namesToCreate.add(secondName);
		}

		return namesToCreate;
	}

	private ConceptName getConceptName(final Collection<ConceptName> conceptNames, final Locale locale) {

		for (final ConceptName conceptName : conceptNames) {

			if ((conceptName != null) && (conceptName.getName() != null) && (conceptName.getName().length() > 2)
					&& conceptName.getLocale().equals(locale)) {
				return conceptName;
			}
		}
		return null;
	}

	private Concept findConceptByName(final ConceptService conceptService, final String conceptName) {

		final Concept concept = conceptService.getConceptByName(conceptName);

		if (concept == null) {
			throw new NoSuchElementException();
		}

		return concept;
	}
}
