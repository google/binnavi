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
package com.google.security.zynamics.reil.algorithms.mono2.registertracking;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.reil.algorithms.mono2.common.enums.AnalysisDirection;

import java.util.HashSet;
import java.util.Set;

public class RegisterTrackingOptions {
  private final boolean m_clearAll;
  private final Set<String> m_clearedRegisters;
  private final boolean m_trackIncoming;
  private final AnalysisDirection m_analysisDirection;

  public RegisterTrackingOptions(final boolean clearAll, final Set<String> clearedRegisters,
      final boolean trackIncoming, final AnalysisDirection analysisDirection) {
    m_clearAll = clearAll;
    m_clearedRegisters =
        new HashSet<String>(Preconditions.checkNotNull(clearedRegisters,
            "Error: clearedRegisters argument can not be null"));
    m_trackIncoming = trackIncoming;
    m_analysisDirection =
        Preconditions.checkNotNull(analysisDirection,
            "Error: Analysis direction argument can not be null");
  }

  public boolean clearsAllRegisters() {
    return m_clearAll;
  }

  public AnalysisDirection getAnalysisDirection() {
    return m_analysisDirection;
  }

  public Set<String> getClearedRegisters() {
    return new HashSet<String>(m_clearedRegisters);
  }

  public boolean trackIncoming() {
    return m_trackIncoming;
  }
}
