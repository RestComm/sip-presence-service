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

package org.openxdm.xcap.common.uri;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.openxdm.xcap.common.xml.NamespaceContext;



public class Parser {

	public static ResourceSelector parseResourceSelector(String xcapRoot, String resourceSelector,String queryComponent) throws ParseException {		
		
		if (resourceSelector == null || resourceSelector.isEmpty()) {
			throw new ParseException(null);			
		}
		
		// undecode uri		
		try {
			resourceSelector = URLDecoder.decode(resourceSelector,"UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// MUST NOT COME HERE			
		}
				
		String documentSelector = null;
		String nodeSelector = null;
		Map<String,String> namespaces = null;
		try {			
			if (xcapRoot != null && resourceSelector.substring(0,xcapRoot.length()).equals(xcapRoot)) {
				// xcap root is set and found, remove it
				resourceSelector = resourceSelector.substring(xcapRoot.length());
			}
			if (resourceSelector.isEmpty()) {
				throw new ParseException(null);
			}
			// be a friend and remove any leading /
			try {
				while(resourceSelector.charAt(0) == '/') {
					resourceSelector = resourceSelector.substring(1);
				}
			} catch (Exception e) {
				throw new ParseException(null);				
			}
			// break on nodeSelectorSeparator, that is, /~~/
			int nodeSelectorSeparator = resourceSelector.indexOf("/~~/");
			if (nodeSelectorSeparator != -1) {
				// get document selector
				documentSelector = resourceSelector.substring(0,nodeSelectorSeparator);
				// get node selector
				nodeSelector = resourceSelector.substring(nodeSelectorSeparator+4);
				// check for query component
				if (queryComponent != null) {					
					namespaces = parseQueryComponent(queryComponent,new ParseException(documentSelector));
				} else {					
					namespaces = new HashMap<String,String>();
				}
			} else {
				if (queryComponent == null) {
					documentSelector = resourceSelector;					
				}
				else {
					// node selector does not exists, query component must not exist too
					throw new ParseException(documentSelector);
				}
			}
		}
		catch (IndexOutOfBoundsException e) {
			throw new ParseException(documentSelector,e.getMessage());
		}
		
		return new ResourceSelector(documentSelector,nodeSelector,namespaces);										
	}
	
	public static Map<String,String> parseQueryComponent(String queryComponent, ParseException exceptionToThrow) throws ParseException {
		
		Map<String,String> namespaces = new HashMap<String,String>();
		// xpointer scheme = ( scheme data ), but scheme data can contain '(' and ')' chars,
		// even unbalanced, but in this case those chars need a '^' escape
		// any scheme data '^' must be escaped with another '^' char
		// we will discard any scheme not xmlns since those are not used in xcap
		// 1) we need to split each scheme, so we find the first ( and then we look for the close ),
		// then, if its xmlns scheme we process its data and continue till xpointer ends
		int schemeOpenParIndex;
		int afterLastSchemeCloseParIndex = 0;
		while((schemeOpenParIndex = queryComponent.indexOf('(',afterLastSchemeCloseParIndex)) >= 0) {
			// get scheme name
			String scheme = queryComponent.substring(afterLastSchemeCloseParIndex,schemeOpenParIndex);			
			if (scheme.equals("xmlns")) {
				// in the xmlns scheme the next close par is the scheme close par
				int schemeCloseParIndex = queryComponent.indexOf(')',schemeOpenParIndex);
				if (schemeCloseParIndex < 0) {
					// close par does not exist, mal formed xpointer
					throw exceptionToThrow;
				} else {
					// get scheme data
					String schemeData = queryComponent.substring(schemeOpenParIndex+1,schemeCloseParIndex);
					// find the '='
					int keyToNamespaceSeparator = schemeData.indexOf('=');
					if (keyToNamespaceSeparator < 0) {
						// does not exist, mal formed xpointer
						throw exceptionToThrow;
					} else {
						// 1st part is the key, 2nd is the namespace
						String key = schemeData.substring(0,keyToNamespaceSeparator);
						String namespace = schemeData.substring(keyToNamespaceSeparator+1);
						// add (key->namespace) to namespaces
						namespaces.put(key,namespace);						
						// update 'after' last scheme close par pointer
						afterLastSchemeCloseParIndex = schemeCloseParIndex+1;
					}				
				}								
			} else {				
				// not xmlns pointer, we need to skip it,
				// but we need to validade the whole xpointer
				if (scheme.indexOf(')') >= 0) {
					// its has close pars in scheme name, mal formed xpointer
					throw exceptionToThrow;
				}
				// count openPars
				int openPars = 1;
				// we need to find the scheme close par
				for(int i=schemeOpenParIndex+1;i<queryComponent.length();i++) {			
					if (queryComponent.charAt(i) == '(') {
						// open par
						openPars++;						
					} else if (queryComponent.charAt(i) == ')'){						
						// close par
						openPars--;
						if (openPars == 0) {
							// found the scheme close par
							// update 'after' last scheme close par pointer
							afterLastSchemeCloseParIndex = i+1;
							break;
						}											
					}
					else if (queryComponent.charAt(i) == '^'){						
						// escape char, we don't need to look at the next char						
						i++;
						continue;
					}					
				}
				if (openPars != 0) {
					// we didn't found the scheme close par, mal formed
					throw exceptionToThrow;
				}
			}
		}		
		return namespaces;
	}
		
	public static NodeSelector parseNodeSelector(String nodeSelector, NamespaceContext namespaceContext) throws ParseException {
				
		String terminalSelector = null;
		String elementSelector = null;
		
		int elementToTerminalSelectorSeparator =  nodeSelector.lastIndexOf('/');
		if (elementToTerminalSelectorSeparator != -1 
				&& elementToTerminalSelectorSeparator < (nodeSelector.length()-1)) {
			// it can be broken, check its last part is a just a element path or a terminal selector
			String elementSelectorCandidate = nodeSelector.substring(0,elementToTerminalSelectorSeparator);
			String terminalSelectorCandidate = nodeSelector.substring(elementToTerminalSelectorSeparator+1);
			//FIXME what about extension selector??
			if (terminalSelectorCandidate.charAt(0) == '@' || terminalSelectorCandidate.equals("namespace::*")) {
				elementSelector = elementSelectorCandidate; 
				terminalSelector = terminalSelectorCandidate;				
			}
			else {
				elementSelector = nodeSelector;
			}
		}
		else {
			throw new ParseException(null);
		}
	
		return new NodeSelector(elementSelector,terminalSelector,namespaceContext);
	}
	
	public static ElementSelector parseElementSelector(String elementSelector) throws ParseException {		
				
		LinkedList<ElementSelectorStep> elementSelectorSteps = new LinkedList<ElementSelectorStep>();
		
		int nextSlashPos = -1;
		String stepString = null;
		do {
			// get next '/' position
			nextSlashPos = elementSelector.indexOf('/');
			// get the next step string and remove that part
			// from element selector
			if (nextSlashPos != -1) {
				// next slash exists
				if (elementSelector.length()-1 > nextSlashPos) {
					stepString = elementSelector.substring(0,nextSlashPos);
					// advance element selector
					elementSelector = elementSelector.substring(nextSlashPos+1);					
				}
				else {
					// we need to have more than the slash
					throw new ParseException(null);
				}
			} else {
				// no more slashes, its the last step
				stepString = elementSelector;
				elementSelector = null;
			}
			// parse and add a step
			elementSelectorSteps.addLast(parseElementSelectorStep(stepString));			
		}
		while (elementSelector != null);
		
		return new ElementSelector(elementSelectorSteps);
	}
	
	public static ElementSelectorStep parseLastElementSelectorStep(String elementSelector) throws ParseException {
		// break the elementSelector on the last '/'
		int lastStepSeparatorIndex = elementSelector.lastIndexOf('/');
		if (lastStepSeparatorIndex > -1 && lastStepSeparatorIndex < elementSelector.length()-1) {
			return parseElementSelectorStep(elementSelector.substring(lastStepSeparatorIndex+1));
		} else {
			throw new ParseException(null);
		}		
	}
	
	public static ElementSelectorStep parseElementSelectorStep(String step) throws ParseException {
		
		try {
			// start breaking the step for '['
			String[] elementParts = step.split("\\[");
			if (elementParts.length == 1) {
				// no '[' found, its the simplest step
				return new ElementSelectorStep(step);
			} 
			else if (elementParts.length == 2) {
				// one '[' found
				// the first part is the element name
				if (elementParts[1].charAt(0) != '@') {
					// 2nd part is the element position
					if (elementParts[1].charAt(elementParts[1].length()-1) == ']') {					
						return new ElementSelectorStepByPos(elementParts[0],Integer.parseInt(elementParts[1].substring(0,elementParts[1].length()-1)));					
					}									
				} else {
					// 2nd part is the element id attribute
					if (elementParts[1].charAt(elementParts[1].length()-1) == ']') {
						String[] elementPartParts = elementParts[1].substring(0,elementParts[1].length()-1).split("=");
						if (elementPartParts.length == 2 && ((elementPartParts[1].charAt(0) == '"' && elementPartParts[1].charAt(elementPartParts[1].length()-1) == '"') || (elementPartParts[1].charAt(0) == '\'' && elementPartParts[1].charAt(elementPartParts[1].length()-1) == '\''))) {
							return new ElementSelectorStepByAttr(elementParts[0],elementPartParts[0].substring(1),elementPartParts[1].substring(1,elementPartParts[1].length()-1));
						}
					}				
				}
			}
			else if (elementParts.length == 3) {
				// two '[' found
				// the first part is the element name			
				// 2nd part is the element position
				int elementPosition = -1;
				if (elementParts[1].charAt(elementParts[1].length()-1) == ']') {							
					elementPosition = Integer.parseInt(elementParts[1].substring(0,elementParts[1].length()-1));				
				}			
				// 3rd part is the element id attribute
				String[] elementPartParts = elementParts[2].substring(0,elementParts[2].length()-1).split("=");
				if (elementPartParts.length == 2 && ((elementPartParts[1].charAt(0) == '"' && elementPartParts[1].charAt(elementPartParts[1].length()-1) == '"') || (elementPartParts[1].charAt(0) == '\'' && elementPartParts[1].charAt(elementPartParts[1].length()-1) == '\''))) {
					return new ElementSelectorStepByPosAttr(elementParts[0],elementPosition,elementPartParts[0].substring(1),elementPartParts[1].substring(1,elementPartParts[1].length()-1));				
				}
			}
		}
		catch (IndexOutOfBoundsException e) {
			// change it to parse exception			
			throw new ParseException(e.getMessage());			
		}		
		// if we get here than throw parse exception
		throw new ParseException(null);		
	}

	public static TerminalSelector parseTerminalSelector(String terminalSelector) throws ParseException {
		if (terminalSelector.charAt(0) == '@') {
			if (terminalSelector.length() > 1) {
				return new AttributeSelector(terminalSelector.substring(1));									
			}	
			else {
				throw new ParseException(null);
			}
		}
		else if (terminalSelector.equals("namespace::*")) {
			return new NamespaceSelector();				
		}
		else {
			//FIXME what about extension selector
			throw new ParseException(null);
		}		
	}
		
}
