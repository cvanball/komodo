/* Generated By:JJTree: Do not edit this line. GroupSymbol.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.teiid.query.sql.symbol;

import org.komodo.spi.annotation.Removed;
import org.komodo.spi.query.sql.symbol.IGroupSymbol;
import org.komodo.spi.query.sql.symbol.ISymbol;
import org.komodo.spi.runtime.version.TeiidServerVersion;
import org.komodo.spi.runtime.version.TeiidServerVersion.Version;
import org.teiid.query.parser.LanguageVisitor;
import org.teiid.query.parser.TeiidParser;
import org.teiid.runtime.client.Messages;

/**
 *
 */
public class GroupSymbol extends Symbol implements IGroupSymbol<LanguageVisitor> {

    /**
     * Prefix used for denoting a temporary group symbol prefix
     */
    public static final String TEMP_GROUP_PREFIX = "#"; //$NON-NLS-1$
    
    /** Definition of the symbol, may be null */
    private String definition;

    private String outputDefinition;

    /** Actual metadata ID */
    private Object metadataID;

    private boolean isProcedure;

    private boolean isTempTable;

    private String schema;

    /**
     * @param p
     * @param id
     */
    public GroupSymbol(TeiidParser p, int id) {
        super(p, id);
    }

    /**
     * @return schema
     */
    public String getSchema() {
        return schema;
    }

    @Override
    public String getName() {
        if (this.schema != null) {
            return this.schema + ISymbol.SEPARATOR + this.getShortName();
        }
        return super.getName();
    }

    @Override
    public void setName(String name) {
        int index = name.indexOf('.');
        if (index > 0) {
            this.schema = new String(name.substring(0, index));
            name = new String(name.substring(index + 1));
        } else {
            this.schema = null;
        }
        super.setShortName(name);
    }

    /**
     * Get the definition for the group symbol, which may be null
     * @return Group definition, may be null
     */
    @Override
    public String getDefinition() {
        return definition;
    }

    /**
     * Set the definition for the group symbol, which may be null
     * @param definition Definition
     */
    @Override
    public void setDefinition(String definition) {
        this.definition = definition;
        this.outputDefinition = definition;
    }

    /**
     * @return output definition
     */
    public String getOutputDefinition() {
        return this.outputDefinition;
    }

    /**
     * @param outputDefinition
     */
    public void setOutputDefinition(String outputDefinition) {
        this.outputDefinition = outputDefinition;
    }

    @Override
    public boolean isProcedure() {
        return this.isProcedure;
    }

    /**
     * @param isProcedure
     */
    public void setProcedure(boolean isProcedure) {
        this.isProcedure = isProcedure;
    }

    /**
     * @param isTempTable
     */
    public void setIsTempTable(boolean isTempTable) {
        this.isTempTable = isTempTable;
    }

    /**
     * Returns if this is a Temp Table 
     * Set after resolving.
     *
     * @return true if temp table, false otherwise
     */
    public boolean isTempTable() {
        return this.isTempTable;
    }

    /**
     * Get the metadata ID that this group symbol resolves to.  If
     * the group symbol has not been resolved yet, this will be null.
     * If the symbol has been resolved, this will never be null.
     * @return Metadata ID object
     */
    @Override
    public Object getMetadataID() {
        return metadataID;
    }

    @Override
    public void setMetadataID(Object metadataID) {
        if(metadataID == null) {
            throw new IllegalArgumentException(Messages.getString(Messages.ERR.ERR_015_010_0016));
        }
        if (this.isImplicitTempGroupSymbol()) {
            this.isTempTable = true;
        }
        this.metadataID = metadataID;
    }

    /**
     * @return non correlation name
     */
    public String getNonCorrelationName() {
        if (this.definition == null) {
            return this.getName();
        }
        return this.getDefinition();
    }

    /**
     * @return canonical name
     */
    @Removed(Version.TEIID_8_0)
    public String getCanonicalName() {
        if (this.schema != null) {
            return this.schema + ISymbol.SEPARATOR + this.getShortCanonicalName();
        }
        return super.getShortCanonicalName();
    }

    /**
     * @param name
     * @return true if given name has the temp group prefix
     */
    public static boolean isTempGroupName(String name) {
        if (name == null) 
            return false;
        return name.startsWith(TEMP_GROUP_PREFIX);
    }

    /**
     * Returns true if this is a symbol for a temporary (implicit or explicit) group
     * May return false for explicit temp tables prior to resolving.
     * see {@link #isTempTable()}
     * @return is temp group symbol
     */
    public boolean isTempGroupSymbol() {
        return isTempTable || (metadataID == null && isImplicitTempGroupSymbol());
    }
    
    /**
     * @return is an implicit temp group symbol
     */
    public boolean isImplicitTempGroupSymbol() {
        if (getNonCorrelationName() == null) 
            return false;

        return getNonCorrelationName().startsWith(TEMP_GROUP_PREFIX);
    }

    /**
     * Returns true if this symbol has been completely resolved with respect
     * to actual runtime metadata.  A resolved symbol has been validated that
     * it refers to actual metadata and will have references to the real metadata
     * IDs if necessary.  Different types of symbols determine their resolution
     * in different ways, so this method is abstract and must be implemented
     * by subclasses.
     * @return True if resolved with runtime metadata
     */
    public boolean isResolved() {
        return (metadataID != null);
    }

    /**
     * Compare two groups and give an ordering.
     * @param other Other group
     * @return -1, 0, or 1 depending on how this compares to group
     */
    public int compareTo(GroupSymbol other) {
        return getName().compareTo(other.getName());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        if (this.schema != null) {
            result = prime * result + this.schema.hashCode();
            if (getTeiidVersion().isLessThan(Version.TEIID_8_0.get()))
                result = prime * result + ((this.getShortName() == null) ? 0 : this.getShortName().hashCode());
            else
                result = prime * result + ((this.getShortCanonicalName() == null) ? 0 : this.getShortCanonicalName().hashCode());

            return result;
        }

        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        GroupSymbol other = (GroupSymbol)obj;
        if (this.schema == null || other.schema == null) {
            if (getTeiidVersion().isLessThan(Version.TEIID_8_0.get())) {
                return this.getCanonicalName().equals(other.getCanonicalName());
            } else {
                return this.getName().equals(other.getName());
            }
        }
        
        if (this.schema == null) {
            if (other.schema != null)
                return false;
        } else if (!this.schema.equals(other.schema))
            return false;

        if (getTeiidVersion().isLessThan(Version.TEIID_8_0.get()))
            return this.getShortCanonicalName().equals(other.getShortCanonicalName());

        return this.getShortName().equals(other.getShortName());
    }

    /** Accept the visitor. **/
    @Override
    public void acceptVisitor(LanguageVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public GroupSymbol clone() {
        GroupSymbol clone = new GroupSymbol(this.parser, this.id);

        if(getDefinition() != null)
            clone.setDefinition(getDefinition());
        if(getOutputDefinition() != null)
            clone.setOutputDefinition(getOutputDefinition());
        if(getShortCanonicalName() != null)
            clone.setShortCanonicalName(getShortCanonicalName());
        if(getOutputName() != null)
            clone.setOutputName(getOutputName());
        if(getShortName() != null)
            clone.setShortName(getShortName());
        if(getName() != null)
            clone.setName(getName());
        if(getMetadataID() != null)
            clone.setMetadataID(getMetadataID());

        clone.setIsTempTable(isTempTable);
        clone.setProcedure(isProcedure);

        return clone;
    }
}
/* JavaCC - OriginalChecksum=f7012acf7f9a059597f0384f4fcb74fa (do not edit this line) */
