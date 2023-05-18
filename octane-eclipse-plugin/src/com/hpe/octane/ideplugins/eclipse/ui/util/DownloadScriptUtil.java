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
package com.hpe.octane.ideplugins.eclipse.ui.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

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
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.ui.internal.registry.FileEditorMapping;
import org.jsoup.Jsoup;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.services.EntityService;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.nonentity.DownloadScriptService;
import com.hpe.adm.octane.ideplugins.services.util.Util;
import com.hpe.octane.ideplugins.eclipse.Activator;

public class DownloadScriptUtil {
	
    private static DownloadScriptService scriptService = Activator.getInstance(DownloadScriptService.class);
    private static EntityService entityService = Activator.getInstance(EntityService.class);
    
    public void downloadScriptForTest(EntityModel entityModel, Menu menu) {
    	File parentFolder = chooseParentFolder();

        if (parentFolder != null) {
        	long testId = Long.parseLong(entityModel.getValue("id").getValue().toString());
            String testName, scriptFileName;

            if(Entity.getEntityType(entityModel) == Entity.BDD_SCENARIO) {
                EntityModel bddScenario = entityService.findEntity(Entity.BDD_SCENARIO, testId, Collections.singleton("bdd_spec"));
                testName = Util.getUiDataFromModel(bddScenario.getValue("bdd_spec"));
                String bddSpecId = Util.getUiDataFromModel(bddScenario.getValue("bdd_spec"), "id");
                scriptFileName = testName.replaceAll("[\\\\/:?*\"<>|]", "") + "_" + bddSpecId + ".feature";  //in windows, a filename cannot contain any of this char -> \/:*?<>|
            } else {
                testName = entityModel.getValue("name").getValue().toString();
                testName = removeHtmlTags(testName);
                scriptFileName = testName.replaceAll("[\\\\/:?*\"<>|]", "") + "_" + testId + ".feature";
            }
            
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
                    String content;
                    try {
                    	content = scriptService.getTestScriptContent(testId);
                    } catch (UnsupportedEncodingException e) {
                    	content = null;
                        new InfoPopup("Unsupported Encoding", "The script you are trying to download contains unsupported characters.").open();
                    }
                    createTestScriptFile(parentFolder.getPath(), scriptFileName,
                            content);

                    associateTextEditorToScriptFile(scriptFile);
                    openInEditor(scriptFile);
                });
            }
        }
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
        File f = new File(path + "/" + fileName);
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

    private String removeHtmlTags(String testName) {
        return Jsoup.parse(testName).text();
    }
}
