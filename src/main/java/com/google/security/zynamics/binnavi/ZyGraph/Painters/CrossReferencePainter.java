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
package com.google.security.zynamics.binnavi.ZyGraph.Painters;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.ZyGraph.CHighlightLayers;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.disassembly.CCodeNodeHelpers;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceReference;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;

public class CrossReferencePainter {

  private CrossReferencePainter() {
  }

  public static void paintCrossReference(final NaviNode node, final INaviCodeNode codeNode,
      final TypeInstanceReference reference, final INaviInstruction instruction) {
    Preconditions.checkNotNull(node, "Error: node argument can not be null");
    Preconditions.checkNotNull(codeNode, "Error: codeNode argument can not be null");
    Preconditions.checkNotNull(reference, "Error: reference argument can not be null");
    Preconditions.checkNotNull(instruction, "Error: instruction argument can not be null");

    final int line = CCodeNodeHelpers.instructionToLine(codeNode, instruction);

    node.clearHighlighting(CHighlightLayers.VARIABLE_LAYER, line);
    node.setHighlighting(CHighlightLayers.VARIABLE_LAYER, line,
        ConfigManager.instance().getColorSettings().getMemRefColor());
  }
}
