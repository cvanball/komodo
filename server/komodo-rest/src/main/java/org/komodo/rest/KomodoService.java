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
package org.komodo.rest;

import static org.komodo.rest.Messages.Error.COMMIT_TIMEOUT;
import static org.komodo.rest.Messages.Error.RESOURCE_NOT_FOUND;
import static org.komodo.rest.Messages.General.GET_OPERATION_NAME;
import java.io.StringWriter;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Variant.VariantListBuilder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import org.komodo.core.KEngine;
import org.komodo.core.KomodoLexicon;
import org.komodo.relational.dataservice.Dataservice;
import org.komodo.relational.vdb.Vdb;
import org.komodo.relational.workspace.WorkspaceManager;
import org.komodo.repository.SynchronousCallback;
import org.komodo.rest.KomodoRestV1Application.V1Constants;
import org.komodo.rest.RestBasicEntity.ResourceNotFound;
import org.komodo.rest.relational.RelationalMessages;
import org.komodo.rest.relational.RestEntityFactory;
import org.komodo.rest.relational.json.KomodoJsonMarshaller;
import org.komodo.spi.KException;
import org.komodo.spi.repository.KomodoObject;
import org.komodo.spi.repository.Repository;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.spi.repository.Repository.UnitOfWorkListener;
import org.komodo.utils.KLog;
import org.komodo.utils.StringUtils;
import org.teiid.modeshape.sequencer.vdb.lexicon.VdbLexicon;
import com.google.gson.Gson;

/**
 * A Komodo service implementation.
 */
public abstract class KomodoService implements V1Constants {

    protected static final KLog LOGGER = KLog.getLogger();

    private static final int TIMEOUT = 30;
    private static final TimeUnit UNIT = TimeUnit.SECONDS;

    private static final String HTML_NEW_LINE = "<br/>"; //$NON-NLS-1$

    /**
     * Query parameter keys used by the service methods.
     */
    public interface QueryParamKeys {

        /**
         * A regex expression used when searching. If not present, all objects are returned.
         */
        String PATTERN = "pattern"; //$NON-NLS-1$

        /**
         * The number of objects to return. If not present, all objects are returned.
         */
        String SIZE = "size"; //$NON-NLS-1$

        /**
         * The index of the first object to return. Defaults to zero.
         */
        String START = "start"; //$NON-NLS-1$

        /**
         * The Komodo Type required.
         */
        String KTYPE = "ktype"; //$NON-NLS-1$
    }

    private class ErrorResponse {
        private final String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        @SuppressWarnings( "unused" )
        public String getError() {
            return error;
        }
    }

    protected final Repository repo;

    protected WorkspaceManager wsMgr;

    protected RestEntityFactory entityFactory = new RestEntityFactory();

