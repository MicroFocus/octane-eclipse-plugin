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
package com.hpe.octane.ideplugins.eclipse.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;

import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.util.DefaultEntityFieldsUtil;
import com.hpe.octane.ideplugins.eclipse.Activator;
import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.EntityModelEditorInput;

public class PluginPreferenceStorage {

    public interface PrefereceChangeHandler {
        void preferenceChanged();
    }

    public interface PreferenceConstants {
        public static final String OCTANE_SERVER_URL = "octaneServerUrl";
        public static final String IS_BROWSER_AUTH = "isBrowserAuth";
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
        public static final String ACTIVE_ITEM_ID = "ACTIVE_ITEM_ID";
        public static final String ACTIVE_ITEM_ENTITY = "ACTIVE_ITEM_ENTITY";
        public static final String ACTIVE_ITEM_TITLE = "ACTIVE_ITEM_TITLE";
        public static final String SHOWN_ENTITY_FIELDS = "SHOWN_ENTITY_FIELDS";
    }

    private static Map<String, List<PrefereceChangeHandler>> prefereceChangeHandlerMap = new HashMap<>();

    public static void setActiveItem(EntityModelEditorInput entityModelEditorInput) {
        ISecurePreferences securePrefs = getSecurePrefs();

        if (entityModelEditorInput == null) {
            securePrefs.remove(PluginPreferenceStorage.PreferenceConstants.ACTIVE_ITEM_ID);
            securePrefs.remove(PluginPreferenceStorage.PreferenceConstants.ACTIVE_ITEM_ENTITY);
            securePrefs.remove(PluginPreferenceStorage.PreferenceConstants.ACTIVE_ITEM_TITLE);
        } else {
            try {
                securePrefs.putLong(PluginPreferenceStorage.PreferenceConstants.ACTIVE_ITEM_ID,
                        entityModelEditorInput.getId(), false);
                securePrefs.put(PluginPreferenceStorage.PreferenceConstants.ACTIVE_ITEM_ENTITY,
                        entityModelEditorInput.getEntityType().name(), false);
                securePrefs.put(PluginPreferenceStorage.PreferenceConstants.ACTIVE_ITEM_TITLE,
                        entityModelEditorInput.getTitle(), false);
            } catch (StorageException e) {
                Activator.getDefault().getLog().log(new Status(Status.ERROR, Activator.PLUGIN_ID, Status.ERROR,
                        "An exception has occured while saving active item", e));
            }
        }

        fireChangeHandler(PreferenceConstants.ACTIVE_ITEM_ID);
        fireChangeHandler(PreferenceConstants.ACTIVE_ITEM_ENTITY);
        fireChangeHandler(PreferenceConstants.ACTIVE_ITEM_TITLE);
    }

    public static EntityModelEditorInput getActiveItem() {
        EntityModelEditorInput editorInput;
        ISecurePreferences securePrefs = getSecurePrefs();
        try {
            Long id = securePrefs.getLong(PreferenceConstants.ACTIVE_ITEM_ID, -1);
            Entity entityType = Entity
                    .valueOf(securePrefs.get(PreferenceConstants.ACTIVE_ITEM_ENTITY, Entity.DEFECT.name()));
            String title = securePrefs.get(PreferenceConstants.ACTIVE_ITEM_TITLE, null);
            editorInput = new EntityModelEditorInput(id, entityType, title);
        } catch (Exception ex) {
            Activator.getDefault().getLog().log(new Status(Status.ERROR, Activator.PLUGIN_ID, Status.ERROR,
                    "An exception has occured while fetching active item", ex));
            return null;
        }

        return (editorInput.getId() != -1) ? editorInput : null;
    }

    public static void setShownEntityFields(Entity entity, Set<String> entityFields) {
        ISecurePreferences securePrefs = getSecurePrefs();

        Map<Entity, Set<String>> showEntityFieldMap;
        String fieldsStr = null;

        try {
            fieldsStr = securePrefs.get(PreferenceConstants.SHOWN_ENTITY_FIELDS, null);
        } catch (StorageException e) {
            Activator.getDefault().getLog().log(new Status(Status.ERROR, Activator.PLUGIN_ID, Status.ERROR,
                    "An exception has occured while loading shown entity fields", e));
        }

        if (fieldsStr == null) {
            showEntityFieldMap = DefaultEntityFieldsUtil.getDefaultFields();
        } else {
            showEntityFieldMap = DefaultEntityFieldsUtil.entityFieldsFromJson(fieldsStr);
        }

        showEntityFieldMap.put(entity, entityFields);

        try {
            securePrefs.put(PreferenceConstants.SHOWN_ENTITY_FIELDS,
                    DefaultEntityFieldsUtil.entityFieldsToJson(showEntityFieldMap), false);
            fireChangeHandler(PreferenceConstants.SHOWN_ENTITY_FIELDS);
        } catch (StorageException e) {
            Activator.getDefault().getLog().log(new Status(Status.ERROR, Activator.PLUGIN_ID, Status.ERROR,
                    "An exception has occured while saving shown entity fields", e));
        }

    }

