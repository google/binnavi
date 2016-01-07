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
package com.google.security.zynamics.binnavi.Gui.SaveFields;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Combo box that shows different backgrounds on different modification states.
 */
public class CSaveCombobox<T> extends JComboBox<T> {
  /**
   * Shows a modified background in case input to the field changed.
   */
  private final CSaveFieldBackground m_background = new CSaveFieldBackground();

  /**
   * Flag that indicates whether input to the field changed.
   */
  private boolean wasModified = false;

  /**
   * Creates a new combo box object.
   *
   * @param model Model of the combo box.
   */
  public CSaveCombobox(final ComboBoxModel<T> model) {
    super(model);

    setRenderer(new InternalRenderer());
  }

  /**
   * Sets the modification state of the field.
   *
   * @param modified The modification state of the field.
   */
  public final void setModified(final boolean modified) {
    wasModified = modified;
  }

  /**
   * Renderer of the combo box.
   */
  private class InternalRenderer implements ListCellRenderer<T> {
    @Override
    public Component getListCellRendererComponent(final JList<? extends T> list, final T value,
        final int index, final boolean isSelected, final boolean cellHasFocus) {
      return new SaveLabel(list, value, index, isSelected);
    }
  }

  /**
   * Label components shown in the combo box.
   */
  private class SaveLabel extends JLabel {
    /**
     * Index of the label in the combo box.
     */
    private final int labelIndex;

    /**
     * Creates a new save label.
     *
     * @param list List where the label is shown.
     * @param value Value to be shown.
     * @param index Index of the label in the list box.
     * @param isSelected True, if the label is selected. False, otherwise.
     */
    public SaveLabel(final JList<? extends T> list, final Object value, final int index,
        final boolean isSelected) {
      labelIndex = index;

      if (isSelected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      } else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }

      setText(value.toString());
    }

    @Override
    public void paintComponent(final Graphics graphics) {
      if (labelIndex == -1) {
        setOpaque(!wasModified);

        if (wasModified) {
          m_background.paint((Graphics2D) graphics, getWidth(), getHeight());
        }
      }

      super.paintComponent(graphics);
    }
  }
}
