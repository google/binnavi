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
package com.google.security.zynamics.binnavi.API.disassembly;

import java.util.Date;

import com.google.security.zynamics.binnavi.API.disassembly.GraphType;
import com.google.security.zynamics.binnavi.API.disassembly.IViewListener;
import com.google.security.zynamics.binnavi.API.disassembly.Tag;
import com.google.security.zynamics.binnavi.API.disassembly.View;
import com.google.security.zynamics.binnavi.API.disassembly.ViewEdge;
import com.google.security.zynamics.binnavi.API.disassembly.ViewNode;


public final class MockViewListener implements IViewListener {
  public String events = "";

  @Override
  public void addedEdge(final View view, final ViewEdge edge) {
    events += "addedEdge;";
  }

  @Override
  public void addedNode(final View view, final ViewNode node) {
    events += "addedNode;";
  }

  @Override
  public void changedDescription(final View view, final String description) {
    events += "changedDescription;";
  }

  @Override
  public void changedGraphType(final View view, final GraphType type) {
    events += "changedGraphType;";
  }

  @Override
  public void changedModificationDate(final View view, final Date date) {
    events += "changedModificationDate;";
  }

  @Override
  public void changedName(final View view, final String name) {
    events += "changedName;";
  }

  @Override
  public void closedView(final View view) {
    events += "closedView;";
  }

  @Override
  public boolean closingView(final View view) {
    events += "closingView;";

    return true;
  }

  @Override
  public void deletedEdge(final View view, final ViewEdge edge) {
    events += "deletedEdge;";
  }

  @Override
  public void deletedNode(final View view, final ViewNode node) {
    events += "deletedNode;";
  }

  @Override
  public void taggedView(final View view, final Tag tag) {
    events += "taggedView;";
  }

  @Override
  public void untaggedView(final View view, final Tag tag) {
    events += "untaggedView;";
  }
}
