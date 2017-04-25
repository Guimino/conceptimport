/*
 * Friends in Global Health - FGH © 2016
 */
package org.openmrs.module.conceptimport.service;

import org.hibernate.FlushMode;
import org.openmrs.api.db.hibernate.DbSessionFactory;

/**
 * @author Stélio Moiane
 */
public class DbSessionManagerImpl implements DbSessionManager {
	
	private DbSessionFactory sessionFactory;
	
	private FlushMode currentFlushMode;
	
	@Override
	public FlushMode getCurrentFlushMode() {
		return this.sessionFactory.getCurrentSession().getFlushMode();
	}
	
	@Override
	public void setManualFlushMode() {
		this.currentFlushMode = this.getCurrentFlushMode();
		this.sessionFactory.getCurrentSession().setFlushMode(FlushMode.MANUAL);
	}
	
	@Override
	public void setFlushMode(final FlushMode flushMode) {
		this.sessionFactory.getCurrentSession().setFlushMode(flushMode);
	}
	
	public void setSessionFactory(final DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public void setAutoFlushMode() {
		this.setFlushMode(this.currentFlushMode);
	}
}
