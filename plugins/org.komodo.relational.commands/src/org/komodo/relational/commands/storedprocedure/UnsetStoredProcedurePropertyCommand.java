/*
 * JBoss, Home of Professional Open Source.
 *
 * See the LEGAL.txt file distributed with this work for information regarding copyright ownership and licensing.
 *
 * See the AUTHORS.txt file distributed with this work for a full listing of individual contributors.
 */
package org.komodo.relational.commands.storedprocedure;

import static org.komodo.relational.commands.workspace.WorkspaceCommandMessages.General.INVALID_PROPERTY_NAME;
import static org.komodo.relational.commands.workspace.WorkspaceCommandMessages.General.UNSET_MISSING_PROPERTY_NAME;
import static org.komodo.relational.commands.workspace.WorkspaceCommandMessages.General.UNSET_PROPERTY_SUCCESS;
import java.util.List;
import org.komodo.relational.model.AbstractProcedure;
import org.komodo.relational.model.StoredProcedure;
import org.komodo.shell.CommandResultImpl;
import org.komodo.shell.api.Arguments;
import org.komodo.shell.api.CommandResult;
import org.komodo.shell.api.WorkspaceStatus;
import org.komodo.shell.commands.UnsetPropertyCommand;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.utils.StringUtils;

/**
 * A shell command to unset {@link StoredProcedure stored procedure} properties.
 */
public final class UnsetStoredProcedurePropertyCommand extends StoredProcedureShellCommand {

    static final String NAME = UnsetPropertyCommand.NAME;

    /**
     * @param status
     *        the shell's workspace status (cannot be <code>null</code>)
     */
    public UnsetStoredProcedurePropertyCommand( final WorkspaceStatus status ) {
        super( NAME, status );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.shell.BuiltInShellCommand#doExecute()
     */
    @Override
    protected CommandResult doExecute() {
        CommandResult result = null;

        try {
            final String name = requiredArgument( 0, getWorkspaceMessage( UNSET_MISSING_PROPERTY_NAME ) );

            final StoredProcedure proc = getStoredProcedure();
            final UnitOfWork transaction = getTransaction();
            String errorMsg = null;

            switch ( name ) {
                case DESCRIPTION:
                    proc.setDescription( getTransaction(), null );
                    break;
                case NAME_IN_SOURCE:
                    proc.setNameInSource( getTransaction(), null );
                    break;
                case NATIVE_QUERY:
                    proc.setNativeQuery( transaction, null );
                    break;
                case NON_PREPARED:
                    proc.setNonPrepared( transaction, StoredProcedure.DEFAULT_NON_PREPARED );
                    break;
                case SCHEMA_ELEMENT_TYPE:
                    proc.setSchemaElementType( transaction, null );
                    break;
                case UPDATE_COUNT:
                    proc.setUpdateCount( transaction, AbstractProcedure.DEFAULT_UPDATE_COUNT );
                    break;
                case UUID:
                    proc.setUuid( getTransaction(), null );
                    break;
                default:
                    errorMsg = getWorkspaceMessage( INVALID_PROPERTY_NAME, name, StoredProcedure.class.getSimpleName() );
                    break;
            }

            if ( StringUtils.isBlank( errorMsg ) ) {
                result = new CommandResultImpl( getWorkspaceMessage( UNSET_PROPERTY_SUCCESS, name ) );
            } else {
                result = new CommandResultImpl( false, errorMsg, null );
            }
        } catch ( final Exception e ) {
            result = new CommandResultImpl( e );
        }

        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.shell.BuiltInShellCommand#getMaxArgCount()
     */
    @Override
    protected int getMaxArgCount() {
        return 1;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.shell.BuiltInShellCommand#tabCompletion(java.lang.String, java.util.List)
     */
    @Override
    public int tabCompletion( final String lastArgument,
                              final List< CharSequence > candidates ) throws Exception {
        final Arguments args = getArguments();

        if ( args.isEmpty() ) {
            if ( lastArgument == null ) {
                candidates.addAll( ALL_PROPS );
            } else {
                for ( final String item : ALL_PROPS ) {
                    if ( item.toUpperCase().startsWith( lastArgument.toUpperCase() ) ) {
                        candidates.add( item );
                    }
                }
            }

            return 0;
        }

        // no tab completion
        return -1;
    }

}