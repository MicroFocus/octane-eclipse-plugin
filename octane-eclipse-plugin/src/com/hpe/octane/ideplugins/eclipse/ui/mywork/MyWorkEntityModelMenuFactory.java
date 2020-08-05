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
package com.hpe.octane.ideplugins.eclipse.ui.mywork;

import static com.hpe.adm.octane.ideplugins.services.util.Util.getUiDataFromModel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.ui.internal.registry.FileEditorMapping;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.ReferenceFieldModel;
import com.hpe.adm.octane.ideplugins.services.EntityService;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkService;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkUtil;
import com.hpe.adm.octane.ideplugins.services.nonentity.DownloadScriptService;
import com.hpe.adm.octane.ideplugins.services.util.UrlParser;
import com.hpe.adm.octane.ideplugins.services.util.Util;
import com.hpe.octane.ideplugins.eclipse.Activator;
import com.hpe.octane.ideplugins.eclipse.filter.EntityListData;
import com.hpe.octane.ideplugins.eclipse.preferences.PluginPreferenceStorage;
import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.EntityModelEditor;
import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.EntityModelEditorInput;
import com.hpe.octane.ideplugins.eclipse.ui.entitylist.EntityModelMenuFactory;
import com.hpe.octane.ideplugins.eclipse.ui.mywork.job.DismissItemJob;
import com.hpe.octane.ideplugins.eclipse.ui.util.InfoPopup;
import com.hpe.octane.ideplugins.eclipse.ui.util.OpenInBrowser;
import com.hpe.octane.ideplugins.eclipse.ui.util.icon.EntityIconFactory;
import com.hpe.octane.ideplugins.eclipse.ui.util.resource.ImageResources;
import com.hpe.octane.ideplugins.eclipse.util.CommitMessageUtil;
import com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants;

public class MyWorkEntityModelMenuFactory implements EntityModelMenuFactory {

    
    private static EntityService entityService = Activator.getInstance(EntityService.class);
    private static MyWorkService myWorkService = Activator.getInstance(MyWorkService.class);
    private static DownloadScriptService scriptService = Activator.getInstance(DownloadScriptService.class);
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

        EntityModel entityModel = MyWorkUtil.getEntityModelFromUserItem(userItem);
        Entity entityType = Entity.getEntityType(entityModel);
        Integer entityId = Integer.valueOf(getUiDataFromModel(entityModel.getValue("id")));

        addMenuItem(
                menu,
                "View in browser (System)",
                ImageResources.BROWSER_16X16.getImage(),
                () -> OpenInBrowser.openEntityInBrowser(entityModel));

        new MenuItem(menu, SWT.SEPARATOR);

        if (entityType != Entity.COMMENT) {
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

        if (entityType == Entity.GHERKIN_TEST) {
            addMenuItem(
                    menu,
                    "Download script",
                    ImageResources.DOWNLOAD.getImage(),
                    () -> {
                        File parentFolder = chooseParentFolder();

                        if (parentFolder != null) {
                            long gherkinTestId = Long.parseLong(entityModel.getValue("id").getValue().toString());
                            String gherkinTestName = entityModel.getValue("name").getValue().toString();
                            String scriptFileName = gherkinTestName + "-" +
                                    gherkinTestId + ".feature";
                            File scriptFile = new File(parentFolder.getPath() + File.separator +
                                    scriptFileName);
                            boolean shouldDownloadScript = true;

                            if (scriptFile.exists()) {
                                MessageBox messageBox = new MessageBox(menu.getShell(), SWT.ICON_QUESTION |
                                        SWT.YES | SWT.NO);
                                messageBox.setMessage("Selected destination folder already contains a file named \"" +
                                        scriptFileName + "\". Do you want to overwrite this file?");
                                messageBox.setText("Confirm file overwrite");
                                shouldDownloadScript = messageBox.open() == SWT.YES;
                            }

                            if (shouldDownloadScript) {
                                BusyIndicator.showWhile(Display.getCurrent(), () -> {
                                    String content = scriptService.getGherkinTestScriptContent(gherkinTestId);
                                    createTestScriptFile(parentFolder.getPath(), scriptFileName,
                                            content);

                                    associateTextEditorToScriptFile(scriptFile);
                                    openInEditor(scriptFile);
                                });
                            }
                        }
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

        if (entityType == Entity.COMMENT || (myWorkService.isAddingToMyWorkSupported(entityType) && MyWorkUtil.isUserItemDismissible(userItem))) {
            new MenuItem(menu, SWT.SEPARATOR);
            addMenuItem(
                    menu,
                    "Dismiss",
                    ImageResources.DISMISS.getImage(),
                    () -> {
                        DismissItemJob job = new DismissItemJob("Dismissing item from \"My Work...\"", entityModel);
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

    private void associateTextEditorToScriptFile(File file) {
        EditorRegistry editorRegistry = (EditorRegistry) PlatformUI.getWorkbench().getEditorRegistry();
        IEditorDescriptor editorDescriptor = editorRegistry.getDefaultEditor(file.getName());
        if (editorDescriptor == null) {
            String extension = "feature";
            String editorId = EditorsUI.DEFAULT_TEXT_EDITOR_ID;

            EditorDescriptor editor = (EditorDescriptor) editorRegistry.findEditor(editorId);
            FileEditorMapping mapping = new FileEditorMapping(extension);
            mapping.addEditor(editor);
            mapping.setDefaultEditor(editor);

            IFileEditorMapping[] mappings = editorRegistry.getFileEditorMappings();
            FileEditorMapping[] newMappings = new FileEditorMapping[mappings.length + 1];
            for (int i = 0; i < mappings.length; i++) {
                newMappings[i] = (FileEditorMapping) mappings[i];
            }
            newMappings[mappings.length] = mapping;
            editorRegistry.setFileEditorMappings(newMappings);
        }
    }

    private void openInEditor(File file) {
        IPath path = new Path(file.getPath());
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

        IFileStore fileStore = EFS.getLocalFileSystem().getStore(file.toURI());
        try {
            // open as external file
            IDE.openEditorOnFileStore(page, fileStore);
            refreshFile(file);
        } catch (PartInitException e) {
            Activator.getDefault().getLog().log(new Status(Status.ERROR, Activator.PLUGIN_ID,
                    Status.ERROR, "Script file could not be opened in the editor", e));
        }
    }

    private void refreshFile(File file) {
        IPath path = new Path(file.getPath());
        IFile eclipseFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
        if (eclipseFile != null) {
            try {
                eclipseFile.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
            } catch (CoreException e) {
                Activator.getDefault().getLog().log(new Status(Status.ERROR, Activator.PLUGIN_ID,
                        Status.ERROR, "Script file could not be refreshed", e));
            }
        }
    }

    private File chooseParentFolder() {
        DirectoryDialog dialog = new DirectoryDialog(Display.getDefault().getActiveShell(), SWT.OPEN);
        dialog.setText("Parent folder selection");
        dialog.setMessage("Select the folder where the script file should be downloaded");
        dialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString());
        String result = dialog.open();
        return result == null ? null : new File(result);
    }

    private File createTestScriptFile(String path, String fileName, String script) {
        File f = new File(path + "/" + fileName.replaceAll("[\\\\/:?*\"<>|]", ""));
        try {
            f.createNewFile();
            if (script != null) {
                Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8));
                out.append(script);
                out.flush();
                out.close();
            }
        } catch (IOException e) {
            Activator.getDefault().getLog().log(new Status(Status.ERROR, Activator.PLUGIN_ID,
                    Status.ERROR, "Could not create or write script file in " + path, e));
        }
        return f;
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
