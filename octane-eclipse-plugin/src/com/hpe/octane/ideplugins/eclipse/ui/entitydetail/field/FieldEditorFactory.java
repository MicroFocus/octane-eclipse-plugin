/*******************************************************************************
 * © 2017 EntIT Software LLC, a Micro Focus company, L.P.
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
package com.hpe.octane.ideplugins.eclipse.ui.entitydetail.field;

import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.hpe.adm.nga.sdk.metadata.FieldMetadata;
import com.hpe.adm.nga.sdk.metadata.FieldMetadata.Target;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.query.Query;
import com.hpe.adm.nga.sdk.query.Query.QueryBuilder;
import com.hpe.adm.nga.sdk.query.QueryMethod;
import com.hpe.adm.octane.ideplugins.services.EntityService;
import com.hpe.adm.octane.ideplugins.services.MetadataService;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.util.Util;
import com.hpe.octane.ideplugins.eclipse.Activator;
import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.model.EntityModelWrapper;
import com.hpe.octane.ideplugins.eclipse.ui.util.EntityComboBox.EntityLoader;
import com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants;

public class FieldEditorFactory {

    private static final int COMBO_BOX_ENTITY_LIMIT = 100;

    private static final LabelProvider DEFAULT_ENTITY_LABEL_PROVIDER = new LabelProvider() {
        @Override
        public String getText(Object element) {
            EntityModel entityModel = (EntityModel) element;
            String fieldName;
            
            if (Entity.getEntityType(entityModel) == Entity.WORKSPACE_USER) {
                fieldName =  EntityFieldsConstants.FIELD_FULL_NAME;
            } else {
                fieldName = EntityFieldsConstants.FIELD_NAME;
            }
            
            return Util.getUiDataFromModel(entityModel.getValue(fieldName));
        }
    };

    private MetadataService metadataService = Activator.getInstance(MetadataService.class);
    private EntityService entityService = Activator.getInstance(EntityService.class);

    public FieldEditor createFieldEditor(Composite parent, EntityModelWrapper entityModelWrapper, String fieldName) {

        ILog log = Activator.getDefault().getLog();

        EntityModel entityModel = entityModelWrapper.getReadOnlyEntityModel();
        Entity entityType = Entity.getEntityType(entityModel);
        FieldMetadata fieldMetadata = metadataService.getMetadata(entityType, fieldName);

        FieldEditor fieldEditor = null;

        if (!fieldMetadata.isEditable()) {
            fieldEditor = new ReadOnlyFieldEditor(parent, SWT.NONE);

        } else {
            switch (fieldMetadata.getFieldType()) {
                case Integer:
                    fieldEditor = new NumericFieldEditor(parent, SWT.NONE, false);
                    ((NumericFieldEditor) fieldEditor).setBounds(0, Long.MAX_VALUE);
                    break;
                case Float:
                    fieldEditor = new NumericFieldEditor(parent, SWT.NONE, true);
                    break;
                case String:
                    fieldEditor = new StringFieldEditor(parent, SWT.BORDER);
                    break;
                case Boolean:
                    fieldEditor = new BooleanFieldEditor(parent, SWT.NONE);
                    break;
                case DateTime:
                    fieldEditor = new DateTimeFieldEditor(parent, SWT.NONE);
                    break;
                case Reference:
                    try {
                        fieldEditor = createReferenceFieldEditor(parent, entityModelWrapper, fieldMetadata);
                    } catch (Exception e) {
                        log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Failed to create reference field editor: " + e));
                        fieldEditor = new ReadOnlyFieldEditor(parent, SWT.NONE);
                    }
                    break;
                default:
                    fieldEditor = new ReadOnlyFieldEditor(parent, SWT.NONE);
                    break;
            }
        }

        try {

            fieldEditor.setField(entityModelWrapper, fieldName);

        } catch (Exception ex) {
            StringBuilder sbMessage = new StringBuilder();
            sbMessage.append("Faied to set field ")
                    .append(fieldName)
                    .append(" in detail tab for entity ")
                    .append(entityModel.getId())
                    .append(": ")
                    .append(ex.getMessage());

            log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, sbMessage.toString()));

            fieldEditor = new ReadOnlyFieldEditor(parent, SWT.NONE);
            fieldEditor.setField(entityModelWrapper, fieldName);
        }
        return fieldEditor;
    }

    private FieldEditor createReferenceFieldEditor(Composite parent, EntityModelWrapper entityModelWrapper, FieldMetadata fieldMetadata) {

        Target[] targets = fieldMetadata.getFieldTypedata().getTargets();
        if (targets.length != 1) {
            throw new RuntimeException("Multiple target refrence fields not supported");
        }

        Target target = targets[0];
        String logicalName = target.logicalName();

        // List node loader
        EntityLoader entityLoader;
        
        if (Entity.LIST_NODE.getEntityName().equals(target.getType())) {
            entityLoader = (searchQuery) -> {
                QueryBuilder qb = Query.statement("list_root", QueryMethod.EqualTo,
                        Query.statement("logical_name", QueryMethod.EqualTo, logicalName));
                                
                Collection<EntityModel> entities = entityService.findEntities(Entity.LIST_NODE, qb, null);
                
                //for some reason list nodes are not server side filterable, so you have to do it client side   
                if(!searchQuery.isEmpty()) {
                    String sanitizedSearchQuery = searchQuery.trim().toLowerCase();
                    
                    entities =
                        entities
                        .stream()
                        .filter(entityModel -> {
                            String listNodeName = Util.getUiDataFromModel(entityModel.getValue(EntityFieldsConstants.FIELD_NAME));
                            listNodeName = listNodeName.trim();
                            listNodeName = listNodeName.toLowerCase();
                            return stringLike(listNodeName, sanitizedSearchQuery);
                        })
                        .collect(Collectors.toList());
                }
                
                return entities;
            };
        } else {
            throw new RuntimeException("Refrence entity type not supported: " + target.getType());
        }
        
        ReferenceFieldEditor fieldEditor = new ReferenceFieldEditor(parent, SWT.NONE);
        
        if (fieldMetadata.getFieldTypedata().isMultiple()) {
            fieldEditor.setSelectionMode(SWT.MULTI);
        } else {
            fieldEditor.setSelectionMode(SWT.SINGLE);
        }
        
        fieldEditor.setEntityLoader(entityLoader);
        fieldEditor.setLabelProvider(DEFAULT_ENTITY_LABEL_PROVIDER);
        return fieldEditor;
    }
    
    private static boolean stringLike(String str, String expr) {
        if(str == null || expr == null) {
            return false;
        }
        return str.contains(expr) || expr.contains(str);
    }

}