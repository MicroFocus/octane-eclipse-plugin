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
package com.hpe.octane.ideplugins.eclipse.ui.util;

import java.util.Arrays;

import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Composite that allows you to add many other {@link Control}, and the switch
 * between which one to show
 */
public class StackLayoutComposite extends Composite {

    // private static final ILog logger = Activator.getDefault().getLog();

    private StackLayout layout;
    private Composite parent;

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    public StackLayoutComposite(Composite parent, int style) {
        super(parent, style);
        this.parent = parent;
        layout = new StackLayout();
        setLayout(layout);
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    public void showControl(Control control) {
    	if(!Arrays.asList(getChildren()).contains(control)){
    		throw new RuntimeException("Cannot show control that is not a child of StackLayoutComposite, control: " + control);
    	}
    	
        layout.topControl = control;
        // layout of parent works
        parent.layout(true, true);
        redraw();
        update();
    }

    public Control getCurrentControl() {
        return layout.topControl;
    }

}
