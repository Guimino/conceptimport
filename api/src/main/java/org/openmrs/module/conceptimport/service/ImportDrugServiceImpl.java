/**
 *
 */
package org.openmrs.module.conceptimport.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.Drug;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.conceptimport.dao.HibernateConceptDictionaryDAO;
import org.openmrs.module.conceptimport.exception.ConceptImportBusinessException;
import org.openmrs.module.conceptimport.exception.EntityNotFoundException;
import org.openmrs.module.conceptimport.util.Row;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Guimino Neves
 *
 */
@Transactional
public class ImportDrugServiceImpl extends BaseOpenmrsService implements ImportDrugService {

	private HibernateConceptDictionaryDAO conceptDictionaryDAO;

	private DbSessionManager dbSessionManager;

	private final Map<Integer, Concept> cachedMapGeneralConceptDrugs = new HashMap<Integer, Concept>();

	private final Map<Integer, Concept> mapConceptDosageAll = new HashMap<Integer, Concept>();

	private final Map<Integer, Concept> mapConceptPharmaceuticFormAll = new HashMap<Integer, Concept>();

	private ConceptClass drugConceptClass;

	private final List<String> DEFAULT_DOSAGE = Arrays.asList("1", "SEM DOSAGEM", "");

	private Concept CONCEPT_NA;

	private ConceptService conceptService;

	private final List<String> ANTI_RETROVIRAL_CLASS = Arrays.asList("ANTI-RETROVIRAIS", "Anti-retrovirais");

	Log log = LogFactory.getLog(this.getClass());

	@Override
	public void setConceptDictionaryDAO(final HibernateConceptDictionaryDAO hibernateConceptDictionary) {
		this.conceptDictionaryDAO = hibernateConceptDictionary;
	}

	@Override
	public void setDbSessionManager(final DbSessionManager dbSessionManager) {

		this.dbSessionManager = dbSessionManager;
	}

	private void init() {

		this.conceptService = Context.getService(ConceptService.class);

		this.drugConceptClass = this.conceptService
				.getConceptClassByName(ImportConceptConstants.CONCEPT_CLASS_NAME_DRUG);

		this.cachedMapGeneralConceptDrugs
		.putAll(this.conceptDictionaryDAO.findConceptsByClass(ImportConceptConstants.CONCEPT_CLASS_NAME_DRUG));
		this.mapConceptDosageAll.putAll(this.getAllDosages());
		this.mapConceptPharmaceuticFormAll.putAll(this.getAllPharmaceuticForm());
		this.CONCEPT_NA = this.conceptService.getConceptByName(ImportConceptConstants.CONCEPT_DATA_TYPE_NA);
	}

	@Override
	public void load(final List<Row> rows) throws ConceptImportBusinessException {
		this.init();

		final List<Drug> drugsToCreate = new ArrayList<Drug>();

		final Concept arvSegment = this.conceptService
				.getConceptByName("ANTI-RETROVIRAIS ANTERIORES USADOS PARA TRATAMENTO");
		final Concept nonARVSegment = this.conceptService.getConceptByName("TRATAMENTO PRESCRITO");

		for (final Row row : rows) {

			if (!this.doesFNMCodeExists(row)) {

				final Concept drugGeneralConcept = this.getDrugGeneralConcept(row);
				final Concept dosage = this.getConceptDosage(row);
				final Concept pharmaceuticFormConcept = this.getConceptPharmaceuticForm(row);

				if ((drugGeneralConcept != null) && (dosage != null) && (pharmaceuticFormConcept != null)) {
					final Drug drug = new Drug();
					drug.setDosageForm(dosage);
					drug.setRoute(pharmaceuticFormConcept);
					drug.setConcept(drugGeneralConcept);
					drug.setUuid(UUID.randomUUID().toString());
					drug.setStrength(row.getFnm());
					final String completeDesignation = Normalizer
							.normalize(row.getCompleteDesignation(), Normalizer.Form.NFD)
							.replaceAll("[^\\p{ASCII}]", "").toUpperCase().trim();
					drug.setName(completeDesignation);
					drugsToCreate.add(drug);

					if (this.ANTI_RETROVIRAL_CLASS.contains(row.getTherapeuticGroup())) {

						this.linkQuestionToCodedConcepts(arvSegment, Arrays.asList(drugGeneralConcept));
					} else {
						this.linkQuestionToCodedConcepts(nonARVSegment, Arrays.asList(drugGeneralConcept));
					}
				}
			}
		}
		this.createDrugs(drugsToCreate);
	}

	private void createDrugs(final List<Drug> drugs) {

		for (final Drug drug : drugs) {

			this.conceptDictionaryDAO.getSessionFactory().getCurrentSession().save(drug);

			this.log.info("Created Drug: " + drug);
		}
	}

	private Concept getDrugGeneralConcept(final Row row) {

		Concept concept = null;

		concept = this.cachedMapGeneralConceptDrugs.get(row.getGenericDrugConceptId());

		if (concept == null) {

			concept = this.processByEqual(row.getGenericDrugConceptId());
		}

		if (concept == null) {

			System.out.println("Conceito para Droga Generica nao encontrada: " + row);
		}

		return concept;
	}

	private Map<Integer, Concept> getAllDosages() {

		final Map<Integer, Concept> results = new HashMap<Integer, Concept>();

		final Concept dosageUnitConcept = this.conceptService.getConceptByName("UNIDADE DE DOSAGEM");

		for (final ConceptAnswer conceptAnswer : dosageUnitConcept.getAnswers()) {

			final Concept answerConcept = conceptAnswer.getAnswerConcept();
			results.put(answerConcept.getConceptId(), answerConcept);
		}

		return results;
	}

	private Map<Integer, Concept> getAllPharmaceuticForm() {

		final Map<Integer, Concept> results = new HashMap<Integer, Concept>();

		final Concept pharmaceuticForm = this.conceptService.getConceptByName("FORMA FARMACEUTICA");

		for (final ConceptAnswer conceptAnswer : pharmaceuticForm.getAnswers()) {

			final Concept answerConcept = conceptAnswer.getAnswerConcept();
			results.put(answerConcept.getConceptId(), answerConcept);
		}

		return results;
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

	private Concept getConceptDosage(final Row row) {

		final String dosage = Normalizer.normalize(row.getDosage(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")
				.toUpperCase().trim();

		if (this.DEFAULT_DOSAGE.contains(dosage)) {
			return this.CONCEPT_NA;
		}

		final Concept concept = this.mapConceptDosageAll.get(row.getDosageConceptId());

		if (concept == null) {
			System.out.println("Dosagem nao encontrada: " + row);
		}

		return concept;
	}

	private Concept getConceptPharmaceuticForm(final Row row) {

		final Concept concept = this.mapConceptPharmaceuticFormAll.get(row.getPharmaceuticFormConceptId());
		if (concept == null) {
			System.out.println("Forma farmaceutica nao encontrada: " + row);
		}
		return concept;
	}

	private Concept processByEqual(final Integer generalDrugConceptId) {

		final Concept concept = this.conceptService.getConcept(generalDrugConceptId);

		if ((concept != null) && concept.getConceptClass().equals(this.drugConceptClass)) {

			this.cachedMapGeneralConceptDrugs.put(generalDrugConceptId, concept);
		}
		return concept;
	}

	private boolean doesFNMCodeExists(final Row row) throws ConceptImportBusinessException {

		try {
			this.conceptDictionaryDAO.findDrugByStrength(row.getFnm());
			return true;

		} catch (final EntityNotFoundException e) {
			return false;
		}
	}
}
