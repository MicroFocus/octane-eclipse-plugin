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
package com.hpe.octane.ideplugins.eclipse.util;

import java.util.stream.Collectors;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

import com.google.common.collect.Sets;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.ReferenceFieldModel;
import com.hpe.adm.octane.ideplugins.services.EntityService;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.nonentity.CommitMessageService;
import com.hpe.octane.ideplugins.eclipse.Activator;
import com.hpe.octane.ideplugins.eclipse.preferences.PluginPreferenceStorage;
import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.EntityModelEditorInput;
import com.hpe.octane.ideplugins.eclipse.ui.util.InfoPopup;

public class CommitMessageUtil {

    private static final ILog logger = Activator.getDefault().getLog();
    
    public static void copyMessageIfValidNoActiveItem(EntityModel entityModel) {
        new Job("Generating commit message ...") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {

                EntityModelEditorInput currentItem = new EntityModelEditorInput(entityModel);
                monitor.beginTask("Generating commit message ...", IProgressMonitor.UNKNOWN);
                
                // Convert to partial entity model
                EntityModel activeEntityModel;
                if (Entity.TASK == currentItem.getEntityType()) {
                    // load task entity, for story field
                    EntityService entityService = Activator.getInstance(EntityService.class);
                    try {
                        activeEntityModel = entityService.findEntity(Entity.TASK, currentItem.getId(), Sets.newHashSet("story", "name"));
                    } catch (Exception e) {
                        logger.log(new Status(
                                Status.ERROR,
                                Activator.PLUGIN_ID,
                                Status.OK,
                                "Failed to fetch parent story of task: " + currentItem.getId(),
                                null));

                        return new Status(Status.ERROR, Activator.PLUGIN_ID, Status.ERROR, "Failed to generate commit message", e);
                    }
                } else {
                    activeEntityModel = currentItem.toEntityModel();
                }
                String commitMessage = generateClientSideCommitMessage(activeEntityModel);

                // Validate against server side patterns, since generation based
                // on a regex with no params is not possible

                // Task are validated against their parent, since Octane has no
                // support for task commits, convert the entity to it's parent
                // from here on
                if (Entity.TASK == Entity.getEntityType(activeEntityModel)) {
                    activeEntityModel = ((ReferenceFieldModel) activeEntityModel.getValue("story")).getValue();
                }

                CommitMessageService commitService = Activator.getInstance(CommitMessageService.class);
                if (commitService.validateCommitMessage(
                        commitMessage,
                        Entity.getEntityType(activeEntityModel),
                        Long.parseLong(activeEntityModel.getValue("id").getValue().toString()))) {

                    Display.getDefault().asyncExec(() -> {
                        Clipboard cp = new Clipboard(Display.getDefault());
                        TextTransfer textTransfer = TextTransfer.getInstance();
                        cp.setContents(new Object[] { commitMessage }, new Transfer[] { textTransfer });
                        new InfoPopup("Commit message copied to clipboard", commitMessage, 300, 100).open();
                    });

                } else {

                    CommitMessageService commitMessageService = Activator.getInstance(CommitMessageService.class);

                    // Make sure you use the parent of the task here, since
                    // server doesn't have scm patterns for tasks
                    Entity activeItemType = Entity.getEntityType(activeEntityModel);

                    String patterns = commitMessageService.getCommitPatternsForStoryType(activeItemType)
                            .stream()
                            .collect(Collectors.joining(SystemUtils.LINE_SEPARATOR));

                    StringBuilder messageBuilder = new StringBuilder();

                    messageBuilder
                            .append("Failed to generate commit message for ")
                            .append(getEntityStringFromType(activeItemType))
                            .append(" ,server side patters are different from the default.")
                            .append(SystemUtils.LINE_SEPARATOR)
                            .append("Please make sure your commit message matches one of the following patterns: ")
                            .append(SystemUtils.LINE_SEPARATOR)
                            .append(patterns);

                    if (currentItem.getEntityType() == Entity.TASK) {
                        messageBuilder
                                .append(SystemUtils.LINE_SEPARATOR)
                                .append("For tasks, use the parent backlog item's commit pattern.");
                    }

                    Display.getDefault().asyncExec(() -> {
                        new InfoPopup("Failed to generate commit message", messageBuilder.toString(), 300, 100).open();
                    });
                }

                monitor.done();
                return Status.OK_STATUS;
            }

        }.schedule();


                

        
    }

    public static void copyMessageIfValid() {
        new Job("Generating commit message ...") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {

                monitor.beginTask("Generating commit message ...", IProgressMonitor.UNKNOWN);

                EntityModelEditorInput activeItem = PluginPreferenceStorage.getActiveItem();

                // Convert to partial entity model
                EntityModel activeEntityModel;
                if (Entity.TASK == activeItem.getEntityType()) {
                    // load task entity, for story field
                    EntityService entityService = Activator.getInstance(EntityService.class);
                    try {
                        activeEntityModel = entityService.findEntity(Entity.TASK, activeItem.getId(), Sets.newHashSet("story", "name"));
                    } catch (Exception e) {
                        logger.log(new Status(
                                Status.ERROR,
                                Activator.PLUGIN_ID,
                                Status.OK,
                                "Failed to fetch parent story of task: " + activeItem.getId(),
                                null));

                        return new Status(Status.ERROR, Activator.PLUGIN_ID, Status.ERROR, "Failed to generate commit message", e);
                    }
                } else {
                    activeEntityModel = activeItem.toEntityModel();
                }

                String commitMessage = generateClientSideCommitMessage(activeEntityModel);

                // Validate against server side patterns, since generation based
                // on a regex with no params is not possible

                // Task are validated against their parent, since Octane has no
                // support for task commits, convert the entity to it's parent
                // from here on
                if (Entity.TASK == Entity.getEntityType(activeEntityModel)) {
                    activeEntityModel = ((ReferenceFieldModel) activeEntityModel.getValue("story")).getValue();
                }

                CommitMessageService commitService = Activator.getInstance(CommitMessageService.class);
                if (commitService.validateCommitMessage(
                        commitMessage,
                        Entity.getEntityType(activeEntityModel),
                        Long.parseLong(activeEntityModel.getValue("id").getValue().toString()))) {

                    Display.getDefault().asyncExec(() -> {
                        Clipboard cp = new Clipboard(Display.getDefault());
                        TextTransfer textTransfer = TextTransfer.getInstance();
                        cp.setContents(new Object[] { commitMessage }, new Transfer[] { textTransfer });
                        new InfoPopup("Commit message copied to clipboard", commitMessage, 300, 100).open();
                    });

                } else {

                    CommitMessageService commitMessageService = Activator.getInstance(CommitMessageService.class);

                    // Make sure you use the parent of the task here, since
                    // server doesn't have scm patterns for tasks
                    Entity activeItemType = Entity.getEntityType(activeEntityModel);

                    String patterns = commitMessageService.getCommitPatternsForStoryType(activeItemType)
                            .stream()
                            .collect(Collectors.joining(SystemUtils.LINE_SEPARATOR));

                    StringBuilder messageBuilder = new StringBuilder();

                    messageBuilder
                            .append("Failed to generate commit message for ")
                            .append(getEntityStringFromType(activeItemType))
                            .append(" ,server side patters are different from the default.")
                            .append(SystemUtils.LINE_SEPARATOR)
                            .append("Please make sure your commit message matches one of the following patterns: ")
                            .append(SystemUtils.LINE_SEPARATOR)
                            .append(patterns);

                    if (activeItem.getEntityType() == Entity.TASK) {
                        messageBuilder
                                .append(SystemUtils.LINE_SEPARATOR)
                                .append("For tasks, use the parent backlog item's commit pattern.");
                    }

                    Display.getDefault().asyncExec(() -> {
                        new InfoPopup("Failed to generate commit message", messageBuilder.toString(), 300, 100).open();
                    });
                }

                monitor.done();
                return Status.OK_STATUS;
            }

        }.schedule();

    }

    /*
     * Task requires story field to be loaded
     */
    private static String generateClientSideCommitMessage(EntityModel entityModel) {

        StringBuilder messageBuilder = new StringBuilder();

        String entityId = String.valueOf(entityModel.getValue("id").getValue());

        if (Entity.TASK == Entity.getEntityType(entityModel)) {
            // Tasks include parent commit message info
            EntityModel taskParent = ((ReferenceFieldModel) entityModel.getValue("story")).getValue();
            messageBuilder
                    .append(generateClientSideCommitMessage(taskParent));
        }

        messageBuilder
                .append(getEntityStringFromType(Entity.getEntityType(entityModel)))
                .append(" #")
                .append(entityId)
                .append(": ");

        return messageBuilder.toString();
    }

    public static String getEntityStringFromType(Entity entity) {
        return entity.toString().toLowerCase().replace("_", " ");
    }

}