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

import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.octane.ideplugins.eclipse.ui.entitylist.EntityModelMenuFactory;

/**
 * This class is used to create a menu on the fly for any control inside the an
 * EntityModelRow (Or any composite designed to show an EntityModel). <br>
 * The context menu is created on the fly using the provided factory whenever a
 * menuDetected event happens on any item of the entityModelRow
 *
 */
class EntityModelRowMenuDetectListener implements MenuDetectListener {

    private EntityModel entityModel;
    private EntityModelMenuFactory menuFactory;

    public EntityModelRowMenuDetectListener(Control entityModelComposite, EntityModel entityModel, EntityModelMenuFactory menuFactory) {
        this.entityModel = entityModel;
        this.menuFactory = menuFactory;
        addMenuDetectListener(entityModelComposite, this);
    }

    private static void addMenuDetectListener(Control control, EntityModelRowMenuDetectListener listener) {
        if (control instanceof Composite) {
            for (Control child : ((Composite) control).getChildren()) {
                addMenuDetectListener(child, listener);
            }
        }
        control.addMenuDetectListener(listener);
    }

    @Override
    public void menuDetected(MenuDetectEvent e) {
        if (e.widget instanceof Control) {
            Control control = (Control) e.widget;

            // Dispose old menu if it exists
            if (control.getMenu() != null && !control.getMenu().isDisposed()) {
                control.getMenu().dispose();
            }

            Menu menu = menuFactory.createMenu(entityModel, control);
            control.setMenu(menu);
        }
    }
}
