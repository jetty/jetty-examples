//
// ========================================================================
// Copyright (c) 1995 Mort Bay Consulting Pty Ltd and others.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v. 2.0 which is available at
// https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
// which is available at https://www.apache.org/licenses/LICENSE-2.0.
//
// SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
// ========================================================================
//

package examples;

import java.util.Collections;
import java.util.List;
import javax.websocket.Extension;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

import org.eclipse.jetty.websocket.api.util.QuoteUtil;

public class JakartaBrowserConfigurator extends ServerEndpointConfig.Configurator
{
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response)
    {
        super.modifyHandshake(sec, request, response);
        sec.getUserProperties().put("userAgent", getHeaderValue(request, "User-Agent"));
        sec.getUserProperties().put("requestedExtensions", getHeaderValue(request, "Sec-WebSocket-Extensions"));
    }

    private String getHeaderValue(HandshakeRequest request, String key)
    {
        List<String> value = request.getHeaders().get(key);
        return QuoteUtil.join(value, ",");
    }

    @Override
    public List<Extension> getNegotiatedExtensions(List<Extension> installed, List<Extension> requested)
    {
        return Collections.emptyList();
    }
}
