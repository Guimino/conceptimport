/**
 *
 */
package org.openmrs.module.conceptimport.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.NonUniqueObjectException;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptName;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.DuplicateConceptNameException;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.conceptimport.dao.HibernateConceptDictionaryDAO;
import org.openmrs.module.conceptimport.exception.ConceptImportBusinessException;
import org.openmrs.module.conceptimport.exception.EntityExistisException;
import org.openmrs.module.conceptimport.exception.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Guimino Neves
 *
 */
@Transactional
public class ImportDosingUnitConceptServiceImpl extends BaseOpenmrsService implements ImportDosingUnitConceptService {

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
	public void load(final List<Concept> concepts) throws ConceptImportBusinessException {

		final ConceptService conceptService = Context.getConceptService();

		this.log.info("-- Begin Importing  Dosing Units... --");

		this.dbSessionManager.setManualFlushMode();

		final List<Concept> conceptToBeCreated = new ArrayList<Concept>();

		final List<Concept> existingsConcepts = new ArrayList<Concept>();

		for (final Concept concept : concepts) {

			try {

				conceptToBeCreated.add(this.setConceptAttributes(conceptService, concept));

			} catch (final DuplicateConceptNameException e) {

				this.log.error("Concept : " + concept, e);
				existingsConcepts.add(concept);
			}

			catch (final IllegalArgumentException e) {

				this.log.error("Concept : " + concept, e);
			} catch (final EntityExistisException e) {

				existingsConcepts.add(concept);
				this.log.error("Concept: " + concept, e);
			}
		}

		final List<Concept> batchCreatedConcepts = this.batchCreateConcepts(conceptToBeCreated, existingsConcepts);
		batchCreatedConcepts.addAll(this.findExistingsConceptToLink(conceptService, existingsConcepts));

		final Concept questionConcept = this.createNewQuestionConcept();

		this.linkQuestionToCodedConcepts(questionConcept, batchCreatedConcepts);

		this.log.info("-- End Importing  Dosing Units! --");
	}

	private final List<Concept> batchCreateConcepts(final List<Concept> concepts,
			final List<Concept> existingsConcepts) {

		final int count = 1;

		final List<Concept> result = new ArrayList<Concept>();
		for (final Concept concept : concepts) {

			try {
				this.conceptDictionaryDAO.getSessionFactory().getCurrentSession().saveOrUpdate(concept);

				this.log.info("Created Concept --> " + concept + " -> [" + count + "]");
				result.add(concept);

			} catch (final NonUniqueObjectException e) {

				existingsConcepts.add(concept);
				// TODO: Having some strange Exceptions
				this.log.error("Creating concept with name: " + concept + " -> " + e.getMessage());
			}
		}
		return result;
	}

	private Concept setConceptAttributes(final ConceptService conceptService, final Concept concept)
			throws ConceptImportBusinessException {

		final Collection<ConceptName> namesToBeCreated = this.getNamesToBeCreated(concept);

		return this.setAttributes(conceptService, namesToBeCreated);
	}

	private Concept checkDuplications(final ConceptService conceptService,
			final Collection<ConceptName> namesToBeCreated) throws EntityNotFoundException {
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
			throw new EntityNotFoundException(" Concept Not Found  name(s) " + sb.toString());
		}

