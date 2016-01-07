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
import com.google.security.zynamics.zylib.gui.jtree.IconNode;

import javax.swing.Icon;

/**
 * Represents a base type node in the types editor tree component.
 */
public class BaseTypeTreeNode extends IconNode {

  private final BaseType baseType;

  public BaseTypeTreeNode(final BaseType baseType) {
    this.baseType = baseType;
    setIcon(determineIcon(baseType));
  }

  private Icon determineIcon(final BaseType baseType) {
    switch (baseType.getCategory()) {
      case ATOMIC:
        return TypeSystemIcons.ATOMIC_ICON;
      case POINTER:
        return TypeSystemIcons.POINTER_ICON;
      case ARRAY:
        return TypeSystemIcons.ARRAY_ICON;
      case STRUCT:
        return TypeSystemIcons.STRUCT_ICON;
      case UNION:
        return TypeSystemIcons.UNION_ICON;
      case FUNCTION_PROTOTYPE:
        return TypeSystemIcons.FUNCTION_POINTER_ICON;
      default:
        return null;
    }
  }

  /**
   * Converts a BaseType instance to a string representation in the same way as a BaseTypeTreeNode.
   */
  public static String renderBaseType(final BaseType baseType) {
    return baseType.getName();
  }
  
  public BaseType getBaseType() {
    return baseType;
  }

  @Override
  public String toString() {
    return renderBaseType(getBaseType());
  }
}
