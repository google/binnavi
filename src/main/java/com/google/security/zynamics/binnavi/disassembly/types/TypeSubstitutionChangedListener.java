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
 * This event is triggered whenever the representation of type substitutions have changed due to a
 * change of a base type or type member in the type manager. This class is used by the GUI to
 * determine which nodes to update if the user changes, e.g., the name of a type or member.
 */
public interface TypeSubstitutionChangedListener {

  /**
   * Called by the type manager after a set of type substitutions has been deleted from the
   * database.
   *
   * @param deletedSubstitutions The set of substitutions that have been deleted.
   */
  public void substitutionsDeleted(Set<TypeSubstitution> deletedSubstitutions);

  /**
   * Called by the type manager whenever a set of substitutions has changed due to a change in a
   * corresponding base type or member name.
   *
   * @param changedSubstitutions The set of substitutions that have changed.
   */
  public void substitutionsChanged(Set<TypeSubstitution> changedSubstitutions);

  /**
   * Called by the type manager after a set of type substitutions has been added to the database.
   *
   * @param addedSubstitutions The set of added substitutions.
   */
  public void substitutionsAdded(Set<TypeSubstitution> addedSubstitutions);
}
