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

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.hpe.adm.nga.sdk.exception.OctaneException;
import com.hpe.adm.octane.ideplugins.services.TestService;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.exception.ServiceException;
import com.hpe.adm.octane.ideplugins.services.nonentity.OctaneVersionService;
import com.hpe.adm.octane.ideplugins.services.util.OctaneVersion;
import com.hpe.adm.octane.ideplugins.services.util.UrlParser;
import com.hpe.octane.ideplugins.eclipse.Activator;
import com.hpe.octane.ideplugins.eclipse.preferences.PluginPreferenceStorage.PreferenceConstants;
import com.hpe.octane.ideplugins.eclipse.ui.util.InfoPopup;
import com.hpe.octane.ideplugins.eclipse.ui.util.error.ErrorComposite;

public class PluginPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
    public PluginPreferencePage() {
    }

    public static final String ID = "com.hpe.octane.ideplugins.eclipse.preferences.PluginPreferencePage";
    public static final String CORRECT_URL_FORMAT_MESSAGE = "Example: (http|https)://{serverurl[:port]}/?p={sharedspaceId}/{workspaceId}";

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

    @Override
    public void init(IWorkbench workbench) {

    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        getApplyButton().setEnabled(false);
    }

    @Override
    protected Control createContents(Composite parent) {

        GridLayout gridLayout = new GridLayout();
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

        Label separator1 = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        Label labelAuthMethod = new Label(parent, SWT.NONE);
        labelAuthMethod.setText("Authentication method:");
        
        Combo comboBoxAuthType = new Combo(parent, SWT.READ_ONLY); 
        comboBoxAuthType.setItems(new String[] {"Username and Password", "Browser"});      
        comboBoxAuthType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        Composite userPassComposite = new Composite(parent, SWT.NONE);
        userPassComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout gl_userPassComposite = new GridLayout();
        gl_userPassComposite.marginWidth = 0;
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

        Composite testConnectionContainer = new Composite(userPassComposite, SWT.NONE);
        testConnectionContainer.setLayout(new GridLayout(2, false));
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        testConnectionContainer.setLayoutData(gridData);

        buttonTestConnection = new Button(testConnectionContainer, SWT.PUSH);
        buttonTestConnection.setText("Test connection");

        labelConnectionStatus = new Label(testConnectionContainer, SWT.NONE);
        labelConnectionStatus.setLayoutData(gridData);
        
        Button buttonOpenBrowser = new Button(parent, SWT.PUSH);
        buttonOpenBrowser.setText("Open login page in browser");
        
        comboBoxAuthType.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                
                GridData userPassCompositeData = (GridData) userPassComposite.getLayoutData();
                GridData buttonOpenBrowserData = (GridData) buttonOpenBrowser.getLayoutData();
                
                if(comboBoxAuthType.getSelectionIndex() == 0) {
                    userPassComposite.setVisible(true);
                    userPassCompositeData.exclude = false;
                    buttonOpenBrowser.setVisible(false);
                    buttonOpenBrowserData.exclude = true;
                } 
                else if(comboBoxAuthType.getSelectionIndex() == 1){
                    userPassComposite.setVisible(false);
                    userPassCompositeData.exclude = true;
                    buttonOpenBrowser.setVisible(true);
                    buttonOpenBrowserData.exclude = false;
                }
                
                parent.layout(true);
            }
        });
        
        buttonOpenBrowser.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                setConnectionStatus(null, null);
                BusyIndicator.showWhile(Display.getCurrent(), () -> {
                    
                    //Let's go baby
                    
                    ConnectionSettings connectionSettings;
                    
                    try {
                        connectionSettings = UrlParser.resolveConnectionSettings(textServerUrl.getText(), textUsername.getText(), textPassword.getText());
                    } catch (ServiceException ex) {
                        ex.printStackTrace();
                        return;
                    }
                    
                    
                    HttpRequestFactory requestFactory  = new NetHttpTransport().createRequestFactory();
                    HttpRequest request;
                    String rawResponse;
                    
                    try {
                        request = requestFactory.buildGetRequest(new GenericUrl(connectionSettings.getBaseUrl() + "/authentication/grant_tool_token"));
                        rawResponse = request.execute().parseAsString();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        return;
                    }
                    
                    JSONObject response = new JSONObject(rawResponse);
                    
                    try {
                        
                        Desktop.getDesktop().browse(new URI(response.getString("authentication_url")));
                        
                    } catch (JSONException | IOException | URISyntaxException ex) {
                        ex.printStackTrace();
                        return;
                    }
                             
                    //Poll it baby
                    
                    String accessToken = null;
                    
                    for (int i = 0; i < 60; i++) {
                        try {Thread.sleep(1000);} catch (InterruptedException e1) {}
                        
                        
                        ByteArrayContent content = ByteArrayContent.fromString("application/json", "{\"identifier\": \"" + response.getString("identifier") +"\"}");
                        
                        try {
                            request = requestFactory.buildPostRequest(new GenericUrl(connectionSettings.getBaseUrl() + "/authentication/grant_tool_token"), content);
                            rawResponse = request.execute().parseAsString();
                        } catch (IOException e1) {
                            System.err.println("Failed " + i + " times, retrying");
                            continue;
                        }
                        
                        response = new JSONObject(rawResponse);
                        accessToken = response.getString("access_token");
                        break;
                    }
                    
                    // Use it baby
                    if(accessToken != null) {
                        
                        try {
                            
                            request = requestFactory.buildGetRequest(new GenericUrl(connectionSettings.getBaseUrl() + "/api/shared_spaces/1001/workspaces/1002/workspace_users?query=\"email%20EQ%20%27atoth@hpe.com%27\""));
                            request.getHeaders().put("Cookie", Collections.singletonList("LWSSO_COOKIE_KEY=" + accessToken));
                            rawResponse = request.execute().parseAsString();
                            System.out.println(rawResponse);
                            
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        
                        
                    } else {
                        System.err.println("Failed to get access token, timed out, move faster");
                    }
                    
    
                });
            }
        });
        
        
        comboBoxAuthType.select(0);   
        buttonOpenBrowser.setVisible(false);

        setHints(true);
        loadSavedValues();
        setFieldsFromServerUrl(false);

        buttonTestConnection.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                setConnectionStatus(null, null);

                BusyIndicator.showWhile(Display.getCurrent(), () -> {
                    testConnection(textServerUrl.getText(), textUsername.getText(), textPassword.getText());
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

    @Override
    protected void performDefaults() {
        super.performDefaults();
        textUsername.setText("");
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
        labelConnectionStatus.getShell().layout(new Control[] {
                labelConnectionStatus.getParent() }, SWT.DEFER);
    }

    private void loadSavedValues() {
        try {
            textServerUrl.setText(securePrefs.get(PluginPreferenceStorage.PreferenceConstants.OCTANE_SERVER_URL, ""));
            textUsername.setText(securePrefs.get(PluginPreferenceStorage.PreferenceConstants.USERNAME, ""));
            textPassword.setText(securePrefs.get(PluginPreferenceStorage.PreferenceConstants.PASSWORD, ""));
        } catch (StorageException e) {
            logger.log(new Status(Status.ERROR, Activator.PLUGIN_ID, Status.ERROR,
                    "An exception has occured when loading the Octane connection details", e));
        }
    }

    private void saveValues() {
        try {
            securePrefs.put(PreferenceConstants.OCTANE_SERVER_URL, textServerUrl.getText(), false);
            securePrefs.put(PreferenceConstants.USERNAME, textUsername.getText(), false);
            securePrefs.put(PreferenceConstants.PASSWORD, textPassword.getText(), true);
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
            if (Activator.getConnectionSettings()
                    .equals(UrlParser.resolveConnectionSettings(textServerUrl.getText(), textUsername.getText(), textPassword.getText()))) {
                return;
            }
        } catch (ServiceException e) {
            setConnectionStatus(false, e.getMessage() + "\n" + CORRECT_URL_FORMAT_MESSAGE);
        }

        BusyIndicator.showWhile(Display.getCurrent(), () -> {
            ConnectionSettings connectionSettings = testConnection(textServerUrl.getText(), textUsername.getText(), textPassword.getText());
            if (connectionSettings != null) {
                textServerUrl.setText(UrlParser.createUrlFromConnectionSettings(connectionSettings));
                saveValues();
                getApplyButton().setEnabled(false);
                Activator.setConnectionSettings(connectionSettings);
            }
        });
    }

    private boolean isConnectionSettingsEmpty() {
        return StringUtils.isEmpty(textServerUrl.getText()) && StringUtils.isEmpty(textUsername.getText())
                && StringUtils.isEmpty(textPassword.getText());
    }

    private ConnectionSettings testConnection(String serverUrl, String username, String password) {
        ConnectionSettings newConnectionSettings;

        try {
            newConnectionSettings = UrlParser.resolveConnectionSettings(serverUrl, username, password);
        } catch (ServiceException e) {
            setConnectionStatus(false, e.getMessage() + "\n" + CORRECT_URL_FORMAT_MESSAGE);
            return null;
        }

        try {
            validateUsernameAndPassword(username, password);
        } catch (ServiceException e) {
            setConnectionStatus(false, e.getMessage());
            return null;
        }

        try {
            testService.testConnection(newConnectionSettings);
            testOctaneVersion(newConnectionSettings);
            setConnectionStatus(true, null);
        } catch (Exception e) {
            String description;
            
            if(e instanceof OctaneException) {
                OctaneException octaneException = (OctaneException) e;
                description = ErrorComposite.getDescriptionFromOctaneException(octaneException.getError());
            } else if (e.getCause() != null && e.getCause() instanceof HttpResponseException) { //sdk exceptions are wrapped in Runtime exceptions
                HttpResponseException httpResponseException = (HttpResponseException) e.getCause();
                description = httpResponseException.getStatusCode() == 401 ? "Invalid username or password." :  httpResponseException.getMessage();
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

    private void setFieldsFromServerUrl(boolean setStatus) {
        ConnectionSettings connectionSettings;
        try {
            connectionSettings = UrlParser.resolveConnectionSettings(textServerUrl.getText(), textUsername.getText(),
            textPassword.getText());
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
