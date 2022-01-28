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
package com.hpe.octane.ideplugins.eclipse.ui.entitylist.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.octane.ideplugins.eclipse.ui.entitylist.EntityListViewer;
import com.hpe.octane.ideplugins.eclipse.ui.entitylist.EntityModelMenuFactory;
import com.hpe.octane.ideplugins.eclipse.ui.entitylist.EntityMouseListener;
import com.hpe.octane.ideplugins.eclipse.ui.util.resource.SWTResourceManager;

public class FatlineEntityListViewer extends Composite implements EntityListViewer {

    private EntityModelRenderer entityModelRenderer;
    private static final Color selectionBackgroundColor = SWTResourceManager.getColor(SWT.COLOR_WHITE);
    private static final Color selectionForegroundColor = SWTResourceManager.getColor(255, 255, 255);

    private static final Color backgroundColor = SWTResourceManager.getColor(255, 255, 255);
    private static final Color foregroundColor = SWTResourceManager.getColor(0, 0, 0);

    // Keep insertion order
    private BiMap<EntityModel, Control> entities;
    private List<EntityMouseListener> entityMouseListeners = new ArrayList<>();

    private Composite rowComposite;
    private ScrolledComposite rowScrolledComposite;

    private EntityModel previousSelection;
    private EntityModel selection;

    private EntityModelMenuFactory entityModelMenuFactory;

    private Listener displayListener = new Listener() {
        @Override
        public void handleEvent(Event event) {
            handleMouseFilterEvent(event);
        }
    };

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    public FatlineEntityListViewer(
            Composite parent,
            int style,
            EntityModelMenuFactory entityModelMenuFactory,
            EntityModelRenderer entityModelRenderer) {

        super(parent, SWT.NONE);
        setLayout(new FillLayout(SWT.HORIZONTAL));

        this.entityModelRenderer = entityModelRenderer;
        this.entityModelMenuFactory = entityModelMenuFactory;

        rowScrolledComposite = new ScrolledComposite(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        rowScrolledComposite.setExpandHorizontal(true);
        rowScrolledComposite.setExpandVertical(true);

        rowComposite = new Composite(rowScrolledComposite, SWT.NONE);
        rowComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

        GridLayout layout = new GridLayout(1, false);
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        layout.marginTop = 0;
        layout.marginRight = 0;
        layout.marginBottom = 0;
        layout.marginLeft = 0;
        layout.marginWidth = 0;
        layout.marginHeight = 0;

        rowComposite.setLayout(layout);

        rowScrolledComposite.setContent(rowComposite);
        rowScrolledComposite.setMinSize(rowComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        // Selection
        rowComposite.getDisplay().addFilter(SWT.MouseDown, displayListener);
        rowComposite.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                rowComposite.getDisplay().removeFilter(SWT.MouseDown, displayListener);
            }
        });
    }

    @Override
    public void setEntityModels(Collection<EntityModel> entityModels) {
        clearRows();

        Map<EntityModel, Control> tempMap = new LinkedHashMap<>();

        entityModels.forEach(entityModel -> {
            // Create the row
            Control row = entityModelRenderer.createRow(rowComposite, entityModel);
            if (entityModelMenuFactory != null) {
                row.addMenuDetectListener(new EntityModelRowMenuDetectListener(row, entityModel, entityModelMenuFactory));
            }
            row.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            tempMap.put(entityModel, row);
        });
        rowScrolledComposite.setMinSize(rowComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        rowComposite.pack();

        this.entities = ImmutableBiMap.copyOf(tempMap);
    }

    public void recreateRows() {
        setEntityModels(entities.keySet());
    }

    private void clearRows() {
        Arrays.stream(rowComposite.getChildren()).forEach(control -> control.dispose());
    }

    private void handleMouseFilterEvent(Event event) {
        Control row = getEntityModelRowFromMouseFilter(event);
        if (row != null) {
            EntityModel entityModel = entities.inverse().get(row);

            row.setFocus();

            // Selection
            changeSelection(entityModel);

            // Fire listeners
            MouseEvent mouseEvent = new MouseEvent(event);
            for (EntityMouseListener listener : entityMouseListeners) {
                listener.mouseClick(entityModel, mouseEvent);
            }
        } else {
            changeSelection(null);
        }
    }

    private Control getEntityModelRowFromMouseFilter(Event event) {
        if (event.widget instanceof Control) {
            for (Control row : entities.values()) {
                if (containsControl(row, (Control) event.widget)) {
                    return row;
                }
            }
        }
        return null;
    }

    private void changeSelection(EntityModel entityModel) {
        if (entityModel != selection) {
            this.previousSelection = selection;
            this.selection = entityModel;
            paintSelected();
        }
    }

    private void paintSelected() {
        if (entities.containsKey(selection) && selection != null) {
            Control row = entities.get(selection);
            if (!row.isDisposed()) {
                row.setBackground(selectionBackgroundColor);
                row.setForeground(selectionForegroundColor);
            }
        } else {
            selection = null;
        }
        if (entities.containsKey(previousSelection)) {
            Control row = entities.get(previousSelection);
            if (!row.isDisposed()) {
                row.setBackground(backgroundColor);
                row.setForeground(foregroundColor);
            }
        } else {
            previousSelection = null;
        }
    }

    private static boolean containsControl(Control source, Control target) {
        if (source == target) {
            return true;
        } else if (source instanceof Composite) {
            for (Control control : ((Composite) source).getChildren()) {
                if (containsControl(control, target)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void addEntityMouseListener(EntityMouseListener entityMouseListener) {
        entityMouseListeners.add(entityMouseListener);

    }

    @Override
    public void removeEntityMouseListener(EntityMouseListener entityMouseListener) {
        entityMouseListeners.remove(entityMouseListener);
    }

    @Override
    public void setEntityModelMenuFatory(EntityModelMenuFactory entityModelMenuFactory) {
        this.entityModelMenuFactory = entityModelMenuFactory;
        recreateRows();
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
}