    /**
     * Constructs a Komodo service.
     *
     * @param engine
     *        the Komodo Engine (cannot be <code>null</code> and must be started)
     */
    protected KomodoService( final KEngine engine ) {
        this.repo = engine.getDefaultRepository();

        try {
            this.wsMgr = WorkspaceManager.getInstance(this.repo);
        } catch (final Exception e) {
            throw new WebApplicationException(new Exception(Messages.getString(Messages.Error.KOMODO_ENGINE_WORKSPACE_MGR_ERROR)), Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * @param value the value
     * @return the value encoded for json
     */
    public static String encode(String value) {
        if (value == null)
            return null;

        value = value.replaceAll(COLON, PREFIX_SEPARATOR);
        return value;
    }

    /**
     * @param value the value
     * @return the value decoded from json transit
     */
    public static String decode(String value) {
        if (value == null)
            return null;

        value = value.replaceAll(PREFIX_SEPARATOR, COLON);
        return value;
    }

    /**
     * @param content
     * @return a base64 encrypted version of the given content
     */
    protected String encrypt(byte[] content) {
        if (content == null)
            return null;

        return Base64.getEncoder().encodeToString(content);
    }

    /**
     * @param content
     * @return a decrypted version of the given base64-encrypted content
     */
    protected byte[] decrypt(String content) {
        if (content == null)
            return null;

        return Base64.getDecoder().decode(content);
    }

    protected Object createErrorResponseEntity(List<MediaType> acceptableMediaTypes, String errorMessage) {
        Object responseEntity = null;

        if (acceptableMediaTypes.contains(MediaType.APPLICATION_JSON_TYPE)) {
            Gson gson = new Gson();
            responseEntity = gson.toJson(new ErrorResponse(errorMessage));
        } else if (acceptableMediaTypes.contains(MediaType.APPLICATION_XML_TYPE)) {
            ErrorResponse errResponse = new ErrorResponse(errorMessage);

            JAXBElement<ErrorResponse> xmlErrResponse = new JAXBElement<ErrorResponse>(
                                                                                        new QName("error"), //$NON-NLS-1$
                                                                                        ErrorResponse.class,
                                                                                        errResponse);

            try {
                JAXBContext context = JAXBContext.newInstance(ErrorResponse.class);
                StringWriter writer = new StringWriter();
                Marshaller m = context.createMarshaller();
                m.marshal(xmlErrResponse, writer);

                responseEntity = writer.toString();
            } catch (Exception ex) {
                // String failed to marshall - return as plain text
                responseEntity = errorMessage;
            }
        } else
            responseEntity = errorMessage;

        return responseEntity;
    }

    protected Response createErrorResponse(List<MediaType> mediaTypes, Throwable ex,
                                                                       RelationalMessages.Error errorType, Object... errorMsgInputs) {
        String errorMsg = ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : ex.getClass().getSimpleName();

        StringBuffer buf = new StringBuffer(errorMsg).append(HTML_NEW_LINE);
        String stackTrace = StringUtils.exceptionToString(ex);
        // Allow sensible formatting in a browser by replacing newline characters
        stackTrace = stackTrace.replaceAll(NEW_LINE, HTML_NEW_LINE);
        buf.append(stackTrace).append(HTML_NEW_LINE);

        String resultMsg = null;
        if (errorMsgInputs == null || errorMsgInputs.length == 0)
            resultMsg = RelationalMessages.getString(errorType, buf.toString());
        else
            resultMsg = RelationalMessages.getString(errorType, errorMsgInputs, buf.toString());

        Object responseEntity = createErrorResponseEntity(mediaTypes, resultMsg);
        return Response.status(Status.FORBIDDEN).entity(responseEntity).build();
    }

    protected ResponseBuilder notAcceptableMediaTypesBuilder() {
        List<Variant> variants = VariantListBuilder.newInstance()
                                                                   .mediaTypes(MediaType.APPLICATION_XML_TYPE,
                                                                                       MediaType.APPLICATION_JSON_TYPE)
                                                                   .build();

        return Response.notAcceptable(variants);
    }

    protected boolean isAcceptable(List<MediaType> acceptableTypes, MediaType candidate) {
        if (acceptableTypes == null || acceptableTypes.isEmpty())
            return false;

        if (candidate == null)
            return false;

        for (MediaType acceptableType : acceptableTypes) {
            if (candidate.isCompatible(acceptableType))
                return true;
        }

        return false;
    }

    protected Response commit( final UnitOfWork transaction, List<MediaType> acceptableMediaTypes,
                               final KRestEntity entity ) throws Exception {
        assert( transaction.getCallback() instanceof SynchronousCallback );
        final int timeout = TIMEOUT;
        final TimeUnit unit = UNIT;

        final SynchronousCallback callback = ( SynchronousCallback )transaction.getCallback();
        transaction.commit();

        if ( !callback.await( timeout, unit ) ) {
            // callback timeout occurred
            String errorMessage = Messages.getString( COMMIT_TIMEOUT, transaction.getName(), timeout, unit );
            Object responseEntity = createErrorResponseEntity(acceptableMediaTypes, errorMessage);
            return Response.status( Status.INTERNAL_SERVER_ERROR )
                           .entity(responseEntity)
                           .build();
        }

        Throwable error = callback.error();

        if ( error != null ) {
            // callback was called because of an error condition
            Object responseEntity = createErrorResponseEntity(acceptableMediaTypes, error.getLocalizedMessage());
            return Response.status( Status.INTERNAL_SERVER_ERROR )
                .entity(responseEntity)
                .build();
        }

        LOGGER.debug( "commit: successfully committed '{0}', rollbackOnly = '{1}'", //$NON-NLS-1$
                      transaction.getName(),
                      transaction.isRollbackOnly() );
        ResponseBuilder builder = null;

        if ( entity == RestBasicEntity.NO_CONTENT ) {
            builder = Response.noContent();
        } else if ( entity instanceof ResourceNotFound ) {
            final ResourceNotFound resourceNotFound = ( ResourceNotFound )entity;

            String notFoundMsg = Messages.getString( RESOURCE_NOT_FOUND,
                                                     resourceNotFound.getResourceName(),
                                                     resourceNotFound.getOperationName() );
            Object responseEntity = createErrorResponseEntity(acceptableMediaTypes, notFoundMsg);
            builder = Response.status( Status.NOT_FOUND ).entity(responseEntity);
        } else {

            //
            // Json will always be preferred over XML if both or the wildcard are present in the header
            //
            if (isAcceptable(acceptableMediaTypes, MediaType.APPLICATION_JSON_TYPE))
                builder = Response.ok( KomodoJsonMarshaller.marshall( entity ), MediaType.APPLICATION_JSON );
            else if (isAcceptable(acceptableMediaTypes, MediaType.APPLICATION_XML_TYPE) && entity.supports(MediaType.APPLICATION_XML_TYPE))
                builder = Response.ok( entity.getXml(), MediaType.APPLICATION_XML );
            else {
                builder = notAcceptableMediaTypesBuilder();
            }
        }

        return builder.build();
    }

    protected Response commit(UnitOfWork transaction, List<MediaType> acceptableMediaTypes) throws Exception {
        assert( transaction.getCallback() instanceof SynchronousCallback );
        final int timeout = TIMEOUT;
        final TimeUnit unit = UNIT;

        final SynchronousCallback callback = ( SynchronousCallback )transaction.getCallback();
        transaction.commit();

        if ( ! callback.await( timeout, unit ) ) {
            // callback timeout occurred
            String errorMessage = Messages.getString( COMMIT_TIMEOUT, transaction.getName(), timeout, unit );
            Object responseEntity = createErrorResponseEntity(acceptableMediaTypes, errorMessage);
            return Response.status( Status.INTERNAL_SERVER_ERROR )
                           .type( MediaType.TEXT_PLAIN )
                           .entity(responseEntity)
                           .build();
        }

        final Throwable error = callback.error();

        if ( error != null ) {
         // callback was called because of an error condition
            Object responseEntity = createErrorResponseEntity(acceptableMediaTypes, error.getLocalizedMessage());
            return Response.status( Status.INTERNAL_SERVER_ERROR )
                           .entity(responseEntity)
                           .build();
        }

        return Response.ok().build();
    }

    protected Response commit( final UnitOfWork transaction, List<MediaType> acceptableMediaTypes,
                               final List<? extends KRestEntity> entities ) throws Exception {

        commit(transaction, acceptableMediaTypes);

        LOGGER.debug( "commit: successfully committed '{0}', rollbackOnly = '{1}'", //$NON-NLS-1$
                      transaction.getName(),
                      transaction.isRollbackOnly() );
        ResponseBuilder builder = null;

        KRestEntity entity;
        if ( entities.size() == 1 && (entity = entities.iterator().next()) instanceof ResourceNotFound ) {
            final ResourceNotFound resourceNotFound = ( ResourceNotFound )entity;

            String notFoundMessage = Messages.getString( RESOURCE_NOT_FOUND,
                                                         resourceNotFound.getResourceName(),
                                                         resourceNotFound.getOperationName() );
            Object responseEntity = createErrorResponseEntity(acceptableMediaTypes, notFoundMessage);
            builder = Response.status( Status.NOT_FOUND ).entity(responseEntity);
        } else {

            if (isAcceptable(acceptableMediaTypes, MediaType.APPLICATION_JSON_TYPE))
                builder = Response.ok( KomodoJsonMarshaller.marshallArray(entities.toArray(new KRestEntity[0]), true), MediaType.APPLICATION_JSON );
            else {
                builder = notAcceptableMediaTypesBuilder();
            }
        }

        return builder.build();
    }

    /**
     * @param name
     *        the name of the transaction (cannot be empty)
     * @param rollbackOnly
     *        <code>true</code> if transaction must be rolled back
     * @param callback the callback to fire when the transaction is committed
     * @return the new transaction (never <code>null</code>)
     * @throws KException
     *         if there is an error creating the transaction
     */
    protected UnitOfWork createTransaction( final String name,
                                            final boolean rollbackOnly, final UnitOfWorkListener callback) throws KException {
        final UnitOfWork result = this.repo.createTransaction( ( getClass().getSimpleName() + COLON + name + COLON
                                                                 + System.currentTimeMillis() ),
                                                               rollbackOnly, callback );
        LOGGER.debug( "createTransaction:created '{0}', rollbackOnly = '{1}'", result.getName(), result.isRollbackOnly() ); //$NON-NLS-1$
        return result;
    }

    /**
     * @param name
     *        the name of the transaction (cannot be empty)
     * @param rollbackOnly
     *        <code>true</code> if transaction must be rolled back
     * @return the new transaction (never <code>null</code>)
     * @throws KException
     *         if there is an error creating the transaction
     */
    protected UnitOfWork createTransaction( final String name,
                                            final boolean rollbackOnly ) throws KException {
        final SynchronousCallback callback = new SynchronousCallback();
        final UnitOfWork result = this.repo.createTransaction( ( getClass().getSimpleName() + COLON + name + COLON
                                                                 + System.currentTimeMillis() ),
                                                               rollbackOnly, callback );
        LOGGER.debug( "createTransaction:created '{0}', rollbackOnly = '{1}'", result.getName(), result.isRollbackOnly() ); //$NON-NLS-1$
        return result;
    }

    protected Vdb findVdb(UnitOfWork uow, String vdbName) throws KException {
        if (! this.wsMgr.hasChild( uow, vdbName, VdbLexicon.Vdb.VIRTUAL_DATABASE ) ) {
            return null;
        }

        final KomodoObject kobject = this.wsMgr.getChild( uow, vdbName, VdbLexicon.Vdb.VIRTUAL_DATABASE );
        final Vdb vdb = this.wsMgr.resolve( uow, kobject, Vdb.class );

        LOGGER.debug( "VDB '{0}' was found", vdbName ); //$NON-NLS-1$
        return vdb;
    }

    protected Dataservice findDataservice(UnitOfWork uow, String dataserviceName) throws KException {
        if (! this.wsMgr.hasChild( uow, dataserviceName, KomodoLexicon.DataService.NODE_TYPE ) ) {
            return null;
        }

        final KomodoObject kobject = this.wsMgr.getChild( uow, dataserviceName, KomodoLexicon.DataService.NODE_TYPE );
        final Dataservice dataservice = this.wsMgr.resolve( uow, kobject, Dataservice.class );

        LOGGER.debug( "Dataservice '{0}' was found", dataserviceName ); //$NON-NLS-1$
        return dataservice;
    }

    protected String uri(String... segments) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < segments.length; ++i) {
            buffer.append(segments[i]);
            if (i < (segments.length - 1))
                buffer.append(FORWARD_SLASH);
        }

        return buffer.toString();
    }

    protected Response commitNoVdbFound(UnitOfWork uow, List<MediaType> mediaTypes, String vdbName) throws Exception {
        LOGGER.debug( "VDB '{0}' was not found", vdbName ); //$NON-NLS-1$
        return commit( uow, mediaTypes, new ResourceNotFound( vdbName, Messages.getString( GET_OPERATION_NAME ) ) );
    }

    protected Response commitNoDataserviceFound(UnitOfWork uow, List<MediaType> mediaTypes, String dataserviceName) throws Exception {
        LOGGER.debug( "Dataservice '{0}' was not found", dataserviceName ); //$NON-NLS-1$
        return commit( uow, mediaTypes, new ResourceNotFound( dataserviceName, Messages.getString( GET_OPERATION_NAME ) ) );
    }

    protected Response commitNoModelFound(UnitOfWork uow, List<MediaType> mediaTypes, String modelName, String vdbName) throws Exception {
        return commit(uow, mediaTypes,
                      new ResourceNotFound(uri(vdbName, MODELS_SEGMENT, modelName),
                                           Messages.getString( GET_OPERATION_NAME)));
    }

    protected Response commitNoDataRoleFound(UnitOfWork uow, List<MediaType> mediaTypes, String dataRoleId, String vdbName) throws Exception {
        LOGGER.debug("No data role '{0}' found for vdb '{1}'", dataRoleId, vdbName); //$NON-NLS-1$
        return commit(uow, mediaTypes, new ResourceNotFound(
                                                     uri(vdbName, DATA_ROLES_SEGMENT, dataRoleId),
                                                     Messages.getString( GET_OPERATION_NAME)));
    }

    protected Response commitNoPermissionFound(UnitOfWork uow, List<MediaType> mediaTypes, String permissionId, String dataRoleId, String vdbName) throws Exception {
        LOGGER.debug("No permission '{0}' for data role '{1}' found for vdb '{2}'", //$NON-NLS-1$
                                                                     permissionId, dataRoleId, vdbName);
        return commit(uow, mediaTypes, new ResourceNotFound(
                                                     uri(vdbName, DATA_ROLES_SEGMENT,
                                                          dataRoleId, PERMISSIONS_SEGMENT,
                                                          permissionId),
                                                     Messages.getString( GET_OPERATION_NAME)));
    }
}
