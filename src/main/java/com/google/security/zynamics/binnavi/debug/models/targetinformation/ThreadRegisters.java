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
package com.google.security.zynamics.binnavi.debug.models.targetinformation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class that associates a thread ID with the values of the registers of that thread.
 */
public final class ThreadRegisters implements Iterable<RegisterValue> {
  /**
   * Thread ID of the thread.
   */
  private final long threadId;

  /**
   * Values of the registers of the thread.
   */
  private final List<RegisterValue> threadRegisterValues;

  /**
   * Creates a new thread registers object.
   *
   * @param tid Thread ID of the thread.
   * @param registers Values of the registers of the thread.
   */
  public ThreadRegisters(final long tid, final List<RegisterValue> registers) {
    threadId = tid;
    threadRegisterValues = new ArrayList<>(registers);
  }

  /**
   * Returns the register values of the thread.
   *
   * @return The register values of the thread.
   */
  public List<RegisterValue> getRegisters() {
    return threadRegisterValues;
  }

  /**
   * Returns the thread ID of the thread.
   *
   * @return The thread ID of the thread.
   */
  public long getTid() {
    return threadId;
  }

  @Override
  public Iterator<RegisterValue> iterator() {
    return threadRegisterValues.iterator();
  }
}
