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
package com.google.security.zynamics.binnavi.API.reil;

import java.util.ArrayList;
import java.util.List;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.API.disassembly.Module;
import com.google.security.zynamics.binnavi.API.disassembly.View;
import com.google.security.zynamics.binnavi.APIHelpers.ObjectFinders;
import com.google.security.zynamics.binnavi.REIL.CReilViewCreator;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;

// ! Generates new views from REIL instructions.
/**
 * This class can be used to create a new view from a list of REIL instructions.
 */
public final class ViewGenerator {
  /**
   * Do not instantiate this class.
   */
  private ViewGenerator() {
    // You are not supposed to instantiate this class
  }

  /**
   * Converts API REIL instructions into native REIL instructions.
   *
   * @param instructions The instructions to convert.
   *
   * @return The converted instructions.
   */
  private static List<com.google.security.zynamics.reil.ReilInstruction> convert(
      final List<ReilInstruction> instructions) {
    final List<com.google.security.zynamics.reil.ReilInstruction> converted =
        new ArrayList<com.google.security.zynamics.reil.ReilInstruction>();

    for (final ReilInstruction reilInstruction : instructions) {
      if (reilInstruction.getNative() == null) {
        throw new IllegalArgumentException(
            "Error: The list of REIL instructions contains null-elements");
      }

      converted.add(reilInstruction.getNative());
    }

    return converted;
  }

  /**
   * Creates a new REIL view from a list of REIL instructions.
   *
   * @param module The module where the view is created.
   * @param instructions The instructions which are put into the view.
   *
   * @return The new view.
   */
  public static View createView(final Module module, final List<ReilInstruction> instructions) {
    Preconditions.checkNotNull(module, "Error: Module argument can not be null");
    Preconditions.checkNotNull(instructions, "Error: Instructions argument can not be null");

    final INaviView view = CReilViewCreator.create(module.getNative(), convert(instructions));

    return ObjectFinders.getObject(view, module.getViews());
  }
}
