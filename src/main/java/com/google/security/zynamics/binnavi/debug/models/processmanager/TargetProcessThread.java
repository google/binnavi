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
package com.google.security.zynamics.binnavi.debug.models.processmanager;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.debug.models.processmanager.interfaces.ThreadListener;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValue;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.util.List;

/**
 * Class that keeps track of the current thread state of one thread of the target process.
 */
public final class TargetProcessThread {
  /**
   * Listeners that want to be notified if values in the thread change.
   */
  private final ListenerProvider<ThreadListener> listeners = new ListenerProvider<>();

  /**
   * The thread ID of the target process.
   */
  private final long threadId;

  /**
   * The current value of the instruction pointer of the thread.
   */
  private RelocatedAddress relocatedAddress;

  /**
   * Last known register values of the thread.
   */
  private ImmutableList<RegisterValue> registerValues =
      ImmutableList.<RegisterValue>builder().build();

  /**
   * The current thread state
   */
  private ThreadState threadState = ThreadState.RUNNING;

  /**
   * Creates a new thread object.
   *
   * @param tid The thread ID of the thread.
   * @param state The state of the thread.
   */
  public TargetProcessThread(final long tid, final ThreadState state) {
    threadState = Preconditions.checkNotNull(state, "IE00823: Invalid state");
    threadId = tid;
  }

  /**
   * Adds a new thread listener to the list of listeners that are notified about changes in the
   * thread object.
   *
   * @param listener The listener to add.
   */
  public void addListener(final ThreadListener listener) {
    listeners.addListener(listener);
  }

  /**
   * Returns the current value of the instruction pointer of the thread or null if the thread is not
   * suspended.
   *
   * @return The current value of the instruction pointer or null.
   */
  public RelocatedAddress getCurrentAddress() {
    return relocatedAddress;
  }

  /**
   * Returns the currently known register values of the thread.
   *
   * @return The currently known register values of the thread.
   */
  public ImmutableList<RegisterValue> getRegisterValues() {
    return ImmutableList.<RegisterValue>copyOf(registerValues);
  }

  /**
   * Returns the current state of the thread.
   *
   * @return The current state of the thread.
   */
  public ThreadState getState() {
    return threadState;
  }

  /**
   * Returns the thread ID of the thread.
   *
   * @return The thread ID of the thread.
   */
  public long getThreadId() {
    return threadId;
  }

  /**
   * Removes a listener from the thread object.
   *
   * @param listener The listener to remove.
   */
  public void removeListener(final ThreadListener listener) {
    listeners.removeListener(listener);
  }

  /**
   * Sets the current value of the instruction pointer of the thread. The parameter can be null to
   * imply that the thread resumed.
   *
   * @param address The new value of the instruction pointer or null.
   */
  public void setCurrentAddress(final RelocatedAddress address) {
    Preconditions.checkNotNull(address, "IE00763: Address argument can not be null");
    final RelocatedAddress oldAddress = relocatedAddress;
    relocatedAddress = address;
    for (final ThreadListener listener : listeners) {
      try {
        listener.instructionPointerChanged(this, oldAddress);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Sets the currently known register values of the thread.
   *
   * @param registerValues The new register values.
   */
  public void setRegisterValues(final List<RegisterValue> registerValues) {
    Preconditions.checkNotNull(registerValues, "IE00764: Register values argument can not be null");
    this.registerValues = ImmutableList.<RegisterValue>copyOf(registerValues);
    for (final ThreadListener listener : listeners) {
      try {
        listener.registersChanged(this);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Sets the current state of the thread.
   *
   * @param state The current state of the thread.
   */
  public void setState(final ThreadState state) {
    Preconditions.checkNotNull(state, "IE00765: Thread state can not be null");
    if (threadState == state) {
      return;
    }
    if (state == ThreadState.RUNNING) {
      relocatedAddress = null;
    }
    threadState = state;
    for (final ThreadListener listener : listeners) {
      try {
        listener.stateChanged(this);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }
}
