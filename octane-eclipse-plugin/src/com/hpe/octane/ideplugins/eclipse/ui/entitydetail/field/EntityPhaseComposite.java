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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.ReferenceFieldModel;
import com.hpe.adm.octane.ideplugins.services.EntityService;
import com.hpe.adm.octane.ideplugins.services.model.EntityModelWrapper;
import com.hpe.adm.octane.ideplugins.services.util.Util;
import com.hpe.octane.ideplugins.eclipse.Activator;
import com.hpe.octane.ideplugins.eclipse.ui.entitydetail.job.GetPossiblePhasesJob;
import com.hpe.octane.ideplugins.eclipse.ui.util.error.ErrorDialog;
import com.hpe.octane.ideplugins.eclipse.ui.util.resource.ImageResources;
import com.hpe.octane.ideplugins.eclipse.ui.util.resource.SWTResourceManager;
import com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants;

public class EntityPhaseComposite extends Composite {

    private static final String CURRENT_PHASE_PLACE_HOLDER = "CURRENT_PHASE";
    private static final String NEXT_PHASE_PLACE_HOLDER = "Move to: NEXT_PHASE";
    private static final String TOOLTIP_BLOCKED_PHASE = "You must save first before doing any more changes to phase";
    private static final String TOOLTIP_CLICKABLE_PHASE = "Click here to choose you desired next phase";

    private static EntityService entityService = Activator.getInstance(EntityService.class);

    private Label lblPhase;
    private Label lblCurrentPhase;
    private Label lblNextPhase;

    private EntityModelWrapper entityModelWrapper;
    private EntityModel newSelection;

    private Label btnSelectPhase;
    private Menu phaseSelectionMenu;
    private GridData gdBtnSelectPhase;

    private Label lblSeparatorLeft;
    private Label lblSeparatorRight;
    private Label lblSeparatorMiddle;

