/**
 *
 */
package org.openmrs.module.conceptimport.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.classic.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptName;
import org.openmrs.Drug;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.DuplicateConceptNameException;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.conceptimport.dao.HibernateConceptDictionaryDAO;
import org.openmrs.module.conceptimport.exception.ConceptImportBusinessException;
import org.openmrs.module.conceptimport.exception.EntityNotFoundException;
import org.openmrs.module.conceptimport.util.Row;
import org.openmrs.module.conceptimport.util.UUIDConstants;
import org.openmrs.module.pharmacyapi.api.model.DrugItem;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Guimino Neves
 *
 */
@Transactional
public class ImportDrugServiceImpl extends BaseOpenmrsService implements ImportDrugService {

	private HibernateConceptDictionaryDAO conceptDictionaryDAO;

	private final Map<String, Concept> cachedMapGeneralConceptDrugs = new HashMap<String, Concept>();

	private final Map<String, Concept> mapConceptDosageAll = new HashMap<String, Concept>();

	private final Map<String, Concept> mapConceptPharmaceuticFormAll = new HashMap<String, Concept>();

	private final Map<String, Concept> mapConcepTherapeuticGroupAll = new HashMap<String, Concept>();

	private final Map<String, Concept> mapConcepTherapeuticClassAll = new HashMap<String, Concept>();

	private ConceptClass drugConceptClass;

	private ConceptDatatype conceptDataTypeNA;

	private final List<String> DEFAULT_DOSAGE = Arrays.asList("1", "SEM DOSAGEM", "");

	private final Locale LOCALE_PT = new Locale(ImportConceptConstants.LOCALE_PT);

	private Concept CONCEPT_NA;

	private ConceptService conceptService;

	private final List<String> ANTI_RETROVIRAL_CLASS = Arrays.asList("ANTI-RETROVIRAIS", "Anti-retrovirais");

	Log log = LogFactory.getLog(this.getClass());

	@Override
	public void setConceptDictionaryDAO(final HibernateConceptDictionaryDAO hibernateConceptDictionary) {
		this.conceptDictionaryDAO = hibernateConceptDictionary;
	}

	@Override
	public void load(final List<Row> rows) throws ConceptImportBusinessException {

		this.log.info("-- Begin Importing Drugs... --");

		// TODO: I was getting Enigmatic ClassCastException so I had to do this
		// manualFlushMode....
		this.conceptDictionaryDAO.getSessionFactory().getCurrentSession().setFlushMode(FlushMode.MANUAL);
		this.init();

		try {

			// TODO: I had to do that (controlling the transaction inside a
			// Transactional Service) so the Controller is rolling back the
			// transaction without no showing specif error Cause
			this.conceptDictionaryDAO.getSessionFactory().getCurrentSession().getTransaction().begin();

			final Concept conceptSegmentARV = this.conceptService
					.getConceptByUuid(UUIDConstants.PREVIOUS_ANTIRETROVIRAL_DRUGS_USED_FOR_TREATMENT_UUID);

			final Concept conceptSegmentNonARV = this.conceptService
					.getConceptByUuid(UUIDConstants.TREATMENT_PRESCRIBED__UUID);

			for (final Row row : rows) {

				if (!this.isRowLoaded(row)) {

					final Concept drugGeneralConcept = this.getDrugGeneralConcept(row);
					final Concept dosage = this.getConceptDosage(row);
					final Concept pharmaceuticFormConcept = this.getConceptPharmaceuticForm(row);
					final Concept terapeuticGroup = this.getConceptGroupTerapeutic(row);
					final Concept terapeuticClass = this.getConceptClassTerapeutic(row);

					if ((drugGeneralConcept != null) && (dosage != null) && (pharmaceuticFormConcept != null)
							&& (terapeuticClass != null) && (terapeuticGroup != null)) {

						Drug drug = new Drug();
						drug.setDosageForm(dosage);
						drug.setConcept(drugGeneralConcept);
						drug.setUuid(UUID.randomUUID().toString());
						drug.setName(this.getFormattedDrugName(row, pharmaceuticFormConcept));

						final DrugItem drugItem = new DrugItem();
						drugItem.setDrug(drug);
						drugItem.setFnmCode(row.getFnm());
						drugItem.setPharmaceuticalForm(pharmaceuticFormConcept);
						drugItem.setTherapeuticGroup(terapeuticGroup);
						drugItem.setPharmaceuticalClass(terapeuticClass);
						drugItem.setUuid(UUID.randomUUID().toString());
						drugItem.setId(null);

						try {
							final Session session = this.conceptDictionaryDAO.getSessionFactory().getCurrentSession();
							drug = this.conceptService.saveDrug(drug);
							session.save(drugItem);
							session.flush();

							this.linkQuestionToCodedConcepts(this.ANTI_RETROVIRAL_CLASS.contains(row.getClassGroup())
									? conceptSegmentARV : conceptSegmentNonARV, Arrays.asList(drugGeneralConcept));

							this.log.info("Created Drug --> " + drug);
							this.log.info("Created DrugItem --> " + drugItem);

						} catch (final ConstraintViolationException e) {

							this.log.info("Drug Already existis for row --> " + row);

						} catch (final APIException e) {

							this.log.info("Error creating Drug for ROW --> " + row);
							e.printStackTrace();
						}

					} else {

						this.log.info("Row not loaded --> " + row);
					}
				}
			}
		} finally {

			this.conceptDictionaryDAO.getSessionFactory().getCurrentSession().getTransaction().commit();
		}

		this.log.info("-- End Importing Drugs... --");

	}

