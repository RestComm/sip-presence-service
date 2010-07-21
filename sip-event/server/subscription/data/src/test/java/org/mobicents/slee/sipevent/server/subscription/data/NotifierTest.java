package org.mobicents.slee.sipevent.server.subscription.data;

import junit.framework.Assert;
import junit.framework.TestCase;

public class NotifierTest extends TestCase {
	
	public void testNotifier() {
		
		String s1 = "sip:eduardo@mobicents.org";
		
		Notifier notifier = new Notifier(s1);
		
		Assert.assertEquals(s1, notifier.getUri());
		Assert.assertEquals(s1, notifier.getUriWithParam());
		Assert.assertNull(notifier.getParam());
	
		String s2 = "sip:eduardo@mobicents.org;pres-list=Default";
		
		notifier = new Notifier(s2);
		
		Assert.assertEquals(s1, notifier.getUri());
		Assert.assertEquals(s2, notifier.getUriWithParam());
		Assert.assertEquals("pres-list=Default", notifier.getParam());
				
	}
}
