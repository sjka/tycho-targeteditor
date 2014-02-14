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
package org.eclipse.tycho.targeteditor.model;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.tycho.targeteditor.Activator;
import org.eclipse.tycho.targeteditor.model.IRepository;
import org.eclipse.tycho.targeteditor.model.NexusRepository;
import org.eclipse.tycho.targeteditor.model.LDIModelFactory;

public class Util {
    public static final String REPOREF_TARGET_DEFINITION_PATH = "resources/reporef.target";
    public static final String REPOTYPES_TARGET_DEFINITION_PATH = "resources/repotypes.target";
    public static final String REPOREF_SIMPLE_RESOLVE_TEST_PATH = "resources/simpleResolveTest.target";

    public static ITargetDefinition loadTargetDefinition(final String path) throws IOException, CoreException {
        final URI targetFile = getResourceURI(path);
        final ITargetDefinition target = LDIModelFactory.getTargetPlatformService().getTarget(targetFile)
                .getTargetDefinition();
        return target;
    }

    public static URI getResourceURI(final String path) throws IOException {
        final URL url = FileLocator.find(Activator.getDefault().getBundle(), new Path(path), null);
        URI result;
        try {
            result = FileLocator.toFileURL(url).toURI();
        } catch (final URISyntaxException e) {
            throw new IOException(e);
        }
        return result;
    }

    public static File getResourceFile(final String path) throws IOException {
        final URL url = FileLocator.find(Activator.getDefault().getBundle(), new Path(path), null);
        final File result = new File(FileLocator.toFileURL(url).getFile());
        return result;
    }

    public static void deleteRecursive(final File fileOrFolder) {
        if (fileOrFolder.isDirectory()) {
            Util.cleanFolder(fileOrFolder);
        }
        assertTrue(fileOrFolder.delete());
    }

    public static File createTempFolder() throws IOException {
        final File tmpFile = File.createTempFile("targetEditorTest", "tmp");
        assertTrue(tmpFile.delete());
        assertTrue(tmpFile.mkdir());
        return tmpFile;
    }

    public static void cleanFolder(final File folder) {
        final File[] files = folder.listFiles();
        for (final File file : files) {
            deleteRecursive(file);
        }
    }

    public static void extractZip(final File zipFile, final File destFolder) throws ZipException, IOException {
        final byte[] buf = new byte[1024];
        final ZipFile zf = new ZipFile(zipFile);
        try {
            final Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry zipEntry = entries.nextElement();
                if (!zipEntry.isDirectory()) {
                    final File outFile = new File(destFolder, zipEntry.getName());
                    outFile.getParentFile().mkdirs();
                    final InputStream ins = zf.getInputStream(zipEntry);
                    OutputStream out = null;
                    try {
                        out = new FileOutputStream(outFile);
                        int n;
                        while ((n = ins.read(buf, 0, 1024)) > -1) {
                            out.write(buf, 0, n);
                        }
                    } finally {
                        if (out != null) {
                            out.close();
                        }
                    }
                }
            }
        } finally {
            zf.close();
        }
    }

    public static IRepository createRepository(final URI uri) {
        return NexusRepository.createRepository(uri);
    }
}
