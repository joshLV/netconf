/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.netconf.util.messages;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.controller.config.util.xml.DocumentedException;
import org.opendaylight.controller.config.util.xml.XmlElement;
import org.opendaylight.netconf.api.NetconfDocumentedException;
import org.opendaylight.netconf.api.NetconfMessage;
import org.opendaylight.netconf.api.xml.XmlNetconfConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public final class NetconfMessageUtil {

    private static final Logger LOG = LoggerFactory.getLogger(NetconfMessageUtil.class);

    private NetconfMessageUtil() {}

    public static boolean isOKMessage(NetconfMessage message) throws NetconfDocumentedException {
        return isOKMessage(message.getDocument());
    }

    public static boolean isOKMessage(Document document) throws NetconfDocumentedException {
        return isOKMessage(XmlElement.fromDomDocument(document));
    }

    public static boolean isOKMessage(XmlElement xmlElement) throws NetconfDocumentedException {
        if (xmlElement.getChildElements().size() != 1) {
            return false;
        }
        try {
            return xmlElement.getOnlyChildElement().getName().equals(XmlNetconfConstants.OK);
        } catch (DocumentedException e) {
            throw new NetconfDocumentedException(e);
        }
    }

    public static boolean isErrorMessage(NetconfMessage message) throws NetconfDocumentedException {
        return isErrorMessage(message.getDocument());
    }

    public static boolean isErrorMessage(Document document) throws NetconfDocumentedException {
        return isErrorMessage(XmlElement.fromDomDocument(document));
    }

    public static boolean isErrorMessage(XmlElement xmlElement) throws NetconfDocumentedException {
        if (xmlElement.getChildElements().size() != 1) {
            return false;
        }
        try {
            return xmlElement.getOnlyChildElement().getName().equals(DocumentedException.RPC_ERROR);
        } catch (DocumentedException e) {
            throw new NetconfDocumentedException(e);
        }
    }

    public static Collection<String> extractCapabilitiesFromHello(Document doc) throws NetconfDocumentedException {
        XmlElement responseElement = XmlElement.fromDomDocument(doc);
        // Extract child element <capabilities> from <hello> with or without(fallback) the same namespace
        Optional<XmlElement> capabilitiesElement = responseElement
                .getOnlyChildElementWithSameNamespaceOptionally(XmlNetconfConstants.CAPABILITIES)
                .or(responseElement
                        .getOnlyChildElementOptionally(XmlNetconfConstants.CAPABILITIES));

        List<XmlElement> caps = capabilitiesElement.get().getChildElements(XmlNetconfConstants.CAPABILITY);
        return Collections2.transform(caps, new Function<XmlElement, String>() {

            @Override
            public String apply(@Nonnull XmlElement input) {
                // Trim possible leading/tailing whitespace
                try {
                    return input.getTextContent().trim();
                } catch (DocumentedException e) {
                    LOG.trace("Error fetching input text content",e);
                    return null;
                }
            }
        });

    }
}
