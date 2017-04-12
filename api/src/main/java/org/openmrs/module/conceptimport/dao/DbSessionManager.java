/*
 * Friends in Global Health - FGH © 2016
 */
package org.openmrs.module.conceptimport.dao;

import org.hibernate.FlushMode;

/**
 */
public interface DbSessionManager {

	FlushMode getCurrentFlushMode();

	void setManualFlushMode();

	void setFlushMode(FlushMode flushMode);

	void setAutoFlushMode();
}
