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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.tycho.targeteditor.Activator;
import org.eclipse.tycho.targeteditor.xml.RecommededRepositoriesParser;
import org.eclipse.tycho.targeteditor.xml.RecommededRepositoriesParserException;
import org.eclipse.tycho.targeteditor.xml.Repository;

public class RecommendedRepositoriesReaderJob extends Job {

    private final String url;
    private IStatus realStatus = Status.OK_STATUS;
    private List<Repository> repositoryList;

    public RecommendedRepositoriesReaderJob(final String repositoryUrl) {
        super("Read recommended repositories");
        url = repositoryUrl;
    }

    @Override
    protected IStatus run(final IProgressMonitor monitor) {
        try {
            final InputStream xmlStream = new URL(url).openStream();
            try {
                repositoryList = new RecommededRepositoriesParser().parse(xmlStream);
            } finally {
                xmlStream.close();
            }
        } catch (final MalformedURLException e) {
            setErrorStatus(e);
        } catch (final IOException e) {
            setErrorStatus(e);
        } catch (final RecommededRepositoriesParserException e) {
            setErrorStatus(e);
        }

        return Status.OK_STATUS;
    }

    private void setErrorStatus(final Exception e) {
        realStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Failed to read repositories from " + url, e);
        Activator.getDefault().getLog().log(realStatus);
    }

    public IStatus getRealStatus() {
        return realStatus;
    }

    public List<Repository> getRepositories() {
        return repositoryList;
    }

}
