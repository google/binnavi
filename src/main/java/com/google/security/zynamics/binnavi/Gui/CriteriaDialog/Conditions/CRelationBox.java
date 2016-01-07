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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

/**
 * Combo box used by numerical criteria to specify the relation between the entered number and the
 * actual number.
 */
public final class CRelationBox extends JComboBox<String> {
  /**
   * The criterium to update on changes to the input.
   */
  private final CAbstractCriterium criterium;

  /**
   * Updates the visible tree on user input.
   */
  private final InternalComboboxListener comboBoxListener = new InternalComboboxListener();

  /**
   * Creates a new box object.
   *
   * @param criterium The criterium to update on changes to the input.
   */
  public CRelationBox(final CAbstractCriterium criterium) {
    this.criterium = criterium;

    addItem("<");
    addItem("=");
    addItem(">");

    addActionListener(comboBoxListener);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    removeActionListener(comboBoxListener);
  }

  /**
   * Updates the visible tree on user input.
   */
  private class InternalComboboxListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      criterium.notifyListeners();
    }
  }
}
