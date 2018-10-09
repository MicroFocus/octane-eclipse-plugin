/*******************************************************************************
 * © 2017 EntIT Software LLC, a Micro Focus company, L.P.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.hpe.octane.ideplugins.eclipse.preferences;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.google.api.client.http.HttpResponseException;
import com.hpe.adm.nga.sdk.exception.OctaneException;
import com.hpe.adm.octane.ideplugins.services.TestService;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.connection.UserAuthentication;
import com.hpe.adm.octane.ideplugins.services.connection.granttoken.GrantTokenAuthentication;
import com.hpe.adm.octane.ideplugins.services.exception.ServiceException;
import com.hpe.adm.octane.ideplugins.services.nonentity.OctaneVersionService;
import com.hpe.adm.octane.ideplugins.services.util.OctaneVersion;
import com.hpe.adm.octane.ideplugins.services.util.UrlParser;
import com.hpe.octane.ideplugins.eclipse.Activator;
import com.hpe.octane.ideplugins.eclipse.preferences.PluginPreferenceStorage.PreferenceConstants;
import com.hpe.octane.ideplugins.eclipse.ui.util.InfoPopup;
import com.hpe.octane.ideplugins.eclipse.ui.util.error.ErrorComposite;
import com.hpe.octane.ideplugins.eclipse.ui.util.resource.ImageResources;

public class PluginPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
    public PluginPreferencePage() {
    }

    public static final String ID = "com.hpe.octane.ideplugins.eclipse.preferences.PluginPreferencePage";
    public static final String CORRECT_URL_FORMAT_MESSAGE = "Example: (http|https)://{serverurl[:port]}/?p={sharedspaceId}/{workspaceId}";
    
    private static final String userPassAuthInfoText = "Log into ALM Octane directly with your user name and password, in non-SSO environments. " + System.lineSeparator() +
            "This method saves your login credentials between sessions, so you don’t have to re-enter them.";

    private static final String browserAuthInfoText = "Log into ALM Octane using a browser. " + System.lineSeparator() +
            "You can use this method for non-SSO, SSO, and federated environments. " + System.lineSeparator() +
            "Your login credentials are not saved between sessions, so you will have to re-enter them each time.";

    private Text textServerUrl;
    private Text textSharedSpace;
    private Text textWorkspace;
    private Text textUsername;
    private Text textPassword;
    private Label labelConnectionStatus;
    private Button buttonTestConnection;

    private ISecurePreferences securePrefs = PluginPreferenceStorage.getSecurePrefs();
    private TestService testService = Activator.getInstance(TestService.class);

    private ILog logger = Activator.getDefault().getLog();
    private Button buttonUserPassAuth;
    private Button buttonBrowserAuth;

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);

        // apply always enabled on browser auth
        getApplyButton().setEnabled(buttonBrowserAuth.getSelection());
    }

    @Override
    protected Control createContents(Composite parent) {

        GridLayout gridLayout = new GridLayout(1, false);
        parent.setLayout(gridLayout);

        Label labelServerUrl = new Label(parent, SWT.NONE);
        labelServerUrl.setText("Server URL:");

        textServerUrl = new Text(parent, SWT.BORDER);
        textServerUrl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Label labelSharedSpace = new Label(parent, SWT.NONE);
        labelSharedSpace.setText("Shared space:");

        textSharedSpace = new Text(parent, SWT.BORDER);
        textSharedSpace.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        textSharedSpace.setEnabled(false);

        Label labelWorkspace = new Label(parent, SWT.NONE);
        labelWorkspace.setText("Workspace:");

        textWorkspace = new Text(parent, SWT.BORDER);
        textWorkspace.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        textWorkspace.setEnabled(false);

        Composite authComposite = new Composite(parent, SWT.NONE);
        authComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

        Label separatorAuthCompositeTop = new Label(authComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
        separatorAuthCompositeTop.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        GridLayout gl_authComposite = new GridLayout();
        gl_authComposite.marginWidth = 0;
        authComposite.setLayout(gl_authComposite);

        Label labelAuthMethod = new Label(authComposite, SWT.NONE);
        labelAuthMethod.setText("Authentication:");
        labelAuthMethod.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        
        Composite buttonUserPassComposite = new Composite(authComposite, SWT.NONE);
        GridData gd_buttonUserPassComposite = new GridData(SWT.FILL, SWT.FILL, true, false);
        gd_buttonUserPassComposite.verticalIndent = 7;
        buttonUserPassComposite.setLayoutData(gd_buttonUserPassComposite);
        RowLayout rowLayoutUserPassAuth = new RowLayout();
        rowLayoutUserPassAuth.marginLeft = 0;
        rowLayoutUserPassAuth.center = true;
        buttonUserPassComposite.setLayout(rowLayoutUserPassAuth);        

        buttonUserPassAuth = new Button(buttonUserPassComposite, SWT.CHECK);
        buttonUserPassAuth.setText("Login with username and password");
        
        Label infoUserPassAuth = new Label(buttonUserPassComposite, SWT.NONE);
        infoUserPassAuth.addMouseListener(new MouseAdapter() {            
            @Override
            public void mouseDown(MouseEvent e) {
                MessageDialog.openInformation(null, "Login with username and password", userPassAuthInfoText);
            }
        });
        infoUserPassAuth.setToolTipText(userPassAuthInfoText);
        infoUserPassAuth.setImage(ImageResources.INFO.getImage());

        Composite userPassComposite = new Composite(authComposite, SWT.NONE);
        GridData gd_userPassComposite = new GridData(SWT.FILL, SWT.FILL, true, false);
        gd_userPassComposite.horizontalIndent = 15;
        userPassComposite.setLayoutData(gd_userPassComposite);

        GridLayout gl_userPassComposite = new GridLayout();
        gl_userPassComposite.marginWidth = 0;
        gl_userPassComposite.marginHeight = 0;
        gl_userPassComposite.horizontalSpacing = 0;
        userPassComposite.setLayout(gl_userPassComposite);

        Label labelUsername = new Label(userPassComposite, SWT.NONE);
        labelUsername.setText("Username:");

        textUsername = new Text(userPassComposite, SWT.BORDER);
        textUsername.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Label labelPassword = new Label(userPassComposite, SWT.NONE);
        labelPassword.setText("Password:");

        textPassword = new Text(userPassComposite, SWT.BORDER | SWT.PASSWORD);
        textPassword.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Composite buttonBrowserAuthComposite = new Composite(authComposite, SWT.NONE);
        GridData gd_buttonBrowserAuth = new GridData(SWT.FILL, SWT.FILL, true, false);
        gd_buttonBrowserAuth.verticalIndent = 7;
        buttonBrowserAuthComposite.setLayoutData(gd_buttonBrowserAuth);
        RowLayout rowLayoutBrowserAuth = new RowLayout();
        rowLayoutBrowserAuth.marginLeft = 0;
        rowLayoutBrowserAuth.center = true;
        buttonBrowserAuthComposite.setLayout(rowLayoutBrowserAuth);
        
        buttonBrowserAuth = new Button(buttonBrowserAuthComposite, SWT.CHECK);
        buttonBrowserAuth.setText("Login using a browser");
        
        Label infoBrowserAuth = new Label(buttonBrowserAuthComposite, SWT.NONE);
        infoBrowserAuth.addMouseListener(new MouseAdapter() {            
            @Override
            public void mouseDown(MouseEvent e) {
                MessageDialog.openInformation(null, "Login using a browser", browserAuthInfoText);
            }
        });
        infoBrowserAuth.setToolTipText(browserAuthInfoText);
        infoBrowserAuth.setImage(ImageResources.INFO.getImage());

        Label separatorAuthCompositeBottom = new Label(authComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
        separatorAuthCompositeBottom.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Composite testConnectionContainer = new Composite(authComposite, SWT.NONE);
        testConnectionContainer.setLayout(new GridLayout(2, false));
        ((GridLayout) testConnectionContainer.getLayout()).marginWidth = 0;
        testConnectionContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        buttonTestConnection = new Button(testConnectionContainer, SWT.PUSH);
        buttonTestConnection.setText("Test connection");

        labelConnectionStatus = new Label(testConnectionContainer, SWT.NONE);
        labelConnectionStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

        buttonTestConnection.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                setConnectionStatus(null, null);

                BusyIndicator.showWhile(Display.getCurrent(), () -> {
                    testConnection();
                });
            }
        });

        KeyAdapter keyListener = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if ((e.widget == textUsername || e.widget == textPassword) && textServerUrl.getText().isEmpty())
                    return;
                setFieldsFromServerUrl(true);
            }
        };

        textServerUrl.addKeyListener(keyListener);
        textUsername.addKeyListener(keyListener);
        textPassword.addKeyListener(keyListener);

        buttonUserPassAuth.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setViewIsBrowserAuth(!buttonUserPassAuth.getSelection());
                setFieldsFromServerUrl(true);
            }
        });
        buttonBrowserAuth.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setViewIsBrowserAuth(buttonBrowserAuth.getSelection());
                setFieldsFromServerUrl(true);
            }
        });

        setHints(true);
        loadSavedValues();
        setFieldsFromServerUrl(false);

        return parent;
    }

    @Override
    protected void performApply() {
        apply();
    }

    @Override
    public boolean performOk() {
        if (getApplyButton().getEnabled()) {
            apply();
        }
        return true;
    }

    private void setViewIsBrowserAuth(boolean isBrowserAuth) {
        buttonBrowserAuth.setSelection(isBrowserAuth);
        buttonUserPassAuth.setSelection(!isBrowserAuth);
        if (isBrowserAuth) {
            buttonBrowserAuth.setFocus();
            textUsername.setText("");
            textPassword.setText("");
        }
        textUsername.setEnabled(!isBrowserAuth);
        textPassword.setEnabled(!isBrowserAuth);
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
        textUsername.setText("");
        buttonUserPassAuth.setSelection(true);
        textPassword.setText("");
        textServerUrl.setText("");
        if (!Activator.getConnectionSettings().isEmpty()) {
            setFieldsFromServerUrl(false);
            getApplyButton().setEnabled(true);
        }
        setConnectionStatus(false, "");
    }

    private void setConnectionStatus(Boolean success, String errorMessage) {
        if (success == null) {
            labelConnectionStatus.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
            labelConnectionStatus.setText("Testing connection, please wait.");
        } else if (success) {
            labelConnectionStatus.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN));
            labelConnectionStatus.setText("Connection successful.");
        } else {
            labelConnectionStatus.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
            labelConnectionStatus.setText(errorMessage);
        }
        buttonTestConnection.setEnabled(success != null);
        labelConnectionStatus.getShell().layout(new Control[] { labelConnectionStatus.getParent() }, SWT.DEFER);
    }

    private void loadSavedValues() {
        try {
            textServerUrl.setText(securePrefs.get(PluginPreferenceStorage.PreferenceConstants.OCTANE_SERVER_URL, ""));

            String browserAuth = securePrefs.get(PreferenceConstants.IS_BROWSER_AUTH, Boolean.FALSE.toString());

            if (Boolean.parseBoolean(browserAuth)) {
                setViewIsBrowserAuth(true);
            } else {
                setViewIsBrowserAuth(false);
                textUsername.setText(securePrefs.get(PluginPreferenceStorage.PreferenceConstants.USERNAME, ""));
                textPassword.setText(securePrefs.get(PluginPreferenceStorage.PreferenceConstants.PASSWORD, ""));
            }
        } catch (StorageException e) {
            logger.log(new Status(Status.ERROR, Activator.PLUGIN_ID, Status.ERROR,
                    "An exception has occured when loading the Octane connection details", e));
        }
    }

    private void saveValues() {
        try {
            securePrefs.put(PreferenceConstants.OCTANE_SERVER_URL, textServerUrl.getText(), false);

            if (buttonBrowserAuth.getSelection()) {
                securePrefs.put(PreferenceConstants.IS_BROWSER_AUTH, Boolean.TRUE.toString(), false);
            } else {
                securePrefs.put(PreferenceConstants.IS_BROWSER_AUTH, Boolean.FALSE.toString(), false);
                securePrefs.put(PreferenceConstants.USERNAME, textUsername.getText(), false);
                securePrefs.put(PreferenceConstants.PASSWORD, textPassword.getText(), true);
            }

            securePrefs.flush();
        } catch (StorageException | IOException e) {
            logger.log(new Status(Status.ERROR, Activator.PLUGIN_ID, Status.ERROR,
                    "An exception has occured when saving the Octane connection details", e));
        }
    }

    private void setHints(boolean forServerUrlField) {
        if (forServerUrlField) {
            textServerUrl.setMessage("Copy paste your Octane URL from the browser here...");
        }
        textSharedSpace.setText("Retrieved from server URL");
        textWorkspace.setText(textSharedSpace.getText());
    }

    private void apply() {

        if (isConnectionSettingsEmpty()) {
            Activator.setConnectionSettings(new ConnectionSettings());
            saveValues();
            getApplyButton().setEnabled(false);
            return;
        }

        try {
            if (Activator.getConnectionSettings().equals(getConnectionSettingsFromView())) {
                return;
            }
        } catch (ServiceException e) {
            setConnectionStatus(false, e.getMessage() + "\n" + CORRECT_URL_FORMAT_MESSAGE);
        }

        BusyIndicator.showWhile(Display.getCurrent(), () -> {
            ConnectionSettings connectionSettings = testConnection();
            if (connectionSettings != null) {
                textServerUrl.setText(UrlParser.createUrlFromConnectionSettings(connectionSettings));
                saveValues();
                getApplyButton().setEnabled(false);
                Activator.setConnectionSettings(connectionSettings);

                // If apply is pressed when sso login option is selected, close
                // the preference window
                // Otherwise it would stop the login dialog from octane from
                // being the "on-top" shell
                if (connectionSettings.getAuthentication() instanceof GrantTokenAuthentication) {
                    // small change the active shell is no longer the preference dialog
                    // calculated risk
                    PlatformUI.getWorkbench().getDisplay().getActiveShell().close();
                }
            }
        });

    }

    private boolean isConnectionSettingsEmpty() {
        return StringUtils.isEmpty(textServerUrl.getText()) && StringUtils.isEmpty(textUsername.getText())
                && StringUtils.isEmpty(textPassword.getText());
    }

    private ConnectionSettings testConnection() {
        ConnectionSettings newConnectionSettings;

        try {
            newConnectionSettings = getConnectionSettingsFromView();
        } catch (ServiceException e) {
            setConnectionStatus(false, e.getMessage() + "\n" + CORRECT_URL_FORMAT_MESSAGE);
            return null;
        }

        testOctaneVersion(newConnectionSettings);

        if (newConnectionSettings.getAuthentication() instanceof UserAuthentication) {
            UserAuthentication userAuthentication = (UserAuthentication) newConnectionSettings.getAuthentication();

            try {
                validateUsernameAndPassword(userAuthentication.getUserName(), userAuthentication.getPassword());
            } catch (ServiceException e) {
                setConnectionStatus(false, e.getMessage());
                return null;
            }
        }

        try {
            // Will only test authentication if it's not browser based
            testService.testConnection(newConnectionSettings);
            setConnectionStatus(true, null);
        } catch (Exception e) {
            String description;

            if (e instanceof OctaneException) {
                OctaneException octaneException = (OctaneException) e;
                description = ErrorComposite.getDescriptionFromOctaneException(octaneException.getError());
            } else if (e.getCause() != null && e.getCause() instanceof HttpResponseException) {
                // sdk exceptions are wrapped in Runtime exceptions
                HttpResponseException httpResponseException = (HttpResponseException) e.getCause();
                description = httpResponseException.getStatusCode() == 401 ? "Invalid username or password." : httpResponseException.getMessage();
            } else {
                description = e.getMessage();
            }

            setConnectionStatus(false, description);
            return null;
        }

        return newConnectionSettings;
    }

    private void testOctaneVersion(ConnectionSettings connectionSettings) {
        OctaneVersion version;
        try {
            version = OctaneVersionService.getOctaneVersion(connectionSettings);
            version.discardBuildNumber();
            if (version.compareTo(OctaneVersion.DYNAMO) < 0) {
                new InfoPopup("ALM Octane Settings",
                        "Octane version not supported. This plugin works with Octane versions starting " + OctaneVersion.DYNAMO.getVersionString(),
                        550, 100).open();
            }
            if (buttonBrowserAuth.getSelection() && version.compareTo(OctaneVersion.INTER_P2) < 0) {
                new InfoPopup("ALM Octane Settings",
                        "Login with browser is only supported starting from Octane server version: " + OctaneVersion.INTER_P2.getVersionString(),
                        550, 100).open();

                // Reset to user pass
                setViewIsBrowserAuth(false);
                connectionSettings.setAuthentication(new UserAuthentication("", ""));
            }
        } catch (Exception ex) {
            version = OctaneVersionService.fallbackVersion;

            StringBuilder message = new StringBuilder();

            message.append("Failed to determine Octane server version, http call to ")
                    .append(OctaneVersionService.getServerVersionUrl(connectionSettings))
                    .append(" failed. Assuming server version is higher or equal to: ")
                    .append(version.getVersionString());

            new InfoPopup("ALM Octane Settings", message.toString(), 550, 100).open();
        }
    }

    private void validateUsernameAndPassword(String username, String password) throws ServiceException {
        StringBuilder errorMessageBuilder = new StringBuilder();
        if (StringUtils.isEmpty(username)) {
            errorMessageBuilder.append("Username cannot be blank.");
        }
        if (errorMessageBuilder.length() != 0) {
            errorMessageBuilder.append(" ");
        }
        if (StringUtils.isEmpty(password)) {
            errorMessageBuilder.append("Password cannot be blank.");
        }

        if (errorMessageBuilder.length() != 0) {
            throw new ServiceException(errorMessageBuilder.toString());
        }
    }

    private ConnectionSettings getConnectionSettingsFromView() throws ServiceException {
        if (buttonBrowserAuth.getSelection()) {
            return UrlParser.resolveConnectionSettings(textServerUrl.getText(), new GrantTokenAuthentication());
        } else {
            return UrlParser.resolveConnectionSettings(textServerUrl.getText(),
                    new UserAuthentication(textUsername.getText(), textPassword.getText()));
        }
    }

    private void setFieldsFromServerUrl(boolean setStatus) {
        ConnectionSettings connectionSettings;
        try {
            connectionSettings = getConnectionSettingsFromView();
            textSharedSpace.setText(connectionSettings.getSharedSpaceId() + "");
            textWorkspace.setText(connectionSettings.getWorkspaceId() + "");
            if (setStatus) {
                getApplyButton().setEnabled(!connectionSettings.equals(Activator.getConnectionSettings()));
            }
            setConnectionStatus(false, "");
        } catch (ServiceException e) {
            setHints(false);
            if (setStatus) {
                getApplyButton().setEnabled(false);
                setConnectionStatus(false,
                        e.getMessage() + "\n" + CORRECT_URL_FORMAT_MESSAGE);
            }
        }
    }
}
