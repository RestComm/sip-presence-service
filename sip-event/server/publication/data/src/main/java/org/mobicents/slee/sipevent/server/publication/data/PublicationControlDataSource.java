package org.mobicents.slee.sipevent.server.publication.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jboss.cache.Cache;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;
import org.mobicents.cache.MobicentsCache;

public class PublicationControlDataSource {

	/*
	 data is structured as:
	 
	  root
	  +--- msps-pub
	  +------ eventPackage
	  +--------- entity
	  +------------ eTag (Boolean.TRUE = publication)
	  +--- msps-pub-timers
	  +------ publicationKey	  
	  +--- msps-cpub
	  +------ eventPackage
	  +--------- entity (Boolean.TRUE = composedPublication)
	  
	  note: publicationKey is (entity,eventPackage,eTag) 
	 */
	
	private final Node pubRoot;  
	private final Node timersRoot;
	private final Node cPubRoot;	
	
	public PublicationControlDataSource(MobicentsCache cache) {
		Cache jbcache = cache.getJBossCache(); 
		pubRoot = jbcache.getRoot().addChild(Fqn.fromElements("msps-pub"));
		timersRoot = jbcache.getRoot().addChild(Fqn.fromElements("msps-pub-timers"));
		cPubRoot = jbcache.getRoot().addChild(Fqn.fromElements("msps-cpub"));
	}
	
	// ----
	
	private Node getEntityAndPackagePubNode(String eventPackage,String entity) {
		Node eventPackageNode = pubRoot.getChild(eventPackage);
		if (eventPackageNode == null) {
			return null;
		}
		Node entityNode = eventPackageNode.getChild(entity);
		if (entityNode == null) {
			return null;
		}
		return entityNode;
	}
	
	public void add(Publication p) {
		final PublicationKey pk = p.getPublicationKey();
		Node eventPackageNode = pubRoot.getChild(pk.getEventPackage());
		if (eventPackageNode == null) {
			eventPackageNode = pubRoot.addChild(Fqn.fromElements(pk.getEventPackage()));
		}
		Node entityNode = eventPackageNode.getChild(pk.getEntity());
		if (entityNode == null) {
			entityNode = eventPackageNode.addChild(Fqn.fromElements(pk.getEntity()));
		}
		Node eTagNode = entityNode.getChild(pk.getETag());
		if (eTagNode == null) {
			eTagNode = entityNode.addChild(Fqn.fromElements(pk.getETag()));
		}
		else {
			throw new IllegalStateException("publication "+pk+" already exists");
		}
		eTagNode.put(Boolean.TRUE, p);
		timersRoot.addChild(Fqn.fromElements(p.getTimerID())).put(Boolean.TRUE, pk);
	}

	public Publication get(PublicationKey pk) {
		final Node entityAndPackageNode = getEntityAndPackagePubNode(pk.getEventPackage(),pk.getEntity());
		if (entityAndPackageNode == null) {
			return null;
		}
		Node eTagNode = entityAndPackageNode.getChild(pk.getETag());
		return eTagNode == null ? null : (Publication) eTagNode.get(Boolean.TRUE);
	}
	
	public Publication getFromTimerID(Serializable timerID) {
		Node timerIDNode = timersRoot.getChild(timerID);
		if (timerIDNode == null) {
			return null;
		}
		return get((PublicationKey)timerIDNode.get(Boolean.TRUE));
	}
	
	public List<Publication> getPublications(String eventPackage,String entity) {
		final Node entityAndPackageNode = getEntityAndPackagePubNode(eventPackage,entity);
		if (entityAndPackageNode == null) {
			return Collections.emptyList();
		}
		final List<Publication> result = new ArrayList<Publication>();
		for (Node eTagNode : (Set<Node>) entityAndPackageNode.getChildren()) {
			result.add((Publication) eTagNode.get(Boolean.TRUE));
		}
		return result;
	}
	
	public void replace(Publication p, Publication q) {
		final PublicationKey pk = p.getPublicationKey();
		final Node entityAndPackageNode = getEntityAndPackagePubNode(pk.getEventPackage(),pk.getEntity());
		if (entityAndPackageNode != null && entityAndPackageNode.removeChild(pk.getETag())) {
			timersRoot.removeChild(p.getTimerID());
		}
		else {
			throw new IllegalStateException("original publication "+pk+" not found");
		}
		final PublicationKey qk = q.getPublicationKey();
		Node eTagNode = entityAndPackageNode.getChild(qk.getETag());
		if (eTagNode == null) {
			eTagNode = entityAndPackageNode.addChild(Fqn.fromElements(qk.getETag()));
		}
		else {
			throw new IllegalStateException("new publication "+qk+" already exists");
		}
		eTagNode.put(Boolean.TRUE, q);
		timersRoot.addChild(Fqn.fromElements(q.getTimerID())).put(Boolean.TRUE, qk);
	}
	
	public void delete(Publication p) {
		final PublicationKey pk = p.getPublicationKey();
		final Node entityAndPackageNode = getEntityAndPackagePubNode(pk.getEventPackage(),pk.getEntity());
		if (entityAndPackageNode != null && entityAndPackageNode.removeChild(pk.getETag())) {
			timersRoot.removeChild(p.getTimerID());
		}
		else {
			throw new IllegalStateException("publication "+pk+" not found");
		}
		if (entityAndPackageNode.isLeaf()) {
			entityAndPackageNode.getParent().removeChild(entityAndPackageNode.getFqn().getLastElement());
		}
	}
	
	// ---
	
	private Node getPackageCPubNode(String eventPackage) {
		Node eventPackageNode = cPubRoot.getChild(eventPackage);
		if (eventPackageNode == null) {
			eventPackageNode = cPubRoot.addChild(Fqn.fromElements(eventPackage));
		}
		return eventPackageNode;
	}
	
	public void add(ComposedPublication p) {
		final ComposedPublicationKey pk = p.getComposedPublicationKey();
		final Node eventPackageNode = getPackageCPubNode(pk.getEventPackage());
		Node entityNode = eventPackageNode.getChild(pk.getEntity());
		if (entityNode == null) {
			entityNode = eventPackageNode.addChild(Fqn.fromElements(pk.getEntity()));
		}
		else {
			throw new IllegalStateException("composed publication "+pk+" already exists");
		}
		entityNode.put(Boolean.TRUE, p);
	}

	public void update(ComposedPublication p) {
		final ComposedPublicationKey pk = p.getComposedPublicationKey();
		final Node eventPackageNode = getPackageCPubNode(pk.getEventPackage());
		Node entityNode = eventPackageNode.getChild(pk.getEntity());
		if (entityNode == null) {
			throw new IllegalStateException("composed publication "+pk+" does not exists");
		}
		entityNode.put(Boolean.TRUE, p);
	}
	
	public ComposedPublication get(ComposedPublicationKey pk) {
		final Node eventPackageNode = getPackageCPubNode(pk.getEventPackage());
		Node node = eventPackageNode.getChild(pk.getEntity());
		return node == null ? null : (ComposedPublication) node.get(Boolean.TRUE);
	}
	
	public boolean delete(ComposedPublicationKey pk) {
		final Node eventPackageNode = getPackageCPubNode(pk.getEventPackage());
		return eventPackageNode.removeChild(pk.getEntity());
	}
}
