/* Generated By:JJTree: Do not edit this line. HasCriteria.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=TeiidNodeFactory,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.teiid.query.sql.lang;

import org.komodo.spi.annotation.Removed;
import org.komodo.spi.query.sql.proc.IHasCriteria;
import org.komodo.spi.runtime.version.TeiidServerVersion.Version;
import org.teiid.query.parser.LanguageVisitor;
import org.teiid.query.parser.TeiidParser;

/**
 *
 */
@Removed(Version.TEIID_8_0)
public class HasCriteria extends Criteria implements PredicateCriteria, IHasCriteria<LanguageVisitor> {

    // the selector object used to determine if a type of criteria is specified 
    // on the user's query
    private CriteriaSelector criteriaSelector;

    /**
     * @param p
     * @param id
     */
    public HasCriteria(TeiidParser p, int id) {
        super(p, id);
    }

    /**
     * Get the <code>CriteriaSelector</code>
     * @return <code>CriteriaSelector</code> of this obj
     */
    public CriteriaSelector getSelector() {
        return criteriaSelector;
    }

    /**
     * Set the <code>CriteriaSelector</code>
     * @param selector The <code>CriteriaSelector</code> of this obj
     */
    public void setSelector(CriteriaSelector selector) {
        this.criteriaSelector = selector;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.criteriaSelector == null) ? 0 : this.criteriaSelector.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        HasCriteria other = (HasCriteria)obj;
        if (this.criteriaSelector == null) {
            if (other.criteriaSelector != null) return false;
        } else if (!this.criteriaSelector.equals(other.criteriaSelector)) return false;
        return true;
    }

    /** Accept the visitor. **/
    @Override
    public void acceptVisitor(LanguageVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public HasCriteria clone() {
        HasCriteria clone = new HasCriteria(this.parser, this.id);

        if(getSelector() != null)
            clone.setSelector(getSelector().clone());

        return clone;
    }

}
/* JavaCC - OriginalChecksum=54952667e8126ec83dae1a24d072a46b (do not edit this line) */
