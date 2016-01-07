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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.InstructionHighlighter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.SettingsDialog.CHintCreator;
import com.google.security.zynamics.zylib.gui.ColorPanel.ColorPanel;
import com.google.security.zynamics.zylib.gui.ColorPanel.IColorPanelListener;

/**
 * Options panel where the user can configure special instruction highlighting.
 */
public final class COptionsPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 4192775617682584194L;

  /**
   * Width of all color panels.
   */
  private static final int COLORPANEL_WIDTH = 200;

  /**
   * Height of all color panels.
   */
  private static final int COLORPANEL_HEIGHT = 25;

  /**
   * Map of listeners added to color panels.
   */
  private final Map<ColorPanel, IColorPanelListener> m_listenerMap =
      new HashMap<ColorPanel, IColorPanelListener>();

  /**
   * Creates a new color settings panel.
   * 
   * @param descriptions Descriptions to be configured..
   */
  public COptionsPanel(final List<ITypeDescription> descriptions) {
    super(new BorderLayout());

    Preconditions.checkNotNull(descriptions, "IE00667: Descriptions argument can not be null");

    final JPanel mainPanel = new JPanel(new BorderLayout());
    final JPanel innerMainPanel = new JPanel(new GridBagLayout());
    final JPanel functionTypeColorPanel = new JPanel(new GridLayout(descriptions.size(), 1, 3, 3));

    for (final ITypeDescription description : descriptions) {
      final ColorPanel colorPanel = new ColorPanel(description.getColor(), true, true);

      final InternalColorPanelListener listener = new InternalColorPanelListener(description);
      colorPanel.addListener(listener);
      m_listenerMap.put(colorPanel, listener);

      buildRow(functionTypeColorPanel, description, description.getHint(), colorPanel, false);
    }

    functionTypeColorPanel.setBorder(new TitledBorder("Colors"));

    final GridBagConstraints constraints = new GridBagConstraints();

    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.anchor = GridBagConstraints.FIRST_LINE_START;
    constraints.weightx = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;

    innerMainPanel.add(functionTypeColorPanel, constraints);

    mainPanel.add(innerMainPanel, BorderLayout.NORTH);

    add(new JScrollPane(mainPanel));
  }

  /**
   * Builds a single row of components in the panel.
   * 
   * @param <T> Type of the editing component.
   * 
   * @param panel Panel the editing component is added to.
   * @param description Type description to be configured in that row.
   * @param hint Hint shown as a tooltip.
   * @param component The component to add to the panel.
   * @param isLast True, if the component is the last component to be added to the panel.
   * 
   * @return The panel passed to the function.
   */
  private <T extends Component> T buildRow(final JPanel panel, final ITypeDescription description,
      final String hint, final T component, final boolean isLast) {
    component.setPreferredSize(new Dimension(COLORPANEL_WIDTH, COLORPANEL_HEIGHT));

    final JPanel rowPanel = new JPanel(new BorderLayout());
    rowPanel.setBorder(new EmptyBorder(0, 2, isLast ? 2 : 0, 2));

    final JPanel innerPanel = new JPanel(new GridLayout(1, 2));

    innerPanel.add(new JCheckBox(
        new CheckboxAction(description, description.getDescription() + ":")), BorderLayout.CENTER);
    innerPanel.add(CHintCreator.createHintPanel(component, hint), BorderLayout.EAST);

    rowPanel.add(innerPanel, BorderLayout.WEST);

    panel.add(rowPanel);

    return component;
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    for (final Entry<ColorPanel, IColorPanelListener> entry : m_listenerMap.entrySet()) {
      entry.getKey().removeListener(entry.getValue());
    }
  }

  /**
   * Action class that handles check box clicks.
   */
  private static class CheckboxAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = -4179349114755180229L;

    /**
     * Special instruction type to be configured.
     */
    private final ITypeDescription m_type;

    /**
     * Creates a new action object.
     * 
     * @param type Special instruction type to be configured.
     * @param string String shown next to the check box.
     */
    public CheckboxAction(final ITypeDescription type, final String string) {
      super(string);

      m_type = type;
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      m_type.setEnabled(((JCheckBox) event.getSource()).isSelected());
    }
  }

  /**
   * Listener object that updates highlighting colors.
   */
  private static class InternalColorPanelListener implements IColorPanelListener {
    /**
     * Special instruction type to be configured.
     */
    private final ITypeDescription m_description;

    /**
     * Creates a new listener object.
     * 
     * @param description Special instruction type to be configured.
     */
    public InternalColorPanelListener(final ITypeDescription description) {
      m_description = description;
    }

    @Override
    public void changedColor(final ColorPanel panel) {
      m_description.setColor(panel.getColor());
    }
  }
}
