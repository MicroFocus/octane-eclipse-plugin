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
package com.hpe.octane.ideplugins.eclipse.ui.entitydetail;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.StringFieldModel;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.octane.ideplugins.eclipse.ui.activeitem.ImageDataImageDescriptor;
import com.hpe.octane.ideplugins.eclipse.ui.util.icon.EntityIconFactory;

public class EntityModelEditorInput implements IElementFactory, IEditorInput {

    private static final String FACTORY_ID = "com.hpe.octane.ideplugins.eclipse.ui.editor.EntityModelEditorInput";

    private long id;
    private Entity entityType;
    private String title = "";

    // Default constructor needed because of IElementFactory
    public EntityModelEditorInput() {
    }

    public EntityModelEditorInput(EntityModel entityModel) {
        this.id = Long.parseLong(entityModel.getValue("id").getValue().toString());
        // Not all entities have a name, this field is optional
        if (entityModel.getValue("name") != null) {
            this.title = entityModel.getValue("name").getValue().toString();
        }
        this.entityType = Entity.getEntityType(entityModel);
    }

    public EntityModelEditorInput(long id, Entity entityType) {
        this.id = id;
        this.entityType = entityType;
    }

    public EntityModelEditorInput(long id, Entity entityType, String title) {
        this.id = id;
        this.entityType = entityType;
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public Entity getEntityType() {
        return entityType;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return new ImageDataImageDescriptor(
                EntityIconFactory.getInstance().getImageIcon(entityType, 20, 8).getImageData());
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }

    @Override
    public String getToolTipText() {
        return String.valueOf(id);
    }

    public EntityModel toEntityModel() {
        EntityModel entityModel = new EntityModel();
        entityModel.setValue(new StringFieldModel("id", String.valueOf(id)));
        entityModel.setValue(new StringFieldModel("name", title));

        if (entityType.isSubtype()) {
            entityModel.setValue(new StringFieldModel("type", entityType.getSubtypeOf().getTypeName()));
            entityModel.setValue(new StringFieldModel("subtype", entityType.getSubtypeName()));
        } else {
            entityModel.setValue(new StringFieldModel("type", entityType.getTypeName()));
        }

        return entityModel;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EntityModelEditorInput other = (EntityModelEditorInput) obj;
        if (entityType != other.entityType)
            return false;
        if (id != other.id)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "EntityModelEditorInput [id=" + id + ", entityType=" + entityType + ", title = " + title + "]";
    }

    @Override
    public IPersistableElement getPersistable() {
        IPersistableElement persistableElement = new IPersistableElement() {
            @Override
            public void saveState(IMemento memento) {
                memento.putString("id", id + "");
                memento.putString("entityType", entityType.name());
                memento.putString("title", title);
            }

            @Override
            public String getFactoryId() {
                return FACTORY_ID;
            }
        };
        return persistableElement;
    }

    @Override
    public IAdaptable createElement(IMemento memento) {
        long id = Long.valueOf(memento.getString("id"));
        Entity entityType = Entity.valueOf(memento.getString("entityType"));
        String title = memento.getString("title");
        return new EntityModelEditorInput(id, entityType, title);
    }

}
