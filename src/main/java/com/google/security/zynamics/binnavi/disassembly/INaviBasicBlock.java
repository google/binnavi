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
package com.google.security.zynamics.binnavi.disassembly;

import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.IBasicBlock;

import java.util.List;


/**
 * Interface for basic blocks.
 */
public interface INaviBasicBlock extends Iterable<INaviInstruction>, IBasicBlock {
  /**
   * Returns the start address of the basic block.
   * 
   * @return The start address of the basic block.
   */
  IAddress getAddress();

  /**
   * Returns the global comment of the basic block.
   * 
   * @return The global comment of the basic block.
   */
  String getGlobalComment();

  /**
   * Returns the instructions of the basic block.
   * 
   * @return The instructions of the basic block.
   */
  List<INaviInstruction> getInstructions();
}
