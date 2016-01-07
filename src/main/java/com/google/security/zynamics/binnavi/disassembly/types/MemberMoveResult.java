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
package com.google.security.zynamics.binnavi.disassembly.types;

import java.util.List;

/**
 * Represents the result of moving a set of members within a base type. The moved list represents
 * the set of members whose positions were explicitly changed. The implicitly moved members
 * represents the set of members that changed positions implicitly due to the move operation. The
 * deltas indicate the number of positions each member in the sets moves.
 *
 * @author jannewger (Jan Newger)
 *
 */
public final class MemberMoveResult {

  private final List<TypeMember> implicitlyMoved;
  private final int implicitlyMovedDelta;

  public MemberMoveResult(final List<TypeMember> implicitlyMoved, final int implicitlyMovedDelta) {
    this.implicitlyMoved = implicitlyMoved;
    this.implicitlyMovedDelta = implicitlyMovedDelta;
  }

  public List<TypeMember> getImplicitlyMoved() {
    return implicitlyMoved;
  }

  public int getImplicitlyMovedDelta() {
    return implicitlyMovedDelta;
  }
}