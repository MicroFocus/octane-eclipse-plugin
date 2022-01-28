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

import static com.hpe.adm.octane.ideplugins.services.util.Util.getUiDataFromModel;

import java.net.MalformedURLException;
import java.net.URI;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.ReferenceFieldModel;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkService;
import com.hpe.adm.octane.ideplugins.services.util.UrlParser;
import com.hpe.adm.octane.ideplugins.services.util.Util;
import com.hpe.octane.ideplugins.eclipse.Activator;
import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.EntityModelEditor;
import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.EntityModelEditorInput;
import com.hpe.octane.ideplugins.eclipse.ui.entitylist.EntityModelMenuFactory;
import com.hpe.octane.ideplugins.eclipse.ui.mywork.MyWorkView;
import com.hpe.octane.ideplugins.eclipse.ui.mywork.job.AddToMyWorkJob;
import com.hpe.octane.ideplugins.eclipse.ui.util.DownloadScriptUtil;
import com.hpe.octane.ideplugins.eclipse.ui.util.InfoPopup;
import com.hpe.octane.ideplugins.eclipse.ui.util.OpenInBrowser;
import com.hpe.octane.ideplugins.eclipse.ui.util.icon.EntityIconFactory;
import com.hpe.octane.ideplugins.eclipse.ui.util.resource.ImageResources;
import com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants;

public class SearchEntityModelMenuFactory implements EntityModelMenuFactory {

    // private static final ILog logger = Activator.getDefault().getLog();
    private static MyWorkService myWorkService = Activator.getInstance(MyWorkService.class);
    private static DownloadScriptUtil downloadScriptUtil = Activator.getInstance(DownloadScriptUtil.class);

    public SearchEntityModelMenuFactory() {
    }

    private void openDetailTab(Integer entityId, Entity entityType) {
        IWorkbench wb = PlatformUI.getWorkbench();
        IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
        IWorkbenchPage page = win.getActivePage();

        EntityModelEditorInput entityModelEditorInput = new EntityModelEditorInput(entityId, entityType);
        try {
            page.openEditor(entityModelEditorInput, EntityModelEditor.ID);
        } catch (PartInitException ex) {
            // logger.log(new Status(Status.ERROR, Activator.PLUGIN_ID,
            // Status.ERROR, "An exception has occurred when opening the
            // editor", ex));
        }
    }

    @Override
    public Menu createMenu(EntityModel entityModel, Control menuParent) {

        Menu menu = new Menu(menuParent);

        Entity entityType = Entity.getEntityType(entityModel);
        Integer entityId = Integer.valueOf(getUiDataFromModel(entityModel.getValue("id")));

        if (EntityFieldsConstants.supportedEntitiesThatAllowDetailView.contains(entityType)) {
            addMenuItem(
                    menu,
                    "View details",
                    EntityIconFactory.getInstance().getImageForEditorPart(entityType, 16, 7),
                    () -> openDetailTab(entityId, entityType));
        }

        if (EntityFieldsConstants.supportedEntitiesThatAllowDetailView.contains(entityType)) {
            new MenuItem(menu, SWT.SEPARATOR);
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

        if (myWorkService.isAddingToMyWorkSupported(entityType)) {
            new MenuItem(menu, SWT.SEPARATOR);
            addMenuItem(
                    menu,
                    "Add to \"My Work\"",
                    ImageResources.ADD.getImage(),
                    () -> {
                        AddToMyWorkJob job = new AddToMyWorkJob("Adding item to \"My Work...\"", entityModel);
                        job.schedule();
                        job.addJobChangeListener(new JobChangeAdapter() {
                            @Override
                            public void done(IJobChangeEvent event) {
                                menuParent.getDisplay().asyncExec(() -> {
                                    if (job.wasAdded()) {
                                        new InfoPopup("My Work", "Item added.").open();
                                        tryMyWorkRefresh();
                                    } else {
                                        new InfoPopup("My Work", "Failed to add item. Already in \"My Work\".").open();
                                    }
                                });
                            }
                        });
                    });
        }
        return menu;
    }

    /**
     * Attempt to refresh the my work viewpart if active
     */
    private static void tryMyWorkRefresh() {
        // Attempt to refresh the my work view
        try {
            IViewPart part = PlatformUI
                    .getWorkbench()
                    .getActiveWorkbenchWindow()
                    .getActivePage()
                    .findView(MyWorkView.ID);

            MyWorkView view = (MyWorkView) part;
            view.refresh();
        } catch (Exception ignored) {
        }
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
