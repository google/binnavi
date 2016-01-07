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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.RegisterTracker;

import java.io.Serializable;
import java.util.Comparator;

import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;


/**
 * Used to sort the register tracking results table according to the content of status column rows.
 */
public final class CTrackingStatusComparator
    implements Comparator<CResultColumnWrapper>, Serializable {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -4613809084268905423L;

  /**
   * Converts an instruction result into an integer number that represents its status.
   *
   * @param startInstruction The start instruction of the register tracking operation.
   * @param trackedRegister The register tracked by the register tracking operation.
   * @param result The instruction result to convert.
   *
   * @return A numerical value that represents the type of the instruction result.
   */
  private static int getStatusValue(final INaviInstruction startInstruction,
      final String trackedRegister, final CInstructionResult result) {
    if (startInstruction == result.getInstruction()) {
      return 0;
    } else if (result.undefinesAll()) {
      return 1;
    } else if (result.getUndefinedRegisters().contains(trackedRegister)) {
      return 2;
    } else if (result.undefinesSome()) {
      return 3;
    } else {
      return 4;
    }
  }

  @Override
  public int compare(final CResultColumnWrapper lhs, final CResultColumnWrapper rhs) // NO_UCD
  {
    return getStatusValue(lhs.getStartInstruction(), lhs.getTrackedRegister(), lhs.getResult())
        - getStatusValue(rhs.getStartInstruction(), rhs.getTrackedRegister(), rhs.getResult());
  }
}
