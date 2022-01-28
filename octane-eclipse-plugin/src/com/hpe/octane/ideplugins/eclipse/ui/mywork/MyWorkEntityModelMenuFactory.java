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
package com.hpe.octane.ideplugins.eclipse.ui.mywork;

import static com.hpe.adm.octane.ideplugins.services.util.Util.getUiDataFromModel;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.services.EntityService;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkService;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkUtil;
import com.hpe.adm.octane.ideplugins.services.util.Util;
import com.hpe.octane.ideplugins.eclipse.Activator;
import com.hpe.octane.ideplugins.eclipse.filter.EntityListData;
import com.hpe.octane.ideplugins.eclipse.preferences.PluginPreferenceStorage;
import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.EntityModelEditor;
import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.EntityModelEditorInput;
import com.hpe.octane.ideplugins.eclipse.ui.entitylist.EntityModelMenuFactory;
import com.hpe.octane.ideplugins.eclipse.ui.mywork.job.DismissItemJob;
import com.hpe.octane.ideplugins.eclipse.ui.util.DownloadScriptUtil;
import com.hpe.octane.ideplugins.eclipse.ui.util.InfoPopup;
import com.hpe.octane.ideplugins.eclipse.ui.util.OpenInBrowser;
import com.hpe.octane.ideplugins.eclipse.ui.util.icon.EntityIconFactory;
import com.hpe.octane.ideplugins.eclipse.ui.util.resource.ImageResources;
import com.hpe.octane.ideplugins.eclipse.util.CommitMessageUtil;
import com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants;

public class MyWorkEntityModelMenuFactory implements EntityModelMenuFactory {

    
    private static EntityService entityService = Activator.getInstance(EntityService.class);
    private static MyWorkService myWorkService = Activator.getInstance(MyWorkService.class);
    private static DownloadScriptUtil downloadScriptUtil = Activator.getInstance(DownloadScriptUtil.class);
    private EntityListData entityListData;

    public MyWorkEntityModelMenuFactory(EntityListData entityListData) {
        this.entityListData = entityListData;
    }

    private void openDetailTab(Integer entityId, Entity entityType) {
        IWorkbench wb = PlatformUI.getWorkbench();
        IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
        IWorkbenchPage page = win.getActivePage();

        EntityModelEditorInput entityModelEditorInput = new EntityModelEditorInput(entityId, entityType);
        try {
            page.openEditor(entityModelEditorInput, EntityModelEditor.ID);
        } catch (PartInitException ex) {
        }
    }

