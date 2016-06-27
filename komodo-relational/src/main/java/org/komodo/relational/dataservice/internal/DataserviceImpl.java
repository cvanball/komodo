/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package org.komodo.relational.dataservice.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.komodo.core.KomodoLexicon;
import org.komodo.relational.Messages;
import org.komodo.relational.RelationalModelFactory;
import org.komodo.relational.dataservice.Dataservice;
import org.komodo.relational.dataservice.DataserviceManifest;
import org.komodo.relational.internal.RelationalObjectImpl;
import org.komodo.relational.vdb.Vdb;
import org.komodo.relational.vdb.internal.VdbImpl;
import org.komodo.spi.KException;
import org.komodo.spi.repository.DocumentType;
import org.komodo.spi.repository.KomodoObject;
import org.komodo.spi.repository.KomodoType;
import org.komodo.spi.repository.PropertyValueType;
import org.komodo.spi.repository.Repository;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.spi.repository.Repository.UnitOfWork.State;
import org.komodo.utils.ArgCheck;
import org.teiid.modeshape.sequencer.vdb.lexicon.VdbLexicon;

/**
 * Implementation of Dataservice instance model
 */
public class DataserviceImpl extends RelationalObjectImpl implements Dataservice {

    /**
     * The allowed child types.
     */
    protected static final KomodoType[] CHILD_TYPES = new KomodoType[] { Vdb.IDENTIFIER };

    /**
     * @param uow
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository
     * @param path
     *        the path
     * @throws KException
     *         if error occurs
     */
    public DataserviceImpl( final UnitOfWork uow,
                      final Repository repository,
                      final String path ) throws KException {
        super(uow, repository, path);
    }

    @Override
    public KomodoType getTypeIdentifier(UnitOfWork uow) {
        return Dataservice.IDENTIFIER;
    }

    @Override
    public DataserviceManifest createManifest(UnitOfWork transaction, Properties properties) throws KException {
        return new DataserviceManifest(transaction, this);
    }

    /* (non-Javadoc)
     * @see org.komodo.spi.repository.Exportable#export(org.komodo.spi.repository.Repository.UnitOfWork, java.util.Properties)
     */
    @Override
    public byte[] export(UnitOfWork transaction, Properties exportProperties) throws KException {
        DataserviceConveyor conveyor = new DataserviceConveyor(getRepository());
        return conveyor.dsExport(transaction, this, exportProperties);
    }

