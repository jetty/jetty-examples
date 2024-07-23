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

import org.eclipse.jetty.http.HttpException;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

public class LimitedRequestsWrapper extends Handler.Wrapper
{
    private int maxRequests = 10;

    public int getMaxRequests()
    {
        return maxRequests;
    }

    public void setMaxRequests(int maxRequests)
    {
        this.maxRequests = maxRequests;
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception
    {
        Response resp = response;

        // Only perform "Connection: close" on HTTP/1.1 or HTTP/1.0 (not HTTP/2 or HTTP/3)
        if (request.getConnectionMetaData().getHttpVersion().getVersion() <= 11)
        {
            long requests = request.getConnectionMetaData().getConnection().getMessagesIn();
            // After X responses, forcibly set connection close on response
            if (requests >= maxRequests)
            {
                resp = new ConnectionCloseWrapper(request, response);
            }
        }
        return super.handle(request, resp, callback);
    }

    private static class ConnectionCloseWrapper extends Response.Wrapper
    {
        private HttpFields.Mutable httpFields;

        public ConnectionCloseWrapper(Request request, Response wrapped)
        {
            super(request, wrapped);

            httpFields = new HttpFields.Mutable.Wrapper(wrapped.getHeaders())
            {
                @Override
                public HttpField onAddField(HttpField field)
                {
                    if (field.getHeader() == HttpHeader.CONNECTION)
                    {
                        if (!field.getValue().equalsIgnoreCase("close"))
                            throw new HttpException.RuntimeException(HttpStatus.INTERNAL_SERVER_ERROR_500, "Connection count exceeded, close is forced");
                    }
                    return super.onAddField(field);
                }
            };
            httpFields.put(HttpHeader.CONNECTION, "close");
        }

        @Override
        public HttpFields.Mutable getHeaders()
        {
            return httpFields;
        }
    }
}