    @Override
    public Menu createMenu(EntityModel userItem, Control menuParent) {

        Menu menu = new Menu(menuParent);
        
        final EntityModel entityModel;
    	if (Entity.USER_ITEM == Entity.getEntityType(userItem)) {
    		entityModel = MyWorkUtil.getEntityModelFromUserItem(userItem);
    	} else {
    		entityModel = userItem;
    	}
        Entity entityType = Entity.getEntityType(entityModel);
        Integer entityId = Integer.valueOf(getUiDataFromModel(entityModel.getValue("id")));

        if (entityType != Entity.COMMENT && entityType != Entity.BDD_SCENARIO) {
            addMenuItem(
                    menu,
                    "View details",
                    EntityIconFactory.getInstance().getImageForEditorPart(entityType, 17, 7),
                    () -> openDetailTab(entityId, entityType));
        }

        if (entityType == Entity.TASK || entityType == Entity.COMMENT) {
            // Get parent info
            EntityModel parentEntityModel;
            if (entityType == Entity.TASK) {
                parentEntityModel = (EntityModel) entityModel.getValue("story").getValue();
            } else {
                parentEntityModel = (EntityModel) Util.getContainerItemForCommentModel(entityModel).getValue();
            }
            Entity parentEntity = Entity.getEntityType(parentEntityModel);

            if (EntityFieldsConstants.supportedEntitiesThatAllowDetailView.contains(parentEntity)) {
                addMenuItem(menu,
                        "View parent details",

                        EntityIconFactory.getInstance().getImageForEditorPart(Entity.getEntityType(parentEntityModel), 17, 7), () -> {
                            Integer parentId = Integer.valueOf(parentEntityModel.getValue("id").getValue().toString());
                            Entity parentEntityType = Entity.getEntityType(parentEntityModel);
                            if (Entity.FEATURE == Entity.getEntityType(parentEntityModel) || Entity.EPIC == Entity.getEntityType(parentEntityModel)) {
                                entityService.openInBrowser(parentEntityModel);
                            } else {
                                openDetailTab(parentId, parentEntityType);
                            }
                        });
            }
        }

        addMenuItem(
                menu,
                "View in browser (System)",
                ImageResources.BROWSER_16X16.getImage(),
                () -> OpenInBrowser.openEntityInBrowser(entityModel));

        if (entityType == Entity.GHERKIN_TEST || entityType == Entity.BDD_SCENARIO) {
            addMenuItem(
                    menu,
                    "Download script",
                    ImageResources.DOWNLOAD.getImage(),
                    () -> {
                    	downloadScriptUtil.downloadScriptForTest(entityModel, menu);
                    });
        }

        if (entityType == Entity.DEFECT ||
                entityType == Entity.USER_STORY ||
                entityType == Entity.QUALITY_STORY ||
                entityType == Entity.TASK) {

            new MenuItem(menu, SWT.SEPARATOR);

            MenuItem startWork = addMenuItem(
                    menu,
                    "Start work",
                    ImageResources.START_TIMER_16X16.getImage(),
                    () -> {
                        PluginPreferenceStorage.setActiveItem(new EntityModelEditorInput(entityModel));
                    });

            MenuItem stopWork = addMenuItem(
                    menu,
                    "Stop work",
                    ImageResources.STOP_TIMER_16X16.getImage(),
                    () -> {
                        PluginPreferenceStorage.setActiveItem(null);
                    });

            MenuItem commitMessage = addMenuItem(
                    menu,
                    "Copy commit message to clipboard",
                    PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY).createImage(),
                    () -> CommitMessageUtil.copyMessageIfValid(entityModel));

            if (!new EntityModelEditorInput(entityModel).equals(PluginPreferenceStorage.getActiveItem())) {
                startWork.setEnabled(true);
                stopWork.setEnabled(false);
                commitMessage.setEnabled(true);
            } else {
                startWork.setEnabled(false);
                stopWork.setEnabled(true);
                commitMessage.setEnabled(true);
            }
        }

        if (entityType == Entity.COMMENT || myWorkService.isAddingToMyWorkSupported(entityType)) {
            new MenuItem(menu, SWT.SEPARATOR);
            addMenuItem(
                    menu,
                    "Dismiss",
                    ImageResources.DISMISS.getImage(),
                    () -> {
                        DismissItemJob job = new DismissItemJob("Dismissing item from \"My Work...\"", entityModel, entityType);
                        job.schedule();
                        job.addJobChangeListener(new JobChangeAdapter() {
                            @Override
                            public void done(IJobChangeEvent event) {
                                menuParent.getDisplay().asyncExec(() -> {
                                    if (job.wasRemoved()) {
                                        entityListData.remove(userItem);
                                        if (PluginPreferenceStorage.getActiveItem() != null
                                                && !MyWorkView.userItemsContainsActiveItem(entityListData.getEntityList())) {
                                            PluginPreferenceStorage.setActiveItem(null);
                                        }
                                        new InfoPopup("My Work", "Item removed.").open();
                                    } else {
                                        new InfoPopup("My Work", "Failed to remove item.").open();
                                    }
                                });
                            }
                        });
                    });
        }
        return menu;
    }

    private static MenuItem addMenuItem(Menu menu, String text, Image image, Runnable selectAction) {
        MenuItem menuItem = new MenuItem(menu, SWT.NONE);
        if (image != null) {
            menuItem.setImage(image);
        }
        menuItem.setText(text);
        menuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                selectAction.run();
            }
        });
        return menuItem;
    }

}
