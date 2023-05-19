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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.services.EntityService;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkUtil;
import com.hpe.adm.octane.ideplugins.services.util.Util;
import com.hpe.octane.ideplugins.eclipse.Activator;
import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.EntityModelEditor;
import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.EntityModelEditorInput;
import com.hpe.octane.ideplugins.eclipse.ui.entitylist.EntityMouseListener;

public class OpenDetailTabEntityMouseListener implements EntityMouseListener {

    private static final ILog logger = Activator.getDefault().getLog();
    private static EntityService entityService = Activator.getInstance(EntityService.class);

    @Override
    public void mouseClick(EntityModel entityModel, MouseEvent event) {
        // Open detail tab
        if (event.count == 2) {
            IWorkbench wb = PlatformUI.getWorkbench();
            IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
            IWorkbenchPage page = win.getActivePage();

            if (Entity.USER_ITEM == Entity.getEntityType(entityModel)) {
                entityModel = MyWorkUtil.getEntityModelFromUserItem(entityModel);
            }

            if (Entity.COMMENT == Entity.getEntityType(entityModel)) {
                entityModel = (EntityModel) Util.getContainerItemForCommentModel(entityModel).getValue();
            }

            Set<Entity> openInBrowserEntities = new LinkedHashSet<>(Arrays.asList(
                    Entity.EPIC,
                    Entity.FEATURE,
                    Entity.AUTOMATED_TEST,
                    Entity.AUTOMATED_TEST_RUN,
                    Entity.BDD_SCENARIO,
                    Entity.BDD_SPEC,
                    Entity.GHERKIN_AUTOMATED_RUN,
                    Entity.REQUIREMENT_FOLDER));

            if (openInBrowserEntities.contains(Entity.getEntityType(entityModel))) {
                entityService.openInBrowser(entityModel);
            } else {
                Long id = Long.parseLong(entityModel.getValue("id").getValue().toString());
                EntityModelEditorInput entityModelEditorInput = new EntityModelEditorInput(id, Entity.getEntityType(entityModel));
                try {
                    logger.log(new Status(Status.INFO, Activator.PLUGIN_ID, Status.OK, entityModelEditorInput.toString(), null));
                    page.openEditor(entityModelEditorInput, EntityModelEditor.ID);
                } catch (PartInitException ex) {
                    logger.log(
                            new Status(Status.ERROR, Activator.PLUGIN_ID, Status.ERROR, "An exception has occured when opening the editor", ex));
                }
            }

        }
    }

}
