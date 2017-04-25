/**
 *
 */
package org.openmrs.module.conceptimport.exception;

/**
 * @author Guimino Neves
 *
 */
public class EntityNotFoundException extends ConceptImportBusinessException {

	private static final long serialVersionUID = 5376749183782471086L;

	public EntityNotFoundException(final String message) {
		super(message);
	}

}
