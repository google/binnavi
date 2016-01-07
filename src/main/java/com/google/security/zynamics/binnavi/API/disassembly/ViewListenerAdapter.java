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
package com.google.security.zynamics.binnavi.API.disassembly;

import java.util.Date;

// / Adapter class for views
/**
 * Adapter class that can be used by objects that want to listen on views but only need to process
 * few events.
 */
public class ViewListenerAdapter implements IViewListener {
  @Override
  public void addedEdge(final View view, final ViewEdge edge) {
    // Adapter method
  }

  @Override
  public void addedNode(final View view, final ViewNode node) {
    // Adapter method
  }

  @Override
  public void changedDescription(final View view, final String description) {
    // Adapter method
  }

  @Override
  public void changedGraphType(final View view, final GraphType type) {
    // Adapter method
  }

  @Override
  public void changedModificationDate(final View view, final Date date) {
    // Adapter method
  }

  @Override
  public void changedName(final View view, final String name) {
    // Adapter method
  }

  @Override
  public void closedView(final View view) {
    // Adapter method
  }

  @Override
  public boolean closingView(final View view) {
    return true;
  }

  @Override
  public void deletedEdge(final View view, final ViewEdge edge) {
    // Adapter method
  }

  @Override
  public void deletedNode(final View view, final ViewNode node) {
    // Adapter method
  }

  @Override
  public void taggedView(final View view, final Tag tag) {
    // Adapter method
  }

  @Override
  public void untaggedView(final View view, final Tag tag) {
    // Adapter method
  }
}
