/*
 * JBoss, Home of Professional Open Source.
 *
 * See the LEGAL.txt file distributed with this work for information regarding copyright ownership and licensing.
 *
 * See the AUTHORS.txt file distributed with this work for a full listing of individual contributors.
 */
package org.komodo.relational.commands.vdb;

import static org.komodo.relational.commands.vdb.VdbCommandMessages.DeleteDataRoleCommand.DATA_ROLE_DELETED;
import static org.komodo.relational.commands.vdb.VdbCommandMessages.General.MISSING_DATA_ROLE_NAME;
import static org.komodo.shell.CompletionConstants.MESSAGE_INDENT;
import java.util.ArrayList;
import java.util.List;
import org.komodo.relational.vdb.DataRole;
import org.komodo.relational.vdb.Vdb;
import org.komodo.shell.api.Arguments;
import org.komodo.shell.api.WorkspaceStatus;
import org.komodo.spi.repository.Repository.UnitOfWork;

/**
 * A shell command to delete a data role from a VDB.
 */
public final class DeleteDataRoleCommand extends VdbShellCommand {

    static final String NAME = "delete-data-role"; //$NON-NLS-1$

    /**
     * @param status
     *        the shell's workspace status (cannot be <code>null</code>)
     */
    public DeleteDataRoleCommand( final WorkspaceStatus status ) {
        super( NAME, true, status );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.shell.BuiltInShellCommand#doExecute()
     */
    @Override
    protected boolean doExecute() throws Exception {
        final String dataRoleName = requiredArgument( 0, getMessage(MISSING_DATA_ROLE_NAME) );

        final Vdb vdb = getVdb();
        vdb.removeDataRole( getTransaction(), dataRoleName );

        // Print success message
        print(MESSAGE_INDENT, getMessage(DATA_ROLE_DELETED,dataRoleName));
        
        return true;
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

        final UnitOfWork uow = getTransaction();
        final Vdb vdb = getVdb();
        final DataRole[] dataRoles = vdb.getDataRoles( uow );
        List<String> existingRoleNames = new ArrayList<String>(dataRoles.length);
        for(DataRole role : dataRoles) {
            existingRoleNames.add(role.getName(uow));
        }
        
        if ( args.isEmpty() ) {
            if ( lastArgument == null ) {
                candidates.addAll( existingRoleNames );
            } else {
                for ( final String item : existingRoleNames ) {
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
