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
package org.komodo.relational.commands.table;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.komodo.relational.commands.AbstractCommandTest;
import org.komodo.relational.model.AccessPattern;
import org.komodo.relational.model.Model;
import org.komodo.relational.model.Table;
import org.komodo.relational.vdb.Vdb;
import org.komodo.relational.workspace.WorkspaceManager;
import org.komodo.shell.api.CommandResult;

/**
 * Test Class for {@link AddAccessPatternCommand}.
 */
@SuppressWarnings( { "javadoc", "nls" } )
public final class AddAccessPatternCommandTest extends AbstractCommandTest {

    @Test
    public void testAdd1() throws Exception {
        final String[] commands = {
            "create-vdb myVdb vdbPath",
            "cd myVdb",
            "add-model myModel",
            "cd myModel",
            "add-table myTable",
            "cd myTable",
            "add-access-pattern myAccessPattern" };
        final CommandResult result = execute( commands );
        assertCommandResultOk(result);

        WorkspaceManager wkspMgr = WorkspaceManager.getInstance(_repo);
        Vdb[] vdbs = wkspMgr.findVdbs(getTransaction());

        assertEquals(1, vdbs.length);

        Model[] models = vdbs[0].getModels(getTransaction());
        assertEquals(1, models.length);
        assertEquals("myModel", models[0].getName(getTransaction()));

        Table[] tables = models[0].getTables(getTransaction());
        assertEquals(1, tables.length);
        assertEquals("myTable", tables[0].getName(getTransaction()));

        AccessPattern[] accessPatterns = tables[0].getAccessPatterns(getTransaction());
        assertEquals(1, accessPatterns.length);
        assertEquals("myAccessPattern", accessPatterns[0].getName(getTransaction()));
    }

    @Test( expected = AssertionError.class )
    public void shouldNotCreateAccessPatternWithNameThatAlreadyExists() throws Exception {
        final String cmd = "add-access-pattern myAccessPattern";
        final String[] commands = { "create-vdb myVdb vdbPath",
                                    "cd myVdb",
                                    "add-model myModel",
                                    "cd myModel",
                                    "add-table myTable",
                                    "cd myTable",
                                    cmd,
                                    cmd };

        execute( commands );
    }

}
