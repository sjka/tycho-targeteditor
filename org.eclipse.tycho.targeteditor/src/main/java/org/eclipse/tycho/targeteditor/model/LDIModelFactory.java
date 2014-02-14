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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.core.target.ITargetLocation;
import org.eclipse.pde.core.target.ITargetPlatformService;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.target.IUBundleContainer;
import org.eclipse.pde.internal.ui.editor.targetdefinition.TargetEditor;
import org.eclipse.tycho.targeteditor.Activator;

/**
 * Factory to create {@link ILDITargetDefintion} and accessing {@link ITargetPlatformService}
 * 
 */
@SuppressWarnings("restriction")
public class LDIModelFactory {

    private static INexusRepositoryService nexusRepositoryService = new NexusRepositoryService();

    /**
     * Create a specialized model wrapper allowing some modifications not supported with the
     * original model.
     * 
     * @param targetDefinition
     *            the underlying target model to be wrapped
     * @return the LDIEditor specific model wrapper
     */
    public static ILDITargetDefintion createLDITargetDefinition(final ITargetDefinition targetDefinition) {
        return new LDITargetDefinition(targetDefinition);
    }

    /**
     * Convenience access to PDE {@link ITargetPlatformService}
     * 
     * @return the PDE <code> ITargetPlatformService </code>
     */
    public static ITargetPlatformService getTargetPlatformService() {
        final ITargetPlatformService service = (ITargetPlatformService) PDECore.getDefault().acquireService(
                ITargetPlatformService.class.getName());
        return service;
    }

    public static INexusRepositoryService getNexusRepositoryService() {
        return nexusRepositoryService;
    }

    /**
     * Creates a new instance of {@link INexusRepository}. The resulting repository reference will
     * represent a URI in the form:</br>
     * http://nexus:8081/nexus/content/repositories/{repoName}/{groupId separated by
     * "/"}/{artifactId}/{version}/{artifactId}-{version}[-{mavenClassifier}].{fileExtension}-unzip
     * 
     * @param repoName
     * @param groupId
     * @param artifactId
     * @param version
     * @param mavenClassifier
     *            optional. null or empty in case no classifier should be used
     * @param fileExtension
     *            of the archive
     * @return
     */
    public static INexusRepository createNexusRepository(final NexusRepositoryNames repoName, final String groupId,
            final String artifactId, final String version, final String mavenClassifier, final String fileExtension) {
        String artifactExtension = (mavenClassifier != null && mavenClassifier.length() > 0) ? "-" + mavenClassifier
                : "";
        artifactExtension += "." + fileExtension + "-unzip";
        final INexusRepository result = new NexusRepository(repoName.nexusName(), groupId, artifactId, version,
                artifactExtension, false);
        return result;
    }

    /**
     * Creates a new instance of {@link INexusRepository} from an uri.
     * 
     * @param uri
     * @return the {@link INexusRepository} or <code>null</code> if uri is not a valid Nexus
     *         repository
     */
    public static INexusRepository createNexusRepository(final URI uri) {
        final IRepository repo = NexusRepository.createRepository(uri);
        if (repo instanceof INexusRepository) {
            return (INexusRepository) repo;
        } else {
            return null;
        }
    }

    /**
     * Utility method to adjust the repository name based on the current version. The repository
     * name will only be changed in case the current repository name is known. (One of
     * {@link NexusRepositoryNames}]) </br> "-SNAPSHOT" versions will use
     * {@link NexusRepositoryNames.SNAPSHOT}, non "-SNAPSHOT" versions
     * {@link NexusRepositoryNames.MILESTONE}
     * 
     * @param repo
     */
    public static void adjustRepositoryName(final INexusRepository repo) {
        if (repo.getVersion().endsWith("-SNAPSHOT") || EDynamicVersions.isDynamicVersion(repo.getVersion())) {
            final NexusRepositoryNames repositoryName = NexusRepositoryNames.byNexusName(repo.getRepositoryName());
            if (NexusRepositoryNames.MILESTONE == repositoryName || NexusRepositoryNames.RELEASE == repositoryName) {
                repo.setRepositoryName(NexusRepositoryNames.SNAPSHOT.nexusName());
            }
        } else {
            if (NexusRepositoryNames.SNAPSHOT == NexusRepositoryNames.byNexusName(repo.getRepositoryName())) {
                repo.setRepositoryName(NexusRepositoryNames.MILESTONE.nexusName());
            }
        }
    }

