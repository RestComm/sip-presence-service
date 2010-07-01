package org.openxdm.xcap.client.test.subscription;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;
import org.openxdm.xcap.client.test.AbstractXDMJunitTest;



// atm xcap diff forbiddens subscriptions on auids, because it has no means to
// auth the request, thus this test is disabled
//public class SubscribeAppUsageTest extends SubscribeDocumentTest {
public class SubscribeAppUsageTest extends AbstractXDMJunitTest {

	/*
	@Override
	protected String getContent() {
		return 	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<resource-lists xmlns=\"urn:ietf:params:xml:ns:resource-lists\">" +
			"<list>" +
				"<entry uri=\""+getDocumentUriKey().getDocumentSelector().getAUID()+"\"/>" +
			"</list>" +
		"</resource-lists>";
	}*/
	
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(SubscribeAppUsageTest.class);
	}
	
	@Test
	public void test() {
		
	}
	
}
