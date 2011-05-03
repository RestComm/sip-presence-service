/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.slee.xdm.server.datasource.jpa;

import javax.slee.facilities.Tracer;
import javax.slee.resource.ConfigProperties;
import javax.slee.resource.InvalidConfigurationException;
import javax.slee.resource.ResourceAdaptorContext;

import org.openxdm.xcap.common.datasource.DataSource;
import org.openxdm.xcap.server.slee.resource.datasource.AbstractDataSourceResourceAdaptor;
import org.openxdm.xcap.server.slee.resource.datasource.InterceptedDataSource;

/**
 * JPA DataSource Resource Adaptor's Implementation.
 * 
 * @author Eduardo Martins
 * @version 1.0
 * 
 */

public class JPADataSourceResourceAdaptor extends AbstractDataSourceResourceAdaptor {
    
	private Tracer logger;
	
    private DataSource dataSource; 

    /* (non-Javadoc)
     * @see javax.slee.resource.ResourceAdaptor#raConfigure(javax.slee.resource.ConfigProperties)
     */
    @Override
    public void raConfigure(ConfigProperties arg0) {
    	// TODO Auto-generated method stub
    	
    }
    
    /* (non-Javadoc)
     * @see javax.slee.resource.ResourceAdaptor#raConfigurationUpdate(javax.slee.resource.ConfigProperties)
     */
    @Override
    public void raConfigurationUpdate(ConfigProperties arg0) {
    	// TODO Auto-generated method stub
    	
    }
    
    /* (non-Javadoc)
     * @see javax.slee.resource.ResourceAdaptor#raVerifyConfiguration(javax.slee.resource.ConfigProperties)
     */
    @Override
    public void raVerifyConfiguration(ConfigProperties arg0)
    		throws InvalidConfigurationException {
    	// TODO Auto-generated method stub
    	
    }
    
    /* (non-Javadoc)
     * @see javax.slee.resource.ResourceAdaptor#raUnconfigure()
     */
    @Override
    public void raUnconfigure() {
    	// TODO Auto-generated method stub
    	
    }
    
    /* (non-Javadoc)
     * @see org.openxdm.xcap.server.slee.resource.datasource.AbstractDataSourceResourceAdaptor#setResourceAdaptorContext(javax.slee.resource.ResourceAdaptorContext)
     */
    @Override
    public void setResourceAdaptorContext(ResourceAdaptorContext arg0) {
    	super.setResourceAdaptorContext(arg0);
    	logger = arg0.getTracer(getClass().getSimpleName());
    }
    
    /* (non-Javadoc)
     * @see org.openxdm.xcap.server.slee.resource.datasource.AbstractDataSourceResourceAdaptor#raActive()
     */
    @Override
    public void raActive() {
    	super.raActive();
    	if (dataSource == null) {
    		dataSource = new InterceptedDataSource(new JPADataSource());
    		try {
    			dataSource.open();
    		} catch (Exception e) {
    			getLogger().severe("Failed to open data source",e);
    		}
    	}
    }
    
    /* (non-Javadoc)
     * @see org.openxdm.xcap.server.slee.resource.datasource.AbstractDataSourceResourceAdaptor#raInactive()
     */
    @Override
    public void raInactive() {
    	super.raInactive();
    	if (dataSource != null) {
    		try {
    			dataSource.close();
    		} catch (Exception e) {
    			getLogger().severe("Failed to close data source",e);
    		}
    	}
    	dataSource = null;
    }
    
    /* (non-Javadoc)
     * @see org.openxdm.xcap.server.slee.resource.datasource.AbstractDataSourceResourceAdaptor#unsetResourceAdaptorContext()
     */
    @Override
    public void unsetResourceAdaptorContext() {
    	super.unsetResourceAdaptorContext();
    	logger = null;
    }
    
	public DataSource getDataSource() {
		return dataSource;
	}
	
	public Tracer getLogger() {
		return logger;
	}

}

