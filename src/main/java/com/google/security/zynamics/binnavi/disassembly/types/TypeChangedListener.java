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

import java.util.Set;

/**
 * Each component that wants to be notified about changes in the type system has to implement this
 * interface.
 */
public interface TypeChangedListener {
  /**
   * Called after the given member was added to a base type.
   *
   * @param member The member that was added.
   */
  void memberAdded(final TypeMember member);

  /**
   * Called after the given member was deleted.
   *
   * @param member The member that was deleted.
   */
  void memberDeleted(final TypeMember member);

  /**
   * Called after a list of members was moved within their parent type.
   *
   * @param affectedTypes The set of base types that are explicitly or implicitly affected by the
   *        move operation.
   */
  void membersMoved(final Set<BaseType> affectedTypes);

  /**
   * Called after any property of the given member was changed.
   *
   * @param member The member that was changed.
   */
  void memberUpdated(final TypeMember member);

  /**
   * Called when the given base type has been added.
   *
   * @param baseType The base type that has been added.
   */
  void typeAdded(final BaseType baseType);

  /**
   * Called after the given base type has been deleted.
   *
   * @param deletedType The base type that has been deleted.
   */
  void typeDeleted(final BaseType deletedType);

  /**
   * Called when any property of the given base types has changed. This event is only triggered for
   * changes to the properties of the base type itself (i.e. changes to name, size, signednes) but
   * not for changes to the contained members.
   *
   * @param baseTypes The set of base type that have been updated.
   */
  void typesUpdated(final Set<BaseType> baseTypes);
}
