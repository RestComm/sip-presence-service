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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.openxdm.xcap.common.datasource.DataSource;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.uri.DocumentSelector;

/**
 * @author eduardomartins
 */
public class JPADataSource implements DataSource {

	private static final org.openxdm.xcap.common.datasource.Document[] EMPTY_DOC_ARRAY = {};

	private EntityManagerFactory entityManagerFactory = null;

	@Override
	public void open() throws InternalServerErrorException {
		entityManagerFactory = Persistence
				.createEntityManagerFactory("mobicents-xdm-core-datasource-pu");
	}

	@Override
	public void close() throws InternalServerErrorException {
		entityManagerFactory.close();
	}

	@Override
	public org.openxdm.xcap.common.datasource.Document getDocument(
			DocumentSelector documentSelector)
			throws InternalServerErrorException {

		EntityManager entityManager = entityManagerFactory
				.createEntityManager();
		final Document document = entityManager.find(
				Document.class,
				new DocumentPrimaryKey(documentSelector.getCollection(), documentSelector
								.getDocumentName()));
		entityManager.close();
		return document;
	}

	@Override
	public void createDocument(DocumentSelector documentSelector, String eTag,
			String xml) throws InternalServerErrorException {
		EntityManager entityManager = entityManagerFactory
				.createEntityManager();
		Document document = new Document(documentSelector.getCollection(),
				documentSelector.getDocumentName());
		document.setETag(eTag);
		document.setXml(xml);
		entityManager.persist(document);
		entityManager.flush();
		entityManager.close();
	}

	@Override
	public void updateDocument(DocumentSelector documentSelector, String eTag,
			String xml) throws InternalServerErrorException {
		EntityManager entityManager = entityManagerFactory
				.createEntityManager();
		entityManager
				.createNamedQuery(Document.JPA_QUERY_UPDATE_DOCUMENTS_FROM_KEY)
				.setParameter("collection", documentSelector.getCollection())
				.setParameter("documentName",
						documentSelector.getDocumentName())
				.setParameter("eTag", eTag).setParameter("xml", xml)
				.executeUpdate();
		entityManager.flush();
		entityManager.close();
	}

	@Override
	public void deleteDocument(DocumentSelector documentSelector)
			throws InternalServerErrorException {
		EntityManager entityManager = entityManagerFactory
				.createEntityManager();
		entityManager
				.createNamedQuery(Document.JPA_QUERY_DELETE_DOCUMENTS_FROM_KEY)
				.setParameter("collection", documentSelector.getCollection())
				.setParameter("documentName",
						documentSelector.getDocumentName()).executeUpdate();
		entityManager.flush();
		entityManager.close();
	}

	@Override
	public org.openxdm.xcap.common.datasource.Document[] getDocuments(
			String collection, boolean includeChildCollections) throws InternalServerErrorException {

		org.openxdm.xcap.common.datasource.Document[] result = null;
		EntityManager entityManager = entityManagerFactory
				.createEntityManager();
		List<?> resultList = entityManager
				.createNamedQuery(includeChildCollections ? Document.JPA_QUERY_SELECT_DOCUMENTS_MATCH_COLLECTION : Document.JPA_QUERY_SELECT_DOCUMENTS_IN_COLLECTION)
				.setParameter("collection", includeChildCollections ? collection + "%" : collection).getResultList();
		int resultListSize = resultList.size();
		if (resultListSize > 0) {
			result = new org.openxdm.xcap.common.datasource.Document[resultListSize];
			for (int i = 0; i < resultListSize; i++) {
				result[i] = (Document) resultList.get(i);
			}
		} else {
			result = EMPTY_DOC_ARRAY;
		}
		entityManager.close();
		return result;

	}

}