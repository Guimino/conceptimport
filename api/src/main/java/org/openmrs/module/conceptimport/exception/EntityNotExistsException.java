/**
 *
 */
package org.openmrs.module.conceptimport.exception;

import org.openmrs.api.APIException;

/**
 * @author Guimino Neves
 *
 */
public class EntityNotExistsException extends APIException {

	/**
	 *
	 */
	private static final long serialVersionUID = -5577828469141400901L;

	public EntityNotExistsException(final String sms) {

		super(sms);
	}
}
