/*******************************************************************************
 * Copyright 2017-2023 Open Text.
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors (“Open Text”) are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.hpe.octane.ideplugins.eclipse;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.hpe.adm.nga.sdk.authentication.JSONAuthentication;
import com.hpe.adm.octane.ideplugins.services.connection.BasicConnectionSettingProvider;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.HttpClientProvider;
import com.hpe.adm.octane.ideplugins.services.connection.granttoken.GrantTokenAuthentication;
import com.hpe.adm.octane.ideplugins.services.connection.granttoken.TokenPollingCompleteHandler;
import com.hpe.adm.octane.ideplugins.services.connection.granttoken.TokenPollingInProgressHandler;
import com.hpe.adm.octane.ideplugins.services.connection.granttoken.TokenPollingStartedHandler;
import com.hpe.adm.octane.ideplugins.services.di.ServiceModule;
import com.hpe.adm.octane.ideplugins.services.util.UrlParser;
import com.hpe.octane.ideplugins.eclipse.preferences.LoginDialog;
import com.hpe.octane.ideplugins.eclipse.preferences.PluginPreferenceStorage;
import com.hpe.octane.ideplugins.eclipse.preferences.PluginPreferenceStorage.PreferenceConstants;
import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.EntityModelEditor;
import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.EntityModelEditorInput;
import com.hpe.octane.ideplugins.eclipse.ui.search.SearchEditor;
import com.hpe.octane.ideplugins.eclipse.ui.snake.KonamiCodeListener;
import com.hpe.octane.ideplugins.eclipse.ui.snake.SnakeEditor;
import com.hpe.octane.ideplugins.eclipse.ui.util.icon.EntityIconFactory;
import com.hpe.octane.ideplugins.eclipse.util.EncodedAuthentication;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "octane.eclipse.plugin"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;

    private static BasicConnectionSettingProvider settingsProviderInstance = new BasicConnectionSettingProvider();
    private static ServiceModule serviceModuleInstance;

    private static LoginDialog loginDialog;

    static {
        TokenPollingStartedHandler pollingStartedHandler = loginPageUrl -> Display.getDefault().asyncExec(() -> {
            IWorkbench wb = PlatformUI.getWorkbench();
            IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
            Shell shell = win != null ? win.getShell() : null;
            loginDialog = new LoginDialog(shell, loginPageUrl);
            loginDialog.open();
        });

        TokenPollingInProgressHandler pollingInProgressHandler = (pollingStatus -> {            
            if(loginDialog != null) {
                Display.getDefault().syncExec(() -> {
                    pollingStatus.shouldPoll = !loginDialog.wasClosed();
                    loginDialog.setTitle(LoginDialog.DEFAULT_TITLE + " (waiting for session, timeout in: " + ((pollingStatus.timeoutTimeStamp - new Date().getTime()) / 1000) + ")");
                });
            }
            return pollingStatus;
        });

        TokenPollingCompleteHandler pollingCompleteHandler = (tokenPollingCompletedStatus) ->
        Display.getDefault().syncExec(() -> {
        	EntityIconFactory.getInstance().clearImageDataCache();
        	
            if(!loginDialog.wasClosed()) { 
                loginDialog.close();
            }
        });

        serviceModuleInstance = new ServiceModule(settingsProviderInstance, pollingStartedHandler, pollingInProgressHandler, pollingCompleteHandler);
    }

    /**
     * The constructor
     */
    public Activator() {
    }

    public static void setConnectionSettings(ConnectionSettings connectionSettings) {
        settingsProviderInstance.setConnectionSettings(connectionSettings);
    }

    public static ConnectionSettings getConnectionSettings() {
        return settingsProviderInstance.getConnectionSettings();
    }

    public static void addConnectionSettingsChangeHandler(Runnable changeHandler) {
        settingsProviderInstance.addChangeHandler(changeHandler);
    }

    public static HttpClientProvider geOctaneHttpClient() {
        return serviceModuleInstance.getOctaneHttpClient();
    }

    public static <T> T getInstance(Class<T> type) {
        return serviceModuleInstance.getInstance(type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
     * BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);

        configureLogbackInBundle(context.getBundle());

        plugin = this;

        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

        try {
            ISecurePreferences securePrefs = PluginPreferenceStorage.getSecurePrefs();
            String baseUrl = securePrefs.get(PreferenceConstants.OCTANE_SERVER_URL, "");
            String isBrowserAuth = securePrefs.get(PreferenceConstants.IS_BROWSER_AUTH, Boolean.FALSE.toString());

            if (StringUtils.isEmpty(baseUrl)) {
                settingsProviderInstance.setConnectionSettings(new ConnectionSettings());
            } else {

                JSONAuthentication authentication;

                if (Boolean.parseBoolean(isBrowserAuth)) {
                    authentication = new GrantTokenAuthentication();
                } else {
                    String username = securePrefs.get(PreferenceConstants.USERNAME, "");
                    authentication = new EncodedAuthentication(username) {

                        @Override
                        public String getAuthenticationSecret() {
                            try {
                                return securePrefs.get(PreferenceConstants.PASSWORD, "");
                            } catch (StorageException e) {
                                return "";
                            }
                        }
                    };
                }

                ConnectionSettings loadedConnectionSettings = UrlParser.resolveConnectionSettings(baseUrl, authentication);
                settingsProviderInstance.setConnectionSettings(loadedConnectionSettings);
            }

        } catch (Exception e) {
            getLog().log(new Status(Status.ERROR, Activator.PLUGIN_ID, Status.ERROR,
                    "An exception has occured when loading the Core Software Delivery Platform connection details", e));
        }

        settingsProviderInstance.addChangeHandler(() -> {
            // Clear active item
            PluginPreferenceStorage.setActiveItem(null);

            // Close active entity editors and search editors
            for (IEditorReference editor : page.getEditorReferences()) {
                if (EntityModelEditor.ID.equals(editor.getId()) ||
                        SearchEditor.ID.equals(editor.getId())) {
                    page.closeEditor(editor.getEditor(false), false);
                }
            }

            // Clear selected fields for detail view
            // One octane can have different fields that the other
            PluginPreferenceStorage.resetShownEntityFields();
        });

        // Restore all entity model editors from their references, this is a
        // silly fix to properly set the editor part icon and tooltip
        IPartListener activePartListener = new IPartListener() {
            public void partActivated(IWorkbenchPart part) {
                String activeEditorTitle = part.getTitle();

                try {
                    for (IEditorReference editorReference : page.getEditorReferences()) {
                        IEditorInput editorInput = editorReference.getEditorInput();

                        if (editorInput instanceof EntityModelEditorInput
                                && !((EntityModelEditorInput) editorInput).getTitle().equals(activeEditorTitle)) {
                            editorReference.getEditor(true);
                        }
                    }
                } catch (PartInitException e) {
                    getLog().log(new Status(Status.WARNING, Activator.PLUGIN_ID, "Could not retrieve the active editor"));
                }
            }

            @Override
            public void partBroughtToTop(IWorkbenchPart part) {
                // TODO Auto-generated method stub
            }

            @Override
            public void partClosed(IWorkbenchPart part) {
                // TODO Auto-generated method stub
            }

            @Override
            public void partDeactivated(IWorkbenchPart part) {
                // TODO Auto-generated method stub
            }

            @Override
            public void partOpened(IWorkbenchPart part) {
                // TODO Auto-generated method stub
            }
        };
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().addPartListener(activePartListener);

        // Easter egg
        KonamiCodeListener konamiCodeListener = new KonamiCodeListener(() -> {
            try {
                // Unfortunately the game explodes on mac os, causing the ide to
                // not respond, don't have time to fix now
                String os = System.getProperty("os.name").toLowerCase();
                if (os != null && os.indexOf("win") >= 0) {
                    IWorkbenchPage currentPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    currentPage.openEditor(SnakeEditor.snakeEditorInput, SnakeEditor.ID);
                }
            } catch (PartInitException ignored) {
            }
        });
        PlatformUI.getWorkbench().getDisplay().addFilter(SWT.KeyDown, konamiCodeListener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
     * BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
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

    /**
     * Returns an image descriptor for the image file at the given plug-in
     * relative path
     *
     * @param path
     *            the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    private void configureLogbackInBundle(Bundle bundle) {

    }
}
