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
package com.google.security.zynamics.binnavi.Tutorials;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Represents a single tutorial
 * 
 * TODO: Separate the static tutorial information from the tutorial execution state (move to
 * CTutorialRunner).
 */
public final class CTutorial {
  /**
   * Name of the tutorial.
   */
  private final String m_name;

  /**
   * Description of the tutorial.
   */
  private final String m_description;

  /**
   * Individual steps of the tutorial.
   */
  private final List<CTutorialStep> m_steps;

  /**
   * Listeners that are notified about changes in the tutorial.
   */
  private final ListenerProvider<ITutorialListener> m_listeners =
      new ListenerProvider<ITutorialListener>();

  /**
   * Keeps track of the currently active step.
   */
  private int m_stepCounter = 0;

  /**
   * Creates a new tutorial object.
   * 
   * @param name Name of the tutorial.
   * @param description Description of the tutorial.
   * @param steps Individual steps of the tutorial.
   */
  public CTutorial(final String name, final String description, final List<CTutorialStep> steps) {
    m_name = Preconditions.checkNotNull(name, "IE00998: Name argument can not be null");
    m_description =
        Preconditions.checkNotNull(description, "IE00999: Description argument can not be null");
    m_steps =
        new ArrayList<CTutorialStep>(Preconditions.checkNotNull(steps,
            "IE01000: Steps argument can not be null"));
    Preconditions.checkArgument(!steps.isEmpty(),
        "IE01001: Tutorials with no steps are not allowed");
  }

  /**
   * Adds a listener that is notified about changes in the tutorial.
   * 
   * @param listener The listener to add.
   */
  public void addListener(final ITutorialListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Returns the currently active tutorial step.
   * 
   * @return The currently active tutorial step.
   */
  public CTutorialStep getCurrentStep() {
    return m_steps.get(m_stepCounter);
  }

  /**
   * Returns the description of the tutorial.
   * 
   * @return The description of the tutorial.
   */
  public String getDescription() {
    return m_description;
  }

  /**
   * Returns the name of the tutorial.
   * 
   * @return The name of the tutorial.
   */
  public String getName() {
    return m_name;
  }

  /**
   * Returns the total number of steps of this tutorial.
   * 
   * @return The total number of steps of this tutorial.
   */
  public int getStepCount() {
    return m_steps.size();
  }

  /**
   * Returns the index of the currently active step.
   * 
   * @return The index of the currently active step.
   */
  public int getStepCounter() {
    return m_stepCounter;
  }

  /**
   * Moves the tutorial to the next step.
   */
  public void next() {
    m_stepCounter++;

    if (m_stepCounter == m_steps.size()) {
      for (final ITutorialListener listener : m_listeners) {
        listener.finished(this);
      }
    } else {
      for (final ITutorialListener listener : m_listeners) {
        listener.changedStep(this);
      }
    }
  }

  /**
   * Removes a listener from the tutorial.
   * 
   * @param listener The listener to remove.
   */
  public void removeListener(final ITutorialListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * Starts a tutorial.
   */
  public void start() {
    m_stepCounter = 0;

    for (final ITutorialListener listener : m_listeners) {
      listener.started(this);
    }
  }

  @Override
  public String toString() {
    return getName();
  }
}
