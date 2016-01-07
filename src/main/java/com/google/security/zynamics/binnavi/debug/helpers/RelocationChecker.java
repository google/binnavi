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
package com.google.security.zynamics.binnavi.debug.helpers;

import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.zylib.disassembly.IReference;
import com.google.security.zynamics.zylib.disassembly.ReferenceType;

import java.util.List;

/**
 * Helper class that is used to check whether given addresses need relocation.
 */
public final class RelocationChecker {
  /**
   * You are not supposed to instantiate this class.
   */
  private RelocationChecker() {}

  /**
   * Determines whether the value of a given reference needs relocation.
   *
   * @param reference The reference to check.
   * @param value The value of the tree node.
   * @param module The module the tree node belongs to.
   *
   * @return True, if the value needs relocation. False, otherwise.
   */
  private static boolean needsRelocation(final IReference reference, final long value,
      final INaviModule module) {
    return reference.getType() == ReferenceType.DATA
        && value >= module.getConfiguration().getFileBase().toLong();
  }

  /**
   * Determines whether the value of a given integer tree node needs relocation.
   *
   * @param treeNode The tree node to check.
   * @param module The module the tree node belongs to.
   *
   * @return True, if the value needs relocation. False, otherwise.
   */
  public static boolean needsRelocation(final INaviOperandTreeNode treeNode,
      final INaviModule module) {
    final List<IReference> references = treeNode.getReferences();
    for (final IReference reference : references) {
      if (needsRelocation(reference, Long.valueOf(treeNode.getValue()), module)) {
        return true;
      }
    }
    return false;
  }
}
