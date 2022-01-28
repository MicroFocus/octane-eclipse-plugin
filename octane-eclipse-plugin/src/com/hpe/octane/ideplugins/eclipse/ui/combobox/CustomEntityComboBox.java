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
package com.hpe.octane.ideplugins.eclipse.ui.combobox;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class CustomEntityComboBox <T> {
	
	private ComboViewer viewer;
    private CustomEntityComboBoxLabelProvider<T> labelProvider;
    private List<T> content;
    private List<CustomEntityComboBoxSelectionListener<T>> selectionListeners;
    private T currentSelection;

    public CustomEntityComboBox(Composite parent) {
    	   this.viewer = new ComboViewer(parent, SWT.READ_ONLY);
           this.viewer.setContentProvider(ArrayContentProvider.getInstance());
           
           viewer.setLabelProvider(new LabelProvider() {
               @Override
               public String getText(Object element) {
                   T entityModelElement = getTypedObject(element);
                   if (labelProvider != null && entityModelElement != null) {
                       if (entityModelElement == currentSelection) {
                           return labelProvider.getSelectedLabel(entityModelElement);
                       } else {
                           return labelProvider.getListLabel(entityModelElement);
                       }

                   } else {
                       return element.toString();
                   }
               }
           });
           
           viewer.addSelectionChangedListener(new ISelectionChangedListener() {
               @Override
               public void selectionChanged(SelectionChangedEvent event) {
                   IStructuredSelection selection = (IStructuredSelection) event
                           .getSelection();
                   T selectedEntityModelElement = getTypedObject(selection.getFirstElement());
                   if (selectedEntityModelElement != null) {
                       currentSelection = selectedEntityModelElement;
                       viewer.refresh();
                       notifySelectionListeners(selectedEntityModelElement);
                   }

               }
           });
           
           this.content = new ArrayList<T>();
           this.selectionListeners = new ArrayList<CustomEntityComboBoxSelectionListener<T>>();
	}
    public void setLabelProvider(CustomEntityComboBoxLabelProvider<T> labelProvider) {
        this.labelProvider = labelProvider;
    }

    public void setContent(List<T> content) {
        this.content = content;
        this.viewer.setInput(content.toArray());
    }
    
    public void setEnabled(boolean setEnabled) {
    	viewer.getCombo().setEnabled(setEnabled);
    }

    public T getSelection() {
        return currentSelection;
    }

    public void setSelection(T selection) {
        if (content.contains(selection)) {
            viewer.setSelection(new StructuredSelection(selection), true);
        }
    }

    public void selectFirstItem() {
        if (content.size()>0) {
            setSelection(content.get(0));
        }
    }
    public void addSelectionListener(CustomEntityComboBoxSelectionListener<T> listener) {
        this.selectionListeners.add(listener);
    }

    public void removeSelectionListener(
    		CustomEntityComboBoxSelectionListener<T> listener) {
        this.selectionListeners.remove(listener);
    }
    
    public void setTooltipText(String string) {
        viewer.getCombo().setToolTipText(string);
    }

    private T getTypedObject(Object o) {
        if (content.contains(o)) {
            return content.get(content.indexOf(o));
        } else {
            return null;
        }
    }

    private void notifySelectionListeners(T newSelection) {
        for (CustomEntityComboBoxSelectionListener<T> listener : selectionListeners) {
            listener.selectionChanged(this, newSelection);
        }
    }
    
    
}
