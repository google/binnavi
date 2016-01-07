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
package com.google.security.zynamics.binnavi.debug.models.breakpoints;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.interfaces.Condition;

public class Breakpoint {
  /**
   * Type of the breakpoint.
   */
  private final BreakpointType type;

  /**
   * Address of the breakpoint.
   */
  private final BreakpointAddress address;

  /**
   * Description of the breakpoint.
   */
  private String description;

  /**
   * Condition of the breakpoint.
   */
  private Condition condition;

  public Breakpoint(final BreakpointType type, final BreakpointAddress address) {
    this.type = Preconditions.checkNotNull(type, "IE00196: type argument can not be null");
    this.address = Preconditions.checkNotNull(address, "IE00717: address argument can not be null");
  }

  @Override
  public boolean equals(final Object rhs) {
    return (rhs instanceof Breakpoint) && address.equals(((Breakpoint) rhs).getAddress())
        && type.equals(((Breakpoint) rhs).getType());
  }

  public BreakpointAddress getAddress() {
    return address;
  }

  public Condition getCondition() {
    return condition;
  }

  public String getDescription() {
    return description;
  }

  public BreakpointType getType() {
    return type;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(address.hashCode(), type.ordinal());
  }

  public void setCondition(final Condition condition) {
    this.condition = condition;
  }

  public void setDescription(final String description) {
    this.description = description;
  }
}
