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
package com.hpe.octane.ideplugins.eclipse.ui.mywork;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.hpe.octane.ideplugins.eclipse.ui.util.resource.ImageResources;
import com.hpe.octane.ideplugins.eclipse.ui.util.resource.SWTResourceManager;

public class NoWorkComposite extends Composite {

    private static final String NO_WORK_TEXT = "You're Awesome! You finished all your work!";
    private static final String NO_WORK_LINK_TEXT = "You may want to talk with your team leader... or have some fun!";
    private static final Image unidragonImage = ImageResources.S_ROCKET.getImage();
    private static final Color hotPink = SWTResourceManager.getColor(255, 105, 180);
    private static final Color defaultColor = SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND);

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    public NoWorkComposite(Composite parent, int style, Runnable linkClickedRunnable) {
        super(parent, style);
        setLayout(new GridLayout(1, false));

        Label lblRocket = new Label(this, SWT.NONE);
        lblRocket.setImage(unidragonImage);
        lblRocket.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, true, true, 1, 1));

        Label lblMessage = new Label(this, SWT.NONE);
        lblMessage.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, false, 1, 1));
        lblMessage.setText(NO_WORK_TEXT);

        Label lblLink = new Label(this, SWT.NONE);
        lblLink.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, true, 1, 1));
        lblLink.setText(NO_WORK_LINK_TEXT);
        lblLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                linkClickedRunnable.run();
            }
        });
        lblLink.addListener(SWT.MouseHover, new Listener() {
            @Override
            public void handleEvent(Event event) {
                lblLink.setForeground(hotPink);
            }
        });
        lblLink.addListener(SWT.MouseExit, new Listener() {
            @Override
            public void handleEvent(Event event) {
                lblLink.setForeground(defaultColor);
            }
        });

    }

    public void setLinkClickedRunnable(Runnable linkClicked) {

    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
}