	private void init() {

		this.conceptService = Context.getService(ConceptService.class);

		this.drugConceptClass = this.conceptService
				.getConceptClassByName(ImportConceptConstants.CONCEPT_CLASS_NAME_DRUG);

		this.conceptDataTypeNA = this.conceptService
				.getConceptDatatypeByName(ImportConceptConstants.CONCEPT_DATA_TYPE_NA);

		this.cachedMapGeneralConceptDrugs
		.putAll(this.conceptDictionaryDAO.findConceptsByClass(ImportConceptConstants.CONCEPT_CLASS_NAME_DRUG));
		this.CONCEPT_NA = this.conceptService.getConceptByName(ImportConceptConstants.CONCEPT_DATA_TYPE_NA);
		this.mapConceptDosageAll.putAll(this.getAllConceptByQuestion("UNIDADE DE DOSAGEM"));
		this.mapConceptPharmaceuticFormAll.putAll(this.getAllConceptByQuestion("FORMA FARMACEUTICA"));
		this.mapConcepTherapeuticGroupAll.putAll(this.getAllConceptByQuestion("GRUPO TERAPEUTICO"));
		this.mapConcepTherapeuticClassAll.putAll(this.getAllConceptByQuestion("CLASSE TERAPEUTICA"));

	}

	private String getFormattedDrugName(final Row row, final Concept pharmaceuticForm) {

		final String drugName = row.getDesignationPT().substring(0, 1)
				+ row.getDesignationPT().substring(1).toLowerCase() + " " + row.getDosage() + " ("
				+ pharmaceuticForm.getName(new Locale(ImportConceptConstants.LOCALE_PT)).getName().toLowerCase() + ")";

		return drugName;
	}

	private Concept getDrugGeneralConcept(final Row row) {

		Concept concept = null;

		concept = this.cachedMapGeneralConceptDrugs.get(row.getDesignationPT());

		if (concept == null) {

			concept = this.processByEqual(row.getDesignationPT());
		}

		if (concept == null) {

			concept = this.createNewQuestionConcept(row);
		}

		if (concept == null) {
			this.log.info("Concept for Drug not found and not Generated -> " + row);
			return null;
		}

		this.cachedMapGeneralConceptDrugs.put(row.getDesignationPT(), concept);

		return concept;
	}

