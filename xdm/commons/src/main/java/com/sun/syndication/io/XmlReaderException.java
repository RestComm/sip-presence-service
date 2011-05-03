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

package com.sun.syndication.io;

import java.io.InputStream;
import java.io.IOException;

/**
 * The XmlReaderException is thrown by the XmlReader constructors if the charset encoding
 * can not be determined according to the XML 1.0 specification and RFC 3023.
 * <p>
 * The exception returns the unconsumed InputStream to allow the application to do an
 * alternate processing with the stream. Note that the original InputStream given to the
 * XmlReader cannot be used as that one has been already read.
 * <p>
 *
 * @author Alejandro Abdelnur
 *
 */
public class XmlReaderException extends IOException {

    private static final long serialVersionUID = 1825940990869092585L;
    
    private String _bomEncoding;
    private String _xmlGuessEncoding;
    private String _xmlEncoding;
    private String _contentTypeMime;
    private String _contentTypeEncoding;
    private InputStream _is;

    /**
     * Creates an exception instance if the charset encoding could not be determined.
     * <p>
     * Instances of this exception are thrown by the XmlReader.
     * <p>
     * @param msg message describing the reason for the exception.
     * @param bomEnc BOM encoding.
     * @param xmlGuessEnc XML guess encoding.
     * @param xmlEnc XML prolog encoding.
     * @param is the unconsumed InputStream.
     *
     */
    public XmlReaderException(String msg,String bomEnc,String xmlGuessEnc,String xmlEnc,InputStream is) {
        this(msg,null,null,bomEnc,xmlGuessEnc,xmlEnc,is);
    }

    /**
     * Creates an exception instance if the charset encoding could not be determined.
     * <p>
     * Instances of this exception are thrown by the XmlReader.
     * <p>
     * @param msg message describing the reason for the exception.
     * @param ctMime MIME type in the content-type.
     * @param ctEnc encoding in the content-type.
     * @param bomEnc BOM encoding.
     * @param xmlGuessEnc XML guess encoding.
     * @param xmlEnc XML prolog encoding.
     * @param is the unconsumed InputStream.
     *
     */
    public XmlReaderException(String msg,String ctMime,String ctEnc,
                              String bomEnc,String xmlGuessEnc,String xmlEnc,InputStream is) {
        super(msg);
        _contentTypeMime = ctMime;
        _contentTypeEncoding = ctEnc;
        _bomEncoding = bomEnc;
        _xmlGuessEncoding = xmlGuessEnc;
        _xmlEncoding = xmlEnc;
        _is = is;
    }

    /**
     * Returns the BOM encoding found in the InputStream.
     * <p>
     * @return the BOM encoding, null if none.
     *
     */
    public String getBomEncoding() {
        return _bomEncoding;
    }

    /**
     * Returns the encoding guess based on the first bytes of the InputStream.
     * <p>
     * @return the encoding guess, null if it couldn't be guessed.
     *
     */
    public String getXmlGuessEncoding() {
        return _xmlGuessEncoding;
    }

    /**
     * Returns the encoding found in the XML prolog of the InputStream.
     * <p>
     * @return the encoding of the XML prolog, null if none.
     *
     */
    public String getXmlEncoding() {
        return _xmlEncoding;
    }

    /**
     * Returns the MIME type in the content-type used to attempt determining the encoding.
     * <p>
     * @return the MIME type in the content-type, null if there was not content-type or the encoding detection
     *         did not involve HTTP.
     *
     */
    public String getContentTypeMime() {
        return _contentTypeMime;
    }

    /**
     * Returns the encoding in the content-type used to attempt determining the encoding.
     * <p>
     * @return the encoding in the content-type, null if there was not content-type, no encoding in it
     *         or the encoding detection did not involve HTTP.
     *
     */
    public String getContentTypeEncoding() {
        return _contentTypeEncoding;
    }

    /**
     * Returns the unconsumed InputStream to allow the application to do an alternate
     * encoding detection on the InputStream.
     * <p>
     * @return the unconsumed InputStream.
     *
     */
    public InputStream getInputStream() {
        return _is;
    }
}
