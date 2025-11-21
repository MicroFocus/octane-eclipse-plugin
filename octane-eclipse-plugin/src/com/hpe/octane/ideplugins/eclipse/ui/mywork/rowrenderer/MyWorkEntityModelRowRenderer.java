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
package com.hpe.octane.ideplugins.eclipse.ui.mywork.rowrenderer;

import static com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants.FIELD_AUTHOR;
import static com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants.FIELD_DETECTEDBY;
import static com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants.FIELD_ENVIROMENT;
import static com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants.FIELD_ESTIMATED_HOURS;
import static com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants.FIELD_INVESTED_HOURS;
import static com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants.FIELD_REMAINING_HOURS;
import static com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants.FIELD_SEVERITY;
import static com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants.FIELD_STORYPOINTS;
import static com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants.FIELD_SUBTYPE;
import static com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants.FIELD_TEST_TYPE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.mywork.MyWorkUtil;
import com.hpe.octane.ideplugins.eclipse.preferences.PluginPreferenceStorage;
import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.EntityModelEditorInput;
import com.hpe.octane.ideplugins.eclipse.ui.entitylist.custom.EntityModelRenderer;
import com.hpe.octane.ideplugins.eclipse.ui.entitylist.custom.EntityModelRow;
import com.hpe.octane.ideplugins.eclipse.ui.entitylist.custom.EntityModelRow.DetailsPosition;
import com.hpe.octane.ideplugins.eclipse.ui.util.icon.EntityIconFactory;
import com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants;

public class MyWorkEntityModelRowRenderer implements EntityModelRenderer {


    // Re-usable field setters
    static TitleFieldSetter titleFieldSetter = new TitleFieldSetter();
    static TitleFieldSetter fsName = new TitleFieldSetter();

    // Top
    static RowFieldSetter fsStoryPoints = new GenericFieldSetter(FIELD_STORYPOINTS, "SP", DetailsPosition.TOP);
    static RowFieldSetter fsAuthor = new GenericFieldSetter(FIELD_AUTHOR, "Author", DetailsPosition.TOP);
    static RowFieldSetter fsOwner = new GenericFieldSetter("owner", "Owner", DetailsPosition.TOP);
    static RowFieldSetter fsPhase = new GenericFieldSetter("phase", "Phase", DetailsPosition.TOP);
    static RowFieldSetter fsStatus = new GenericFieldSetter(EntityFieldsConstants.FIELD_TEST_RUN_NATIVE_STATUS, "Status", DetailsPosition.TOP);

    // Bottom
    static RowFieldSetter fsAutomationStatus = new GenericFieldSetter("automation_status", "Automation status", DetailsPosition.BOTTOM);
    static RowFieldSetter fsStarted = new GenericFieldSetter("started", "Started", DetailsPosition.BOTTOM);

    static RowFieldSetter fsInvestedHours = new GenericFieldSetter(FIELD_INVESTED_HOURS, "Invested hours", DetailsPosition.BOTTOM);
    static RowFieldSetter fsRemainingHours = new GenericFieldSetter(FIELD_REMAINING_HOURS, "Remaining hours", DetailsPosition.BOTTOM);
    static RowFieldSetter fsEstimatedHours = new GenericFieldSetter(FIELD_ESTIMATED_HOURS, "Estimated hours", DetailsPosition.BOTTOM);

    static RowFieldSetter fsSeverity = new GenericFieldSetter("serverity", "Phase", DetailsPosition.TOP);
    static RowFieldSetter fsDetecedBy = new GenericFieldSetter(FIELD_DETECTEDBY, "Phase", DetailsPosition.TOP);

    // Subtile
    static RowFieldSetter fsEnvironment = new GenericFieldSetter(FIELD_ENVIROMENT, "[No environment]");
    static RowFieldSetter fsRelease = new GenericFieldSetter("release", "[No release]");
    static RowFieldSetter fsTestType = new GenericFieldSetter(FIELD_TEST_TYPE, "");

