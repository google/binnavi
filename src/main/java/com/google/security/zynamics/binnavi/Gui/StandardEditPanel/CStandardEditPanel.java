/*
Copyright 2011-2016 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.google.security.zynamics.binnavi.Gui.StandardEditPanel;



import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.CLabeledComponent;
import com.google.security.zynamics.binnavi.Gui.SaveFields.CSaveField;
import com.google.security.zynamics.binnavi.Gui.SaveFields.CSavePane;
import com.google.security.zynamics.binnavi.Help.CHelpLabel;
import com.google.security.zynamics.binnavi.Help.CHelpSaveField;
import com.google.security.zynamics.binnavi.Help.CHelpSavePane;
import com.google.security.zynamics.zylib.date.DateHelpers;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.textfields.JTextFieldLimit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Provides a standard panel to display and edit all parts of the database which have a name, a
 * description, a creation date, and a modification date.
 */
public final class CStandardEditPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 143124176789333548L;

  /**
   * Text field where the user can modify the name property.
   */
  private final CSaveField m_nameTextField;

  /**
   * Label that displays the creation date property.
   */
  private final JLabel m_creationDateValueLabel;

  /**
   * Label that displays the modification date property.
   */
  private final JLabel m_modificationDateValueLabel;

  /**
   * Text fields where the user can modify the description property.
   */
  private final CSavePane m_descriptionField;

  /**
   * Listeners that are notified about input changes.
   */
  private final ListenerProvider<IInputPanelListener> m_listeners =
      new ListenerProvider<IInputPanelListener>();

  /**
   * Last known saved name.
   */
  private String m_savedName;

  /**
   * Last known saved description.
   */
  private String m_savedDescription;

  /**
   * Creates a new standard edit panel.
   * 
   * @param headline Headline to be displayed in the panel border.
   * @param name Initial name value.
   * @param description Initial description value.
   * @param creationDate Initial creation date value.
   * @param modificationDate Initial modification date value.
   */
  public CStandardEditPanel(final String headline, final IFieldDescription<String> name,
      final IFieldDescription<String> description, final IFieldDescription<Date> creationDate,
      final IFieldDescription<Date> modificationDate) {
    super(new BorderLayout(5, 5));

    Preconditions.checkNotNull(headline, "IE02072: Headline argument can not be null");
    Preconditions.checkNotNull(name, "IE02073: Name argument can not be null");
    Preconditions.checkNotNull(description, "IE02074: Description argument can not be null");
    Preconditions.checkNotNull(creationDate, "IE02075: Creation date argument can not be null");
    Preconditions.checkNotNull(modificationDate,
        "IE02076: Modification date argument can not be null");

    m_savedName = name.getValue();
    m_savedDescription = description.getValue();

    setBorder(new EmptyBorder(0, 0, 0, 0));

    m_nameTextField = new CHelpSaveField(name.getHelp());
    m_nameTextField.setDocument(new JTextFieldLimit());
    m_nameTextField.setText(name.getValue());

    m_creationDateValueLabel =
        new CHelpLabel(DateHelpers.formatDateTime(creationDate.getValue()), creationDate.getHelp());
    m_modificationDateValueLabel =
        new CHelpLabel(DateHelpers.formatDateTime(modificationDate.getValue()),
            modificationDate.getHelp());

    m_descriptionField = new CHelpSavePane(description.getHelp());
    m_descriptionField.setDocument(new JTextFieldLimit());
    m_descriptionField.setText(description.getValue());

    addNameDatesPanel(headline, name, creationDate, modificationDate);
    addDescriptionPanel();

    final UpdateListener updateListener = new UpdateListener();

    m_nameTextField.getDocument().addDocumentListener(updateListener);
    m_descriptionField.getDocument().addDocumentListener(updateListener);
  }

  /**
   * Creates the panel where the description text field is located.
   */
  private void addDescriptionPanel() {
    final JPanel descriptionPanel = new JPanel(new BorderLayout());

    descriptionPanel.setMinimumSize(new Dimension(descriptionPanel.getPreferredSize().width, 50));
    descriptionPanel.add(new JScrollPane(m_descriptionField));
    descriptionPanel.setBorder(new TitledBorder("Description" + ":"));
    descriptionPanel.setMinimumSize(new Dimension(0, 146));
    descriptionPanel.setPreferredSize(new Dimension(0, 146));

    add(descriptionPanel, BorderLayout.CENTER);
  }

  /**
   * Creates the panel where the name text field and the creation date and modification date labels
   * are located.
   * 
   * @param headline Headline to be shown in the border of this panel.
   * @param name Provides information for the name field.
   * @param creationDate Provides information for the creation date field.
   * @param modificationDate Provides information for the modification date field.
   */
  private void addNameDatesPanel(final String headline, final IFieldDescription<String> name,
      final IFieldDescription<Date> creationDate, final IFieldDescription<Date> modificationDate) {
    final JPanel nameDatesPanel = new JPanel(new GridLayout(3, 1, 5, 5));

    nameDatesPanel.setBorder(new TitledBorder(headline));

    nameDatesPanel.add(new CLabeledComponent("Name" + ":", name.getHelp(), m_nameTextField));
    nameDatesPanel.add(new CLabeledComponent("Creation Date" + ":", creationDate.getHelp(),
        m_creationDateValueLabel));
    nameDatesPanel.add(new CLabeledComponent("Modification Date" + ":", modificationDate.getHelp(),
        m_modificationDateValueLabel));

    add(nameDatesPanel, BorderLayout.NORTH);
  }

  /**
   * Notifies the registered change listeners about changes in the input.
   */
  private void notifyChangeListeners() {
    for (final IInputPanelListener listener : m_listeners) {
      // ESCA-JAVA0166: Catch Exception because we call a listener function.
      try {
        listener.changedInput();
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Updates the save field backgrounds.
   */
  private void updateSaveFields() {
    m_nameTextField.setModified(!m_savedName.equals(m_nameTextField.getText()));
    m_descriptionField.setModified(!m_savedDescription.equals(m_descriptionField.getText()));
  }

  /**
   * Adds a listener object that is notified about changes in the input.
   * 
   * @param listener The listener object to add.
   */
  public void addInputListener(final IInputPanelListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Returns the description string that is currently entered in the description text field.
   * 
   * @return The description string.
   */
  public String getDescription() {
    return m_descriptionField.getText();
  }

  /**
   * Returns the name string that is currently entered in the name text field.
   * 
   * @return The description string.
   */
  public String getNameString() {
    return m_nameTextField.getText();
  }

  /**
   * Changes the content of the description text field.
   * 
   * @param description The new content of the description text field.
   */
  public void setDescription(final String description) {
    Preconditions.checkNotNull(description, "IE02077: Description argument can not be null");

    m_savedDescription = description;

    m_descriptionField.setText(description);

    updateSaveFields();
  }

  /**
   * Changes the text of the modification date label.
   * 
   * @param date The new modification date that is shown in the label.
   */
  public void setModificationDate(final Date date) {
    Preconditions.checkNotNull(date, "IE02078: Date argument can not be null");

    m_modificationDateValueLabel.setText(DateHelpers.formatDateTime(date));
  }

  /**
   * Changes the content of the name text field.
   * 
   * @param name The new content of the name text field.
   */
  public void setNameString(final String name) {
    Preconditions.checkNotNull(name, "IE02079: Name argument can not be null");

    m_savedName = name;

    m_nameTextField.setText(name);

    updateSaveFields();
  }

  /**
   * Listener that keeps track of input changes.
   */
  private class UpdateListener implements DocumentListener {
    @Override
    public void changedUpdate(final DocumentEvent event) {
      updateSaveFields();

      notifyChangeListeners();
    }

    @Override
    public void insertUpdate(final DocumentEvent event) {
      updateSaveFields();

      notifyChangeListeners();
    }

    @Override
    public void removeUpdate(final DocumentEvent event) {
      updateSaveFields();

      notifyChangeListeners();
    }
  }
}
