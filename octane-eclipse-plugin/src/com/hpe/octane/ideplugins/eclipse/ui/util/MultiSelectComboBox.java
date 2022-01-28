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
package com.hpe.octane.ideplugins.eclipse.ui.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.hpe.octane.ideplugins.eclipse.Activator;
import com.hpe.octane.ideplugins.eclipse.ui.util.resource.SWTResourceManager;

public class MultiSelectComboBox<T> {

	private static final String BTN_DATA_CONSTANT = "option";
	private static final int MAX_HEIGHT = 300;

	private List<T> options = new ArrayList<>();
	private List<T> selection = new ArrayList<>();
	private LabelProvider labelProvider;

	private List<Button> buttons = new ArrayList<>();

	private List<ModifyListener> modifyListeners = new ArrayList<ModifyListener>();
	private List<SelectionListener> selectionListeners = new ArrayList<SelectionListener>();
	private List<VerifyListener> verifyListeners = new ArrayList<VerifyListener>();

	private Shell floatShell;
	private Label emptyLabel;

	private Runnable resetRunnable;

	public MultiSelectComboBox(LabelProvider labelProvider) {
		this.labelProvider = labelProvider;      
	}

	public void showFloatShell(Control positionControl) {
		floatShell = new Shell(positionControl.getShell(), SWT.BORDER);

		buttons = new ArrayList<>();
		floatShell.setLayout(new GridLayout());

		Text text = new Text(floatShell, SWT.BORDER);
		text.setMessage("Filter");
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		ScrolledComposite scrolledComposite = new ScrolledComposite(floatShell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		scrolledComposite.setExpandHorizontal(true);
		Composite btnComposite = new Composite(scrolledComposite, SWT.NONE);
		scrolledComposite.setContent(btnComposite);		
		btnComposite.setLayout(new GridLayout());

		emptyLabel = new Label(btnComposite, SWT.BOLD);
		emptyLabel.setText("No results");
		emptyLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		emptyLabel.setLayoutData(new GridData());
		((GridData) emptyLabel.getLayoutData()).exclude = true;
		
		for (T option : options) {
			Button button = new Button(btnComposite, SWT.CHECK);
			button.setText(labelProvider.getText(option));
			button.setSelection(selection.contains(option));
			button.setData(BTN_DATA_CONSTANT, option);
			button.addListener(SWT.Selection, e -> {
				if (button.getSelection()) {
					selection.add(option);
				} else {
					selection.remove(option);
				}
				selectionListeners.forEach(l -> l.widgetSelected(new SelectionEvent(e)));
			});
			button.pack();
			buttons.add(button);
		}
		btnComposite.setSize(btnComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		Composite ctrlComposite = new Composite(floatShell, SWT.NONE);
		ctrlComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
		if(resetRunnable != null) {
			ctrlComposite.setLayout(new GridLayout(3, true));
		} else {
			ctrlComposite.setLayout(new GridLayout(2, true));
		}
		
		Button all = new Button(ctrlComposite, SWT.BUTTON1);
		all.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		all.setText("All");
		all.addListener(SWT.MouseDown, event -> selectAll());

		Button none = new Button(ctrlComposite, SWT.BUTTON1);
		none.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		none.setText("None");
		none.addListener(SWT.MouseDown, event -> clearSelection());

		if(resetRunnable != null) {
			Button reset = new Button(ctrlComposite, SWT.BUTTON1);
			reset.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
			reset.setText("Reset");
			reset.addListener(SWT.MouseDown,event -> {
				resetRunnable.run();
			});
		}
		
		text.addModifyListener(new DelayedModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				Arrays.stream(btnComposite.getChildren())
				.filter(child -> child.getData(BTN_DATA_CONSTANT) != null)
				.forEach(child -> {
					
					String lbl = labelProvider.getText(child.getData(BTN_DATA_CONSTANT));
					lbl = lbl.toLowerCase();
					lbl = lbl.replaceAll("\\s+","");
					String query = text.getText();
					query = query.toLowerCase();
					query = query.replaceAll("\\s+","");
					
					if (lbl.contains(query) || query.contains(lbl)) {
						child.setVisible(true);
						((GridData) child.getLayoutData()).exclude = false;
					} else {
						child.setVisible(false);
						((GridData) child.getLayoutData()).exclude = true;
					}
				});
				
				long visCount = Arrays.stream(btnComposite.getChildren())
										.filter(child -> child.getData(BTN_DATA_CONSTANT) != null)
										.filter(child -> child.isVisible()).count();
				
				if(visCount == 0) {
					emptyLabel.setVisible(true);
					((GridData) emptyLabel.getLayoutData()).exclude = false;
				} else {
					emptyLabel.setVisible(false);
					((GridData) emptyLabel.getLayoutData()).exclude = true;
				}

				// sizing
				btnComposite.setSize(btnComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				setFloatShellBounds(positionControl);
				floatShell.open();
			}
		}));

		floatShell.addListener(SWT.Deactivate, e -> {
			if (floatShell != null && !floatShell.isDisposed()) {
				floatShell.setVisible(false);

				for (VerifyListener l : verifyListeners) {
					VerifyEvent v = new VerifyEvent(e);
					l.verifyText(v);
				}

				for (ModifyListener l : modifyListeners) {
					l.modifyText(new ModifyEvent(e));
				}

				if (buttons != null) {
					for (Button b : buttons) {
						if (b != null) {
							b.dispose();
						}
					}
				}
				buttons = null;
				floatShell.dispose();
			}
		});

		setFloatShellBounds(positionControl);
		floatShell.open();
	}
	