    public EntityPhaseComposite(Composite parent, int style) {
        super(parent, style);
        setLayout(new GridLayout(7, false));

        lblSeparatorLeft = new Label(this, SWT.SEPARATOR | SWT.VERTICAL);
        lblSeparatorLeft.setLayoutData(createSeparatorGridData());

        lblPhase = new Label(this, SWT.NONE);
        lblPhase.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblPhase.setAlignment(SWT.CENTER);
        lblPhase.setText("Phase:");

        lblCurrentPhase = new Label(this, SWT.NONE);
        lblCurrentPhase.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblCurrentPhase.setFont(SWTResourceManager.getBoldFont(lblPhase.getFont()));
        lblCurrentPhase.setText(CURRENT_PHASE_PLACE_HOLDER);

        lblSeparatorMiddle = new Label(this, SWT.SEPARATOR | SWT.VERTICAL);
        lblSeparatorMiddle.setLayoutData(createSeparatorGridData());

        lblNextPhase = new Label(this, SWT.NONE);
        lblNextPhase.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblNextPhase.setFont(SWTResourceManager.getBoldFont(lblNextPhase.getFont()));
        lblNextPhase.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLUE));
        lblNextPhase.setText(NEXT_PHASE_PLACE_HOLDER);
        lblNextPhase.setToolTipText(TOOLTIP_CLICKABLE_PHASE);
        lblNextPhase.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND));

        lblNextPhase.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                updateCurrentEntity();
                disableDisplayButtons();
            }
        });

        btnSelectPhase = new Label(this, SWT.NONE);
        btnSelectPhase.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND));
        gdBtnSelectPhase = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        btnSelectPhase.setLayoutData(gdBtnSelectPhase);
        btnSelectPhase.setImage(ImageResources.DROP_DOWN.getImage());

        phaseSelectionMenu = new Menu(btnSelectPhase);
        phaseSelectionMenu.setOrientation(SWT.RIGHT_TO_LEFT);
        btnSelectPhase.setMenu(phaseSelectionMenu);

        lblSeparatorRight = new Label(this, SWT.SEPARATOR | SWT.VERTICAL);
        lblSeparatorRight.setLayoutData(createSeparatorGridData());

        btnSelectPhase.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                final Rectangle bounds = btnSelectPhase.getBounds();
                Point btnPosition = btnSelectPhase.toDisplay(bounds.width, bounds.height);
                int x = btnPosition.x;
                int y = btnPosition.y + 1;
                phaseSelectionMenu.setLocation(x, y);
                phaseSelectionMenu.setVisible(true);
            }
        });
    }

    public void setEntityModel(EntityModelWrapper entityModelWrapper) {
        this.entityModelWrapper = entityModelWrapper;
        getPossiblePhaseTransitions();
        enableDisplayButtons();
    }

    private GridData createSeparatorGridData() {
        GridData gdSeparator = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
        gdSeparator.heightHint = 16;
        return gdSeparator;
    }

    private void getPossiblePhaseTransitions() {
        if (GetPossiblePhasesJob.hasPhases(entityModelWrapper.getEntityType())) {
            String currentPhaseName = Util.getUiDataFromModel(entityModelWrapper.getValue(EntityFieldsConstants.FIELD_PHASE));
            lblCurrentPhase.setText(currentPhaseName);

            // load possible phases
            GetPossiblePhasesJob getPossiblePhasesJob = new GetPossiblePhasesJob("Loading possible phases",
                    entityModelWrapper.getReadOnlyEntityModel());
            getPossiblePhasesJob.addJobChangeListener(new JobChangeAdapter() {
                @Override
                public void scheduled(IJobChangeEvent event) {
                    Display.getDefault().asyncExec(() -> {
                        if (!phaseSelectionMenu.isDisposed()) {
                            for (MenuItem items : phaseSelectionMenu.getItems()) {
                                items.dispose();
                            }
                        }
                    });
                }

                @Override
                public void done(IJobChangeEvent event) {
                    Display.getDefault().asyncExec(() -> {
                        try {
                            Exception exception = getPossiblePhasesJob.getException();
                            if (exception != null) {
                                throw exception;
                            } else {
                                Collection<EntityModel> possibleTransitions = getPossiblePhasesJob.getPossibleTransitions();

                                if (possibleTransitions.isEmpty()) {
                                    lblNextPhase.setText("No transition");
                                    lblNextPhase.setEnabled(false);
                                    btnSelectPhase.setEnabled(false);
                                } else {
                                    List<EntityModel> possiblePhasesList = new ArrayList<>(getPossiblePhasesJob.getPossibleTransitions());

                                    // initialize the label next-phase with the first items
                                    String initialValueNextPhase = "Move to: "
                                            + Util.getUiDataFromModel((possiblePhasesList.get(0)).getValue("target_phase"));
                                    lblNextPhase.setText(initialValueNextPhase);
                                    newSelection = possiblePhasesList.get(0);
                                    if (possiblePhasesList.size() < 2) {
                                        btnSelectPhase.setVisible(false);
                                        gdBtnSelectPhase.exclude = true;
                                    } else {
                                        btnSelectPhase.setVisible(true);
                                        gdBtnSelectPhase.exclude = false;
                                        for (int i = 1; i < possiblePhasesList.size(); i++) {
                                            EntityModel nextTargetPhase = possiblePhasesList.get(i);
                                            String nextTargetPhaseName = Util.getUiDataFromModel(nextTargetPhase.getValue("target_phase"));

                                            MenuItem menuItemPhase = new MenuItem(phaseSelectionMenu, SWT.NONE);
                                            menuItemPhase.setText(nextTargetPhaseName);
                                            menuItemPhase.addListener(SWT.Selection, new Listener() {
                                                @Override
                                                public void handleEvent(Event e) {
                                                    String newString = "Move to: " + menuItemPhase.getText();
                                                    lblNextPhase.setText(newString);
                                                    newSelection = nextTargetPhase;
                                                    updateCurrentEntity();
                                                    disableDisplayButtons();
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            ErrorDialog errorDialog = new ErrorDialog(getParent().getShell());
                            errorDialog.addButton("Back", () -> errorDialog.close());
                            errorDialog.addButton("Open in browser", () -> {
                                entityService.openInBrowser(entityModelWrapper.getReadOnlyEntityModel());
                                errorDialog.close();
                            });
                            errorDialog.displayException(getPossiblePhasesJob.getException(), "Failed to change phase");
                        }
                    });

                }
            });
            getPossiblePhasesJob.schedule();
        }
    }

    private void updateCurrentEntity() {
        if (newSelection.getValue("target_phase") instanceof ReferenceFieldModel) {
            ReferenceFieldModel targetPhaseFieldModel = (ReferenceFieldModel) newSelection.getValue("target_phase");
            entityModelWrapper.setValue(new ReferenceFieldModel("phase", targetPhaseFieldModel.getValue()));
            String newString = lblNextPhase.getText();
            newString = newString.replace("Move to: ", "");
            lblCurrentPhase.setText(newString);
            layout();
            getParent().layout();
        }
    }

    private void enableDisplayButtons() {
        lblNextPhase.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLUE));
        lblNextPhase.setToolTipText(TOOLTIP_CLICKABLE_PHASE);
        lblNextPhase.setEnabled(true);
        btnSelectPhase.setEnabled(true);
    }

    private void disableDisplayButtons() {
        lblNextPhase.setToolTipText(TOOLTIP_BLOCKED_PHASE);
        lblNextPhase.setForeground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
        String newString = lblNextPhase.getText();
        newString = newString.replace("Move to: ", "Moved to: ");
        lblNextPhase.setText(newString);
        btnSelectPhase.setEnabled(false);
        lblNextPhase.setEnabled(false);
        layout();
        getParent().layout();

    }
}
