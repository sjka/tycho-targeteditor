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
package org.eclipse.tycho.targeteditor.jobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.tycho.targeteditor.Activator;
import org.eclipse.tycho.targeteditor.jobs.RecommendedRepositoriesReaderJob;
import org.eclipse.tycho.targeteditor.xml.Repository;
import org.junit.Test;

public class RecommendedRepositoriesReaderJobTest {

    @Test
    public void testReadValidRepositories() {
        final URL url = FileLocator.find(Activator.getDefault().getBundle(), new Path(
                "resources/recommendedRepositories.xml"), null);
        final RecommendedRepositoriesReaderJob job = new RecommendedRepositoriesReaderJob(url.toExternalForm());
        final IStatus status = job.run(new NullProgressMonitor());
        assertTrue(status.isOK());
        final List<Repository> repositories = job.getRepositories();
        assertEquals(2, repositories.size());
        final Set<String> names = new HashSet<String>();
        for (final Repository repository : repositories) {
            names.add(repository.getName());
        }
        assertEquals(new HashSet<String>(Arrays.asList("JPaaS SDK", "Phoenix")), names);
    }

    @Test
    public void testReadBrokenUrl() {
        final RecommendedRepositoriesReaderJob job = new RecommendedRepositoriesReaderJob("file:///nofile");
        final IStatus status = job.run(new NullProgressMonitor());
        assertTrue(status.isOK());
        final IStatus realStatus = job.getRealStatus();
        assertFalse(realStatus.isOK());
        assertTrue(realStatus.getException() instanceof FileNotFoundException);
        assertNull(job.getRepositories());
    }

}
