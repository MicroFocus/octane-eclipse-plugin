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

import static com.hpe.adm.octane.ideplugins.services.util.Util.getUiDataFromModel;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.Status;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.ReferenceFieldModel;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.util.UrlParser;
import com.hpe.adm.octane.ideplugins.services.util.Util;
import com.hpe.octane.ideplugins.eclipse.Activator;

/*
 * There is a problem with opening an entity in the OS default browser using Desktop.
 * This is a workaround strictly for Linux.
 */

public class OpenInBrowser {

    public static void openURI(URI uri) throws IOException {
        if (!SystemUtils.IS_OS_LINUX) {
            Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
            desktop.browse(uri);
        } else {
            String finalUrlToString = uri.toString();
            if (Runtime.getRuntime().exec(new String[] { "which", "xdg-open" }).getInputStream().read() != -1) {
                Runtime.getRuntime().exec(new String[] { "xdg-open", finalUrlToString });
            } else {
                Activator.getDefault().getLog().log(new Status(Status.ERROR, Activator.PLUGIN_ID,
                        "You need xdg-utils in order to open the backlog item. Alternatively, here's the link: " + finalUrlToString,
                        new RuntimeException(
                                "You need xdg-utils in order to open the backlog item. Alternatively, here's the link: " + finalUrlToString)));
            }
        }
    }

    public static void openEntityInBrowser(EntityModel entityModel) {
        Entity entityType = Entity.getEntityType(entityModel);
        Integer entityId = Integer.valueOf(getUiDataFromModel(entityModel.getValue("id")));

        try {
            Entity ownerEntityType = null;
            Integer ownerEntityId = null;

            if (entityType == Entity.COMMENT) {
                ReferenceFieldModel owner = (ReferenceFieldModel) Util.getContainerItemForCommentModel(entityModel);
                ownerEntityType = Entity.getEntityType(owner.getValue());
                ownerEntityId = Integer.valueOf(Util.getUiDataFromModel(owner, "id"));
            }
            URI uri = UrlParser.createEntityWebURI(
                    Activator.getConnectionSettings(),
                    entityType == Entity.COMMENT ? ownerEntityType : entityType,
                    entityType == Entity.COMMENT ? ownerEntityId : entityId);
            openURI(uri);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