    public static void resetShownEntityFields() {
        ISecurePreferences securePrefs = getSecurePrefs();
        try {
            securePrefs.put(PreferenceConstants.SHOWN_ENTITY_FIELDS,
                    DefaultEntityFieldsUtil.entityFieldsToJson(DefaultEntityFieldsUtil.getDefaultFields()), false);
            fireChangeHandler(PreferenceConstants.SHOWN_ENTITY_FIELDS);
        } catch (StorageException e) {
            Activator.getDefault().getLog().log(new Status(Status.ERROR, Activator.PLUGIN_ID, Status.ERROR,
                    "An exception has occured while saving shown entity fields", e));
        }
    }

    public static Set<String> getShownEntityFields(Entity entity) {
        ISecurePreferences securePrefs = getSecurePrefs();
        String shownEntityFields;
        Map<Entity, Set<String>> showEntityFieldMap;

        try {
            shownEntityFields = securePrefs.get(PreferenceConstants.SHOWN_ENTITY_FIELDS, null);
        } catch (StorageException e) {
            Activator.getDefault().getLog().log(new Status(Status.ERROR, Activator.PLUGIN_ID, Status.ERROR,
                    "An exception has occured while getting shown entity fields", e));
            throw new RuntimeException(e);
        }

        if (shownEntityFields == null) {
            return DefaultEntityFieldsUtil.getDefaultFields().get(entity);
        } else {
            showEntityFieldMap = DefaultEntityFieldsUtil.entityFieldsFromJson(shownEntityFields);
            if (!showEntityFieldMap.containsKey(entity)) {
                return DefaultEntityFieldsUtil.getDefaultFields().get(entity);
            } else {
                return showEntityFieldMap.get(entity);
            }
        }
    }

    public static boolean areShownEntityFieldsDefaults(Entity entity) {
        Set<String> defaults = DefaultEntityFieldsUtil.getDefaultFields().get(entity);
        Set<String> current = getShownEntityFields(entity);
        return defaults.equals(current);
    }

    /**
     * TODO: atoth, don't allow direct access
     * 
     * @return bad thingy
     */
    public static ISecurePreferences getSecurePrefs() {

        QualifiedName qualifiedName = new QualifiedName(Activator.PLUGIN_ID, "workspaceId");
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        try {
            String workspaceId = root.getPersistentProperty(qualifiedName);
            if (workspaceId == null) {
                workspaceId = UUID.randomUUID().toString();
                root.setPersistentProperty(qualifiedName, workspaceId);
            }
            return SecurePreferencesFactory.getDefault().node(workspaceId);
        } catch (CoreException e) {
            Activator.getDefault().getLog().log(new Status(Status.ERROR, Activator.PLUGIN_ID, Status.ERROR,
                    "An exception has occured when trying to access the Core Software Delivery Platform connection details", e));

            String workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
            return SecurePreferencesFactory.getDefault().node(workspacePath);
        }
    }

    public static void addPrefenceChangeHandler(String preferenceKey, PrefereceChangeHandler prefereceChangeHandler) {
        if (!prefereceChangeHandlerMap.containsKey(preferenceKey)) {
            prefereceChangeHandlerMap.put(preferenceKey, new ArrayList<>());
        }
        prefereceChangeHandlerMap.get(preferenceKey).add(prefereceChangeHandler);
    }

    public static void removePrefenceChangeHandler(String preferenceKey,
            PrefereceChangeHandler prefereceChangeHandler) {
        if (!prefereceChangeHandlerMap.containsKey(preferenceKey)) {
            prefereceChangeHandlerMap.put(preferenceKey, new ArrayList<>());
        }
        prefereceChangeHandlerMap.get(preferenceKey).remove(prefereceChangeHandler);
    }

    private static void fireChangeHandler(String preferenceKey) {
        List<PrefereceChangeHandler> handlers = prefereceChangeHandlerMap.get(preferenceKey);
        if (handlers != null) {
            handlers.forEach(prefereceChangedHandler -> prefereceChangedHandler.preferenceChanged());
        }
    }

}
