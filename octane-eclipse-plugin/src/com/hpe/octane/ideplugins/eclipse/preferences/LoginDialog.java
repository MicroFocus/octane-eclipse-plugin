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

package com.hpe.octane.ideplugins.eclipse.preferences;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

import com.hpe.octane.ideplugins.eclipse.ui.util.LoadingComposite;
import com.hpe.octane.ideplugins.eclipse.ui.util.StackLayoutComposite;

public class LoginDialog extends Dialog {

    public  static final String DEFAULT_TITLE = "OpenText™️ Core Software Delivery Platform Plugin: Login";
    private static final String LOGIN_TEXT = "If the page below does not display correctly, <a href=\"\">click here to use your system default browser.</a>";

    private String loginUrl;
    private boolean wasClosed;
    private Shell shell;

    public LoginDialog(Shell shell, String loginPageUrl) {
        super(shell);
        this.loginUrl = loginPageUrl;
        setShellStyle(SWT.CLOSE | SWT.BORDER | SWT.TITLE | SWT.RESIZE);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        this.shell = shell;
        shell.setText("OpenText™️ Core Software Delivery Platform Plugin: Login");
    }
    
    public void setTitle(String title) {
        if(shell != null && !shell.isDisposed()) {
            shell.setText(title);
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginHeight = 10;
        gridLayout.marginWidth = 10;
        container.setLayout(gridLayout);

        Link link = new Link(container, SWT.NONE);
        link.setText(LOGIN_TEXT);
        
        StackLayoutComposite stackLayoutComposite = new StackLayoutComposite(container, SWT.BORDER);
        stackLayoutComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        LoadingComposite loadingComposite = new LoadingComposite(stackLayoutComposite, SWT.NONE, 128);

        ScrolledComposite scrolledComposite = new ScrolledComposite(stackLayoutComposite, SWT.H_SCROLL | SWT.H_SCROLL);
        Browser.clearSessions();   
        Browser browser = new Browser(scrolledComposite, SWT.NONE);
        scrolledComposite.setMinSize(800, 600);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setContent(browser);
        browser.setUrl(LoginDialog.this.loginUrl);
  
        browser.addLocationListener(new LocationListener() { 
            @Override
            public void changing(LocationEvent event) {
                stackLayoutComposite.showControl(loadingComposite);
            }
            @Override
            public void changed(LocationEvent event) {
                stackLayoutComposite.showControl(scrolledComposite);
            }
        });
        
        // Event handling when users click on links.
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Program.launch(LoginDialog.this.loginUrl);
            }
        });

        return container;
    }
    
    protected Control createButtonBar(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(0, 0));
        return composite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {}

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    public boolean close() {
        this.wasClosed = true;
        return super.close();
    }

    public boolean wasClosed() {
        return wasClosed;
    }

    @Override
    protected Point getInitialSize() {
        return new Point(800, 600);
    }

}