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
package com.hpe.octane.ideplugins.eclipse.ui.search;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.hpe.octane.ideplugins.eclipse.ui.util.resource.ImageResources;

public class NoSearchResultsComposite extends Composite {

    public NoSearchResultsComposite(Composite parent, int style) {
        super(parent, style);
        setLayout(new GridLayout(1, false));

        Composite rootComposite = new Composite(this, SWT.NONE);
        rootComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
        rootComposite.setLayout(new GridLayout(1, false));

        Label lblRobot = new Label(rootComposite, SWT.NONE);
        lblRobot.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblRobot.setImage(ImageResources.S_NO_ITEMS_TO_DISPLAY.getImage());

        Label lblNoResults = new Label(rootComposite, SWT.NONE);
        lblNoResults.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblNoResults.setText("No results");

    }

}