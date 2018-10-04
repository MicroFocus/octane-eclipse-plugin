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
package com.hpe.octane.ideplugins.eclipse.ui.entitylist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.octane.ideplugins.eclipse.ui.util.icon.EntityIconFactory;
import com.hpe.octane.ideplugins.eclipse.ui.util.resource.SWTResourceManager;
import com.hpe.octane.ideplugins.eclipse.util.PredefinedEntityComparator;

public class EntityTypeSelectorComposite extends Composite {

    private Map<Entity, Button> checkBoxes = new HashMap<>();
    private List<Runnable> selectionListeners = new ArrayList<>();
    private Label totalCountLbl;
    private Color backgroundColor = SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT);

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
        
        List<Entity> sortedEntityTypes = Arrays
                .stream(supportedEntityTypes)
                .sorted(new PredefinedEntityComparator())
                .collect(Collectors.toList());
        
        for (Entity entity : sortedEntityTypes) {
            Button btnCheckButton = new Button(this, SWT.CHECK);
            btnCheckButton.setBackground(backgroundColor);
            btnCheckButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    fireAllSelectionListeners();
                }
            });

            btnCheckButton.setFont(SWTResourceManager.getBoldFont(btnCheckButton.getFont()));
            btnCheckButton.setData(entity);
            btnCheckButton.setImage(EntityIconFactory.getInstance().getImageIcon(entity, 25, 8));
            checkBoxes.put(entity, btnCheckButton);
        }
        
        totalCountLbl = new Label(this, SWT.NONE);
        totalCountLbl.setFont(SWTResourceManager.getBoldFont(totalCountLbl.getFont()));
        totalCountLbl.setBackground(backgroundColor);
    }

    public void setEntityTypeCount(Map<Entity, Integer> entityTypeCount) {
        checkBoxes.values().forEach(checkBox -> {
            checkBox.setBackground(backgroundColor);
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
        for (Button checkBox : checkBoxes.values()) {
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
        checkBoxes.values().forEach(checkBox -> checkBox.setSelection(true));
        fireAllSelectionListeners();
    }

    public void checkNone() {
        checkBoxes.values().forEach(checkBox -> checkBox.setSelection(false));
        fireAllSelectionListeners();
    }

    private void fireAllSelectionListeners() {
        selectionListeners.forEach(listener -> listener.run());
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
    
    public void refreshIcons() {
        for(Entity entity : checkBoxes.keySet()) {
            Button button = checkBoxes.get(entity);
            button.setImage(EntityIconFactory.getInstance().getImageIcon(entity, 25, 8));
        }
    }

}
