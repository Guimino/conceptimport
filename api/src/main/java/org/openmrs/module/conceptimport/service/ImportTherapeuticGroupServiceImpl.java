/**
 *
 */
package org.openmrs.module.conceptimport.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.conceptimport.dao.HibernateConceptDictionaryDAO;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Guimino Neves
 *
 */
@Transactional
public class ImportTherapeuticGroupServiceImpl extends BaseOpenmrsService implements ImportTherapeuticGroupService {

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

		this.log.info("-- Begin Importing  Therapeutic Groups... --");

		this.dbSessionManager.setManualFlushMode();

		final List<Concept> lstCodedConceptCreated = this.createNewCodedConcepts(codedConcepts);

		final Concept questionConcept = this.createNewQuestionConcept();

		this.linkQuestionToCodedConcepts(questionConcept, lstCodedConceptCreated);

		this.log.info("-- End Importing  Therapeutic Groups! --");
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

	private Concept createNewQuestionConcept() {

		final ConceptService conceptService = Context.getConceptService();

		final ConceptClass conceptClassQuestion = conceptService
				.getConceptClassByName(ImportConceptConstants.CONCEPT_CLASS_NAME_QUESTION);
		final ConceptDatatype conceptDatatypeCoded = conceptService
				.getConceptDatatypeByName(ImportConceptConstants.CONCEPT_DATA_TYPE_CODED);

		Concept pharmaceuticalForm = new Concept();
		pharmaceuticalForm.setConceptClass(conceptClassQuestion);
		pharmaceuticalForm.setDatatype(conceptDatatypeCoded);

		final Collection<ConceptName> names = new HashSet<ConceptName>();

		names.add(new ConceptName(this.getQuestionConceptNamePT(), new Locale("pt")));
		names.add(new ConceptName(this.getQuestionConceptNameEN(), Locale.ENGLISH));

		pharmaceuticalForm.setNames(names);

		try {

			pharmaceuticalForm = this.conceptDictionaryDAO
					.findConceptByConceptNameAndClassName(pharmaceuticalForm.getName(), conceptClassQuestion.getName());

			if (pharmaceuticalForm.isRetired()) {

				pharmaceuticalForm.setRetired(false);
				conceptService.saveConcept(pharmaceuticalForm);
			}

		} catch (final NoSuchElementException e) {

			for (final ConceptName conceptName : names) {

				conceptName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
				conceptName.setUuid(UUID.randomUUID().toString());
			}
			conceptService.saveConcept(pharmaceuticalForm);

			this.log.info("Created Concept Question --> " + pharmaceuticalForm);
		}

		return conceptService.getConcept(pharmaceuticalForm.getName().getName());
	}

	private List<Concept> createNewCodedConcepts(final List<Concept> codedConcepts) {

		final ConceptService conceptService = Context.getConceptService();

		final List<Concept> toCreate = new ArrayList<Concept>();
		final List<Concept> existings = new ArrayList<Concept>();

		for (Concept codedConcept : codedConcepts) {

			for (final ConceptName conceptName : codedConcept.getNames()) {

				try {
					codedConcept = this.findConceptByName(conceptName.getName());

					if (codedConcept.isRetired()) {
						codedConcept.setRetired(false);
						conceptService.saveConcept(codedConcept);
					}
					existings.add(codedConcept);
					break;

				} catch (final NoSuchElementException e) {

					toCreate.add(codedConcept);
				}
			}
		}

		final ConceptClass conceptClassMisc = conceptService
				.getConceptClassByName(ImportConceptConstants.CONCEPT_CLASS_NAME_MISC);
		final ConceptDatatype conceptDatatypeNA = conceptService
				.getConceptDatatypeByName(ImportConceptConstants.CONCEPT_DATA_TYPE_NA);

		for (final Concept codedConcept : toCreate) {

			final Iterator<ConceptName> iterator = codedConcept.getNames().iterator();

			final ConceptName firstName = iterator.next();

			firstName.setUuid(UUID.randomUUID().toString());
			firstName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
			firstName.setLocalePreferred(true);

			codedConcept.setConceptClass(conceptClassMisc);
			codedConcept.setDatatype(conceptDatatypeNA);
			codedConcept.setUuid(UUID.randomUUID().toString());

			conceptService.saveConcept(codedConcept);
			this.log.info("Created Coded Concept Response --> " + codedConcept.getName());
		}
		existings.addAll(codedConcepts);

		return existings;
	}

	private Concept findConceptByName(final String conceptName) {

		final ConceptService conceptService = Context.getConceptService();

		final Concept concept = conceptService.getConceptByName(conceptName);

		if (concept == null) {
			throw new NoSuchElementException();
		}

		return concept;
	}

	@Override
	public String getQuestionConceptNamePT() {
		return "GRUPO TERAPEUTICO";
	}

	@Override
	public String getQuestionConceptNameEN() {
		return "THERAPEUTIC GROUP";
	}
}
