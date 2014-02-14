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
package org.eclipse.tycho.targeteditor;

import java.net.URI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.p2.repository.IRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.jface.action.Action;
import org.eclipse.pde.internal.core.target.P2TargetUtils;

@SuppressWarnings("restriction")
class RefreshAllCacheAction extends Action {

    void runRefreshCacheJob(final LDITargetDefinitionProvider targetDefinitionProvider) {
        final Job refreshCacheJob = new Job("Refresh target definition editor caches") {
            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                RefreshAllCacheAction.refreshAllCaches(targetDefinitionProvider, monitor);
                return Status.OK_STATUS;
            }
        };
        refreshCacheJob.setUser(true);
        refreshCacheJob.schedule();
    }

    static void refreshAllCaches(final LDITargetDefinitionProvider targetDefinitionProvider,
            final IProgressMonitor monitor) {
        RepositoryVersionCache.getInstance().clean();
        cleanP2Cache(monitor);
        clearPDEResolutionState(targetDefinitionProvider);
    }

    private static void clearPDEResolutionState(final LDITargetDefinitionProvider targetDefinitionProvider) {
        targetDefinitionProvider.getLDITargetDefinition().clearResolutionStatus();
    }

    private static void cleanP2Cache(final IProgressMonitor monitor) {
        IMetadataRepositoryManager repoManager;
        try {
            repoManager = P2TargetUtils.getRepoManager();
        } catch (final CoreException e) {
            throw new RuntimeException("Unable to determine target definition repository manager", e);
        }
        final URI[] knownRepositories = repoManager.getKnownRepositories(IRepositoryManager.REPOSITORIES_ALL);
        monitor.beginTask("Remove cached p2 repositories", knownRepositories.length);
        for (final URI uri : knownRepositories) {
            repoManager.removeRepository(uri);
            monitor.worked(1);
            if (monitor.isCanceled()) {
                break;
            }
        }
    }

}
