/*******************************************************************************
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
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
package com.hpe.octane.ideplugins.eclipse.ui.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.EditorPart;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.FieldModel;
import com.hpe.adm.octane.ideplugins.services.EntityService;
import com.hpe.adm.octane.ideplugins.services.exception.ServiceException;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import com.hpe.adm.octane.ideplugins.services.ui.FormField;
import com.hpe.adm.octane.ideplugins.services.ui.FormLayout;
import com.hpe.adm.octane.ideplugins.services.ui.FormLayoutSection;
import com.hpe.adm.octane.ideplugins.services.util.Util;
import com.hpe.octane.ideplugins.eclipse.Activator;
import com.hpe.octane.ideplugins.eclipse.ui.combobox.CustomEntityComboBox;
import com.hpe.octane.ideplugins.eclipse.ui.combobox.CustomEntityComboBoxLabelProvider;
import com.hpe.octane.ideplugins.eclipse.ui.editor.job.ChangePhaseJob;
import com.hpe.octane.ideplugins.eclipse.ui.editor.job.GetEntityDetailsJob;
import com.hpe.octane.ideplugins.eclipse.ui.editor.job.SendCommentJob;
import com.hpe.octane.ideplugins.eclipse.ui.util.LoadingComposite;
import com.hpe.octane.ideplugins.eclipse.ui.util.StackLayoutComposite;
import com.hpe.octane.ideplugins.eclipse.ui.util.TruncatingStyledText;
import com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants;
import com.hpe.octane.ideplugins.eclipse.util.EntityIconFactory;
import com.hpe.octane.ideplugins.eclipse.util.InfoPopup;
import com.hpe.octane.ideplugins.eclipse.util.LinkInterceptListener;
import com.hpe.octane.ideplugins.eclipse.util.PropagateScrollBrowserFactory;
import com.hpe.octane.ideplugins.eclipse.util.resource.ImageResources;
import com.hpe.octane.ideplugins.eclipse.util.resource.SWTResourceManager;

public class EntityModelEditor extends EditorPart {
	
    public static final String ID = "com.hpe.octane.ideplugins.eclipse.ui.EntityModelEditor"; //$NON-NLS-1$
    private static final String GO_TO_BROWSER_DIALOG_MESSAGE = "You can try to change the phase using ALM Octane in a browser."
            + "\nDo you want to do this now?";
    private static final int MIN_WIDTH = 800;
    private static EntityIconFactory entityIconFactoryForTabInfo = new EntityIconFactory(20, 20, 7);
    private static EntityIconFactory entityIconFactory = new EntityIconFactory(25, 25, 7);
    private static EntityService entityService = Activator.getInstance(EntityService.class);
    private EntityModel entityModel;
    @SuppressWarnings("rawtypes")
    private FieldModel currentPhase;
    private EntityModel selectedPhase;
    private EntityModelEditorInput input;
    private Collection<EntityModel> possibleTransitions;
    private boolean shouldShowPhase = true;
    private boolean shouldCommentsBeShown;
    private GetEntityDetailsJob getEntiyJob;
    private Text inputComments;
    private String comments;
    private StackLayoutComposite rootComposite;
    private ScrolledComposite headerAndEntityDetailsScrollComposite;
    private Composite entityDetailsParentComposite;
    private LoadingComposite loadingComposite;
    private FormLayout octaneEntityForm;
    private Form sectionsParentForm;
    private FormToolkit formGenerator;
    private Composite headerAndEntityDetailsParent;
    private ToolTip truncatedLabelTooltip;  
    private Color backgroundColor = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry().get(JFacePreferences.CONTENT_ASSIST_BACKGROUND_COLOR);
    private Color foregroundColor = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry().get(JFacePreferences.CONTENT_ASSIST_FOREGROUND_COLOR);
   
    public EntityModelEditor() {
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        if (!(input instanceof EntityModelEditorInput)) {
            throw new RuntimeException("Wrong input");
        }
        this.input = (EntityModelEditorInput) input;
        setSite(site);
        setInput(input);
        setPartName(String.valueOf(this.input.getId()));
        setTitleImage(entityIconFactoryForTabInfo.getImageIcon(this.input.getEntityType()));
    }

    /**
     * Create contents of the editor part.
     * 
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {               
        truncatedLabelTooltip = new ToolTip(parent.getShell(), SWT.ICON_INFORMATION);               
        rootComposite = new StackLayoutComposite(parent, SWT.NONE);     
        // set loading GIF until the data is loaded
        loadingComposite = new LoadingComposite(rootComposite, SWT.NONE);
        rootComposite.showControl(loadingComposite);
        // This job retrieves the necessary data for the details view
        getEntiyJob = new GetEntityDetailsJob("Retiving entity details", this.input.getEntityType(), this.input.getId());
        getEntiyJob.schedule();
        getEntiyJob.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void scheduled(IJobChangeEvent event) {
                Display.getDefault().asyncExec(() -> {
                    rootComposite.showControl(loadingComposite);
                });
            }
            @Override
            public void done(IJobChangeEvent event) {
                if (getEntiyJob.wasEntityRetrived()) {
                    entityModel = getEntiyJob.getEntiyData();
                    Display.getDefault().asyncExec(() -> {
                        entityModel = getEntiyJob.getEntiyData();
                        octaneEntityForm = getEntiyJob.getFormForCurrentEntity();
                        if (getEntiyJob.shouldShowPhase()) {
                            shouldShowPhase = true;
                            currentPhase = getEntiyJob.getCurrentPhase();
                            possibleTransitions = getEntiyJob.getPossibleTransitionsForCurrentEntity();
                        } else {
                            shouldShowPhase = false;
                        }
                        if (getEntiyJob.shouldCommentsBeShown()) {
                            shouldCommentsBeShown = true;
                            comments = getEntiyJob.getCommentsForCurrentEntity();
                        } else {
                            shouldCommentsBeShown = false;
                        }
                        // After the data is loaded the UI is created
                        createEntityDetailsView(rootComposite);
                        // After the UI is created it gets displayed
                        rootComposite.showControl(headerAndEntityDetailsScrollComposite);
                    });
                }
            }
        });
    }

    private void createEntityDetailsView(Composite parent) {
        headerAndEntityDetailsScrollComposite = new ScrolledComposite(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        headerAndEntityDetailsParent = new Composite(headerAndEntityDetailsScrollComposite, SWT.NONE);
        headerAndEntityDetailsParent.addPaintListener(new PaintListener() {
    		@Override
    	    public void paintControl(PaintEvent paintEvent) {        
    			headerAndEntityDetailsParent.setBackground(backgroundColor);        	    
    	    }
        });  
        headerAndEntityDetailsParent.setLayout(new FillLayout(SWT.HORIZONTAL));
        headerAndEntityDetailsParent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
        createHeaderPanel(headerAndEntityDetailsParent);    
        headerAndEntityDetailsScrollComposite.setContent(headerAndEntityDetailsParent);
        formGenerator = new FormToolkit(parent.getDisplay());

        Composite entityDetailsAndCommentsComposite = new Composite(headerAndEntityDetailsParent, SWT.NONE);
        entityDetailsAndCommentsComposite.setForeground(SWTResourceManager.getColor(SWT.COLOR_LIST_SELECTION));        
        entityDetailsAndCommentsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        entityDetailsAndCommentsComposite.addPaintListener(new PaintListener() {
    		@Override
    	    public void paintControl(PaintEvent paintEvent) {        
    			entityDetailsAndCommentsComposite.setBackground(backgroundColor);        	    
    	    }
        });  
        formGenerator.adapt(entityDetailsAndCommentsComposite);
        formGenerator.paintBordersFor(entityDetailsAndCommentsComposite);
        entityDetailsAndCommentsComposite.setLayout(new GridLayout(3, false));
        entityDetailsParentComposite = new Composite(entityDetailsAndCommentsComposite, SWT.NONE);
        entityDetailsParentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        entityDetailsParentComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
        entityDetailsParentComposite.addPaintListener(new PaintListener() {
    		@Override
    	    public void paintControl(PaintEvent paintEvent) {        
    			entityDetailsParentComposite.setBackground(backgroundColor);        	    
    	    }
        }); 
        formGenerator.adapt(entityDetailsParentComposite);
        formGenerator.paintBordersFor(entityDetailsParentComposite);
        sectionsParentForm = formGenerator.createForm(entityDetailsParentComposite);
        sectionsParentForm.getBody().setLayout(new GridLayout(1, false)); 
        sectionsParentForm.getBody().addPaintListener(new PaintListener() {
    		@Override
    	    public void paintControl(PaintEvent paintEvent) {        
    			sectionsParentForm.getBody().setBackground(backgroundColor);        	    
    	    }
        });
        if (shouldCommentsBeShown) {
            Label commentsSeparator = new Label(entityDetailsAndCommentsComposite, SWT.SEPARATOR | SWT.SHADOW_IN);
            commentsSeparator.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT));
            commentsSeparator.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
            formGenerator.adapt(commentsSeparator, true, true);
            Composite commentsParentComposite = new Composite(entityDetailsAndCommentsComposite, SWT.NONE);
            commentsParentComposite.addPaintListener(new PaintListener() {
        		@Override
        	    public void paintControl(PaintEvent paintEvent) {        
        			commentsParentComposite.setBackground(backgroundColor);        	    
        	    }
            }); 
            GridData gd_commentsParentComposite = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
            gd_commentsParentComposite.widthHint = 300;
            gd_commentsParentComposite.minimumWidth = 300;
            commentsParentComposite.setLayoutData(gd_commentsParentComposite);
            formGenerator.adapt(commentsParentComposite);
            formGenerator.paintBordersFor(commentsParentComposite);
            commentsParentComposite.setLayout(new GridLayout(1, false));
            Label commentsTitleLabel = new Label(commentsParentComposite, SWT.NONE);
            formGenerator.adapt(commentsTitleLabel, true, true);
            commentsTitleLabel.setText("Comments");
            commentsTitleLabel.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
            //commentsTitleLabel.setMargins(5, 0, 0, 0);
            commentsTitleLabel.addPaintListener(new PaintListener() {
        		@Override
        	    public void paintControl(PaintEvent paintEvent) {        
        			commentsTitleLabel.setBackground(backgroundColor);        	    
        	    }
            }); 
            commentsTitleLabel.setForeground(foregroundColor);          
            Composite inputCommentAndSendButtonComposite = new Composite(commentsParentComposite, SWT.NONE);
            inputCommentAndSendButtonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));       
            inputCommentAndSendButtonComposite.addPaintListener(new PaintListener() {
        		@Override
        	    public void paintControl(PaintEvent paintEvent) {        
        			inputCommentAndSendButtonComposite.setBackground(backgroundColor);        	    
        	    }
            }); 
            formGenerator.adapt(inputCommentAndSendButtonComposite);
            formGenerator.paintBordersFor(inputCommentAndSendButtonComposite);
            inputCommentAndSendButtonComposite.setLayout(new GridLayout(2, false));         
            inputComments = new Text(inputCommentAndSendButtonComposite, SWT.BORDER);
            inputComments.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
            inputComments.setToolTipText("Add new comment");          
            inputComments.setEnabled(true);
            inputComments.addListener(SWT.Traverse, (Event event) -> {
                if (event.detail == SWT.TRAVERSE_RETURN && inputComments.isEnabled()) {
                    postComment(inputComments.getText());
                    inputComments.setEnabled(false);
                }
            });
            formGenerator.adapt(inputComments, true, true);
            Button postCommentBtn = new Button(inputCommentAndSendButtonComposite, SWT.NONE);
            postCommentBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));          
            postCommentBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (inputComments.isEnabled()) {
                        postComment(inputComments.getText());
                        inputComments.setEnabled(false);
                    }
                }
            });
            formGenerator.adapt(postCommentBtn, true, true);
            postCommentBtn.setText("Send");
            PropagateScrollBrowserFactory fac = new PropagateScrollBrowserFactory();
            Browser commentsPanel = fac.createBrowser(commentsParentComposite, SWT.NONE);
            commentsPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
            formGenerator.adapt(commentsPanel);
            formGenerator.paintBordersFor(commentsPanel);
            commentsPanel.setText(comments);
            commentsPanel.addLocationListener(new LinkInterceptListener());
        }
        if (entityModel != null) {
            // create other Sections
            for (FormLayoutSection formSection : octaneEntityForm.getFormLayoutSections()) {
                createSectionsWithEntityData(formSection);
            }
            // Create Description Section
            createDescriptionFormSection();
        }
        // Make scroll force child into submission (width)
        Point childSize = headerAndEntityDetailsScrollComposite.getContent().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        headerAndEntityDetailsScrollComposite.setMinHeight(childSize.y);
        headerAndEntityDetailsScrollComposite.setMinWidth(MIN_WIDTH);
        headerAndEntityDetailsScrollComposite.setExpandHorizontal(true);
        headerAndEntityDetailsScrollComposite.setExpandVertical(true);
    }

    private void createHeaderPanel(Composite parent) {
        headerAndEntityDetailsParent.setLayout(new GridLayout(1, false));
        Composite headerComposite = new Composite(parent, SWT.NONE);
        headerComposite.addPaintListener(new PaintListener() {
    		@Override
    	    public void paintControl(PaintEvent paintEvent) {        
    			headerComposite.setBackground(backgroundColor);        	    
    	    }
        });    
        headerComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        headerComposite.setLayout(new GridLayout(6, false));
        Label entityIcon = new Label(headerComposite, SWT.NONE);
        entityIcon.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        entityIcon.setImage(entityIconFactory.getImageIcon(Entity.getEntityType(entityModel)));
        Link linkEntityName = new Link(headerComposite, SWT.NONE);
        linkEntityName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        linkEntityName.addPaintListener(new PaintListener() {
    		@Override
    	    public void paintControl(PaintEvent paintEvent) {        
    			linkEntityName.setBackground(backgroundColor);        	    
    	    }
        });  
        Font boldFont = new Font(linkEntityName.getDisplay(), new FontData(JFaceResources.DEFAULT_FONT, 12, SWT.BOLD ));
        //linkEntityName.setForeground(foregroundColor);
        linkEntityName.setFont(boldFont);
        try {
			linkEntityName.setText("<A>" + entityService.findEntity(Entity.getEntityType(entityModel),Long.parseLong((String) entityModel.getValue(EntityFieldsConstants.FIELD_ID).getValue())).getValue(EntityFieldsConstants.FIELD_NAME).getValue() + "</A>");
		} catch (ServiceException e) {
			e.printStackTrace();
		}
        linkEntityName.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                Activator.getInstance(EntityService.class).openInBrowser(entityModel);
            }
        });
        //linkEntityName.setLinkForeground(linkEntityName.getForeground());
        if (shouldShowPhase) {
            Label lblCurrentPhase = new Label(headerComposite, SWT.NONE);
            lblCurrentPhase.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            lblCurrentPhase.addPaintListener(new PaintListener() {
        		@Override
        	    public void paintControl(PaintEvent paintEvent) {        
        			lblCurrentPhase.setBackground(backgroundColor);        	    
        	    }
            });  
            lblCurrentPhase.setText(Util.getUiDataFromModel(currentPhase, EntityFieldsConstants.FIELD_NAME));
            CustomEntityComboBox<EntityModel> nextPhasesComboBox = new CustomEntityComboBox<EntityModel>(headerComposite);
            nextPhasesComboBox.addSelectionListener((phaseEntityModel, newSelection) -> {
                selectedPhase = newSelection;
            });
            nextPhasesComboBox.setLabelProvider(new CustomEntityComboBoxLabelProvider<EntityModel>() {
                @Override
                public String getSelectedLabel(EntityModel entityModelElement) {
                    return Util.getUiDataFromModel(entityModelElement.getValue("target_phase"), "name");
                }
                @Override
                public String getListLabel(EntityModel entityModelElement) {
                    return Util.getUiDataFromModel(entityModelElement.getValue("target_phase"), "name");
                }
            });
            nextPhasesComboBox.setContent(new ArrayList<>(possibleTransitions));
            nextPhasesComboBox.selectFirstItem();
            Button savePhase = new Button(headerComposite, SWT.NONE);
            savePhase.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ETOOL_SAVE_EDIT));
            savePhase.addListener(SWT.Selection, new Listener() {

                @Override
                public void handleEvent(Event event) {
                    saveCurrentPhase();
                }
            });
        }
        Button refresh = new Button(headerComposite, SWT.NONE);
        refresh.setImage(ImageResources.REFRESH_16X16.getImage());
        refresh.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                getEntiyJob.schedule();
            }
        });
    }

    // STEP 3
    private void createDescriptionFormSection() {
        Section section = formGenerator.createSection(sectionsParentForm.getBody(), Section.TREE_NODE | Section.EXPANDED);
        formGenerator.createCompositeSeparator(section);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        gd.minimumHeight = 400;
        section.setLayoutData(gd);
        section.setLayout(new FillLayout(SWT.HORIZONTAL));
        section.setText("Description");       
        section.addPaintListener(new PaintListener() {
    		@Override
    	    public void paintControl(PaintEvent paintEvent) {            			  	  
    			section.setTitleBarForeground(foregroundColor);
    		}
        }); 
        
        PropagateScrollBrowserFactory factory = new PropagateScrollBrowserFactory();
        Browser descriptionPanel = factory.createBrowser(section, SWT.NONE);       
        String backgroundColorString = "rgb(" + backgroundColor.getRed() + "," + backgroundColor.getGreen() + "," + backgroundColor.getBlue() + ")" ;       
        String foregroundColorString = "rgb(" + foregroundColor.getRed() + "," + foregroundColor.getGreen() + "," + foregroundColor.getBlue() + ")" ;
        String descriptionText = "<html><body bgcolor =" + backgroundColorString +">" + "<font color =" + foregroundColorString +">" +
        						  Util.getUiDataFromModel(entityModel.getValue(EntityFieldsConstants.FIELD_DESCRIPTION)) + "</font></body></html>";
        if (descriptionText.equals("<html><body bgcolor =" + backgroundColorString +">" + "<font color =" + foregroundColorString +">"+ "</font></body></html>")) {
            descriptionPanel.setText("<html><body bgcolor =" + backgroundColorString +">" + "<font color =" + foregroundColorString +">"+"No description"+ "</font></body></html>");
        } else {
            descriptionPanel.setText(descriptionText);
        }
        descriptionPanel.addLocationListener(new LinkInterceptListener());
        section.setClient(descriptionPanel);
    }

    // STEP 4
    private void createSectionsWithEntityData(FormLayoutSection formSection) {
        Section section = formGenerator.createSection(sectionsParentForm.getBody(), Section.TREE_NODE | Section.EXPANDED);              
        section.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
        section.setText(formSection.getSectionTitle());
        section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        section.setExpanded(true);              
        section.addPaintListener(new PaintListener() {
    		@Override
    	    public void paintControl(PaintEvent paintEvent) {        
    			section.setTitleBarForeground(foregroundColor);       	    
    	    }
        });  
        formGenerator.createCompositeSeparator(section);
        Composite sectionClient = new Composite(section, SWT.NONE);
        sectionClient.setLayout(new FillLayout(SWT.HORIZONTAL));     
        Composite sectionClientLeft = new Composite(sectionClient, SWT.NONE);
        sectionClientLeft.setLayout(new GridLayout(2, false));
        sectionClientLeft.setForeground(foregroundColor);
        sectionClientLeft.addPaintListener(new PaintListener() {
    		@Override
    	    public void paintControl(PaintEvent paintEvent) {        
    			sectionClientLeft.setBackground(backgroundColor);
    	    }
        });  
        Composite sectionClientRight = new Composite(sectionClient, SWT.NONE);
        sectionClientRight.setLayout(new GridLayout(2, false));
        sectionClientRight.setForeground(foregroundColor);
        sectionClientRight.addPaintListener(new PaintListener() {
    		@Override
    	    public void paintControl(PaintEvent paintEvent) {        
    			sectionClientRight.setBackground(backgroundColor);        	    
    	    }
        });  
        List<FormField> formFields = formSection.getFields();

        for (int i = 0; i < formFields.size(); i++) {
            String fieldName = formFields.get(i).getName();
            String fielValue;
            if (EntityFieldsConstants.FIELD_OWNER.equals(fieldName)
                    || EntityFieldsConstants.FIELD_AUTHOR.equals(fieldName)
                    || EntityFieldsConstants.FIELD_TEST_RUN_RUN_BY.equals(fieldName)
                    || EntityFieldsConstants.FIELD_DETECTEDBY.equals(fieldName)) {
                fielValue = Util.getUiDataFromModel(entityModel.getValue(fieldName), EntityFieldsConstants.FIELD_FULL_NAME);
            } else {
                fielValue = Util.getUiDataFromModel(entityModel.getValue(fieldName));
            }
            // Skip the description fields as it's set into another ui component
            // below this one
            if (EntityFieldsConstants.FIELD_DESCRIPTION.equals(fieldName)) {
                formFields.remove(i);
                i--;
                continue;
            }
            // Determine if we put the label pair in the left or right container
            Composite parent;
            if (i % 2 == 0) {
                parent = sectionClientLeft;
            } else {
                parent = sectionClientRight;
            }
            // Add the pair of labels for field and value                        
            CLabel labelFieldName = new CLabel(parent, SWT.TRANSPARENT);
            labelFieldName.setText(prettifyLabels(fieldName));
            labelFieldName.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
            labelFieldName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
            labelFieldName.addPaintListener(new PaintListener() {
            		@Override
            	    public void paintControl(PaintEvent paintEvent) {        
	            	    labelFieldName.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
	            	    Pattern pattern;
	            	    pattern = new Pattern(paintEvent.gc.getDevice(), 0,0,0,100, paintEvent.gc.getDevice().getSystemColor(SWT.COLOR_TRANSPARENT),230, paintEvent.gc.getDevice().getSystemColor(SWT.COLOR_TRANSPARENT),230);
	            	    paintEvent.gc.setBackgroundPattern(pattern);
            	    }
            });            
            TruncatingStyledText labelValue = new TruncatingStyledText(parent, SWT.NONE, truncatedLabelTooltip);
            labelValue.setText(fielValue);
            labelValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            labelValue.setForeground(foregroundColor);
        }
        section.setClient(sectionClient);
    }

    private String prettifyLabels(String str1) {
        //for udfs
        if(str1.contains("_udf")) {
        	str1 = getEntiyJob.getUdfLabel(str1);
        }
    	str1 = str1.replaceAll("_udf", "");
        str1 = str1.replaceAll("_", " ");
        char[] chars = str1.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        for (int x = 1; x < chars.length; x++) {
            if (chars[x - 1] == ' ') {
                chars[x] = Character.toUpperCase(chars[x]);
            }
        }
        return new String(chars);
    }

    private void saveCurrentPhase() {
        ChangePhaseJob changePhaseJob = new ChangePhaseJob("Chaging phase of entity", entityModel, selectedPhase);
        changePhaseJob.schedule();
        changePhaseJob.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(IJobChangeEvent event) {
                Display.getDefault().asyncExec(() -> {
                    if (changePhaseJob.isPhaseChanged()) {
                        new InfoPopup("Phase Transition", "Phase was changed").open();
                    } else {
                        boolean shouldGoToBroeser = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "Business rule violation",
                                "Phase changed failed \n" + GO_TO_BROWSER_DIALOG_MESSAGE);
                        if (shouldGoToBroeser) {
                            entityService.openInBrowser(entityModel);
                        }
                    }
                    getEntiyJob.schedule();
                });
            }
        });
    }

    private void postComment(String text) {
        SendCommentJob sendCommentJob = new SendCommentJob("Sending Comments", entityModel, text);
        sendCommentJob.schedule();
        sendCommentJob.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(IJobChangeEvent event) {
                Display.getDefault().asyncExec(() -> {
                    if (sendCommentJob.isCommentsSaved()) {
                        new InfoPopup("Comments", "Comment send").open();
                    } else {
                        MessageDialog.openError(Display.getCurrent().getActiveShell(), "ERROR",
                                "Comments could not be send \n ");
                    }
                    getEntiyJob.schedule();
                });
            }
        });

    }

    @Override
    public void setFocus() {
        rootComposite.setFocus();
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        // Not supported
    }

    @Override
    public void doSaveAs() {
        // Not supported
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }
    
}