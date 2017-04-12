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
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.conceptimport.dao.DbSessionManager;
import org.openmrs.module.conceptimport.dao.HibernateConceptDictionaryDAO;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Guimino Neves
 *
 */
@Transactional
public class ImportConceptDictionaryServiceImpl extends BaseOpenmrsService implements ImportConceptDictionaryService {

	protected final Log log = LogFactory.getLog(this.getClass());

	private HibernateConceptDictionaryDAO conceptDictionaryDAO;
	private ConceptService conceptService;

	private DbSessionManager dbSessionManager;

	private static final String CONCEPT_CLASS_NAME_MISC = "Misc";
	private static final String CONCEPT_CLASS_NAME_QUESTION = "Question";

	private static final String CONCEPT_DATA_TYPE_NA = "N/A";
	private static final String CONCEPT_DATA_TYPE_CODED = "Coded";

	private static final String PHARMACEUTICAL_FORM_NAME_PT = "FORMA FARMACEUTICA";
	private static final String PHARMACEUTICAL_FORM_NAME_EN = "PHARMACEUTICAL FORM";

	@Override
	public void setConceptService(final ConceptService conceptService) {

		this.conceptService = conceptService;
	}

	@Override
	public void setConceptDictionaryDAO(final HibernateConceptDictionaryDAO conceptDictionaryDAO) {
		this.conceptDictionaryDAO = conceptDictionaryDAO;
	}

	@Override
	public void setDbSessionManager(final DbSessionManager dbSessionManager) {
		this.dbSessionManager = dbSessionManager;
	}

	@Override
	public void createPharmaceuticalFormConcepts(final List<Concept> codedConcepts) {

		this.log.info("-- Begin Importing Pharmacy Concepts --");

		this.dbSessionManager.setManualFlushMode();

		final Collection<Concept> toCreate = new ArrayList<Concept>();
		final Collection<Concept> existings = new ArrayList<Concept>();

		for (final Concept codedConcept : codedConcepts) {

			try {

				final Concept existing = this.conceptDictionaryDAO.findConceptByConceptNameAndClassName(
						codedConcept.getName(), ImportConceptDictionaryServiceImpl.CONCEPT_CLASS_NAME_MISC);
				existings.add(existing);

			} catch (final NoSuchElementException e) {

				toCreate.add(codedConcept);
			}
		}

		final ConceptClass conceptClassMisc = this.conceptService
				.getConceptClassByName(ImportConceptDictionaryServiceImpl.CONCEPT_CLASS_NAME_MISC);
		final ConceptDatatype conceptDatatypeNA = this.conceptService
				.getConceptDatatypeByName(ImportConceptDictionaryServiceImpl.CONCEPT_DATA_TYPE_NA);

		for (final Concept codedConcept : toCreate) {

			final Iterator<ConceptName> iterator = codedConcept.getNames().iterator();

			final ConceptName firstName = iterator.next();
			final ConceptName secondName = iterator.next();

			firstName.setUuid(UUID.randomUUID().toString());
			firstName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
			secondName.setUuid(UUID.randomUUID().toString());
			secondName.setConceptNameType(ConceptNameType.SHORT);

			codedConcept.setConceptClass(conceptClassMisc);
			codedConcept.setDatatype(conceptDatatypeNA);
			codedConcept.setUuid(UUID.randomUUID().toString());

			this.conceptService.saveConcept(codedConcept);

			this.log.info("Created Coded Concept Response --> " + codedConcept.getName());
		}

		final ConceptClass conceptClassQuestion = this.conceptService
				.getConceptClassByName(ImportConceptDictionaryServiceImpl.CONCEPT_CLASS_NAME_QUESTION);
		final ConceptDatatype conceptDatatypeCoded = this.conceptService
				.getConceptDatatypeByName(ImportConceptDictionaryServiceImpl.CONCEPT_DATA_TYPE_CODED);

		Concept pharmaceuticalForm = new Concept();
		pharmaceuticalForm.setConceptClass(conceptClassQuestion);
		pharmaceuticalForm.setDatatype(conceptDatatypeCoded);

		final Collection<ConceptName> names = new HashSet<ConceptName>();

		names.add(new ConceptName(ImportConceptDictionaryServiceImpl.PHARMACEUTICAL_FORM_NAME_PT,
				new Locale("pt", "PT")));
		names.add(new ConceptName(ImportConceptDictionaryServiceImpl.PHARMACEUTICAL_FORM_NAME_EN, Locale.ENGLISH));

		pharmaceuticalForm.setNames(names);

		try {

			pharmaceuticalForm = this.conceptDictionaryDAO
					.findConceptByConceptNameAndClassName(pharmaceuticalForm.getName(), conceptClassQuestion.getName());

			if ((pharmaceuticalForm == null) || (pharmaceuticalForm.getConceptClass().getConceptClassId()
					.compareTo(conceptClassQuestion.getConceptClassId()) != 0)) {
				throw new NoSuchElementException();
			}

		} catch (final NoSuchElementException e) {

			for (final ConceptName conceptName : names) {

				conceptName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
				conceptName.setUuid(UUID.randomUUID().toString());
			}
			this.conceptService.saveConcept(pharmaceuticalForm);

			this.log.info("Created Concept Question --> " + pharmaceuticalForm);
		}

		existings.addAll(toCreate);

		for (final Concept answer : existings) {

			if (!this.conceptDictionaryDAO.hasConceptAnswered(pharmaceuticalForm, answer)) {

				final ConceptAnswer conceptAnswer = new ConceptAnswer();
				conceptAnswer.setConcept(pharmaceuticalForm);
				conceptAnswer.setAnswerConcept(answer);
				conceptAnswer.setUuid(UUID.randomUUID().toString());

				this.conceptDictionaryDAO.getSessionFactory().getCurrentSession().saveOrUpdate(conceptAnswer);

				this.log.info("Created Concept Response Answer --> " + conceptAnswer);
			}
		}
		this.log.info("-- End Importing Pharmacy Concepts --");

		this.dbSessionManager.setAutoFlushMode();
	}
}
