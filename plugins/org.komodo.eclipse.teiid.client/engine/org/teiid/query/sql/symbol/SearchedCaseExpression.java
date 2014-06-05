/* Generated By:JJTree: Do not edit this line. SearchedCaseExpression.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.teiid.query.sql.symbol;

import java.util.Collections;
import java.util.List;

import org.komodo.spi.query.sql.symbol.ISearchedCaseExpression;
import org.teiid.query.parser.LanguageVisitor;
import org.teiid.query.parser.TeiidParser;
import org.teiid.query.sql.lang.Criteria;
import org.teiid.query.sql.lang.SimpleNode;
import org.teiid.runtime.client.Messages;

/**
 *
 */
public class SearchedCaseExpression extends SimpleNode
    implements Expression, ISearchedCaseExpression<LanguageVisitor> {

    private Class<?> type;

    /**
     * Ordered List of Criteria in the WHEN parts of this expression.
     */
    private List<Criteria> when = null;

    /** Ordered List containing Expression objects. */
    private List<Expression> then = null;

    /** The (optional) expression in the ELSE part of the expression */
    private Expression elseExpression = null;

    /**
     * @param p
     * @param id
     */
    public SearchedCaseExpression(TeiidParser p, int id) {
        super(p, id);
    }

    @Override
    public Class getType() {
        return type;
    }

    /**
    * @return number of when criteria
    */
   public int getWhenCount() {
       return (when == null) ? 0 : when.size();
   }

   /**
    * @return the List of Criteria in the WHEN parts of this expression. Never null.
    */
   public List<Criteria> getWhen() {
       return when;
   }

   /**
    * @param index
    * @return the WHEN criteria at the given 0-based index.
    */
   public Criteria getWhenCriteria(int index) {
       return when.get(index);
   }

   /**
    * Sets the WHEN and THEN parts of this CASE expression.
    * Both lists should have the same number of items.
    * @param when a non-null List of at least one Criteria
    * @param then a non-null List of at least one Expression
    */
   public void setWhen(List<? extends Criteria> when, List<? extends Expression> then) {
       if (when == null || then == null) {
           throw new IllegalArgumentException(Messages.getString(Messages.ERR.ERR_015_010_0036));
       }

       if (when.size() != then.size() ||
           when.size() < 1) {
           throw new IllegalArgumentException(Messages.getString(Messages.ERR.ERR_015_010_0036));
       }

       if (this.when != when) {
           this.when = Collections.unmodifiableList(when);
       }
       setThen(then);
   }

   /**
    * @return Gets the expression in the ELSE part of this expression. May be null as
    * the ELSE is optional.
    */
   public Expression getElseExpression() {
       return elseExpression;
   }
   
   /**
    * Sets the expression in the ELSE part of this expression. Can be null.
    * @param elseExpression
    */
   public void setElseExpression(Expression elseExpression) {
       this.elseExpression = elseExpression;
   }

   /**
    * @return Gets the List of THEN expressions in this CASE expression. Never null.
    */
   public List<Expression> getThen() {
       return then;
   }

   /**
    * @param index
    *
    * @return the expression of the THEN part at the given index.
    */
   public Expression getThenExpression(int index) {
       return then.get(index);
   }

   /**
    * Sets the List of THEN expressions in this CASE expression
    * @param then
    */
   public void setThen(List<? extends Expression> then) {
       if (this .then != then) {
           this.then = Collections.unmodifiableList(then);
       }
   }

    /**
     * Sets the type to which this expression has resolved.
     * @param type
     */
    public void setType(Class type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.elseExpression == null) ? 0 : this.elseExpression.hashCode());
        result = prime * result + ((this.then == null) ? 0 : this.then.hashCode());
        result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
        result = prime * result + ((this.when == null) ? 0 : this.when.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        SearchedCaseExpression other = (SearchedCaseExpression)obj;
        if (this.elseExpression == null) {
            if (other.elseExpression != null) return false;
        } else if (!this.elseExpression.equals(other.elseExpression)) return false;
        if (this.then == null) {
            if (other.then != null) return false;
        } else if (!this.then.equals(other.then)) return false;
        if (this.type == null) {
            if (other.type != null) return false;
        } else if (!this.type.equals(other.type)) return false;
        if (this.when == null) {
            if (other.when != null) return false;
        } else if (!this.when.equals(other.when)) return false;
        return true;
    }

    /** Accept the visitor. **/
    @Override
    public void acceptVisitor(LanguageVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public SearchedCaseExpression clone() {
        SearchedCaseExpression clone = new SearchedCaseExpression(this.parser, this.id);

        if(getWhen() != null)
            clone.setWhen(cloneList(getWhen()), cloneList(getThen()));
        if(getElseExpression() != null)
            clone.setElseExpression(getElseExpression().clone());
        if(getType() != null)
            clone.setType(getType());

        return clone;
    }

}
/* JavaCC - OriginalChecksum=04edede68784e204c845cc05bef87d10 (do not edit this line) */
