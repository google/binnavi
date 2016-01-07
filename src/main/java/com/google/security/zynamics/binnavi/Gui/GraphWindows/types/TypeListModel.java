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

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.types.BaseTypeCategory;

import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
 * The model for the combo box which contains all the types in the type editor.
 */
public class TypeListModel extends AbstractListModel<BaseType> implements ComboBoxModel<BaseType> {

  private BaseType selectedType;
  private final ImmutableList<BaseType> filteredTypes;

  public static class PrototypesFilter implements Predicate<BaseType> {
    @Override
    public boolean apply(final BaseType baseType) {
      return baseType.getCategory() != BaseTypeCategory.FUNCTION_PROTOTYPE;
    }
  }

  public static class ArrayTypesFilter implements Predicate<BaseType> {
    @Override
    public boolean apply(final BaseType baseType) {
      return baseType.getCategory() != BaseTypeCategory.ARRAY;
    }
  }

  /**
   * Creates a new types list model with the given filter. Takes ownership of the given list.
   *
   * @param types The set of types to be potentially included in this model.
   * @param filter A filter predicate to narrow the set of types that is included in this model.
   */
  public TypeListModel(final List<BaseType> types, final Predicate<BaseType> filter) {
    final Builder<BaseType> builder = ImmutableList.builder();
    for (BaseType baseType : types) {
      if (filter.apply(baseType)) {
        builder.add(baseType);
      }
    }
    filteredTypes = builder.build();
  }

  @Override
  public BaseType getElementAt(final int index) {
    return filteredTypes.get(index);
  }

  @Override
  public Object getSelectedItem() {
    return selectedType;
  }

  @Override
  public int getSize() {
    return filteredTypes.size();
  }

  /**
   * Selects the given entry corresponding to the given base type.
   *
   * @param baseType The base type that should be selected in the combobox model.
   */
  public void selectByBaseType(final BaseType baseType) {
    final int index = filteredTypes.indexOf(baseType);
    if (index != -1) {
      setSelectedItem(baseType);
    }
  }

  @Override
  public void setSelectedItem(final Object anItem) {
    selectedType = (BaseType) anItem;
  }
}
