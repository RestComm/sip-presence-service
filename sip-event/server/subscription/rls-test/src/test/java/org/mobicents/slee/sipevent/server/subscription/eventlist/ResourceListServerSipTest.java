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

package org.mobicents.slee.sipevent.server.subscription.eventlist;

import java.text.ParseException;
import java.util.TooManyListenersException;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;

import org.junit.Assert;
import org.junit.Test;

public class ResourceListServerSipTest {

	private String[] publishers = {"sip:alice@localhost","sip:bob@localhost"};
	private String subscriber = "sip:carol@localhost";
	private String resourceList = "sip:carol_enemies@localhost";
	
	private RlsServicesManager rlsServicesManager = new RlsServicesManager(resourceList,publishers,this);
	
	@Test
	public void test() throws InterruptedException, SipException, InvalidArgumentException, ParseException, TooManyListenersException {
		// create rls services
		rlsServicesManager.putRlsServices();
		// create and init publishers
		Publisher publisher1 = new Publisher(publishers[0],6060,this,"bs35r9");
		publisher1.publish();
		Publisher publisher2 = new Publisher(publishers[1],6061,this,"ty4658");
		publisher2.publish();
		// create and init subscriber
		Subscriber subscriber = new Subscriber(this.subscriber,resourceList,6062,this);
		subscriber.subscribe();
		// sleep half a sec
		Thread.sleep(90000);
		// unpublish
		publisher1.unpublish();
		publisher2.unpublish();
		// unsubscribe
		subscriber.unsubscribe();
		// remove rls services
		rlsServicesManager.deleteRlsServices();
	}
		
	protected void failTest(String message) {
		Assert.fail(message);
		rlsServicesManager.deleteRlsServices();
	}
}
