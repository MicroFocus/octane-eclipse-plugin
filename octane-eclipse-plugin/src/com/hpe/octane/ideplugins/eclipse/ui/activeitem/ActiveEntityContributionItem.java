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
package com.hpe.octane.ideplugins.eclipse.ui.activeitem;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

import com.hpe.octane.ideplugins.eclipse.Activator;
import com.hpe.octane.ideplugins.eclipse.preferences.PluginPreferenceStorage;
import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.EntityModelEditor;
import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.EntityModelEditorInput;
import com.hpe.octane.ideplugins.eclipse.ui.util.icon.EntityIconFactory;
import com.hpe.octane.ideplugins.eclipse.ui.util.resource.ImageResources;
import com.hpe.octane.ideplugins.eclipse.util.CommitMessageUtil;

public class ActiveEntityContributionItem extends WorkbenchWindowControlContribution {

    private static final ILog logger = Activator.getDefault().getLog();
    private static ToolBarManager manager;
    private static EntityModelEditorInput entityModelEditorInput;
    private static ToolBar toolbar;
    private static Action openAction = new Action() {
        @Override
        public void run() {
            IWorkbench wb = PlatformUI.getWorkbench();
            IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
            IWorkbenchPage page = win.getActivePage();
            try {
                page.openEditor(entityModelEditorInput, EntityModelEditor.ID);
            } catch (PartInitException ex) {
            }
        }
    };
    
    private static Action commitMessageAction = new Action() {
        @Override
        public void run() {
            CommitMessageUtil.copyMessageIfValid(null);
        }
    };
    
    private static Action stopWorkAction = new Action() {
        @Override
        public void run() {
            PluginPreferenceStorage.setActiveItem(null);
        }
    };

    public ActiveEntityContributionItem() {
        PluginPreferenceStorage.addPrefenceChangeHandler(PluginPreferenceStorage.PreferenceConstants.ACTIVE_ITEM_ID, (() -> {
            entityModelEditorInput = PluginPreferenceStorage.getActiveItem();
            addAction();
        }));
        entityModelEditorInput = PluginPreferenceStorage.getActiveItem();
    }

    public ActiveEntityContributionItem(String id) {
        super(id);
        PluginPreferenceStorage.addPrefenceChangeHandler(PluginPreferenceStorage.PreferenceConstants.ACTIVE_ITEM_ID, (() -> {
            entityModelEditorInput = PluginPreferenceStorage.getActiveItem();
            addAction();
        }));
        entityModelEditorInput = PluginPreferenceStorage.getActiveItem();
    }

    @Override
    protected Control createControl(Composite parent) {
        try {
            // Brilliant
            toolbar = (ToolBar) parent.getParent();
            manager = (ToolBarManager) toolbar.getData();
            addAction();
        } catch (Exception e) {
            logger.log(new Status(
                    Status.ERROR,
                    Activator.PLUGIN_ID,
                    Status.OK,
                    "Failed to add active item menu contribution to toolbar",
                    e));
        }
        return null;
    }

    private static void addAction() {
        manager.removeAll();

        if (entityModelEditorInput != null) {
            openAction.setText(entityModelEditorInput.getId() + "");
            Image img = EntityIconFactory.getInstance().getImageIcon(entityModelEditorInput.getEntityType(), 20, 8);
            openAction.setImageDescriptor(
                    new ImageDataImageDescriptor(img.getImageData()));
            openAction.setEnabled(true);
            
            commitMessageAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
            commitMessageAction.setToolTipText("Generate and copy commit message to clipboard");
            
            stopWorkAction.setImageDescriptor(new ImageDataImageDescriptor(ImageResources.STOP_TIMER_16X16.getImage().getImageData()));
            stopWorkAction.setToolTipText("Stop work on current entity");
            
        } else {
            openAction.setImageDescriptor(
                    new ImageDataImageDescriptor(ImageResources.DISMISS.getImage().getImageData()));
            openAction.setText("No active item");
            openAction.setEnabled(false);
        }

        ActionContributionItem contributionItem = new ActionContributionItem(openAction);
        contributionItem.setMode(ActionContributionItem.MODE_FORCE_TEXT);
        manager.add(contributionItem);
        
        if (entityModelEditorInput != null) {
            manager.add(new ActionContributionItem(commitMessageAction));
            manager.add(new ActionContributionItem(stopWorkAction));
        }
        
        manager.update(true);

        // Just perfect
        toolbar.getParent().getParent().layout(true, true);
        toolbar.getParent().redraw();
        toolbar.getParent().update();

    }

}
