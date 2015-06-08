/*
 * JBoss, Home of Professional Open Source.
 *
 * See the LEGAL.txt file distributed with this work for information regarding copyright ownership and licensing.
 *
 * See the AUTHORS.txt file distributed with this work for a full listing of individual contributors.
 */
package org.komodo.relational.vdb.internal;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.komodo.relational.RelationalModelTest;
import org.komodo.relational.RelationalObject.Filter;
import org.komodo.relational.internal.RelationalModelFactory;
import org.komodo.relational.internal.RelationalObjectImpl;
import org.komodo.relational.vdb.DataRole;
import org.komodo.relational.vdb.Mask;
import org.komodo.relational.vdb.Permission;
import org.komodo.relational.vdb.Vdb;
import org.komodo.spi.KException;
import org.komodo.spi.repository.KomodoType;
import org.modeshape.sequencer.teiid.lexicon.VdbLexicon;

@SuppressWarnings( { "javadoc", "nls" } )
public final class MaskImplTest extends RelationalModelTest {

    private DataRole dataRole;
    private Mask mask;
    private Permission permission;
    private Vdb vdb;

    @Before
    public void init() throws Exception {
        this.vdb = RelationalModelFactory.createVdb( this.uow, _repo, null, "vdb", "/Users/sledge/hammer/MyVdb.vdb" );
        this.dataRole = RelationalModelFactory.createDataRole( this.uow, _repo, this.vdb, "dataRole" );
        this.permission = RelationalModelFactory.createPermission( this.uow, _repo, this.dataRole, "permission" );
        this.mask = RelationalModelFactory.createMask( this.uow, _repo, this.permission, "mask" );
        commit();
    }

    @Test
    public void shouldBeChildRestricted() {
        assertThat( this.mask.isChildRestricted(), is( true ) );
    }

    @Test
    public void shouldFailConstructionIfNotMask() {
        if ( RelationalObjectImpl.VALIDATE_INITIAL_STATE ) {
            try {
                new MaskImpl( this.uow, _repo, this.vdb.getAbsolutePath() );
                fail();
            } catch ( final KException e ) {
                // expected
            }
        }
    }

    @Test
    public void shouldHaveCorrectPrimaryType() throws Exception {
        assertThat( this.mask.getPrimaryType( this.uow ).getName(), is( VdbLexicon.DataRole.Permission.Mask.MASK ) );
    }

    @Test
    public void shouldHaveCorrectTypeIdentifier() throws Exception {
        assertThat(this.mask.getTypeIdentifier( this.uow ), is(KomodoType.VDB_MASK));
    }

    @Test
    public void shouldHaveMoreRawProperties() throws Exception {
        final String[] filteredProps = this.mask.getPropertyNames( this.uow );
        final String[] rawProps = this.mask.getRawPropertyNames( this.uow );
        assertThat( ( rawProps.length > filteredProps.length ), is( true ) );
    }

    @Test
    public void shouldHaveParentPermission() throws Exception {
        assertThat( this.mask.getParent( this.uow ), is( instanceOf( Permission.class ) ) );
    }

    @Test( expected = UnsupportedOperationException.class )
    public void shouldNotAllowChildren() throws Exception {
        this.mask.addChild( this.uow, "blah", null );
    }

    @Test
    public void shouldNotContainFilteredProperties() throws Exception {
        final String[] filteredProps = this.mask.getPropertyNames( this.uow );
        final Filter[] filters = this.mask.getFilters();

        for ( final String name : filteredProps ) {
            for ( final Filter filter : filters ) {
                assertThat( filter.rejectProperty( name ), is( false ) );
            }
        }
    }

    @Test
    public void shouldNotHaveOrderAfterConstruction() throws Exception {
        assertThat( this.mask.getOrder( this.uow ), is( nullValue() ) );
    }

    @Test
    public void shouldSetOrder() throws Exception {
        final String newValue = "newOrder";
        this.mask.setOrder( this.uow, newValue );
        assertThat( this.mask.getOrder( this.uow ), is( newValue ) );
    }

}
