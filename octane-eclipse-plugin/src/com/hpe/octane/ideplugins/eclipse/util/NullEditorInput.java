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
package com.hpe.octane.ideplugins.eclipse.util;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class NullEditorInput implements IEditorInput {

    public NullEditorInput() {
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return ImageDescriptor.getMissingImageDescriptor();
    }

    @Override
    public String getName() {
        return ""; //$NON-NLS-1$
    }

    @Override
    public IPersistableElement getPersistable() {
        return null;
    }

    @Override
    public String getToolTipText() {
        return ""; //$NON-NLS-1$
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        return null;
    }

}
