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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.http.MultiPart;
import org.eclipse.jetty.http.MultiPartFormData;
import org.eclipse.jetty.http.pathmap.PathSpec;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.FormFields;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.PathMappingsHandler;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.util.StringUtil;

public class FormEndpoints
{
    public static void main(String[] args) throws Exception
    {
        Server server = FormEndpoints.newServer(8080);
        server.start();
        server.join();
    }

    public static Server newServer(int port) throws IOException
    {
        Server server = new Server();

        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setFormEncodedMethods("POST");

        ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
        connector.setPort(port);
        server.addConnector(connector);

        Path workDir = Files.createTempDirectory("multipart-work");

        PathMappingsHandler pathMappingsHandler = new PathMappingsHandler();
        pathMappingsHandler.addMapping(PathSpec.from("/form/query"), new QueryHandler());
        pathMappingsHandler.addMapping(PathSpec.from("/form/urlencoded"), new UrlEncodedFormHandler());
        pathMappingsHandler.addMapping(PathSpec.from("/form/multipart"), new MultipartFormHandler(workDir));

        Handler.Sequence handlers = new Handler.Sequence();
        handlers.addHandler(pathMappingsHandler);
        handlers.addHandler(new DefaultHandler());

        server.setHandler(handlers);
        return server;
    }

    /**
     * Example of how to process HTTP Query Parameters in a Handler
     */
    public static class QueryHandler extends Handler.Abstract
    {
        @Override
        public boolean handle(Request request, Response response, Callback callback) throws Exception
        {
            Fields query = Request.extractQueryParameters(request, StandardCharsets.UTF_8);

            String member = query.getValue("Member");
            if (member == null)
            {
                Response.writeError(request, response, callback, HttpStatus.NOT_ACCEPTABLE_406, "Form not valid");
                return true;
            }

            response.getHeaders().put(HttpHeader.CONTENT_TYPE, "text/plain;charset=utf-8");

            String msg = String.format("Got (QueryHandler) Member [%s]%n", member);
            Content.Sink.write(response, true, msg, callback);
            return true;
        }
    }

    /**
     * Example of how to process an HTTP form send with application/x-www-form-urlencoded
     */
    public static class UrlEncodedFormHandler extends Handler.Abstract
    {
        private static final int MAX_KEYS = 100;
        private static final int MAX_CONTENT_SIZE = 10000;

        @Override
        public boolean handle(Request request, Response response, Callback callback) throws Exception
        {
            String contentType = request.getHeaders().get(HttpHeader.CONTENT_TYPE);
            String baseType = HttpField.getValueParameters(contentType, null);
            if (!MimeTypes.Type.FORM_ENCODED.is(baseType))
            {
                Response.writeError(request, response, callback, HttpStatus.NOT_ACCEPTABLE_406, "Form not valid");
                return true;
            }

            Fields form = FormFields.from(request, StandardCharsets.UTF_8, MAX_KEYS, MAX_CONTENT_SIZE).get();

            String member = form.getValue("Member");
            if (member == null)
            {
                Response.writeError(request, response, callback, HttpStatus.NOT_ACCEPTABLE_406, "Form not valid");
                return true;
            }

            response.getHeaders().put(HttpHeader.CONTENT_TYPE, "text/plain;charset=utf-8");

            String msg = String.format("Got (UrlEncodedFormHandler) Member [%s]%n", member);
            Content.Sink.write(response, true, msg, callback);
            return true;
        }
    }

    /**
     * Example of how to process an HTTP form sent with multipart/form-data
     */
    public static class MultipartFormHandler extends Handler.Abstract
    {
        private Path workDir;

        public MultipartFormHandler(Path workDir)
        {
            this.workDir = workDir;
        }

        @Override
        public boolean handle(Request request, Response response, Callback callback) throws Exception
        {
            String contentType = request.getHeaders().get(HttpHeader.CONTENT_TYPE);
            String baseType = HttpField.getValueParameters(contentType, null);
            if (!MimeTypes.Type.MULTIPART_FORM_DATA.is(baseType))
            {
                Response.writeError(request, response, callback, HttpStatus.NOT_ACCEPTABLE_406, "Form not valid");
                return true;
            }

            Map<String, String> form = extractMultiPartForm(request, contentType);

            String member = form.get("Member");
            if (member == null)
            {
                Response.writeError(request, response, callback, HttpStatus.NOT_ACCEPTABLE_406, "Form not valid");
                return true;
            }

            response.getHeaders().put(HttpHeader.CONTENT_TYPE, "text/plain;charset=utf-8");

            String msg = String.format("Got (UrlEncodedFormHandler) Member [%s]%n", member);
            Content.Sink.write(response, true, msg, callback);
            return true;
        }

        private Map<String, String> extractMultiPartForm(Request request, String contentType)
        {
            String boundary = MultiPart.extractBoundary(contentType);
            MultiPartFormData.Parser formData = new MultiPartFormData.Parser(boundary);
            formData.setFilesDirectory(workDir);

            // we are assuming a simple form with no binary data (like a file upload)
            Map<String, String> form = new HashMap<>();

            // May block waiting for multipart form data.
            try (MultiPartFormData.Parts parts = formData.parse(request).join())
            {
                parts.forEach(part ->
                {
                    if (StringUtil.isNotBlank(part.getFileName()))
                        return; // skip files

                    String value = part.getContentAsString(StandardCharsets.UTF_8);
                    form.put(part.getName(), value);
                });
            }
            return form;
        }
    }
}
