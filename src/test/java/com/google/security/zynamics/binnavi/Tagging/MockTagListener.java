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
package com.google.security.zynamics.binnavi.Tagging;

import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.ITagListener;


public final class MockTagListener implements ITagListener {
  String eventList = "";

  @Override
  public void changedDescription(final CTag tag, final String description) {
    eventList += "changedDescription=" + description + "/";
  }

  @Override
  public void changedName(final CTag tag, final String name) {
    eventList += "changedName=" + name + "/";
  }

  @Override
  public void deletedTag(final CTag tag) {
    eventList += "deletedTag/";
  }
}
