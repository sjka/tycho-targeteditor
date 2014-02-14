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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tycho.targeteditor.model.INexusRepository;
import org.eclipse.tycho.targeteditor.model.INexusVersionInfoCallback;
import org.eclipse.tycho.targeteditor.model.ModelChangeProvider;
import org.eclipse.tycho.targeteditor.model.NexusVersionCalculationResult;
import org.eclipse.tycho.targeteditor.model.LDIModelFactory;

public class RepositoryVersionCache extends ModelChangeProvider {

    static final String EVENT_PROPERTY_CACHE_CLEAN = "NexusVersionCacheClean";
    private static final RepositoryVersionCache instance = new RepositoryVersionCache();
    private final Map<Object, NexusVersionCalculationResult> versionMap = new HashMap<Object, NexusVersionCalculationResult>();
    private static final NexusVersionCalculationResult CALCULATION_IN_PROGRESS = new NexusVersionCalculationResult(
            null, null, null);

    private RepositoryVersionCache() {
    }

    /**
     * @return the singleton instance
     */
    public static RepositoryVersionCache getInstance() {
        return instance;
    }

    /**
     * Gets version information from the cache.</br> The method always return immediately. In case
     * the requested information is not yet contained in the cache it's asynchronous calculation is
     * triggered.</br> Interested parties might register as listener to be informed when information
     * is available.</br>
     * 
     * @param repository
     *            the INexusRepository for which version information should be returned
     * @return the cached version information or <code>null</code> in case the information was not
     *         yet cached
     */
    public NexusVersionCalculationResult getAllAvailableVersions(final INexusRepository repository) {
        return getAvailableVersions(LDIModelFactory.createRelatedSnapshotRepository(repository));
    }

    /**
     * Triggers an asynchronous recalculation of cache content
     */
    public void reload() {
        final List<INexusRepository> repos = new ArrayList<INexusRepository>();
        synchronized (versionMap) {
            for (final NexusVersionCalculationResult currentResult : versionMap.values()) {
                repos.add(currentResult.getRepository());
            }
        }
        for (final INexusRepository repository : repos) {
            LDIModelFactory.getNexusRepositoryService().getVersionInfoAsync(repository, new VersionCallback());
        }
    }

    public void clean() {
        synchronized (versionMap) {
            versionMap.clear();
        }
        fireModelObjectChanged(this, EVENT_PROPERTY_CACHE_CLEAN, null, null);
    }

    private NexusVersionCalculationResult getAvailableVersions(final INexusRepository repository) {
        synchronized (versionMap) {
            NexusVersionCalculationResult result = versionMap.get(createVersionKey(repository));
            if (result == null) {
                versionMap.put(createVersionKey(repository), CALCULATION_IN_PROGRESS);
                LDIModelFactory.getNexusRepositoryService().getVersionInfoAsync(repository, new VersionCallback());
            }
            if (result == CALCULATION_IN_PROGRESS) {
                result = null;
            }
            return result;
        }
    }

    private Object createVersionKey(final INexusRepository repository) {
        Object key = null;
        if (repository != null) {
            key = repository.getRepositoryName() + "/" + repository.getGroupId() + "/" + repository.getArtifactId();
        }
        return key;
    }

    private class VersionCallback implements INexusVersionInfoCallback {
        @Override
        public void notifyVersionCalculated(final NexusVersionCalculationResult calculationResult) {
            NexusVersionCalculationResult oldResult;
            synchronized (versionMap) {
                final Object key = createVersionKey(calculationResult.getRepository());
                oldResult = versionMap.get(key);
                versionMap.put(key, calculationResult);
            }
            oldResult = (oldResult == CALCULATION_IN_PROGRESS) ? null : oldResult;
            RepositoryVersionCache.this.fireModelObjectChanged(RepositoryVersionCache.this,
                    "NexusVersionCalculationResult", oldResult, calculationResult);
        }
    }

}