    /**
     * Describe the way the fields are set into the row composite. <br>
     * This map is also used to determine what fields are needed for the rest
     * call, when getting the entities. <br>
     * The detail fields are added from right to left
     */
    private static Map<Entity, Collection<RowFieldSetter>> fieldSetterMap = new LinkedHashMap<>();
    static {
        fieldSetterMap.put(Entity.USER_STORY, asList(
                // top
                fsPhase,
                fsStoryPoints,
                fsOwner,
                fsAuthor,
                // bottom
                fsInvestedHours,
                fsRemainingHours,
                fsEstimatedHours,
                // subtitle
                fsRelease));
        
        fieldSetterMap.put(Entity.FEATURE, asList(
                // top
                fsPhase,
                fsStoryPoints,
                fsOwner,
                fsAuthor,
                // bottom
                fsInvestedHours,
                fsRemainingHours,
                fsEstimatedHours,
                // subtitle
                fsRelease));
        
        fieldSetterMap.put(Entity.TEST_SUITE, asList(
                // top
                fsPhase,
                fsStoryPoints,
                fsOwner,
                fsAuthor,
                // bottom
                fsInvestedHours,
                fsRemainingHours,
                fsEstimatedHours,
                // subtitle
                fsRelease));
        
        fieldSetterMap.put(Entity.UNIT, asList(
                // top
                fsPhase,
                fsStoryPoints,
                fsOwner,
                fsAuthor,
                // bottom
                fsInvestedHours,
                fsRemainingHours,
                fsEstimatedHours,
                // subtitle
                fsRelease));
        
        fieldSetterMap.put(Entity.MODEL, asList(
                // top
                fsPhase,
                fsStoryPoints,
                fsOwner,
                fsAuthor,
                // bottom
                fsInvestedHours,
                fsRemainingHours,
                fsEstimatedHours,
                // subtitle
                fsRelease));
        
        fieldSetterMap.put(Entity.MANUAL_ACTION, asList(
                // top
                fsPhase,
                fsStoryPoints,
                fsOwner,
                fsAuthor,
                // bottom
                fsInvestedHours,
                fsRemainingHours,
                fsEstimatedHours,
                // subtitle
                fsRelease));
        
        
        fieldSetterMap.put(Entity.AUTO_ACTION, asList(
                // top
                fsPhase,
                fsStoryPoints,
                fsOwner,
                fsAuthor,
                // bottom
                fsInvestedHours,
                fsRemainingHours,
                fsEstimatedHours,
                // subtitle
                fsRelease));
        
        fieldSetterMap.put(Entity.QUALITY_GATE, asList(
                // top
                fsPhase,
                fsStoryPoints,
                fsOwner,
                fsAuthor,
                // bottom
                fsInvestedHours,
                fsRemainingHours,
                fsEstimatedHours,
                // subtitle
                fsRelease));

        fieldSetterMap.put(Entity.QUALITY_STORY, asList(
                // top
                fsPhase,
                fsStoryPoints,
                fsOwner,
                fsAuthor,
                // bottom
                fsInvestedHours,
                fsRemainingHours,
                fsEstimatedHours,
                // subtitle
                fsRelease));

        fieldSetterMap.put(Entity.DEFECT, asList(
                // top
                fsPhase,
                fsStoryPoints,
                fsOwner,
                new GenericFieldSetter(FIELD_SEVERITY, "Severity", DetailsPosition.TOP),
                new GenericFieldSetter(FIELD_DETECTEDBY, "Detected by", DetailsPosition.TOP),
                // bottom
                fsInvestedHours,
                fsRemainingHours,
                fsEstimatedHours,
                // subtitle
                fsEnvironment));

        fieldSetterMap.put(Entity.TASK, asList(
                // top
                fsPhase,
                fsOwner,
                fsAuthor,
                // bottom
                fsInvestedHours,
                fsRemainingHours,
                fsEstimatedHours,
                // subtitle
                new TaskSubtitleRowFieldSetter()));

        fieldSetterMap.put(Entity.MANUAL_TEST, asList(
                // top
                fsPhase,
                fsOwner,
                fsAuthor,
                // bottom
                new GenericFieldSetter("steps_num", "Steps", DetailsPosition.BOTTOM),
                fsAutomationStatus,
                // subtitle
                fsTestType));

        fieldSetterMap.put(Entity.GHERKIN_TEST, asList(
                // top
                fsPhase,
                fsOwner,
                fsAuthor,
                // bottom
                fsAutomationStatus,
                // subtitle
                fsTestType));
        
        fieldSetterMap.put(Entity.BDD_SCENARIO, asList(
                // top
                fsOwner,
                // bottom
                fsAutomationStatus,
                // subtitle
                fsTestType));

        fieldSetterMap.put(Entity.TEST_SUITE_RUN, asList(
                // top
                fsStatus,
                // bottom
                fsStarted,
                // subtitle
                fsEnvironment));

        fieldSetterMap.put(Entity.MANUAL_TEST_RUN, asList(
                // top
                fsStatus,
                // bottom
                fsStarted,
                // subtitle
                fsEnvironment));

        fieldSetterMap.put(Entity.COMMENT, asList(
                new CommentFieldSetter(), // sets the name and subtitle
                fsAuthor));

        fieldSetterMap.put(Entity.REQUIREMENT, asList(
                // top
                fsOwner,
                fsPhase));

        // Add common details
        fieldSetterMap.forEach((entityType, fieldSetters) -> {
            // Add ID and Name field setters for everything except the COMMENT
            if (entityType != Entity.COMMENT) {
                fieldSetters.add(titleFieldSetter);
            }
        });

    }

