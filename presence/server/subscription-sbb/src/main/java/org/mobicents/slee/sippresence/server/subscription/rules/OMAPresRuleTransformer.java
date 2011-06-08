package org.mobicents.slee.sippresence.server.subscription.rules;

import org.openxdm.xcap.common.error.InternalServerErrorException;

public interface OMAPresRuleTransformer<T> {

	public T transform(T content, OMAPresRule rule) throws InternalServerErrorException;
	
}
