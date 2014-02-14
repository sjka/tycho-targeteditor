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

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.tycho.targeteditor.Activator;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

class NexusRepositoryService implements INexusRepositoryService {
    static class CalculationRequest {
        final INexusRepository repository;
        final INexusVersionInfoCallback callback;

        CalculationRequest(final INexusRepository repository, final INexusVersionInfoCallback callback) {
            this.repository = repository;
            this.callback = callback;
        }
    }

    static class VersionCalculationJob extends Job {
        static List<NexusRepositoryService.CalculationRequest> requests = new LinkedList<NexusRepositoryService.CalculationRequest>();
        private final NexusRepositoryService repositoryService;

        VersionCalculationJob(final String name, final NexusRepositoryService repositoryService) {
            super(name);
            this.repositoryService = repositoryService;
        }

        static void addRequest(final CalculationRequest request) {
            synchronized (VersionCalculationJob.requests) {
                requests.add(request);
            }
        }

        @Override
        protected IStatus run(final IProgressMonitor monitor) {
            CalculationRequest request;
            do {
                request = null;
                synchronized (VersionCalculationJob.requests) {
                    if (!requests.isEmpty()) {
                        request = requests.remove(0);
                    }
                }
                if (request != null) {
                    final String errorMessage = "Unable to calculate available versions";
                    IStatus resultStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, errorMessage);
                    INexusVersionInfo result = null;
                    try {
                        try {
                            result = repositoryService.getVersionInfo(request.repository);
                            resultStatus = Status.OK_STATUS;
                        } catch (final WorkbenchException e) {
                            resultStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, errorMessage, e);
                        } catch (final IOException e) {
                            resultStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, errorMessage, e);
                        }

                    } finally {
                        request.callback.notifyVersionCalculated(new NexusVersionCalculationResult(request.repository,
                                result, resultStatus));
                    }
                }
            } while (request != null);
            return Status.OK_STATUS;
        }
    }

    private final List<VersionCalculationJob> versionCalculationJobs;

    NexusRepositoryService() {
        versionCalculationJobs = new ArrayList<NexusRepositoryService.VersionCalculationJob>(10);
        for (int i = 0; i < 10; i++) {
            versionCalculationJobs.add(new VersionCalculationJob("Fetching available versions from Nexus...", this));
        }
    }

    @Override
    public INexusVersionInfo getVersionInfo(final INexusRepository repository) throws WorkbenchException, IOException {
        if (repository instanceof NexusRepository) {
            final NexusRepository nexusRepo = (NexusRepository) repository;
            final URL metaDataUrl = nexusRepo.getMavenVersionMetaUrl();
            return new NexusVersionInfo(getMemento(metaDataUrl));
        }
        throw new IllegalArgumentException("Not an instance of NexusRepository.");
    }

    IMemento getMemento(final URL metaDataUrl) throws WorkbenchException, IOException {
        IMemento memento = null;
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(metaDataUrl.openStream());
            memento = XMLMemento.createReadRoot(reader);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return memento;
    }

    @Override
    public void getVersionInfoAsync(final INexusRepository repository, final INexusVersionInfoCallback callback) {
        final CalculationRequest request = new CalculationRequest(repository, callback);
        VersionCalculationJob.addRequest(request);
        boolean allJobsBusy = true;
        for (final VersionCalculationJob job : versionCalculationJobs) {
            if (job.getState() != Job.RUNNING) {
                job.schedule();
                allJobsBusy = false;
                break;
            }
        }
        if (allJobsBusy) {
            // No free job. The request will be handled by one currently running job.
            // To avoid the once in a life time state that all jobs are running, but already left the request loop a new job has to be scheduled.
            // Any job would do. To get the next free job, just schedule them all. 
            for (final VersionCalculationJob job : versionCalculationJobs) {
                job.schedule();
            }
        }
    }
}
