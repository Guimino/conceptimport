/**
 *
 */
package org.openmrs.module.conceptimport.exception;

import org.openmrs.OpenmrsObject;

/**
 * @author Guimino Neves
 *
 */
public class EntityExistisException extends ConceptImportBusinessException {

	private static final long serialVersionUID = 1L;

	public EntityExistisException(final OpenmrsObject obj) {

		super("Class = [" + obj.getClass() + "], ID = [" + obj.getId() + "] already exists");
	}

}
