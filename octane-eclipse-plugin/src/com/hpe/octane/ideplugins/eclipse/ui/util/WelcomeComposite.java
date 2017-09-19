/*******************************************************************************
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
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

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;

import com.hpe.octane.ideplugins.eclipse.util.resource.ImageResources;
import com.hpe.octane.ideplugins.eclipse.util.resource.SWTResourceManager;

public class WelcomeComposite extends Composite {

    private static final String OCTANE_SETTINGS_TEXT = "To start, go to Settings and connect.";
    private static final String WELCOME_TEXT = "Welcome to the ALM Octane plugin";
    private Color backgroundColor = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry().get(JFacePreferences.CONTENT_ASSIST_BACKGROUND_COLOR);;
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
        

        Label lblCompanyLogo = new Label(this, SWT.NONE);
        lblCompanyLogo.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, true, 1, 1));
        lblCompanyLogo.setImage(ImageResources.HPE_LOGO.getImage());
        lblCompanyLogo.addPaintListener(new PaintListener() {
    		@Override
    	    public void paintControl(PaintEvent paintEvent) {        
        	    lblCompanyLogo.setBackground(backgroundColor);        	    
    	    }
        });  
        

        Label lblProductLogo = new Label(this, SWT.NONE);
        lblProductLogo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
        lblProductLogo.setImage(ImageResources.OCTANE_LOGO.getImage());
        lblProductLogo.addPaintListener(new PaintListener() {
    		@Override
    	    public void paintControl(PaintEvent paintEvent) {        
        	    lblProductLogo.setBackground(backgroundColor);        	    
    	    }
        });  
        
    
        Label lblWelcome = new Label(this, SWT.NONE);
        lblWelcome.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 3, 1));
        lblWelcome.setText(WELCOME_TEXT);
        lblWelcome.addPaintListener(new PaintListener() {
    		@Override
    	    public void paintControl(PaintEvent paintEvent) {        
        	    lblWelcome.setBackground(backgroundColor);        	    
    	    }
        });  
        

        Link link = new Link(this, SWT.NONE);
        link.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, true, 3, 1));
        link.setText("<A>" + OCTANE_SETTINGS_TEXT + "</A>");
        link.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                settingsLinkClicked.run();
            }
        });
        link.addPaintListener(new PaintListener() {
    		@Override
    	    public void paintControl(PaintEvent paintEvent) {        
        	    link.setBackground(backgroundColor);        	    
    	    }
        });  
        
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

}
