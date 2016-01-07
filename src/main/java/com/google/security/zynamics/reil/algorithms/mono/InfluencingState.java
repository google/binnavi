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
package com.google.security.zynamics.reil.algorithms.mono;

import com.google.security.zynamics.reil.algorithms.mono.interfaces.IInfluencingState;
import com.google.security.zynamics.reil.algorithms.mono.interfaces.ILatticeElementMono1;

public class InfluencingState<LatticeElement extends ILatticeElementMono1<LatticeElement>, ObjectType>
    implements IInfluencingState<LatticeElement, ObjectType> {
  private final LatticeElement element;

  private final ObjectType object;

  public InfluencingState(final LatticeElement element, final ObjectType object) {
    this.element = element;
    this.object = object;
  }

  @Override
  public LatticeElement getElement() {
    return element;
  }

  @Override
  public ObjectType getObject() {
    return object;
  }

  @Override
  public String toString() {
    return element.toString();
  }
}