	private void setFloatShellBounds(Control positionControl) {
		Point shellSize = floatShell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		shellSize = limitContentSize(floatShell);
		floatShell.setSize(shellSize);
	
		Point parentBounds = positionControl.getParent().toDisplay(positionControl.getLocation());
		Point size = positionControl.getSize();
		Rectangle shellRect = new Rectangle(parentBounds.x - shellSize.x + size.x, parentBounds.y + size.y, shellSize.x, shellSize.y);
		floatShell.setBounds(shellRect);
	}
	
	private Point limitContentSize(Control control) {
		Point contentSize = control.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		int shellHeight = contentSize.y > MAX_HEIGHT ? MAX_HEIGHT : contentSize.y;
		contentSize.y = shellHeight;
		return contentSize;	
	}

	public void setResetRunnable(Runnable r) {
		resetRunnable = r;
	}

	public boolean isVisible() {
		return floatShell != null && !floatShell.isDisposed() && floatShell.isVisible();
	}

	public void setSelected(T option) {
		setSelected(option, true, true);
	}

	public void setSelected(Collection<T> selectOptions) {
		setSelected(selectOptions, true);
	}
	
	public void setSelected(Collection<T> selectOptions, boolean fireSelectionHandlers) {
		for (T selectOption : selectOptions) {
			if (options.contains(selectOption)) {
				setSelected(selectOption, true, false);
			} else {
	            ILog log = Activator.getDefault().getLog();
	            StringBuilder sbMessage = new StringBuilder();
	            sbMessage.append("Failed to select: \"")
	                    .append(selectOption)
	                    .append("\", ")
	                    .append(MultiSelectComboBox.class.getName())
	                    .append(" does not contain that option");
	            log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, sbMessage.toString()));
			}
		}
		if(fireSelectionHandlers) {
			selectionListeners.forEach(l -> l.widgetSelected(null));
		}
	}
	
	public void setSelection(Collection<T> selectOptions) {
		setSelection(selectOptions, true);
	}
	
	public void setSelection(Collection<T> selectOptions, boolean fireSelectionHandlers) {
		clearSelection(false);
		setSelected(selectOptions, fireSelectionHandlers);
	}
	
	public void clearSelection() {
		clearSelection(true);
	}
	
	public void clearSelection(boolean fireSelectionHandlers) {
		selection.clear();
		redrawButtonSelectionState();
		if(fireSelectionHandlers) {
			selectionListeners.forEach(l -> l.widgetSelected(null));
		}
	}
	
	public void selectAll() {
		selection.addAll(options);
		redrawButtonSelectionState();
		selectionListeners.forEach(l -> l.widgetSelected(null));
	}

	public void setSelected(T option, boolean isSelected, boolean fireSelectionListeners) {
		if (isSelected) {
			selection.add(option);
		} else {
			selection.remove(option);
		}
		
		redrawButtonSelectionState();
		
		if(fireSelectionListeners) { 
			selectionListeners.forEach(l -> l.widgetSelected(null));
		}
	}
	
	private void redrawButtonSelectionState() {
		if(buttons != null) {
			buttons.stream()
				.filter(btn -> !btn.isDisposed())
				.forEach(btn -> btn.setSelection(selection.contains(btn.getData(BTN_DATA_CONSTANT))));
		}
	}

	public void add(T option) {
		options.add(option);
	}

	public void addAll(Collection<T> options) {
		this.options.addAll(options);
	}
	
	/**
	 * Remove all options
	 */
	public void clear() {
		this.options.clear();
		this.selection.clear();
	}

	public void add(int index, T t, boolean isSelected) {
		options.add(index, t);
		setSelected(t, isSelected, false);
		selectionListeners.forEach(l -> l.widgetSelected(null));
	}

	public int getItemCount() {
		return options.size();
	}

	public List<T> getSelections() {
		return selection;
	}

	public int getTextHeight() {
		return 20;
	}

	public int getTextLimit() {
		return 20;
	}

	public void addModifyListener(ModifyListener listener) {
		modifyListeners.add(listener);
	}

	public void addSelectionListener(SelectionListener listener) {
		selectionListeners.add(listener);
	}

	public void addVerifyListener(VerifyListener listener) {
		verifyListeners.add(listener);
	}

	public void removeSelectionListener(SelectionListener listener) {
		selectionListeners.remove(listener);
	}

	public void removeVerifyListener(VerifyListener listener) {
		verifyListeners.remove(listener);
	}

	public void removeModifyListener(ModifyListener listener) {
		modifyListeners.remove(listener);
	}

}