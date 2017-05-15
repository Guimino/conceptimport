/**
 *
 */
package org.openmrs.module.conceptimport.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptName;
import org.openmrs.api.ConceptService;
import org.openmrs.api.DuplicateConceptNameException;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.conceptimport.dao.HibernateConceptDictionaryDAO;
import org.openmrs.module.conceptimport.exception.EntityNotFoundException;
import org.openmrs.module.conceptimport.util.UUIDConstants;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Guimino Neves
 *
 */
@Transactional
public class ImportConceptServiceImpl extends BaseOpenmrsService implements ImportConceptService {

	protected final Log log = LogFactory.getLog(this.getClass());

	private HibernateConceptDictionaryDAO conceptDictionaryDAO;

	private ConceptService conceptService;
	private final Locale LOCALE_PT = new Locale(ImportConceptConstants.LOCALE_PT);

	private ConceptClass conceptClassQuestion;
	private ConceptDatatype conceptDatatypeCoded;
	private ConceptClass conceptClassMisc;
	private ConceptDatatype conceptDatatypeNA;

	List<Locale> LOCALES = Arrays.asList(this.LOCALE_PT, Locale.ENGLISH);

	private final List<String> UNIDADE_DOSAGEM = Arrays.asList("UNIDADE DE DOSAGEM", "DOSING UNIT");

	private boolean isDosageUnit = false;

	@Override
	public void setConceptDictionaryDAO(final HibernateConceptDictionaryDAO conceptDictionaryDAO) {
		this.conceptDictionaryDAO = conceptDictionaryDAO;
	}

	@Override
	public void load(final List<List<Concept>> ltsConcepts) {

		this.log.info("-- Begin Importing Concepts... --");

		// TODO: I was getting Enigmatic ClassCastException so I had to do this
		// manualFlushMode....
		this.conceptDictionaryDAO.getSessionFactory().getCurrentSession().setFlushMode(FlushMode.MANUAL);

		this.init();

		try {
			// TODO: I had to do that (controlling the transaction inside a
			// Transactional Service) so the Controller is rolling back the
			// transaction without no showing specif error Cause
			this.conceptDictionaryDAO.getSessionFactory().getCurrentSession().getTransaction().begin();

			for (final List<Concept> list : ltsConcepts) {

				final Concept questionConcept = this.createQuestionConcept(list.get(0));
				this.log.info("Created Concept of Type Question: " + questionConcept.getName());

				final List<Concept> answersConcepts = this.createAnswerConcepts(list.subList(1, list.size()));
				this.linkQuestionToCodedConcepts(questionConcept, answersConcepts);
			}

		} finally {

			this.conceptDictionaryDAO.getSessionFactory().getCurrentSession().getTransaction().commit();
		}
		this.log.info("-- End Importing Concepts! --");
	}

	/**
	 * Creates the Concept which will represent the Concept Question
	 *
	 * @return
	 */
	private Concept createQuestionConcept(final Concept concept) {

		try {
			return this.findConceptByName(concept);

		} catch (final EntityNotFoundException e) {

			concept.setConceptClass(this.conceptClassQuestion);
			concept.setDatatype(this.conceptDatatypeCoded);
			concept.setUuid(UUID.randomUUID().toString());

			final Concept saveConcept = this.conceptService.saveConcept(concept);

			this.saveConceptDescriptions(saveConcept);
			return saveConcept;
		}
	}

	private void saveConceptDescriptions(final Concept concept) {

		final List<ConceptDescription> conceptDescriptions = this.getConceptDescriptions(concept);
		for (final ConceptDescription conceptDescription : conceptDescriptions) {

			conceptDescription.setConcept(concept);
			this.conceptDictionaryDAO.getSessionFactory().getCurrentSession().save(conceptDescription);

			this.log.info("Concept Description Saved  for concept " + concept);
		}
	}

	private Concept findConceptByName(final Concept concept) throws EntityNotFoundException {

		Concept found;

		for (final Locale locale : this.LOCALES) {

			final ConceptName conceptName = concept.getName(locale);

			if (conceptName != null) {

				if (this.UNIDADE_DOSAGEM.contains(conceptName.getName())) {
					this.isDosageUnit = true;
				}
				if (conceptName.isFullySpecifiedName()) {

					found = this.conceptDictionaryDAO.findConceptByName(conceptName.getName());
					if (found != null) {
						return found;
					}
				}
			}
		}

		throw new EntityNotFoundException("Concept not found : " + concept.getName().getName());
	}

