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
package com.google.security.zynamics.binnavi.Gui.Debug.EventLists;

import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.ConcreteTree.IFilterExpression;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceRegister;
import com.google.security.zynamics.zylib.general.Convert;



/**
 * Filter expression for filtering trace events by memory content.
 */
public final class CMemoryFilterExpression implements IFilterExpression<CTraceEventWrapper> {
  /**
   * The filter string.
   */
  private final String m_data;

  /**
   * Creates a new expression object.
   *
   * @param data The filter string.
   */
  public CMemoryFilterExpression(final String data) {
    m_data = data;
  }

  /**
   * Turns a byte array into a string.
   *
   * @param memory The byte array.
   *
   * @return The generated string.
   */
  private String toComparableString(final byte[] memory) {
    final StringBuffer stringBuffer = new StringBuffer();

    for (final byte b : memory) {
      stringBuffer.append(Convert.byteToHexString(b));
    }

    return stringBuffer.toString();
  }

  @Override
  public boolean evaluate(final CTraceEventWrapper element) {
    for (final TraceRegister register : element.unwrap().getRegisterValues()) {
      if (toComparableString(register.getMemory()).contains(m_data)) {
        return true;
      }
    }

    return false;
  }
}
