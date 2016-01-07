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

// TODO(jannewger): split the listener up into two interfaces: TypeInstanceListener and
// TypeInstanceReferenceListener

/**
 * Listens on changes in the type instance container, i.e. adding/removal of type instances.
 */
public interface TypeInstanceContainerListener {

  /**
   * Invoked when a {@link TypeInstance} is added.
   *
   * @param instance the {@link TypeInstance} that has been added.
   */
  void addedTypeInstance(TypeInstance instance);

  /**
   * Invoked when a {@link TypeInstanceReference} is added.
   *
   * @param reference The {@link TypeInstanceReference} that has been added.
   */
  void addedTypeInstanceReference(TypeInstanceReference reference);

  /**
   * Invoked when a {@link TypeInstance} has been changed.
   *
   */
  void changedTypeInstance(TypeInstance instance);

  /**
   * Invoked when the association between a node and a {@link TypeInstanceReference} is changed.
   *
   * @param reference The {@link TypeInstanceReference} that has been changed.
   */
  void changedTypeInstanceReference(TypeInstanceReference reference);

  /**
   * Invoked when a {@link TypeInstance} is removed.
   *
   * @param instance The {@link TypeInstance} that has been removed.
   */
  void removedTypeInstance(TypeInstance instance);

  /**
   * Invoked when a {@link TypeInstanceReference} is removed.
   *
   * @param reference The {@link TypeInstanceReference} that has been removed.
   */
  void removedTypeInstanceReference(TypeInstanceReference reference);
}
