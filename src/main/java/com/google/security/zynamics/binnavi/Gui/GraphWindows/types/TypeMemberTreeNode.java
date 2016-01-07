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

import com.google.security.zynamics.binnavi.disassembly.types.TypeMember;
import com.google.security.zynamics.zylib.gui.jtree.IconNode;

import javax.swing.ImageIcon;

/**
 * The base node class for any node that represents a type member. This can either be a member that
 * is a compound type or primitive type.
 */
public class TypeMemberTreeNode extends IconNode {

  private final TypeMember typeMember;

  public TypeMemberTreeNode(final TypeMember member) {
    this.typeMember = member;
    setIcon(determineIcon(typeMember));
  }

  private ImageIcon determineIcon(final TypeMember typeMember) {
    switch (typeMember.getBaseType().getCategory()) {
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

  public TypeMember getTypeMember() {
    return typeMember;
  }

  @Override
  public String toString() {
    if (getTypeMember().isOffsetType()) {
      return String.format("+%d %s %s", getTypeMember().getByteOffset().get(),
          getTypeMember().getBaseType().getName(), getTypeMember().getName());
    } else if (getTypeMember().isIndexType()) {
      final String argument_prefix = getTypeMember().getArgumentIndex().get() == 0 ? "ret"
          : "arg_" + String.valueOf(getTypeMember().getArgumentIndex().get() - 1);
      return String.format("[%s] %s %s", argument_prefix, getTypeMember().getBaseType().getName(),
          getTypeMember().getName());
    } else {
      throw new IllegalStateException("Error: can not render type member tree node");
    }
  }

  /**
   * Updates the icon to be used for this tree node. This should be called when the underlying
   * member changed its type category.
   */
  public void updateIcon() {
    setIcon(determineIcon(typeMember));
  }
}
