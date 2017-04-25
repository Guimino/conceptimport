package org.openmrs.module.conceptimport.util;

import java.io.Serializable;

public class Row implements Serializable {

	private static final long serialVersionUID = -8463201223444297627L;

	private String fnm;
	private String designationPT;
	private String designationEN;
	private String completeDesignation;
	private String pharmaceuticForm;
	private String dosage;
	private String therapeuticGroup;
	private Integer genericDrugConceptId;
	private Integer pharmaceuticFormConceptId;
	private Integer dosageConceptId;

	public Row(final String fnm, final String designationPT, final String designationEN,
			final String completeDesignation, final String pharmaceuticForm, final String dosage,
			final String therapeuticGroup, final Integer genericDrugConceptId, final Integer pharmaceuticFormConceptId,
			final Integer dosageConceptId) {
		super();
		this.fnm = fnm;
		this.designationPT = designationPT;
		this.designationEN = designationEN;
		this.completeDesignation = completeDesignation;
		this.pharmaceuticForm = pharmaceuticForm;
		this.dosage = dosage;
		this.therapeuticGroup = therapeuticGroup;
		this.genericDrugConceptId = genericDrugConceptId;
		this.pharmaceuticFormConceptId = pharmaceuticFormConceptId;
		this.dosageConceptId = dosageConceptId;
	}

	public String getFnm() {
		return this.fnm;
	}

	public void setFnm(final String fnm) {
		this.fnm = fnm;
	}

	public String getDesignationPT() {
		return this.designationPT;
	}

	public void setDesignationPT(final String designationPT) {
		this.designationPT = designationPT;
	}

	public String getDesignationEN() {
		return this.designationEN;
	}

	public void setDesignationEN(final String designationEN) {
		this.designationEN = designationEN;
	}

	public String getCompleteDesignation() {
		return this.completeDesignation;
	}

	public void setCompleteDesignation(final String completeDesignation) {
		this.completeDesignation = completeDesignation;
	}

	public String getPharmaceuticForm() {
		return this.pharmaceuticForm;
	}

	public void setPharmaceuticForm(final String pharmaceuticForm) {
		this.pharmaceuticForm = pharmaceuticForm;
	}

	public String getDosage() {
		return this.dosage;
	}

	public void setDosage(final String dosage) {
		this.dosage = dosage;
	}

	public String getTherapeuticGroup() {
		return this.therapeuticGroup;
	}

	public void setTherapeuticGroup(final String therapeuticGroup) {
		this.therapeuticGroup = therapeuticGroup;
	}

	public Integer getGenericDrugConceptId() {
		return this.genericDrugConceptId;
	}

	public void setGenericDrugConceptId(final Integer genericDrugConceptId) {
		this.genericDrugConceptId = genericDrugConceptId;
	}

	public Integer getPharmaceuticFormConceptId() {
		return this.pharmaceuticFormConceptId;
	}

	public void setPharmaceuticFormConceptId(final Integer pharmaceuticFormConceptId) {
		this.pharmaceuticFormConceptId = pharmaceuticFormConceptId;
	}

	public Integer getDosageConceptId() {
		return this.dosageConceptId;
	}

	public void setDosageConceptId(final Integer dosageConceptId) {
		this.dosageConceptId = dosageConceptId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.fnm == null) ? 0 : this.fnm.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {

		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final Row other = (Row) obj;
		if (this.fnm == null) {
			if (other.fnm != null) {
				return false;
			}
		} else if (!this.fnm.equals(other.fnm)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Row [fnm=" + this.fnm + ", designationPT=" + this.designationPT + ", designationEN="
				+ this.designationEN + ", completeDesignation=" + this.completeDesignation + ", pharmaceuticForm="
				+ this.pharmaceuticForm + ", dosage=" + this.dosage + ", therapeuticGroup=" + this.therapeuticGroup
				+ "]";
	}
}
