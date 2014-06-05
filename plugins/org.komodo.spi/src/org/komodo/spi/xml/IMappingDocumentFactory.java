/*************************************************************************************
 * Copyright (c) 2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.komodo.spi.xml;

import java.io.InputStream;




/**
 *
 */
public interface IMappingDocumentFactory {

    /**
     * Add a namespace to the given element
     * 
     * @param element
     * @param prefix 
     * @param uri
     */
    void addNamespace(IMappingElement element, String prefix, String uri);

    /**
     * Load a mapping document from the input stream and assign 
     * the document the given name.
     * 
     * @param inputStream
     * @param documentName
     * @return new mapping document
     * 
     * @throws Exception
     */
    IMappingDocument loadMappingDocument(InputStream inputStream, String documentName) throws Exception;
    
    /**
     * Create an XML Mapping Document
     * 
     * @param encoding 
     * @param formatted 
     * 
     * @return instance of {@link IMappingDocument} 
     */
    IMappingDocument createMappingDocument(String encoding, boolean formatted);

    /**
     * Create a mapping element
     * 
     * @param name
     * @param nsPrefix
     * 
     * @return instance of {@link IMappingElement}
     */
    IMappingElement createMappingElement(String name, String nsPrefix);

    /**
     * Create a recursive mapping element
     * 
     * @param name
     * @param nsPrefix
     * @param recursionMappingClass
     * 
     * @return instance of {@link IMappingRecursiveElement}
     */
    IMappingRecursiveElement createMappingRecursiveElement(String name,
                                                           String nsPrefix,
                                                           String recursionMappingClass);

    /**
     * Create a mapping attribute
     * 
     * @param name
     * @param nsPrefix
     * 
     * @return instance of {@link IMappingAttribute}
     */
    IMappingAttribute createMappingAttribute(String name, String nsPrefix);

    /**
     * Create a mapping criteria node
     * 
     * @param criteria
     * @param isDefault
     * 
     * @return instance of {@link IMappingCriteriaNode}
     */
    IMappingCriteriaNode createMappingCriteriaNode(String criteria, boolean isDefault);
    
    /**
     * Create a mapping choice node
     * 
     * @param exceptionOnDefault
     * 
     * @return instance of {@link IMappingChoiceNode}
     */
    IMappingChoiceNode createMappingChoiceNode(boolean exceptionOnDefault);

    /**
     * Create a mapping sequence node
     * 
     * @return instance of {@link IMappingSequenceNode}
     */
    IMappingSequenceNode createMappingSequenceNode();

    /**
     * Create a mapping all node
     *
     * @return instance of {@link IMappingAllNode}
     */
    IMappingAllNode createMappingAllNode();

}
