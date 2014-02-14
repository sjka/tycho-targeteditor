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
package org.eclipse.tycho.targeteditor.dialogs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.tycho.targeteditor.model.INexusRepository;
import org.junit.Test;

public class ClipboardAccessorTest {

    @Test
    public void testgetRepositoryFromStringInputEmpty() {
        final ClipboardAccessor accessor = new ClipboardAccessor("");
        assertNull(accessor.getRepository());
        assertFalse(accessor.hasRepository());
    }

    @Test
    public void testgetRepositoryFromNullInput() {
        final ClipboardAccessor accessor = new ClipboardAccessor(null);
        assertNull(accessor.getRepository());
        assertFalse(accessor.hasRepository());
    }

    @Test
    public void testgetRepositoryFromStringInputNoRepo() {
        assertNull(new ClipboardAccessor(
                " http://nonexus:8081/nexus/content/repositories/build.snapshots.unzip/g1of3/g2of3/g3of3/a/v/a-v-c.zip-unzip/  ")
                .getRepository());
        assertNull(new ClipboardAccessor("hallo").getRepository());
        assertNull(new ClipboardAccessor("<hallo/>").getRepository());
    }

    @Test
    public void testgetRepositoryFromStringInputUrl() {
        final ClipboardAccessor accessor = new ClipboardAccessor(
                " http://nexus:8081/nexus/content/repositories/build.snapshots.unzip/g1of3/g2of3/g3of3/a/v/a-v-c.zip-unzip/  ");
        assertTrue(accessor.hasRepository());
        final INexusRepository repo = accessor.getRepository();
        assertEquals("g1of3.g2of3.g3of3", repo.getGroupId());
        assertEquals("a", repo.getArtifactId());
        assertEquals("v", repo.getVersion());
        assertEquals("c", repo.getClassifier());
        assertEquals("zip", repo.getType());
    }

    @Test
    public void testgetRepositoryFromStringInputGavXmlNoClassifier() {
        final String gavXml = "<dependency>" + "<groupId>org.eclipse.tycho.demo.helloworld</groupId>"
                + "<artifactId>org.eclipse.tycho.demo.helloworld.updatesite</artifactId>"
                + "<version>0.1.1-SNAPSHOT</version>" + "<type>zip</type>" + "</dependency>";
        final INexusRepository repo = new ClipboardAccessor(gavXml).getRepository();
        assertEquals("org.eclipse.tycho.demo.helloworld", repo.getGroupId());
        assertEquals("org.eclipse.tycho.demo.helloworld.updatesite", repo.getArtifactId());
        assertEquals("0.1.1-SNAPSHOT", repo.getVersion());
        assertNull(repo.getClassifier());
        assertEquals("zip", repo.getType());
    }

    @Test
    public void testgetRepositoryFromStringInputGavXmlWithClassifier() {
        final String gavXml = "<dependency>" + "<groupId>org.eclipse.tycho.demo.helloworld</groupId>"
                + "<artifactId>org.eclipse.tycho.demo.helloworld.updatesite</artifactId>"
                + "<version>0.1.1-SNAPSHOT</version>" + "<classifier>assembly</classifier>" + "<type>zip</type>"
                + "</dependency>";
        final INexusRepository repo = new ClipboardAccessor(gavXml).getRepository();
        assertEquals("org.eclipse.tycho.demo.helloworld", repo.getGroupId());
        assertEquals("org.eclipse.tycho.demo.helloworld.updatesite", repo.getArtifactId());
        assertEquals("0.1.1-SNAPSHOT", repo.getVersion());
        assertEquals("assembly", repo.getClassifier());
        assertEquals("zip", repo.getType());
    }

    @Test
    public void testgetRepositoryFromStringInputGavXmlBadType() {
        final String gavXml = "<dependency>" + "<groupId>org.eclipse.tycho.demo.helloworld</groupId>"
                + "<artifactId>org.eclipse.tycho.demo.helloworld.updatesite</artifactId>"
                + "<version>0.1.1-SNAPSHOT</version>" + "<classifier>assembly</classifier>" + "<type>jar</type>"
                + "</dependency>";
        assertNull(new ClipboardAccessor(gavXml).getRepository());
    }
}
