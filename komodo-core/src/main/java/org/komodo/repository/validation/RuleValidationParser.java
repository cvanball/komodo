/*
 * JBoss, Home of Professional Open Source.
 *
 * See the LEGAL.txt file distributed with this work for information regarding copyright ownership and licensing.
 *
 * See the AUTHORS.txt file distributed with this work for a full listing of individual contributors.
 */
package org.komodo.repository.validation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * The <code>ValidationRuleParser</code> parses validation rule definition input streams. Each input stream is validated
 * against a validation rule definition schema.
 */
public class RuleValidationParser {

    //private final File ruleSchemaFile;
    private Handler handler;

    /**
     * The parser of the rules file
     */
    private SAXParser parser;

    /**
     * Constructs a parser.
     * @param rulesSchemaFile the rules schema file
     * @throws Exception if there were problems with the rules schema file
     */
    public RuleValidationParser( File rulesSchemaFile ) throws Exception {
        // load validation XSD for validation using parser
        //this.ruleSchemaFile = new File( "../../plugins/org.komodo.core/resources/komodoValidation.xsd" ); //$NON-NLS-1$
        
        setupParser(rulesSchemaFile);
    }
    
    private void setupParser(File schemaFile) throws Exception {
        if (!schemaFile.exists()) {
            throw new IllegalStateException( "Validation schema file does not exist" ); //$NON-NLS-1$
        }

        // create parser
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware( true );
        factory.setValidating( true );
        
        this.parser = factory.newSAXParser();
        this.parser.setProperty( "http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema" ); //$NON-NLS-1$ //$NON-NLS-2$
        this.parser.setProperty( "http://java.sun.com/xml/jaxp/properties/schemaSource", schemaFile ); //$NON-NLS-1$
    }

    /**
     * @return the error messages output from the last parse operation,
     *  or <code>null</code> if parse has not been called
     */
    public Collection<String> getErrors() {
        if (this.handler == null) {
            return null;
        }

        return this.handler.getErrors();
    }

    /**
     * @return the fatal error messages output from the last parse operation,
     *  or <code>null</code> if parse has not been called
     */
    public Collection<String> getFatalErrors() {
        if (this.handler == null) {
            return null;
        }

        return this.handler.getFatalErrors();
    }

    /**
     * @param rulesFile the rules File (cannot be <code>null</code>)
     * @throws Exception if the definition file is <code>null</code> or if there is a problem parsing the file
     */
    public void parse( File rulesFile ) throws Exception {
        this.handler = new Handler();
        this.parser.parse(rulesFile, handler);
    }

    /**
     * The handler used by the parser. Each instance should be only used to parse one file.
     */
    class Handler extends DefaultHandler {

        private final Collection<String> fatals;
        private final Collection<String> errors;

        public Handler( ) {
            this.fatals = new ArrayList<String>();
            this.errors = new ArrayList<String>();
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException)
         */
        @Override
        public void error( SAXParseException e ) {
            this.errors.add(e.getLocalizedMessage());
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.xml.sax.helpers.DefaultHandler#fatalError(org.xml.sax.SAXParseException)
         */
        @Override
        public void fatalError( SAXParseException e ) {
            this.fatals.add(e.getLocalizedMessage());
        }

        /**
         * @return the error messages output from the last parse operation (never <code>null</code> but can be empty)
         */
        Collection<String> getErrors() {
            return this.errors;
        }

        /**
         * @return the fatal error messages output from the last parse operation (never <code>null</code> but can be empty)
         */
        Collection<String> getFatalErrors() {
            return this.fatals;
        }

    }

}
