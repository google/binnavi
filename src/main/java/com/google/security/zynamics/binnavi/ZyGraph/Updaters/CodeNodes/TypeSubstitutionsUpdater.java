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
package com.google.security.zynamics.binnavi.ZyGraph.Updaters.CodeNodes;

import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.types.TypeSubstitution;
import com.google.security.zynamics.binnavi.disassembly.types.TypeSubstitutionChangedListener;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.util.Set;

/**
 * The object that is listening for changes in the type system and triggers a rebuild of the
 * corresponding nodes in the graph view.
 */
public class TypeSubstitutionsUpdater implements TypeSubstitutionChangedListener {

  private final INaviCodeNode codeNode;
  private final NaviNode naviNode;

  public TypeSubstitutionsUpdater(final INaviCodeNode codeNode, final NaviNode naviNode) {
    this.codeNode = codeNode;
    this.naviNode = naviNode;
  }

  @Override
  public void substitutionsChanged(final Set<TypeSubstitution> changedTypeSubstitutions) {
    rebuild(changedTypeSubstitutions);
  }

  @Override
  public void substitutionsDeleted(final Set<TypeSubstitution> deletedTypeSubstitutions) {
    rebuild(deletedTypeSubstitutions);
  }

  @Override
  public void substitutionsAdded(Set<TypeSubstitution> addedTypeSubstitutions) {
    rebuild(addedTypeSubstitutions);
  }

  /**
   * Rebuilds all affected nodes for the given {@link TypeSubstitution type substitutions}.
   *
   * @param typeSubstitutions A set of {@link TypeSubstitution type substitutions} from a listener
   *        event.
   */
  private void rebuild(Set<TypeSubstitution> typeSubstitutions) {
    final IAddress startAddress = codeNode.getAddress();
    final IAddress endAddress = codeNode.getLastInstruction().getAddress();
    for (final TypeSubstitution substitution : typeSubstitutions) {
      if (substitution.getAddress().toLong() >= startAddress.toLong()
          && substitution.getAddress().toLong() <= endAddress.toLong()) {
        naviNode.getRealizer().regenerate();
        naviNode.getRealizer().repaint();
        return;
      }
    }
  }
}
