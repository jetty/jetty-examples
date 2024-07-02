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

package org.eclipse.jetty.examples.checks;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.jetty.toolchain.test.MavenPaths;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class ReadmeChecks
{
    @Test
    public void testEmbeddedReadme() throws IOException
    {
        Path embedded = MavenPaths.projectBase().resolve("../embedded");
        Path pom = embedded.resolve("pom.xml");
        Path readme = embedded.resolve("README.md");
        checkReadme(pom, readme);
    }

    @Test
    public void testStandaloneReadme() throws IOException
    {
        Path embedded = MavenPaths.projectBase().resolve("../standalone");
        Path pom = embedded.resolve("pom.xml");
        Path readme = embedded.resolve("README.md");
        checkReadme(pom, readme);
    }

    @Test
    public void testWebAppsReadme() throws IOException
    {
        Path embedded = MavenPaths.projectBase().resolve("../webapps");
        Path pom = embedded.resolve("pom.xml");
        Path readme = embedded.resolve("README.md");
        checkReadme(pom, readme);
    }

    private void checkReadme(Path pom, Path readme) throws IOException
    {
        List<String> activeModules = loadDeclaredModules(pom);
        String readmeText = Files.readString(readme, StandardCharsets.UTF_8);

        for (String expectedName: activeModules)
        {
            assertThat("Missing reference to [" + expectedName + "] in " + readme.normalize(),
                readmeText,
                containsString(String.format("[`%s/`](%s/)", expectedName, expectedName)));
        }
    }

    private List<String> loadDeclaredModules(Path pom) throws IOException
    {
        try
        {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setValidating(false);
            builderFactory.setNamespaceAware(false);
            builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            DocumentBuilder builder = builderFactory.newDocumentBuilder();

            try (InputStream inputStream = Files.newInputStream(pom))
            {
                Document doc = builder.parse(inputStream);

                Element root = doc.getDocumentElement();
                if (!root.getNodeName().equals("project"))
                {
                    throw new IOException("Not a <project> file: " + pom);
                }

                XPath xPath = XPathFactory.newInstance().newXPath();
                NodeList moduleNodes = (NodeList)xPath.compile("//modules/module").evaluate(doc, XPathConstants.NODESET);

                List<String> moduleNames = new ArrayList<>();

                asList(moduleNodes)
                    .stream()
                    .filter((node) -> node.getNodeType() == Node.ELEMENT_NODE)
                    .map(Node::getTextContent)
                    .forEach(moduleNames::add);
                return moduleNames;
            }
        }
        catch (XPathExpressionException | ParserConfigurationException | SAXException e)
        {
            throw new IOException("Unable to load pom: " + pom, e);
        }
    }

    private List<Node> asList(NodeList nodeList)
    {
        if (nodeList == null)
            return List.of();

        List<Node> nodes = new ArrayList<>();
        int len = nodeList.getLength();
        for (int i = 0; i < len; i++)
        {
            nodes.add(nodeList.item(i));
        }
        return nodes;
    }
}