    /**
     * Creates a new instance of {@link INexusRepository} using
     * {@link NexusRepositoryNames.SNAPSHOT} as Nexus repository. All other parts of the provided
     * repository will be unchanged. The method will always return a newly created instance even in
     * case the provided repository already uses {@link NexusRepositoryNames.SNAPSHOT}
     * 
     * @param repository
     * @return
     */
    public static INexusRepository createRelatedSnapshotRepository(final INexusRepository repository) {
        final INexusRepository result = (INexusRepository) NexusRepository.createRepository(repository.getURI());
        if (NexusRepositoryNames.byNexusName(repository.getRepositoryName()) != null) {
            result.setRepositoryName(NexusRepositoryNames.SNAPSHOT.nexusName());
        }
        return result;
    }

    /**
     * Workaround to access the JobFamily used for target definition resolution jobs
     * 
     * @param an
     *            TargetEditor instance
     * @return the fJobFamily member of related TargetChangeListener implementation
     */
    public static Object getResolveJobFamily(final TargetEditor targetEditor) {
        return getField(targetEditor.getTargetChangedListener(), "fJobFamily");
    }

    /**
     * Workaround to access package private fields from {@link IUBundleContainer}
     * 
     * @param an
     *            IUBundleContainer instance
     * @return the fFlags member value
     */
    static int getResolutionFlags(final IUBundleContainer iubc) {
        return ((Integer) getField(iubc, "fFlags")).intValue();
    }

    /**
     * Workaround to access package private fields from {@link IUBundleContainer}
     * 
     * @param an
     *            IUBundleContainer instance
     * @return the fIds member value
     */
    static String[] getIds(final IUBundleContainer iubc) {
        return (String[]) getField(iubc, "fIds");

    }

    /**
     * Workaround to access package private fields from {@link IUBundleContainer}
     * 
     * @param an
     *            IUBundleContainer instance
     * @return the fVersions member value
     */
    static Version[] getVersions(final IUBundleContainer iubc) {
        return (Version[]) getField(iubc, "fVersions");
    }

    static Object getField(final Object o, final String fieldName) {
        try {
            final Field field = o.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(o);
        } catch (final Exception e) {
            Activator
                    .getDefault()
                    .getLog()
                    .log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Unable to determine field value for field: "
                            + fieldName, e));
            throw new RuntimeException("Unable to determine field value for field: " + fieldName, e);
        }
    }

    static void clearResolutionStatus(final ITargetLocation container) {
        try {
            final Method method = findMethodInHierarchy("clearResolutionStatus", container.getClass());
            method.setAccessible(true);
            method.invoke(container, new Object[0]);
        } catch (final Exception e) {
            Activator
                    .getDefault()
                    .getLog()
                    .log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                            "Unable to invoke method clearResolutionStatus on: " + container, e));
            throw new RuntimeException("Unable to invoke method clearResolutionStatus on: " + container, e);
        }
    }

    private static Method findMethodInHierarchy(final String methodName, final Class<?> clazz)
            throws NoSuchMethodException {
        Method result = null;
        if (clazz == null) {
            throw new NoSuchMethodException(methodName);
        }
        try {
            result = clazz.getDeclaredMethod(methodName, new Class[0]);
        } catch (final NoSuchMethodException e) {
            result = findMethodInHierarchy(methodName, clazz.getSuperclass());
        }
        return result;
    }

    /**
     * For test purpose only
     * 
     * @param service
     *            the new service to be used
     * @return the currently used service
     */
    static INexusRepositoryService setNexusRepositoryService(final INexusRepositoryService service) {
        final INexusRepositoryService ret = nexusRepositoryService;
        nexusRepositoryService = service;
        return ret;
    }

}
