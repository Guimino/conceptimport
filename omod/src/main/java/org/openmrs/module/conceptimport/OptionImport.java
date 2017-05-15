/**
 *
 */
package org.openmrs.module.conceptimport;

/**
 *
 */
public enum OptionImport {

	LOAD_CONCEPTS("1", "conceptimport.load.concepts"),

	LOAD_DRUGS("2", "conceptimport.load.drugs");

	private final String code;
	private final String description;

	OptionImport(final String code, final String description) {
		this.code = code;
		this.description = description;
	}

	public String getCode() {
		return this.code;
	}

	public String getDescription() {

		return this.description;
	}
}
