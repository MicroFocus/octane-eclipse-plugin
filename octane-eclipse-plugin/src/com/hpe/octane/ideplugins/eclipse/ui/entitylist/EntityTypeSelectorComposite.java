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
package com.hpe.octane.ideplugins.eclipse.ui.entitylist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.octane.ideplugins.eclipse.util.EntityIconFactory;
import com.hpe.octane.ideplugins.eclipse.util.resource.SWTResourceManager;

public class EntityTypeSelectorComposite extends Composite {

    private static final EntityIconFactory entityIconFactory = new EntityIconFactory(20, 20, 7);
    private List<Button> checkBoxes = new ArrayList<>();
    private List<Runnable> selectionListeners = new ArrayList<>();
    private Label totalCountLbl;
    
    private Color backgroundColor = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry().get(JFacePreferences.CONTENT_ASSIST_BACKGROUND_COLOR);
    private Color foregroundColor = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry().get(JFacePreferences.CONTENT_ASSIST_FOREGROUND_COLOR);

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    public EntityTypeSelectorComposite(Composite parent, int style, Entity... supportedEntityTypes) {
        super(parent, style);
        RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
        rowLayout.center = true;
        rowLayout.spacing = 7;
        setLayout(rowLayout);

        for (Entity entity : supportedEntityTypes) {
            Button btnCheckButton = new Button(this, SWT.CHECK);          
            btnCheckButton.addPaintListener(new PaintListener() {
        		@Override
        	    public void paintControl(PaintEvent paintEvent) {        
        			btnCheckButton.setBackground(backgroundColor);
        	    }
            });  

            btnCheckButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    fireAllSelectionListeners();
                }
            });

            btnCheckButton.setFont(SWTResourceManager.getBoldFont(btnCheckButton.getFont()));
            btnCheckButton.setData(entity);
            btnCheckButton.setImage(entityIconFactory.getImageIcon(entity));
            checkBoxes.add(btnCheckButton);
        }

        totalCountLbl = new Label(this, SWT.NONE);
        totalCountLbl.setFont(SWTResourceManager.getBoldFont(totalCountLbl.getFont()));
        totalCountLbl.addPaintListener(new PaintListener() {
    		@Override
    	    public void paintControl(PaintEvent paintEvent) {        
    			totalCountLbl.setBackground(backgroundColor);
    	    }
        }); 
    }

    public void setEntityTypeCount(Map<Entity, Integer> entityTypeCount) {
        checkBoxes.forEach(checkBox -> {
            Integer count = entityTypeCount.get(checkBox.getData());
            if (count != null) {
                checkBox.setText("" + count);
            } else {
                checkBox.setText("0");
            }
        });
        totalCountLbl.setText("Total: " + entityTypeCount.values().stream().mapToInt(i -> i.intValue()).sum());
    }

    public Set<Entity> getCheckedEntityTypes() {
        Set<Entity> result = new HashSet<>();
        for (Button checkBox : checkBoxes) {
            if (checkBox.getSelection()) {
                result.add((Entity) checkBox.getData());
            }
        }
        return result;
    }

    public void addSelectionListener(Runnable listener) {
        selectionListeners.add(listener);
    }

    public void checkAll() {
        checkBoxes.forEach(checkBox -> checkBox.setSelection(true));
        fireAllSelectionListeners();
    }

    public void checkNone() {
        checkBoxes.forEach(checkBox -> checkBox.setSelection(false));
        fireAllSelectionListeners();
    }

    private void fireAllSelectionListeners() {
        selectionListeners.forEach(listener -> listener.run());
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

}
