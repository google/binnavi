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
package com.google.security.zynamics.binnavi.ZyGraph.Builders.Modifiers;

import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;

/**
 * Interface to be implemented by objects that want to calculate addresses that are displayed in
 * graphs.
 */
public interface INodeModifier {
  /**
   * Calculates the displayed address string that is put before an instruction in the graph.
   * @param instruction The instruction whose visible address string is calculated.
   *
   * @return The address string to be printed before the instruction in the graph.
   */
  String getAddress(INaviInstruction instruction);

  /**
   * Calculates the displayed address string that is put before a function name in the graph.
   *
   * @param node The function node where the address string is printed.
   *
   * @return The address string to be printed before the function name.
   */
  String getAddress(INaviFunctionNode node);

  /**
   * Calculates the displayed address string for an arbitrary address of a module.
   *
   * @param module The module the address belongs to.
   * @param address The address to relocate.
   * @param pad If true, the address string is padded to 8 characters.
   *
   * @return The address string of the relocated address.
   */
  String getAddress(INaviModule module, UnrelocatedAddress address, boolean pad);
}
