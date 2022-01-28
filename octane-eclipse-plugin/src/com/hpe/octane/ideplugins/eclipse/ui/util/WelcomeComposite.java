/*******************************************************************************
 * © Copyright 2017-2022 Micro Focus or one of its affiliates.
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
package com.hpe.octane.ideplugins.eclipse.ui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;

import com.hpe.octane.ideplugins.eclipse.ui.util.resource.ImageResources;

public class WelcomeComposite extends Composite {

    private static final String OCTANE_SETTINGS_TEXT = "To start, go to Settings and connect.";
    private static final String WELCOME_TEXT = "Welcome to the ALM Octane plugin";

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    public WelcomeComposite(Composite parent, int style, Runnable settingsLinkClicked) {
        super(parent, style);

        setLayout(new GridLayout(3, false));
        Label lblPlaceholder = new Label(this, SWT.NONE);
        lblPlaceholder.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 3, 1));


        Label lblProductLogo = new Label(this, SWT.NONE);
        lblProductLogo.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 2, 1));
        lblProductLogo.setImage(ImageResources.OCTANE_LOGO.getImage());

        Label lblWelcome = new Label(this, SWT.NONE);
        lblWelcome.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 2, 1));
        lblWelcome.setText(WELCOME_TEXT);

        Link link = new Link(this, SWT.NONE);
        link.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, true, 2, 1));
        link.setText("<A>" + OCTANE_SETTINGS_TEXT + "</A>");
        link.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                settingsLinkClicked.run();
            }
        });
        

        Label lblCompanyLogo = new Label(this, SWT.NONE);
        lblCompanyLogo.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 2, 1));
        lblCompanyLogo.setImage(ImageResources.MICROFOCUS_LOGO.getImage());
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

}
