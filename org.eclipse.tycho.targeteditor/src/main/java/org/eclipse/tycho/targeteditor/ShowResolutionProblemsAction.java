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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;

@SuppressWarnings("restriction")
class ShowResolutionProblemAction extends Action {
    static final String TOOL_TIP_TEXT = "Show resolution problem details";
    static final String CONSOLE_NAME = "Target definition resolution problems";
    private final LDITargetDefinitionProvider targetDefinitionProvider;

    ShowResolutionProblemAction(final LDITargetDefinitionProvider targetDefinitionProvider) {
        this.targetDefinitionProvider = targetDefinitionProvider;
    }

    @Override
    public boolean isEnabled() {
        final IStatus resolutionStatus = targetDefinitionProvider.getLDITargetDefinition().getResolutionStatus();
        return resolutionStatus != null && !resolutionStatus.isOK();
    }

    @Override
    public void run() {
        final IStatus resolutionStatus = targetDefinitionProvider.getLDITargetDefinition().getResolutionStatus();
        final MessageConsole problemConsole = getResolutionProblemConsole();
        writeStatusToConsole(resolutionStatus, problemConsole);
        showConsole(problemConsole);
    }

    private static void writeStatusToConsole(final IStatus resolutionStatus, final MessageConsole problemConsole) {
        final MessageConsoleStream messageConsoleStream = problemConsole.newMessageStream();
        try {
            messageConsoleStream.println(ResolutionStatusFormatter.format(resolutionStatus));
        } finally {
            try {
                messageConsoleStream.close();
            } catch (final IOException e) {
                Activator.getDefault().getLog()
                        .log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, "Unable to close console stream", e));
            }
        }
    }

    private static void showConsole(final MessageConsole problemConsole) {
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        try {
            final IConsoleView view = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
            view.display(problemConsole);
        } catch (final PartInitException e) {
            Activator.getDefault().getLog()
                    .log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Unable to open console view", e));
        }
    }

    private static MessageConsole getResolutionProblemConsole() {
        final IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
        final IConsole[] currentConsoles = consoleManager.getConsoles();
        for (int i = 0; i < currentConsoles.length; i++) {
            if (ShowResolutionProblemAction.CONSOLE_NAME.equals(currentConsoles[i].getName())) {
                return (MessageConsole) currentConsoles[i];
            }
        }
        final MessageConsole newResolutionProblemConsole = new MessageConsole(ShowResolutionProblemAction.CONSOLE_NAME,
                null);
        consoleManager.addConsoles(new IConsole[] { newResolutionProblemConsole });
        return newResolutionProblemConsole;
    }

    static void createAndAssign(final LDITargetDefinitionProvider targetDefinitionProvider,
            final IManagedForm managedForm, final Object resolveJobFamily) {
        final ShowResolutionProblemAction showProblemDetailsAction = new ShowResolutionProblemAction(
                targetDefinitionProvider);
        showProblemDetailsAction.setImageDescriptor(PDEPluginImages.DESC_ERROR_ST_OBJ);
        showProblemDetailsAction.setToolTipText(TOOL_TIP_TEXT);

        final ActionContributionItem showProblemDetailsActionContributionItem = new ActionContributionItem(
                showProblemDetailsAction);
        showProblemDetailsActionContributionItem.setId(ShowResolutionProblemAction.class.getName());

        final ResolutionProblemActionLifecyclePart lifecyclePart = new ResolutionProblemActionLifecyclePart(
                targetDefinitionProvider, managedForm, showProblemDetailsActionContributionItem,
                showProblemDetailsAction, resolveJobFamily);
        managedForm.addPart(lifecyclePart);
    }

    private static final class ResolutionProblemActionLifecyclePart extends AbstractFormPart {
        private static final int RESOLUTION_STATUS_POLL_TIMER_PERIOD = 500;
        private static final String MSG_RESOLVING_TARGET_DEFINITION = "Resolving target definition...";
        private final LDITargetDefinitionProvider targetDefinitionProvider;
        private final IManagedForm managedForm;
        private final ActionContributionItem showProblemDetailsActionContributionItem;
        private final ShowResolutionProblemAction showProblemDetailsAction;
        private final Timer refreshTimer;
        private final Object resolveJobFamily;

        private String currentMessage;

        private ResolutionProblemActionLifecyclePart(final LDITargetDefinitionProvider targetDefinitionProvider,
                final IManagedForm managedForm, final ActionContributionItem showProblemDetailsActionContributionItem,
                final ShowResolutionProblemAction showProblemDetailsAction, final Object resolveJobFamily) {
            this.targetDefinitionProvider = targetDefinitionProvider;
            this.managedForm = managedForm;
            this.showProblemDetailsActionContributionItem = showProblemDetailsActionContributionItem;
            this.showProblemDetailsAction = showProblemDetailsAction;
            this.resolveJobFamily = resolveJobFamily;

            this.refreshTimer = new Timer("TargetResolutionRefreshTimer", true);
            refreshTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            ResolutionProblemActionLifecyclePart.this.refresh();
                        }
                    });
                }
            }, RESOLUTION_STATUS_POLL_TIMER_PERIOD, RESOLUTION_STATUS_POLL_TIMER_PERIOD);
        }

        @Override
        public void refresh() {
            final String newMessage = createMessageText();
            if (!newMessage.equals(currentMessage)) {
                updateMessageAndToolbar(newMessage);
                currentMessage = newMessage;
            }
        }

        private void updateMessageAndToolbar(final String newMessage) {
            if ("".equals(newMessage)) {
                // ok
                managedForm.getMessageManager().removeMessage(ShowResolutionProblemAction.class);
                removeActionFromToolbar();
            } else if (MSG_RESOLVING_TARGET_DEFINITION.equals(newMessage)) {
                // resolving
                managedForm.getMessageManager().removeMessage(ShowResolutionProblemAction.class);
                managedForm.getMessageManager().addMessage(ShowResolutionProblemAction.class,
                        MSG_RESOLVING_TARGET_DEFINITION, null, IMessageProvider.INFORMATION);
                removeActionFromToolbar();
            } else {
                // some problem
                managedForm.getMessageManager()
                        .addMessage(ShowResolutionProblemAction.class, newMessage,
                                targetDefinitionProvider.getLDITargetDefinition().getResolutionStatus(),
                                IMessageProvider.ERROR);
                final IContributionItem showProblemDetailsItem = managedForm.getForm().getToolBarManager()
                        .find(showProblemDetailsActionContributionItem.getId());
                if (showProblemDetailsItem == null) {
                    final IContributionItem[] items = managedForm.getForm().getToolBarManager().getItems();
                    managedForm.getForm().getToolBarManager()
                            .insertBefore(items[0].getId(), showProblemDetailsActionContributionItem);
                    managedForm.getForm().getToolBarManager().update(true);
                }
            }
        }

        private void removeActionFromToolbar() {
            if (managedForm.getForm().getToolBarManager().remove(showProblemDetailsActionContributionItem) != null) {
                managedForm.getForm().getToolBarManager().update(true);
            }
        }

        private String createMessageText() {
            if (isCurrentlyResolving()) {
                // do not use resolution state while resolution job is running
                // Currently the underlying PDE model status might change from resolving to error to ok.
                // This would be quite confusing for users
                return MSG_RESOLVING_TARGET_DEFINITION;
            }
            if (showProblemDetailsAction.isEnabled()) {
                return ResolutionStatusFormatter.getShortMsgText(targetDefinitionProvider.getLDITargetDefinition()
                        .getResolutionStatus());
            } else {
                if (targetDefinitionProvider.getLDITargetDefinition().getResolutionStatus() == null) {
                    return MSG_RESOLVING_TARGET_DEFINITION;
                } else {
                    return "";
                }
            }
        }

        private boolean isCurrentlyResolving() {
            final Job[] resolveJobs = Job.getJobManager().find(resolveJobFamily);
            if (resolveJobs != null) {
                for (int i = 0; i < resolveJobs.length; i++) {
                    if (resolveJobs[i].getState() == Job.RUNNING) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean isStale() {
            return true;
        }

        @Override
        public void dispose() {
            refreshTimer.cancel();
        }
    }

    static final class ResolutionStatusFormatter {
        static final String MSG_UNRESOLVED = "Unresolved";
        private static final String NEW_LINE = "\n";

        static String getShortMsgText(final IStatus resolutionStatus) {
            String result = MSG_UNRESOLVED;
            if (resolutionStatus != null) {
                final List<IStatus> children = removeDuplicates(resolutionStatus.getChildren());
                if (children.size() > 0) {
                    result = children.get(0).getMessage();
                    if (children.size() > 1) {
                        result = "[" + children.size() + " problems] " + result;
                    }
                }
            }
            return result;
        }

        static String format(final IStatus resolutionStatus) {
            final StringBuilder sb = new StringBuilder();
            if (resolutionStatus != null) {
                formatTargetResolutionStatusMessage(resolutionStatus, sb, 0);
                sb.append(NEW_LINE);
                formatTargetResolutionStatusExceptions(resolutionStatus, sb);
            } else {
                sb.append(MSG_UNRESOLVED);
                sb.append(NEW_LINE);
            }
            return sb.toString();
        }

        private static void formatTargetResolutionStatusMessage(final IStatus resolutionStatus, final StringBuilder sb,
                final int indent) {
            final char[] indentChars = new char[indent];
            Arrays.fill(indentChars, ' ');
            sb.append(indentChars);
            sb.append(resolutionStatus.getMessage());
            if (resolutionStatus.isMultiStatus()) {
                final List<IStatus> children = removeDuplicates(resolutionStatus.getChildren());
                for (final IStatus iStatus : children) {
                    sb.append(NEW_LINE);
                    formatTargetResolutionStatusMessage(iStatus, sb, indent + 2);
                }
            }
        }

        private static void formatTargetResolutionStatusExceptions(final IStatus resolutionStatus,
                final StringBuilder sb) {
            final Throwable exception = resolutionStatus.getException();
            if (exception != null) {
                sb.append(NEW_LINE);
                sb.append("Status containing exception:");
                sb.append(NEW_LINE);
                sb.append(resolutionStatus.getMessage());
                sb.append(NEW_LINE);
                sb.append("Contained exception");
                sb.append(NEW_LINE);
                dumpException(exception, sb);
            }
            if (resolutionStatus.isMultiStatus()) {
                for (final IStatus iStatus : resolutionStatus.getChildren()) {
                    formatTargetResolutionStatusExceptions(iStatus, sb);
                }
            }
        }

        private static void dumpException(final Throwable exception, final StringBuilder sb) {
            sb.append(exception.toString());
            sb.append(NEW_LINE);
            final StackTraceElement[] stackTrace = exception.getStackTrace();
            for (final StackTraceElement stackTraceElement : stackTrace) {
                sb.append(" at ");
                sb.append(stackTraceElement.toString());
                sb.append(NEW_LINE);
            }
            final Throwable cause = exception.getCause();
            if (cause != null) {
                sb.append("Caused by ");
                dumpException(cause, sb);
            }
        }

        private static List<IStatus> removeDuplicates(final IStatus[] children) {
            final List<IStatus> result = new ArrayList<IStatus>();
            final Set<String> compareSet = new HashSet<String>();
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    final IStatus status = children[i];
                    final StringBuilder sb = new StringBuilder();
                    formatTargetResolutionStatusMessage(status, sb, 0);
                    final String compareKey = sb.toString();
                    if (compareSet.add(compareKey)) {
                        result.add(status);
                    }
                }
            }
            return result;
        }
    }
}
