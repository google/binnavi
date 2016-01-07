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

import javax.swing.ImageIcon;

/**
 * Provides icons used in the GUI components of the type system.
 */
public class TypeSystemIcons {

  public static final ImageIcon STRUCT_ICON = new ImageIcon(TypeSystemIcons.class.getResource(
      "/com/google/security/zynamics/binnavi/data/typeeditoricons/compound_type.png"));
  public static final ImageIcon ATOMIC_ICON = new ImageIcon(TypeSystemIcons.class.getResource(
      "/com/google/security/zynamics/binnavi/data/typeeditoricons/atomic_type.png"));
  public static final ImageIcon POINTER_ICON = new ImageIcon(TypeSystemIcons.class.getResource(
      "/com/google/security/zynamics/binnavi/data/typeeditoricons/pointer_type.png"));
  public static final ImageIcon ARRAY_ICON = new ImageIcon(TypeSystemIcons.class.getResource(
      "/com/google/security/zynamics/binnavi/data/typeeditoricons/array_type.png"));
  public static final ImageIcon UNION_ICON = new ImageIcon(TypeSystemIcons.class.getResource(
      "/com/google/security/zynamics/binnavi/data/typeeditoricons/union_type.png"));
  public static final
      ImageIcon FUNCTION_POINTER_ICON = new ImageIcon(TypeSystemIcons.class.getResource(
          "/com/google/security/zynamics/binnavi/data/typeeditoricons/function_pointer_type.png"));
  public static final
      ImageIcon LOCAL_VARIABLE_ICON = new ImageIcon(TypeSystemIcons.class.getResource(
          "/com/google/security/zynamics/binnavi/data/typeeditoricons/local_variable.png"));
  public static final
      ImageIcon GLOBAL_VARIABLE_ICON = new ImageIcon(TypeSystemIcons.class.getResource(
          "/com/google/security/zynamics/binnavi/data/typeeditoricons/global_variable.png"));

}
