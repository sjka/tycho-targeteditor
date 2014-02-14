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
package org.eclipse.tycho.targeteditor.uitest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.tycho.targeteditor.Activator;

public class Util {

    private static NullProgressMonitor nullMonitor;

    static IProject createAndOpenTestProject() throws CoreException {
        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IProject project = workspace.getRoot().getProject("testProject");
        nullMonitor = new NullProgressMonitor();
        project.create(nullMonitor);
        project.open(nullMonitor);
        return project;
    }

    static IFile createTestTargetFile(final IProject project) throws IOException, CoreException {
        if (project == null) {
            throw new IllegalStateException("Target file cannot be created without a project");
        }
        final IFile targetFile = project.getFile(new Path("testTarget.target"));
        final URL targetTemplateUrl = FileLocator.find(Activator.getDefault().getBundle(), new Path(
                "resources/testTarget.target"), null);
        final InputStream inputStream = targetTemplateUrl.openStream();
        try {
            if (targetFile.exists()) {
                targetFile.setContents(inputStream, IResource.FORCE, nullMonitor);
            } else {
                targetFile.create(inputStream, true, nullMonitor);
            }
        } finally {
            inputStream.close();
        }
        return targetFile;
    }
}
