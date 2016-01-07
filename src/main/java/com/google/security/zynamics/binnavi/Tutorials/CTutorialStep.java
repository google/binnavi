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

/**
 * Represents a single tutorial step.
 */
public final class CTutorialStep {
  /**
   * Description of the step.
   */
  private final String m_description;

  /**
   * Action identifiers that finish this tutorial step.
   */
  private final List<Long> m_mandatory;

  /**
   * Action identifiers that are allowed but do not finish the tutorial step.
   */
  private final List<Long> m_allowed;

  /**
   * Flag that determines whether the Next button is available for this step.
   */
  private final boolean m_next;

  /**
   * Creates a new tutorial step object.
   * 
   * @param description Description of the step.
   * @param mandatory Action identifiers that finish this tutorial step.
   * @param allowed Action identifiers that are allowed but do not finish the tutorial step.
   * @param next Flag that determines whether the Next button is available for this step.
   */
  public CTutorialStep(final String description, final List<Long> mandatory,
      final List<Long> allowed, final boolean next) {
    Preconditions.checkNotNull(description, "IE01003: Description argument can not be null");

    Preconditions.checkNotNull(mandatory, "IE01004: Mandatory argument can not be null");

    Preconditions.checkNotNull(allowed, "IE01005: Allowed argument can not be null");

    m_description = description;
    m_mandatory = new ArrayList<Long>(mandatory);
    m_allowed = new ArrayList<Long>(allowed);
    m_next = next;
  }

  /**
   * Returns whether the Next button is available for this tutorial step.
   * 
   * @return A flag that indicates whether the Next button is available.
   */
  public boolean canNext() {
    return m_next;
  }

  /**
   * Returns the description of the step.
   * 
   * @return The description of the step.
   */
  public String getDescription() {
    return m_description;
  }

  /**
   * Decides whether a given action can be executed while this tutorial step is active.
   * 
   * @param identifier An action identifier.
   * 
   * @return True, if the given action is allowed for this tutorial step. False, if it is not.
   */
  public boolean handles(final long identifier) {
    return m_mandatory.contains(identifier) || m_allowed.contains(identifier);
  }

  /**
   * Decides whether a given action finishes this tutorial step.
   * 
   * @param identifier An action identifier.
   * 
   * @return True, if the given action finishes this tutorial step. False, if it does not.
   */
  public boolean mandates(final long identifier) {
    return m_mandatory.contains(identifier);
  }
}
