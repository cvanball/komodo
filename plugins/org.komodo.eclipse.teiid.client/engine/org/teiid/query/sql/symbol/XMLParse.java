/* Generated By:JJTree: Do not edit this line. XMLParse.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.teiid.query.sql.symbol;

import org.komodo.spi.query.sql.symbol.IXMLParse;
import org.teiid.core.types.DataTypeManagerService;
import org.teiid.query.parser.LanguageVisitor;
import org.teiid.query.parser.TeiidParser;
import org.teiid.query.sql.lang.SimpleNode;

/**
 *
 */
public class XMLParse extends SimpleNode implements Expression, IXMLParse<LanguageVisitor> {

    private boolean document;

    private Expression expression;

    private boolean wellFormed;

    /**
     * @param p
     * @param id
     */
    public XMLParse(TeiidParser p, int id) {
        super(p, id);
    }

    @Override
    public Class<?> getType() {
        return DataTypeManagerService.DefaultDataTypes.XML.getTypeClass();
    }

    /**
     * @return expression
     */
    public Expression getExpression() {
        return expression;
    }
    
    /**
     * @return document
     */
    public boolean isDocument() {
        return document;
    }
    
    /**
     * @param document
     */
    public void setDocument(boolean document) {
        this.document = document;
    }
    
    /**
     * @param expression
     */
    public void setExpression(Expression expression) {
        this.expression = expression;
    }
    
    /**
     * @return well formed
     */
    public boolean isWellFormed() {
        return wellFormed;
    }
    
    /**
     * @param wellFormed
     */
    public void setWellFormed(boolean wellFormed) {
        this.wellFormed = wellFormed;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (this.document ? 1231 : 1237);
        result = prime * result + ((this.expression == null) ? 0 : this.expression.hashCode());
        result = prime * result + (this.wellFormed ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        XMLParse other = (XMLParse)obj;
        if (this.document != other.document) return false;
        if (this.expression == null) {
            if (other.expression != null) return false;
        } else if (!this.expression.equals(other.expression)) return false;
        if (this.wellFormed != other.wellFormed) return false;
        return true;
    }

    /** Accept the visitor. **/
    @Override
    public void acceptVisitor(LanguageVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public XMLParse clone() {
        XMLParse clone = new XMLParse(this.parser, this.id);

        if(getExpression() != null)
            clone.setExpression(getExpression().clone());
        clone.setDocument(isDocument());
        clone.setWellFormed(isWellFormed());

        return clone;
    }

}
/* JavaCC - OriginalChecksum=3149fac262ffb7a7b54539656eb4178a (do not edit this line) */