	private Map<String, Concept> getAllConceptByQuestion(final String conceptQuestion) {

		final Map<String, Concept> results = new HashMap<String, Concept>();

		final Concept pharmaceuticForm = this.conceptService.getConceptByName(conceptQuestion);

		for (final ConceptAnswer conceptAnswer : pharmaceuticForm.getAnswers()) {

			final Concept answerConcept = conceptAnswer.getAnswerConcept();
			results.put(answerConcept.getName(new Locale(ImportConceptConstants.LOCALE_PT)).getName(), answerConcept);
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
			this.log.info("Created " + questionConcept.getAnswers().size() + " Concept Answers");
		}
	}

	private Concept getConceptDosage(final Row row) {

		final String dosage = Normalizer.normalize(row.getDosage(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")
				.toUpperCase().trim();

		if (this.DEFAULT_DOSAGE.contains(dosage)) {
			return this.CONCEPT_NA;
		}

		final Concept concept = this.mapConceptDosageAll.get(row.getDosage());

		if (concept == null) {
			this.log.info("Dosage Not Found --> " + row);
		}

		return concept;
	}

	private Concept getConceptPharmaceuticForm(final Row row) {

		final Concept concept = this.mapConceptPharmaceuticFormAll.get(row.getPharmaceuticForm());
		if (concept == null) {
			this.log.info("Pharmaceutic Form Not Found --> " + row);
		}
		return concept;
	}

	private Concept getConceptGroupTerapeutic(final Row row) {

		final Concept concept = this.mapConcepTherapeuticGroupAll.get(row.getTherapeuticGroup());
		if (concept == null) {
			this.log.info("Therapeutic Group Not Found --> " + row);
		}
		return concept;
	}

	private Concept getConceptClassTerapeutic(final Row row) {

		final Concept concept = this.mapConcepTherapeuticClassAll.get(row.getClassGroup());
		if (concept == null) {
			this.log.info("Therapeutic Class Not Found --> " + row);
		}
		return concept;
	}

	private Concept processByEqual(final String generalDrugConcept) {

		final Concept concept = this.conceptService.getConcept(generalDrugConcept);

		if ((concept != null) && concept.getConceptClass().equals(this.drugConceptClass)) {

			this.cachedMapGeneralConceptDrugs.put(generalDrugConcept, concept);
		}
		return concept;
	}

	private void saveConceptDescriptions(final Concept concept) {

		final List<ConceptDescription> conceptDescriptions = this.getConceptDescriptions(concept);
		for (final ConceptDescription conceptDescription : conceptDescriptions) {

			conceptDescription.setConcept(concept);
			this.conceptDictionaryDAO.getSessionFactory().getCurrentSession().save(conceptDescription);
			this.log.info("Concept Description Saved  for concept " + concept);
		}
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

	private boolean isRowLoaded(final Row row) throws ConceptImportBusinessException {

		try {
			this.conceptDictionaryDAO.findDrugByFnmCode(row.getFnm());
			return true;

		} catch (final EntityNotFoundException e) {
			return false;
		}
	}

	private Concept createNewQuestionConcept(final Row row) {

		Concept drugConcept = new Concept();
		drugConcept.setConceptClass(this.drugConceptClass);
		drugConcept.setDatatype(this.conceptDataTypeNA);

		final Collection<ConceptName> names = new HashSet<ConceptName>();

		final ConceptName conceptNamePT = new ConceptName(row.getDesignationPT(),
				new Locale(ImportConceptConstants.LOCALE_PT));
		conceptNamePT.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		conceptNamePT.setUuid(UUID.randomUUID().toString());

		names.add(conceptNamePT);

		if ((row.getDesignationEN() != null) && (row.getDesignationEN().length() > 0)) {
			final ConceptName conceptNameEN = new ConceptName(row.getDesignationEN(), Locale.ENGLISH);
			conceptNameEN.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
			conceptNameEN.setUuid(UUID.randomUUID().toString());
			names.add(conceptNameEN);
		}
		drugConcept.setNames(names);

		try {

			drugConcept = this.conceptService.saveConcept(drugConcept);
			this.saveConceptDescriptions(drugConcept);

			this.log.info("Created Drug Concept Question --> " + drugConcept);
			return drugConcept;

		} catch (final DuplicateConceptNameException e) {

			return this.conceptService.getConcept(drugConcept.getName().getName());
		}
	}
}