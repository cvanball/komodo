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
package org.komodo.rest.relational;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.jboss.resteasy.test.TestPortProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.komodo.core.KEngine;
import org.komodo.repository.SynchronousCallback;
import org.komodo.repository.search.ComparisonOperator;
import org.komodo.repository.search.ObjectSearcher;
import org.komodo.rest.KomodoRestV1Application;
import org.komodo.rest.KomodoRestV1Application.V1Constants;
import org.komodo.rest.RestLink;
import org.komodo.rest.RestLink.LinkType;
import org.komodo.rest.RestProperty;
import org.komodo.rest.relational.response.RestVdb;
import org.komodo.spi.constants.SystemConstants;
import org.komodo.spi.repository.KomodoType;
import org.komodo.spi.repository.Repository;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.test.utils.TestUtilities;
import org.modeshape.jcr.ModeShapeLexicon;
import org.teiid.modeshape.sequencer.ddl.TeiidDdlLexicon;
import org.teiid.modeshape.sequencer.vdb.lexicon.VdbLexicon;

/**
 *
 */
@SuppressWarnings( {"nls", "javadoc"} )
public abstract class AbstractKomodoServiceTest implements V1Constants {

    private static Path _kengineDataDir;
    protected static KomodoRestV1Application _restApp;
    protected static TJWSEmbeddedJaxrsServer _server;
    protected static KomodoRestUriBuilder _uriBuilder;
    private static URI _appUri;

    @AfterClass
    public static void afterAll() throws Exception {
        if (_server != null)
            _server.stop();

        if (_restApp != null)
            _restApp.stop();

        //
        // Allow other instances of the KomodoRestV1Application to be deployed
        // with a clean komodo engine by destroying the current static instance
        // loaded from these tests
        //
        Field instanceField = KEngine.class.getDeclaredField("_instance");
        instanceField.setAccessible(true);
        instanceField.set(KEngine.class, null);

        //
        // Remove the temp repository
        //
        Files.walkFileTree(_kengineDataDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                try {
                    Files.delete(file);
                } catch (Exception ex) {
                    file.toFile().deleteOnExit();
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                try {
                    Files.delete(dir);
                } catch (Exception ex) {
                    dir.toFile().deleteOnExit();
                }
                return FileVisitResult.CONTINUE;
            }
        });