    @Override
    public EntityModelRow createRow(Composite parent, EntityModel userItem) {

        final EntityModel entityModel;
    	if (Entity.USER_ITEM == Entity.getEntityType(userItem)) {
    		entityModel = MyWorkUtil.getEntityModelFromUserItem(userItem);
    	} else {
    		entityModel = userItem;
    	}
        Entity entityType = Entity.getEntityType(entityModel);

        final EntityModelRow rowComposite = new EntityModelRow(parent, SWT.NONE);
        rowComposite.setForeground(PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry()
                .get(JFacePreferences.CONTENT_ASSIST_FOREGROUND_COLOR));

        EntityModelEditorInput activeItem = null;
        try {
            activeItem = PluginPreferenceStorage.getActiveItem();
        } catch (Exception ignored) {
            // this won't work when debugging w/o starting the whole IDE
        }

        if (new EntityModelEditorInput(entityModel).equals(activeItem)) {
            rowComposite.setEntityIcon(EntityIconFactory.getInstance().getImageIcon(entityType, 40, 14, true));
        } else {
            rowComposite.setEntityIcon(EntityIconFactory.getInstance().getImageIcon(entityType, 40, 14, false));
        }

        // Setup row based on field setters
        Collection<RowFieldSetter> fieldSetters = fieldSetterMap.get(entityType);
        fieldSetters.forEach(fs -> fs.setField(rowComposite, entityModel));

        return rowComposite;
    }

    /**
     * Create an object to use for server side call when deciding what fields
     * are needed for every entity. This is based on the field setters described
     * in this class, in this way, changing the field setters will also change
     * the data that is retrieved from the server
     * 
     * @return map where the key is the enity type, and the value is a set of
     *         field names
     */
    public static Map<Entity, Set<String>> getRequiredFields() {

        Map<Entity, Set<String>> result = new HashMap<>();

        fieldSetterMap.forEach((key, fieldSetters) -> {
            result.put(
                    key,
                    fieldSetters.stream().flatMap(fs -> Arrays.stream(fs.getFieldNames())).collect(Collectors.toSet()));
        });

        // Not shown fields
        result
                .keySet()
                .stream()
                .filter(key -> key.isSubtype())
                .forEach(key -> result.get(key).add(FIELD_SUBTYPE));

        return result;
    }

    private static ArrayList<RowFieldSetter> asList(RowFieldSetter... fieldSetters) {
        ArrayList<RowFieldSetter> result = new ArrayList<RowFieldSetter>(Arrays.asList(fieldSetters));
        return result;
    }

}