		return found;
	}

	private Concept setAttributes(final ConceptService conceptService, final Collection<ConceptName> namesToBeCreated)
			throws EntityExistisException {

		final ConceptClass conceptClassMisc = this.conceptDictionaryDAO
				.findConceptClassByName(ImportConceptConstants.CONCEPT_CLASS_NAME_MISC);
		final ConceptDatatype conceptDatatypeNA = this.conceptDictionaryDAO
				.findConceptDataTypeByName(ImportConceptConstants.CONCEPT_DATA_TYPE_NA);

		final ConceptDescription conceptDescriptionPT = new ConceptDescription(
				"Resposta para Unidade de dosagem gerada automaticamente pelo super User",
				new Locale(ImportConceptConstants.LOCALE_PT));
		final ConceptDescription conceptDescriptionEN = new ConceptDescription(
				"Response for Dosing Unit auto generated by Super User", Locale.ENGLISH);

		conceptDescriptionPT.setUuid(UUID.randomUUID().toString());
		conceptDescriptionEN.setUuid(UUID.randomUUID().toString());

		Concept found = null;
		try {

			found = this.checkDuplications(conceptService, namesToBeCreated);

		} catch (final EntityNotFoundException e) {
		}

		if (found != null) {

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
				newConcept.setConceptClass(conceptClassMisc);
				newConcept.setDatatype(conceptDatatypeNA);
				newConcept.setNames(namesToBeCreated);
				newConcept.setDescriptions(Arrays.asList(conceptDescriptionPT, conceptDescriptionEN));

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
				new Locale(ImportConceptConstants.LOCALE_PT));
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

			// TODO : dosing Units cold have a String with lenght 2
			if ((conceptName != null) && (conceptName.getName() != null) && (conceptName.getName().length() > 1)
					&& conceptName.getLocale().equals(locale)) {
				return conceptName;
			}
		}
		return null;
	}

	private List<Concept> findExistingsConceptToLink(final ConceptService conceptService,
			final List<Concept> existings) {

		final List<Concept> result = new ArrayList<Concept>();

		for (final Concept concept : existings) {

			Concept found = null;
			for (final ConceptName conceptName : concept.getNames()) {

				try {
					found = this.findConceptByName(conceptService, conceptName.getName());
					result.add(found);
					break;

				} catch (final NoSuchElementException e) {
				}
			}

		}

		return result;
	}

	private Concept findConceptByName(final ConceptService conceptService, final String conceptName) {

		final Concept concept = conceptService.getConceptByName(conceptName);

		if (concept == null) {
			throw new NoSuchElementException();
		}

		return concept;
	}

	private Concept createNewQuestionConcept() {

		final ConceptService conceptService = Context.getConceptService();

		final ConceptClass conceptClassQuestion = conceptService
				.getConceptClassByName(ImportConceptConstants.CONCEPT_CLASS_NAME_QUESTION);
		final ConceptDatatype conceptDatatypeCoded = conceptService
				.getConceptDatatypeByName(ImportConceptConstants.CONCEPT_DATA_TYPE_CODED);

		final ConceptDescription conceptDescriptionPT = new ConceptDescription(
				"Unidade de dosagem gerada automaticamente pelo super User",
				new Locale(ImportConceptConstants.LOCALE_PT));
		final ConceptDescription conceptDescriptionEN = new ConceptDescription(
				"Dosing Unit auto generated by Super User", Locale.ENGLISH);
		conceptDescriptionPT.setUuid(UUID.randomUUID().toString());
		conceptDescriptionEN.setUuid(UUID.randomUUID().toString());

		Concept dosageUnit = new Concept();
		dosageUnit.setConceptClass(conceptClassQuestion);
		dosageUnit.setDatatype(conceptDatatypeCoded);
		dosageUnit.setDescriptions(Arrays.asList(conceptDescriptionPT, conceptDescriptionEN));

		final Collection<ConceptName> names = new HashSet<ConceptName>();

		names.add(new ConceptName(this.getQuestionConceptNamePT(), new Locale(ImportConceptConstants.LOCALE_PT)));
		names.add(new ConceptName(this.getQuestionConceptNameEN(), Locale.ENGLISH));

		dosageUnit.setNames(names);

		try {

			dosageUnit = this.conceptDictionaryDAO.findConceptByConceptNameAndClassName(dosageUnit.getName(),
					conceptClassQuestion.getName());

			if (dosageUnit.isRetired()) {

				dosageUnit.setRetired(false);
				conceptService.saveConcept(dosageUnit);
			}

		} catch (final NoSuchElementException e) {

			for (final ConceptName conceptName : names) {

				conceptName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
				conceptName.setUuid(UUID.randomUUID().toString());
			}
			conceptService.saveConcept(dosageUnit);

			this.log.info("Created Concept Question --> " + dosageUnit);
		}

		return conceptService.getConcept(dosageUnit.getName().getName());
	}

	private void linkQuestionToCodedConcepts(final Concept questionConcept, final List<Concept> lstCodedConcepts) {

		final ConceptService conceptService = Context.getConceptService();

		final Set<String> currentLoadedAnswers = new HashSet<String>();

		boolean hasNews = false;
		for (Concept answer : lstCodedConcepts) {

			answer = conceptService.getConcept(answer.getName().getName());

			if (!this.conceptDictionaryDAO.hasConceptAnswered(questionConcept, answer)
					&& !currentLoadedAnswers.contains(answer.getName().getName())) {

				ConceptAnswer conceptAnswer = new ConceptAnswer(answer);
				conceptAnswer.setCreator(questionConcept.getCreator());
				questionConcept.addAnswer(conceptAnswer);
				conceptAnswer.setUuid(UUID.randomUUID().toString());
				conceptAnswer.setConcept(questionConcept);

				this.conceptDictionaryDAO.getSessionFactory().getCurrentSession().saveOrUpdate(conceptAnswer);
				conceptAnswer = (ConceptAnswer) this.conceptDictionaryDAO.getSessionFactory().getCurrentSession()
						.get(ConceptAnswer.class, conceptAnswer.getId());

				hasNews = true;
				currentLoadedAnswers.add(answer.getName().getName());
			}
		}

		if (hasNews) {
			conceptService.saveConcept(questionConcept);
			this.log.info("Created Concept Answers in quantity of  --> " + questionConcept.getAnswers().size());
		}
	}

	@Override
	public String getQuestionConceptNamePT() {
		return "UNIDADE DE DOSAGEM";
	}

	@Override
	public String getQuestionConceptNameEN() {
		return "DOSING UNIT";
	}
}