    @Override
    public DocumentType getDocumentType(UnitOfWork uow) throws KException {
        return DocumentType.ZIP;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.repository.ObjectImpl#getChildTypes()
     */
    @Override
    public KomodoType[] getChildTypes() {
        return CHILD_TYPES;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.internal.RelationalObjectImpl#getChild(org.komodo.spi.repository.Repository.UnitOfWork,
     *      java.lang.String)
     */
    @Override
    public KomodoObject getChild( final UnitOfWork transaction,
                                  final String name ) throws KException {
        // check Vdbs
        KomodoObject[] kids = getVdbs( transaction, name );

        if ( kids.length != 0 ) {
            return kids[ 0 ];
        }

        // child does not exist
        throw new KException( Messages.getString( org.komodo.repository.Messages.Komodo.CHILD_NOT_FOUND,
                                                  name,
                                                  getAbsolutePath() ) );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.internal.RelationalObjectImpl#getChild(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String, java.lang.String)
     */
    @Override
    public KomodoObject getChild( final UnitOfWork transaction,
                                  final String name,
                                  final String typeName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state must be NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( name, "name" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( typeName, "typeName" ); //$NON-NLS-1$

        if ( VdbLexicon.Vdb.VIRTUAL_DATABASE.equals( typeName ) ) {
            final KomodoObject[] vdbs = getVdbs( transaction, name );

            if ( vdbs.length != 0 ) {
                return vdbs[ 0 ];
            }
        }

        // child does not exist
        throw new KException( Messages.getString( org.komodo.repository.Messages.Komodo.CHILD_NOT_FOUND,
                                                  name,
                                                  getAbsolutePath() ) );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.internal.RelationalObjectImpl#getChildren(org.komodo.spi.repository.Repository.UnitOfWork,
     *      java.lang.String[])
     */
    @Override
    public KomodoObject[] getChildren( final UnitOfWork transaction,
                                       final String... namePatterns ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        final Vdb[] vdbs = getVdbs( transaction, namePatterns );

        final KomodoObject[] result = new KomodoObject[ vdbs.length ];
        System.arraycopy( vdbs, 0, result, 0, vdbs.length );

        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.internal.RelationalObjectImpl#getChildrenOfType(org.komodo.spi.repository.Repository.UnitOfWork,
     *      java.lang.String, java.lang.String[])
     */
    @Override
    public KomodoObject[] getChildrenOfType( final UnitOfWork transaction,
                                             final String type,
                                             final String... namePatterns ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        KomodoObject[] result = null;

        if ( VdbLexicon.Vdb.VIRTUAL_DATABASE.equals( type ) ) {
            result = getVdbs( transaction, namePatterns );
        } else {
            result = super.getChildrenOfType(transaction, type, namePatterns);
        }

        return result;
    }
    
    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.internal.RelationalObjectImpl#hasChild(org.komodo.spi.repository.Repository.UnitOfWork,
     *      java.lang.String)
     */
    @Override
    public boolean hasChild( final UnitOfWork transaction,
                             final String name ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state must be NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( name, "name" ); //$NON-NLS-1$

        return ( ( getVdbs( transaction, name ).length != 0 ) );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.internal.RelationalObjectImpl#hasChild(org.komodo.spi.repository.Repository.UnitOfWork,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public boolean hasChild( final UnitOfWork transaction,
                             final String name,
                             final String typeName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state must be NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( name, "name" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( typeName, "typeName" ); //$NON-NLS-1$

        if ( VdbLexicon.Vdb.VIRTUAL_DATABASE.equals( typeName ) ) {
            return ( getVdbs( transaction, name ).length != 0 );
        }

        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.internal.RelationalObjectImpl#hasChildren(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public boolean hasChildren( final UnitOfWork transaction ) throws KException {
        if ( super.hasChildren( transaction ) ) {
            return ( ( getVdbs( transaction ).length != 0 ) );
        }

        return false;
    }
    
    @Override
    public Vdb addVdb( final UnitOfWork uow,
                       final String vdbName,
                       final String externalFilePath ) throws KException {
        return RelationalModelFactory.createVdb( uow, getRepository(), this.getAbsolutePath(), vdbName, externalFilePath );
    }

    @Override
    public Vdb[] getVdbs( final UnitOfWork transaction,
                          final String... namePatterns ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        final List< Vdb > result = new ArrayList< Vdb >();

        for ( final KomodoObject kobject : super.getChildrenOfType( transaction, VdbLexicon.Vdb.VIRTUAL_DATABASE, namePatterns ) ) {
            final Vdb vdb = new VdbImpl( transaction, getRepository(), kobject.getAbsolutePath() );
            result.add( vdb );
        }

        if ( result.isEmpty() ) {
            return Vdb.NO_VDBS;
        }

        return result.toArray( new Vdb[ result.size() ] );
    }

    /* (non-Javadoc)
     * @see org.komodo.relational.dataservice.Dataservice#getServiceVdb(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public Vdb getServiceVdb(UnitOfWork uow) throws KException {
        Vdb[] vdbs = getVdbs(uow, getName(uow));
        if(vdbs.length==0) {
            return null;
        }
        return vdbs[0];
    }
    
    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.dataservice.Dataservice#getDescription(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public String getDescription( final UnitOfWork uow ) throws KException {
        return getObjectProperty(uow, PropertyValueType.STRING, "getDescription", KomodoLexicon.LibraryComponent.DESCRIPTION); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.dataservice.Dataservice#setDescription(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public void setDescription( final UnitOfWork uow,
                                final String newDescription ) throws KException {
        setObjectProperty(uow, "setDescription", KomodoLexicon.LibraryComponent.DESCRIPTION, newDescription); //$NON-NLS-1$
    }


}
