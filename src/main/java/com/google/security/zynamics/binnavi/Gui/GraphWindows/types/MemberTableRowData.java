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
import com.google.security.zynamics.binnavi.disassembly.types.TypeMember;

/**
 * Holds all data contained in a single row of a {@link MemberTableModel} instance and contains a
 * reference to a corresponding TypeMember instance (if there is one).
 */
class MemberTableRowData {

  private int indexOrOffset;
  private BaseType baseType;
  private String name;
  private final TypeMember existingMember;

  public MemberTableRowData() {
    name = "";
    existingMember = null;
  }

  public MemberTableRowData(final TypeMember existingMember) {
    if (existingMember.isOffsetType()) {
      indexOrOffset = existingMember.getByteOffset().get();
    } else if (existingMember.isIndexType()) {
      indexOrOffset = existingMember.getArgumentIndex().get();
    } else {
      throw new IllegalStateException("Error: index or offset can not be determined.");
    }
    baseType = existingMember.getBaseType();
    name = existingMember.getName();
    this.existingMember = existingMember;
  }

  public Integer getIndex() {
    return indexOrOffset;
  }

  public void setIndex(int index) {
    this.indexOrOffset = index;
  }

  public BaseType getBaseType() {
    return baseType;
  }

  public void setBaseType(final BaseType baseType) {
    this.baseType = baseType;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public TypeMember getExistingMember() {
    return existingMember;
  }
}
