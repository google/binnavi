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

import com.google.common.collect.ImmutableList;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.types.TypeMember;

/**
 * The minimal set of functions a type editor has to provide in order to query which type / member
 * is currently selected in the GUI.
 */
public interface TypeEditor {

  /**
   * Returns the currently selected member. Returns null if there is no such selection.
   *
   * @return The currently selected member.
   */
  TypeMember getSelectedMember();

  /**
   * Returns an immutable list of all selected members. Returns an empty list if there is no such
   * selection.
   *
   * @return The list of currently selected members.
   */
  ImmutableList<TypeMember> getSelectedMembers();

  /**
   * Returns an immutable list of all selected types. Returns an empty list if there is no such
   * selection.
   *
   * @return The list of currently selected base types.
   */
  ImmutableList<BaseType> getSelectedTypes();

  /**
   * Returns the currently selected base type. Returns null if there is no such selection.
   *
   * @return The currently selected base type.
   */
  public BaseType getSelectedType();
}