	private List<ConceptDescription> getConceptDescriptions(final Concept concept) {
		final Set<ConceptDescription> descriptions = new HashSet<ConceptDescription>();

		for (final ConceptName conceptName : concept.getNames()) {

			conceptName.setUuid(UUID.randomUUID().toString());

			final ConceptDescription cDescription = new ConceptDescription();
			cDescription.setConcept(concept);
			cDescription.setUuid(UUID.randomUUID().toString());

			if (conceptName.getLocale().equals(Locale.ENGLISH)) {

				cDescription.setDescription(ImportConceptConstants.MESSAGE_DESCRIPTION_EN);
				cDescription.setLocale(Locale.ENGLISH);

			} else if (conceptName.getLocale().equals(this.LOCALE_PT)) {

				cDescription.setDescription(ImportConceptConstants.MESSAGE_DESCRIPTION_PT);
				cDescription.setLocale(this.LOCALE_PT);
			}
			descriptions.add(cDescription);
		}

		return new ArrayList<ConceptDescription>(descriptions);
	}

	/**
	 * Creates Concepts which will be used as the ANSWERS for que Concept
	 * Question
	 *
	 * @param codedConcepts
	 * @return
	 */
	private List<Concept> createAnswerConcepts(final List<Concept> codedConcepts) {

		this.log.info("Is about to be created " + codedConcepts.size() + " Concept answers");

		final List<Concept> toCreate = new ArrayList<Concept>();
		final List<Concept> existings = new ArrayList<Concept>();

		for (Concept codedConcept : codedConcepts) {

			try {
				codedConcept = this.findConceptByName(codedConcept);

				if (codedConcept.isRetired()) {
					codedConcept.setRetired(false);
					codedConcept = this.conceptService.saveConcept(codedConcept);
				}

				if (!existings.contains(codedConcept)) {
					existings.add(codedConcept);
				}

			} catch (final EntityNotFoundException e) {

				if (!toCreate.contains(codedConcept)) {
					toCreate.add(codedConcept);
				}
			}
		}

		for (Concept codedConcept : toCreate) {

			for (final ConceptName conceptName : codedConcept.getNames()) {

				if (this.isDosageUnit) {
					conceptName.setName(conceptName.getName().toLowerCase());
				}
			}

			codedConcept.setConceptClass(this.conceptClassMisc);

			codedConcept.setDatatype(this.conceptDatatypeNA);

			codedConcept.setUuid(UUID.randomUUID().toString());

			try {

				codedConcept = this.conceptService.saveConcept(codedConcept);
				this.saveConceptDescriptions(codedConcept);

				this.log.info("Created Concept Response --> " + codedConcept.getName());

				if (!existings.contains(codedConcept)) {
					existings.add(codedConcept);
				}

			} catch (final DuplicateConceptNameException e) {

				this.log.info("Concept Duplicated " + codedConcept.getName().getName());
			}
		}

		return existings;
	}

	/**
	 * Creates the relation QUESTION/ANSWER known as <b>ConceptAnswer</b>
	 *
	 * @param questionConcept
	 * @param lstCodedConcepts
	 */
	private void linkQuestionToCodedConcepts(final Concept questionConcept, final List<Concept> lstCodedConcepts) {

		final ConceptService conceptService = Context.getConceptService();

		final Set<String> currentLoadedAnswers = new HashSet<String>();

		boolean hasNews = false;
		for (final Concept answer : lstCodedConcepts) {

			final String answerName = ((answer != null) && (answer.getName().getName() != null)
					&& (answer.getId() != null)) ? answer.getName().getName() : "{WITHOUT_ID}";

					try {

						final ConceptAnswer conceptAnswer = new ConceptAnswer(answer);
						conceptAnswer.setCreator(questionConcept.getCreator());
						conceptAnswer.setUuid(UUID.randomUUID().toString());
						conceptAnswer.setConcept(questionConcept);
						questionConcept.addAnswer(conceptAnswer);

						this.conceptDictionaryDAO.getSessionFactory().getCurrentSession().save(conceptAnswer);

						hasNews = true;

						this.log.info("ConceptResponse:[" + answerName + "]  ANSWERING -->> ConceptQuestion:["
								+ questionConcept.getName().getName() + "]");

					} catch (final Exception e) {

						this.log.info("ConceptResponse [" + answerName + "]" + " already associated to ConceptQuestion ["
								+ questionConcept.getName().getName() + "]  ERROR Below: ");
						e.printStackTrace();
					}
					currentLoadedAnswers.add(answer.getName().getName());
		}

		if (hasNews) {
			conceptService.saveConcept(questionConcept);
			this.log.info("Created total of " + questionConcept.getAnswers().size() + " Concept Answers");
		}
	}

	private void init() {

		this.conceptService = Context.getConceptService();

		this.conceptClassQuestion = this.conceptService
				.getConceptClassByUuid(UUIDConstants.CONCEPT_CLASS_NAME_QUESTION_UUID);
		this.conceptDatatypeCoded = this.conceptService
				.getConceptDatatypeByUuid(UUIDConstants.CONCEPT_DATA_TYPE_CODED_UUID);

		this.conceptClassMisc = this.conceptService.getConceptClassByUuid(UUIDConstants.CONCEPT_CLASS_NAME_MISC_UUID);
		this.conceptDatatypeNA = this.conceptService.getConceptDatatypeByUuid(UUIDConstants.CONCEPT_DATA_TYPE_NA_UUID);

	}
}
