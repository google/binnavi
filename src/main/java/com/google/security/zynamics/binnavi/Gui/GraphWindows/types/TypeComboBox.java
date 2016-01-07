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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.types;

import com.google.security.zynamics.binnavi.disassembly.types.BaseType;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;

/**
 * The combobox to display all available base types using the TypeListModel.
 */
public class TypeComboBox extends JComboBox<BaseType> {

  public TypeComboBox(final TypeListModel model) {
    super(model);
    setRenderer(new TypeRenderer());
  }

  @Override
  public TypeListModel getModel() {
    return (TypeListModel) super.getModel();
  }

  private class TypeRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(final JList<?> list, final Object value,
        final int index, final boolean isSelected, final boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      final BaseType baseType = (BaseType) value;
      if (value != null) {
        setText(baseType.getName());
      }
      return this;
    }
  }
}
