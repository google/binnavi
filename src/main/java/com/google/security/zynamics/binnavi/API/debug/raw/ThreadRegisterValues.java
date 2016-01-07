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
package com.google.security.zynamics.binnavi.API.debug.raw;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.security.zynamics.binnavi.API.debug.Register;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValue;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.ThreadRegisters;


// ! Contains the current register values of a single thread.
/**
 * Some replies sent by debug clients contain the current register values of one thread of the
 * debugged target process. These register values are stored in the ThreadRegisterValues class.
 */
public final class ThreadRegisterValues implements Iterable<Register> {
  // / @cond INTERNAL

  /**
   * Thread ID of the thread.
   */
  private final long m_tid;

  /**
   * Register values of the thread.
   */
  private final List<Register> m_values = new ArrayList<Register>();

  /**
   * Creates a new thread register values object.
   *
   * @param values The internal thread registers object to wrap.
   */
  // / @endcond
  public ThreadRegisterValues(final ThreadRegisters values) {
    m_tid = values.getTid();

    for (final RegisterValue value : values.getRegisters()) {
      m_values.add(new Register(value));
    }
  }

  // ! Thread ID of the thread the register values belong to.
  /**
   * Returns the Thread ID of the thread the registers belong to.
   *
   * @return The Thread ID of the thread the registers belong to.
   */
  public long getThreadId() {
    return m_tid;
  }

  // ! Current values of the thread.
  /**
   * Returns the current values of the given thread.
   *
   * @return The current values of the given thread.
   */
  public List<Register> getValues() {
    return new ArrayList<Register>(m_values);
  }

  @Override
  public Iterator<Register> iterator() {
    return getValues().iterator();
  }
}
