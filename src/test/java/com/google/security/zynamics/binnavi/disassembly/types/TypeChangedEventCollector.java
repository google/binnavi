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

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Collects an unordered set of type manager events so it can be compared against an expected
 * set of events.
 */
public final class TypeChangedEventCollector implements TypeChangedListener {
  private static final String MEMBER_CREATED_STRING = "MEMBER_CREATED";
  private static final String MEMBER_DELETED_STRING = "MEMBER_DELETED";
  private static final String MEMBER_UPDATED_STRING = "MEMBER_UPDATED";
  private static final String TYPE_CREATED_STRING = "TYPE_CREATED";
  private static final String TYPE_DELETED_STRING = "TYPE_DELETED";
  private static final String TYPE_UPDATED_STRING = "TYPES_UPDATED";

  private final Set<String> events = new HashSet<String>();
  
  private class BaseTypeComparator implements Comparator<BaseType> {
    @Override
    public int compare(final BaseType lhs, final BaseType rhs) {
      return lhs.getId() - rhs.getId();
    }
  }

  private List<BaseType> getSortedTypes(final Set<BaseType> baseTypes) {
    final List<BaseType> sortedTypes = Lists.newArrayList(baseTypes);
    Collections.sort(sortedTypes, new BaseTypeComparator());
    return sortedTypes;
  }

  @Override
  public boolean equals(final Object o) {
    if (o instanceof TypeChangedEventCollector) {
      return events.equals(((TypeChangedEventCollector) o).events);
    }
    return false;
  }

  @Override
  public void memberAdded(final TypeMember member) {
    events.add(getEventString(MEMBER_CREATED_STRING, member.getId(), member.getName()));
  }

  @Override
  public void memberDeleted(final TypeMember member) {
    events.add(getEventString(MEMBER_DELETED_STRING, member.getId(), member.getName()));
  }

  @Override
  public void membersMoved(final Set<BaseType> affectedTypes) {
    throw new IllegalStateException("Not yet implemented!");
  }

  @Override
  public void memberUpdated(final TypeMember member) {
    events.add(getEventString(MEMBER_UPDATED_STRING, member.getId(), member.getName()));
  }

  @Override
  public void typeAdded(final BaseType baseType) {
    events.add(getEventString(TYPE_CREATED_STRING, baseType.getId(), baseType.getName()));
  }

  @Override
  public void typeDeleted(final BaseType baseType) {
    events.add(getEventString(TYPE_DELETED_STRING, baseType.getId(), baseType.getName()));
  }

  @Override
  public void typesUpdated(final Set<BaseType> baseTypes) {
    final StringBuilder builder = new StringBuilder(TYPE_UPDATED_STRING);
    builder.append(": [");
    for (final BaseType baseType : getSortedTypes(baseTypes)) {
      builder.append(baseType.getName());
      builder.append('(');
      builder.append(baseType.getId());
      builder.append("),"); 
    }
    builder.append(']');
    events.add(builder.toString());
  }

  private static String getEventString(final String event, final int id, final String name) {
    final StringBuilder builder = new StringBuilder(event);
    builder.append(": ");
    builder.append(name);
    builder.append('(');
    builder.append(id);
    builder.append(')'); 
    return builder.toString();
  }
  
  @Override
  public String toString() {
    return events.toString();
  }
}
