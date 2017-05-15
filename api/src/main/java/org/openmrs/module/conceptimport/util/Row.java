package org.openmrs.module.conceptimport.util;

import java.io.Serializable;

public class Row implements Serializable {

	private static final long serialVersionUID = -8463201223444297627L;

	private final String fnm;
	private final String designationPT;
	private final String designationEN;
	private final String pharmaceuticForm;
	private final String dosage;
	private final String therapeuticGroup;
	private final String classGroup;

	public Row(final String fnm, final String designationPT, final String designationEN, final String pharmaceuticForm,
			final String dosage, final String therapeuticGroup, final String classGroup) {
		super();
		this.fnm = fnm;
		this.designationPT = designationPT;
		this.designationEN = designationEN;
		this.pharmaceuticForm = pharmaceuticForm;
		this.dosage = dosage;
		this.therapeuticGroup = therapeuticGroup;
		this.classGroup = classGroup;
	}

	public String getFnm() {
		return this.fnm;
	}

	public String getDesignationPT() {
		return this.designationPT;
	}

	public String getDesignationEN() {
		return this.designationEN;
	}

	public String getPharmaceuticForm() {
		return this.pharmaceuticForm;
	}

	public String getDosage() {
		return this.dosage;
	}

	public String getTherapeuticGroup() {
		return this.therapeuticGroup;
	}

	public String getClassGroup() {
		return this.classGroup;
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
				+ this.designationEN + ", pharmaceuticForm=" + this.pharmaceuticForm + ", dosage=" + this.dosage
				+ ", therapeuticGroup=" + this.therapeuticGroup + ", classGroup=" + this.classGroup + "]";
	}
}
