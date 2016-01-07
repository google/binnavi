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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Data.Component;

import com.google.security.zynamics.binnavi.disassembly.types.Section;

import java.awt.Component;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;

/**
 * The combobox that displays sections of a module.
 */
public class SectionComboBox extends JComboBox<Section> {
  public SectionComboBox() {
    setRenderer(new SectionComboBoxRenderer());
  }

  public void setSections(final List<Section> sections) {
    setModel(new SectionComboBoxModel(sections));
  }

  private class SectionComboBoxModel extends AbstractListModel<Section> implements
      ComboBoxModel<Section> {
    private final List<Section> sectionsList;
    private Section selectedSection;

    public SectionComboBoxModel(final List<Section> sections) {
      this.sectionsList = sections;
      selectedSection = sections.get(0);
    }

    @Override
    public Section getElementAt(final int index) {
      return sectionsList.get(index);
    }

    @Override
    public Object getSelectedItem() {
      return selectedSection;
    }

    @Override
    public int getSize() {
      return sectionsList.size();
    }

    @Override
    public void setSelectedItem(final Object anItem) {
      selectedSection = (Section) anItem;
    }
  }

  /**
   * Renders the section object for the combobox model, so we can store actual section instances in
   * the model and don't have to rely on the toString method of the section.
   */
  private class SectionComboBoxRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(final JList<?> list, final Object value,
        final int index, final boolean isSelected, final boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      final Section section = (Section) value;
      // Null check so WindowBuilder doesn't crash.
      if (section != null) {
        setText(String.format("%s [0x%X-0x%X]", section.getName(),
            section.getStartAddress().toLong(), section.getEndAddress().toLong()));
      }
      return this;
    }
  }
}
