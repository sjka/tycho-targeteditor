/**
 * Copyright (c) 2011, 2014 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 */
package org.eclipse.tycho.targeteditor.xml;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.junit.Test;

public class RecommendedRepositoriesParserTest {

    private static final String XML_HEADER = "<?xml version=\"1.0\"?><repositories version='1.0' xmlns='urn:org.eclipse.tycho:TychoTargetEditor:RecommendedRepositories:1.0'>";
    private static final String FIRST_REPOSITORY = "" //
            + "  <repository name=\"Project XYZ API Repository\">\r\n" //
            + "    <groupId>com.sap.example</groupId>\r\n" //
            + "    <artifactId>com.sap.example.xyz.sdk</artifactId>\r\n" // 
            + "    <version>1.0.6</version>\r\n" //
            + "    <classifier>assembly</classifier>\r\n" //
            + "    <description>This is a long description of the project XYZ API repository\r\n\r\n" //
            + "A line in a new paragraph.\r\n" //
            + "    </description>\r\n" //
            + "    <extension>zip</extension>\r\n" //
            + "  </repository>\r\n";
    private static final String SECOND_REPOSITORY = "" //
            + "  <repository name=\"Second Repository\">\r\n" //
            + "    <groupId>com.sap.core</groupId>\r\n" //
            + "    <artifactId>com.sap.core.util</artifactId>\r\n" //
            + "    <version>1.0</version>\r\n" //
            + "    <classifier></classifier>\r\n" //
            + "    <description></description>\r\n" //
            + "    <extension>jar</extension>\r\n" //
            + "  </repository>\r\n";
    private static final String XML_TAIL = "</repositories>";

    @Test(expected = RecommededRepositoriesParserException.class)
    public void testParseEmptyListOfRepositories() {
        final List<Repository> result = parse(XML_HEADER + XML_TAIL);
        assertThat(result.size(), is(0));
    }

    @Test(expected = RecommededRepositoriesParserException.class)
    public void testEmptyRepositoryIsNotAllowed() {
        final String testContent = XML_HEADER + "  <repository name=\"Other SDK\"></repository>" + XML_TAIL;
        parse(testContent);
    }

    @Test
    public void testTwoRepositories() {
        final String testContent = XML_HEADER + FIRST_REPOSITORY + SECOND_REPOSITORY + XML_TAIL;
        final List<Repository> result = parse(testContent);
        assertThat(result.size(), is(2));
        assertThat(result.get(0).getName(), is("Project XYZ API Repository"));
        assertThat(result.get(1).getName(), is("Second Repository"));
    }

    @Test(expected = RecommededRepositoriesParserException.class)
    public void testWrongExtension() {
        final String testContent = XML_HEADER
                + "<repository name=''><groupId></groupId><artifactId></artifactId><version></version><classifier></classifier><description></description>"
                + "<extension>class</extension></repository>" + XML_TAIL;
        parse(testContent);
    }

    @Test
    public void testRepositoryWithGav() {
        final String testContent = XML_HEADER + FIRST_REPOSITORY + XML_TAIL;
        final List<Repository> result = parse(testContent);
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getName(), is("Project XYZ API Repository"));
        assertThat(result.get(0).getGroupId(), is("com.sap.example"));
        assertThat(result.get(0).getArtifactId(), is("com.sap.example.xyz.sdk"));
        assertThat(result.get(0).getVersion(), is("1.0.6"));
        assertThat(result.get(0).getClassifier(), is("assembly"));
        assertThat(result.get(0).getExtension(), is("zip"));
        // line endings are converted from \r\n to \n. probably not a problem...(?) 
        assertThat(result.get(0).getDescription(),
                is("This is a long description of the project XYZ API repository\n\nA line in a new paragraph.\n    "));
    }

    @Test(expected = RecommededRepositoriesParserException.class)
    public void testWrongVersion() {
        parse("<?xml version=\"1.0\"?><repositories version='2.0' xmlns='urn:org.eclipse.tycho:TychoTargetEditor:RecommendedRepositories:1.0'>"
                + FIRST_REPOSITORY + XML_TAIL);
    }

    private List<Repository> parse(final String testContent) {
        final RecommededRepositoriesParser parser = new RecommededRepositoriesParser();
        final List<Repository> result = parser.parse(new ByteArrayInputStream(testContent.getBytes()));
        return result;
    }

}
