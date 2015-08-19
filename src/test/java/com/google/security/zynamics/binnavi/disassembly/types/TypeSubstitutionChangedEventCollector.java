/*
Copyright 2014 Google Inc. All Rights Reserved.

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
 * Collect events when changes to existing type substitutions are made via the
 * type manager.
 */
public class TypeSubstitutionChangedEventCollector implements TypeSubstitutionChangedListener {
  private static final String SUBSTITUTION_CHANGED = "SUBSTITUTION_CHANGED";
  private static final String SUBSTITUTION_DELETED = "SUBSTITUTION_DELETED";
  private static final String SUBSTITUTION_ADDED = "SUBSTITUTION_ADDED";
  private final StringBuilder events = new StringBuilder();

  @Override
  public boolean equals(final Object o) {
    return o instanceof TypeSubstitutionChangedEventCollector
        && ((TypeSubstitutionChangedEventCollector) o).events.toString().equals(events.toString());
  }

  @Override
  public void substitutionsChanged(final Set<TypeSubstitution> changedSubstitutions) {
    events.append(SUBSTITUTION_CHANGED).append(';');
  }

  @Override
  public void substitutionsDeleted(final Set<TypeSubstitution> deletedSubstitutions) {
    events.append(SUBSTITUTION_DELETED).append(';');
  }

  @Override
  public void substitutionsAdded(Set<TypeSubstitution> addedSubstitutions) {
    events.append(SUBSTITUTION_ADDED).append(';');
  }
  
  @Override
  public String toString() {
    return events.toString();
  }
}