        try {
            Files.deleteIfExists(_kengineDataDir);
        } catch (Exception ex) {
            _kengineDataDir.toFile().deleteOnExit();
        }
    }

    @BeforeClass
    public static void beforeAll() throws Exception {
        _kengineDataDir = Files.createTempDirectory(null, new FileAttribute[0]);
        System.setProperty(SystemConstants.ENGINE_DATA_DIR, _kengineDataDir.toString());

        _server = new TJWSEmbeddedJaxrsServer();        
        _server.setPort(TestPortProvider.getPort());

        _restApp = new KomodoRestV1Application();
        _server.getDeployment().setApplication(_restApp);
        _server.start();

        final URI baseUri = URI.create(TestPortProvider.generateBaseUrl());
        //
        // Note this lacks the /v1 context since the embedded server does not
        // seem to detect context from the application
        //
        _appUri = UriBuilder.fromUri(baseUri).build();
        _uriBuilder = new KomodoRestUriBuilder(_appUri);
    }

    /**
     *
     */
    public AbstractKomodoServiceTest() {
        super();
    }

    @After
    public void afterEach() throws Exception {
        _restApp.clearRepository();
    }

    @Before
    public void beforeEach() {
    }

    protected KomodoRestV1Application getRestApp() {
        return _restApp;
    }

    protected void loadVdbs() throws Exception {
        _restApp.importVdb(TestUtilities.allElementsExample());
        _restApp.importVdb(TestUtilities.portfolioExample());
        _restApp.importVdb(TestUtilities.partsExample());
        _restApp.importVdb(TestUtilities.tweetExample());

        Assert.assertEquals(4, _restApp.getVdbs().length);
    }

    protected void createDataservice( String serviceName ) throws Exception {
        _restApp.createDataservice(serviceName, false);

        Assert.assertEquals(1, _restApp.getDataservices().length);
    }

    protected List<String> loadSampleSearches() throws Exception {
        List<String> searchNames = new ArrayList<>();
        Repository repository = _restApp.getDefaultRepository();

        final SynchronousCallback callback = new SynchronousCallback();
        UnitOfWork uow = repository.createTransaction(
                                                      getClass().getSimpleName() + COLON + "SaveSearchInWorkspace" + COLON + System.currentTimeMillis(),
                                                      false, callback);

        ObjectSearcher vdbsSearch = new ObjectSearcher(repository);
        vdbsSearch.addFromType(VdbLexicon.Vdb.VIRTUAL_DATABASE, "vdbs");
        String vdbSearchName = "Vdbs Search";
        vdbsSearch.write(uow, vdbSearchName);

        ObjectSearcher columnsSearch = new ObjectSearcher(repository);
        columnsSearch.addFromType(TeiidDdlLexicon.CreateTable.TABLE_ELEMENT, "c");
        String columnSearchName = "Columns Search";
        columnsSearch.write(uow, columnSearchName);

        ObjectSearcher columnsWithParamSearch = new ObjectSearcher(repository);
        columnsWithParamSearch.addFromType(TeiidDdlLexicon.CreateTable.TABLE_ELEMENT, "c");
        columnsWithParamSearch.addWhereCompareClause(null, "c", ModeShapeLexicon.LOCALNAME.getString(), ComparisonOperator.LIKE, "{valueParam}");
        String columnsWithParamSearchName = "Columns Search With Where Parameter";
        columnsWithParamSearch.write(uow, columnsWithParamSearchName);

        ObjectSearcher fromParameterSearch = new ObjectSearcher(repository);
        fromParameterSearch.addFromType("{fromTypeParam}", "c");
        String fromParamSearchName = "From Parameter Search";
        fromParameterSearch.write(uow, fromParamSearchName);

        uow.commit();

        if (!callback.await(3, TimeUnit.MINUTES)) {
            throw new Exception("Timed out while loading saved searches");
        }

        if (callback.error() != null)
            throw new Exception(callback.error());

        searchNames.add(vdbSearchName);
        searchNames.add(columnSearchName);
        searchNames.add(columnsWithParamSearchName);
        searchNames.add(fromParamSearchName);

        return searchNames;
    }

    protected ClientRequest request(final URI uri, MediaType type) {
        ClientRequest request = new ClientRequest(uri.toString());
        if (type != null)
            request.accept(type);

        return request;
    }

    protected void addJsonConsumeContentType(ClientRequest request) {
        //
        // Have to add this as the REST operation has a @Consumes annotation
        //
        request.header("Content-Type", MediaType.APPLICATION_JSON);
    }

    protected void addXmlConsumeContentType(ClientRequest request) {
        //
        // Have to add this as the REST operation has a @Consumes annotation
        //
        request.header("Content-Type", MediaType.APPLICATION_XML);
    }

    protected void addBody(ClientRequest request, Object data) {
        request.body(MediaType.APPLICATION_JSON_TYPE, data);
    }

    protected void addHeader(ClientRequest request, String name, Object value) {
        request.getHeadersAsObjects().add(name, value);
    }

    protected void assertPortfolio(RestVdb vdb) {
        assertEquals("/tko:komodo/tko:workspace/Portfolio", vdb.getDataPath());
        assertEquals(KomodoType.VDB, vdb.getkType());
        assertTrue(vdb.hasChildren());
        assertEquals(TestUtilities.PORTFOLIO_VDB_NAME, vdb.getName());
        assertEquals("The Portfolio Dynamic VDB", vdb.getDescription());
        assertEquals("/tko:komodo/tko:workspace/Portfolio", vdb.getOriginalFilePath());
        assertFalse(vdb.isPreview());
        assertEquals("BY_VERSION", vdb.getConnectionType());
        assertEquals(1, vdb.getVersion());

        List<RestProperty> properties = vdb.getProperties();
        assertEquals(1, properties.size());
        RestProperty property = properties.iterator().next();
        assertEquals("UseConnectorMetadata", property.getName());
        assertEquals("true", property.getValue());

        Collection<RestLink> links = vdb.getLinks();
        assertEquals(7, links.size());

        int linkCounter = 0;
        for (RestLink link : links) {
            String href = link.getHref().toString();

            if (link.getRel().equals(LinkType.SELF)) {
                linkCounter++;
                assertTrue(href.startsWith(_appUri.toString() + "/workspace/vdbs"));
                assertTrue(href.endsWith(TestUtilities.PORTFOLIO_VDB_NAME));
            } else if (link.getRel().equals(LinkType.PARENT)) {
                linkCounter++;
                assertTrue(href.startsWith(_appUri.toString() + "/workspace/vdbs"));
            } else if (link.getRel().equals(LinkType.CHILDREN)) {
                linkCounter++;
                assertTrue(href.startsWith(_appUri.toString() + "/workspace/search"));
            } else {
                assertTrue(href.startsWith(_appUri.toString() + "/workspace/vdbs"));

                String suffixPrefix = TestUtilities.PORTFOLIO_VDB_NAME + FORWARD_SLASH;

                if (link.getRel().equals(LinkType.IMPORTS)) {
                    linkCounter++;
                    assertTrue(href.endsWith(suffixPrefix + LinkType.IMPORTS.uriName()));
                } else if (link.getRel().equals(LinkType.MODELS)) {
                    linkCounter++;
                    assertTrue(href.endsWith(suffixPrefix + LinkType.MODELS.uriName()));
                } else if (link.getRel().equals(LinkType.TRANSLATORS)) {
                    linkCounter++;
                    assertTrue(href.endsWith(suffixPrefix + LinkType.TRANSLATORS.uriName()));
                } else if (link.getRel().equals(LinkType.DATA_ROLES)) {
                    linkCounter++;
                    assertTrue(href.endsWith(suffixPrefix + LinkType.DATA_ROLES.uriName()));
                }
            }
        }

        assertEquals(7, linkCounter);
    }

}
