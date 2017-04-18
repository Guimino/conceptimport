/**
 *
 */
package org.openmrs.module.conceptimport.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Guimino Neves
 *
 */
@Transactional
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

		this.log.info("-- Begin Importing General Drugs Concepts... --");

		this.dbSessionManager.setManualFlushMode();

		for (final Concept concept : codedConcepts) {

			this.createNewQuestionConcept(concept);
		}

		this.log.info("--End Importing General Drugs Concepts! --");
	}

	/**
	 * Creates the Concept which will represent General Term for Drugs
	 *
	 * @return
	 */
	private void createNewQuestionConcept(final Concept concept) {

		final ConceptService conceptService = Context.getConceptService();

		final ConceptClass conceptClassDrug = conceptService
				.getConceptClassByName(ImportGeneralDrugConceptService.CONCEPT_CLASS_NAME_DRUG);

		final ConceptDatatype conceptDatatypeNA = conceptService
				.getConceptDatatypeByName(ImportGeneralDrugConceptService.CONCEPT_DATA_TYPE_NA);

		Concept newConcept = new Concept();
		newConcept.setConceptClass(conceptClassDrug);
		newConcept.setDatatype(conceptDatatypeNA);

		final Collection<ConceptName> namesToCreate = new HashSet<ConceptName>();
		final Iterator<ConceptName> names = concept.getNames().iterator();

		final ConceptName firstName = names.next();
		final ConceptName secondName = names.next();

		namesToCreate.add(new ConceptName(firstName.getName(), new Locale(ImportGeneralDrugConceptService.LOCALE_PT)));

		if ((secondName != null) && (secondName.getName() != null) && (secondName.getName().length() > 2)) {

			namesToCreate.add(new ConceptName(secondName.getName(), Locale.ENGLISH));
		}
		newConcept.setNames(namesToCreate);

		Concept found = null;
		for (final ConceptName conceptName : namesToCreate) {

			// TODO: Review this duplicated code
			if ((conceptName.getName() != null) && (conceptName.getName().length() > 2)) {
				try {
					found = this.findConceptByName(conceptName.getName());

				} catch (final NoSuchElementException e) {

				}
			}
		}

		if (found != null) {

			if (!found.getConceptClass().getName().equals(ImportGeneralDrugConceptService.CONCEPT_CLASS_NAME_DRUG)) {

				this.log.info("Concept With name " + found.getName().getName()
						+ " Exists and does not belong to Drug Class. Belongs to Class ["
						+ found.getConceptClass().getName() + "]");

				System.out.println("Concept With name " + found.getName().getName()
						+ " Exists and does not belong to Drug Class. Belongs to Class ["
						+ found.getConceptClass().getName() + "]");

				return;
			}

			newConcept = found;

			if (newConcept.isRetired()) {

				newConcept.setRetired(false);
				conceptService.saveConcept(newConcept);
			}
		} else {

			boolean shouldSave = false;

			for (final ConceptName conceptName : namesToCreate) {

				// TODO: Review this duplicated code
				if ((conceptName.getName() != null) && (conceptName.getName().length() > 2)) {
					conceptName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
					conceptName.setUuid(UUID.randomUUID().toString());

					shouldSave = true;
				}
			}
			if (shouldSave) {

				try {

					newConcept.setNames(namesToCreate);
					conceptService.saveConcept(newConcept);
					this.log.info("Created Concept --> " + newConcept);

				} catch (final DuplicateConceptNameException e) {

					this.log.info(
							" Found Used Concept Name: " + newConcept.getName().getName() + " Error:" + e.getMessage());

					System.out.println(
							" Found Used Concept Name: " + newConcept.getName().getName() + " Error:" + e.getMessage());
				}
			}
		}
	}

	private Concept findConceptByName(final String conceptName) {

		final ConceptService conceptService = Context.getConceptService();

		final Concept concept = conceptService.getConceptByName(conceptName);

		if (concept == null) {
			throw new NoSuchElementException();
		}

		return concept;
	}

}
