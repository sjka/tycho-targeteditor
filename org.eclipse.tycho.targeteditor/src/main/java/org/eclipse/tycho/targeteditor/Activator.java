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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.eclipse.tycho.targeteditor"; //$NON-NLS-1$

    public static final String NEXUS_IMG = "NEXUS_IMAGE";
    public static final String VERSION_ACTION_IMG = "VERSION_ACTION_IMAGE";
    public static final String VERSION_ACTION_DISABLED_IMG = "VERSION_ACTION_DISABLED_IMAGE";

    public static final String LOADING_OVERLAY = "LOADING_OVERLAY";
    public static final String ERROR_OVERLAY = "ERROR_OVERLAY";
    public static final String UPDATEABLE_OVERLAY = "UPDATEABLE_OVERLAY";

    // The shared instance
    private static Activator plugin;

    /**
     * The constructor
     */
    public Activator() {
    }

    @Override
    protected void initializeImageRegistry(final ImageRegistry reg) {
        reg.put(NEXUS_IMG, imageDescriptorFromPlugin(PLUGIN_ID, "icons/nexusRepo.png"));
        reg.put(VERSION_ACTION_DISABLED_IMG,
                imageDescriptorFromPlugin(PLUGIN_ID, "icons/versionUpdateAction_disabled.png"));
        reg.put(VERSION_ACTION_IMG, imageDescriptorFromPlugin(PLUGIN_ID, "icons/versionUpdateAction.png"));

        final Image nexusLogo = getImageFromRegistry(NEXUS_IMG);
        final DecorationOverlayIcon errorOverlay = new DecorationOverlayIcon(nexusLogo,
                PDEPluginImages.DESC_INTERNAL_CO, IDecoration.TOP_RIGHT);
        reg.put(ERROR_OVERLAY, errorOverlay.createImage());

        final DecorationOverlayIcon updateableOverlay = new DecorationOverlayIcon(nexusLogo,
                PDEPluginImages.DESC_FRIEND_CO, IDecoration.TOP_RIGHT);
        reg.put(UPDATEABLE_OVERLAY, updateableOverlay.createImage());

        final DecorationOverlayIcon loadingOverlay = new DecorationOverlayIcon(nexusLogo,
                PDEPluginImages.DESC_OPTIONAL_CO, IDecoration.TOP_RIGHT);
        reg.put(LOADING_OVERLAY, loadingOverlay.createImage());

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(final BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    public Image getImageFromRegistry(final String key) {
        return getImageRegistry().get(key);
    }

    void logException(final CoreException e) {
        this.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage(), e));
    }
}
