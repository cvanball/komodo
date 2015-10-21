/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.komodo.relational.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileWriter;
import org.junit.Test;
import org.komodo.relational.model.Schema;
import org.komodo.relational.workspace.WorkspaceManager;
import org.komodo.shell.CompletionConstants;
import org.komodo.shell.api.Arguments;
import org.komodo.shell.api.CommandResult;

/**
 * Test Class to test CreateSchemaCommand
 *
 */
@SuppressWarnings("javadoc")
public class CreateSchemaCommandTest extends AbstractCommandTest {

    /**
	 * Test for CreateSchemaCommand
	 */
	public CreateSchemaCommandTest( ) {
		super();
	}

    @Test
    public void testCreateSchema1() throws Exception {
        File cmdFile = File.createTempFile("TestCommand", ".txt");  //$NON-NLS-1$  //$NON-NLS-2$
        cmdFile.deleteOnExit();
        
        FileWriter writer = new FileWriter(cmdFile);
        writer.write("workspace" + NEW_LINE);  //$NON-NLS-1$
        writer.write("create-schema testSchema" + NEW_LINE);  //$NON-NLS-1$
        writer.close();
        
    	setup(cmdFile.getAbsolutePath(), CreateSchemaCommand.class);

        CommandResult result = execute();
        assertCommandResultOk(result);
    	
    	WorkspaceManager wkspMgr = WorkspaceManager.getInstance(_repo);
    	Schema[] schemas = wkspMgr.findSchemas(uow);
    	
    	assertEquals(1, schemas.length);
    	assertEquals("testSchema", schemas[0].getName(uow)); //$NON-NLS-1$
    }
    
    @Test
    public void shouldDisplayHelp( ) throws Exception {
        setup(CreateSchemaCommand.class);
        
        CreateSchemaCommand command = new CreateSchemaCommand(wsStatus);
        command.setWriter( this.commandWriter );
        command.printHelp(CompletionConstants.MESSAGE_INDENT);
        
        String writerOutput = getCommandOutput();
        assertTrue(writerOutput.contains("DESCRIPTION")); //$NON-NLS-1$
        assertTrue(writerOutput.contains(CreateSchemaCommand.NAME));
    }
    
    @Test
    public void shouldFailTooManyArgs( ) throws Exception {
        setup(CreateSchemaCommand.class);
        
        CreateSchemaCommand command = new CreateSchemaCommand(wsStatus);
        command.setArguments(new Arguments( "aName anExtraArg" ));  //$NON-NLS-1$
        CommandResult result = command.execute();
        
        assertFalse(result.isOk());
        assertTrue(result.getMessage().contains("Too many arguments were used for the command"));  //$NON-NLS-1$
    }

}
