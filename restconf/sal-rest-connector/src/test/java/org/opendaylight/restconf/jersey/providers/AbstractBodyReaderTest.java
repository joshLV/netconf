/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.restconf.jersey.providers;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collections;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import org.opendaylight.controller.md.sal.rest.common.TestRestconfUtils;
import org.opendaylight.netconf.sal.rest.api.RestconfConstants;
import org.opendaylight.netconf.sal.restconf.impl.ControllerContext;
import org.opendaylight.netconf.sal.restconf.impl.NormalizedNodeContext;
import org.opendaylight.netconf.sal.restconf.impl.PATCHContext;
import org.opendaylight.restconf.RestConnectorProvider;
import org.opendaylight.restconf.handlers.DOMMountPointServiceHandler;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

abstract class AbstractBodyReaderTest {

    protected final static ControllerContext controllerContext = ControllerContext.getInstance();
    protected final MediaType mediaType;
    protected final static DOMMountPointServiceHandler mountPointServiceHandler = mock(
            DOMMountPointServiceHandler.class);

    AbstractBodyReaderTest() throws NoSuchFieldException, IllegalAccessException {
        mediaType = getMediaType();

        final Field mountPointServiceHandlerField = RestConnectorProvider.class.
                getDeclaredField("mountPointServiceHandler");
        mountPointServiceHandlerField.setAccessible(true);
        mountPointServiceHandlerField.set(RestConnectorProvider.class, mountPointServiceHandler);
    }

    protected abstract MediaType getMediaType();

    protected static SchemaContext schemaContextLoader(final String yangPath,
            final SchemaContext schemaContext) {
        return TestRestconfUtils.loadSchemaContext(yangPath, schemaContext);
    }

    protected static <T extends AbstractIdentifierAwareJaxRsProvider> void mockBodyReader(
            final String identifier, final T normalizedNodeProvider,
            final boolean isPost) throws NoSuchFieldException,
            SecurityException, IllegalArgumentException, IllegalAccessException {
        final UriInfo uriInfoMock = mock(UriInfo.class);
        final MultivaluedMap<String, String> pathParm = new MultivaluedHashMap<>(1);

        if (!identifier.isEmpty()) {
            pathParm.put(RestconfConstants.IDENTIFIER, Collections.singletonList(identifier));
        }

        when(uriInfoMock.getPathParameters()).thenReturn(pathParm);
        when(uriInfoMock.getPathParameters(false)).thenReturn(pathParm);
        when(uriInfoMock.getPathParameters(true)).thenReturn(pathParm);
        normalizedNodeProvider.setUriInfo(uriInfoMock);

        final Request request = mock(Request.class);
        if (isPost) {
            when(request.getMethod()).thenReturn("POST");
        } else {
            when(request.getMethod()).thenReturn("PUT");
        }

        normalizedNodeProvider.setRequest(request);
    }

    protected static void checkNormalizedNodeContext(
            final NormalizedNodeContext nnContext) {
        assertNotNull(nnContext.getData());
        assertNotNull(nnContext.getInstanceIdentifierContext()
                .getInstanceIdentifier());
        assertNotNull(nnContext.getInstanceIdentifierContext()
                .getSchemaContext());
        assertNotNull(nnContext.getInstanceIdentifierContext().getSchemaNode());
    }

    protected static void checkPATCHContext(final PATCHContext patchContext) {
        assertNotNull(patchContext.getData());
        assertNotNull(patchContext.getInstanceIdentifierContext().getInstanceIdentifier());
        assertNotNull(patchContext.getInstanceIdentifierContext().getSchemaContext());
        assertNotNull(patchContext.getInstanceIdentifierContext().getSchemaNode());
    }

    protected static void checkPATCHContextMountPoint(final PATCHContext patchContext) {
        checkPATCHContext(patchContext);
        assertNotNull(patchContext.getInstanceIdentifierContext().getMountPoint());
        assertNotNull(patchContext.getInstanceIdentifierContext().getMountPoint().getSchemaContext());
    }
}
