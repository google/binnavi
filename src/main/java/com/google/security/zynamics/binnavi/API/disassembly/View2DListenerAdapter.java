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

import com.google.security.zynamics.binnavi.yfileswrap.API.disassembly.View2D;

// / Simplifies View2D listeners.
/**
 * Adapter class that can be used by classes that want to listen on View2D objects but only need to
 * be notified about few events.
 */
public class View2DListenerAdapter implements IView2DListener {
  @Override
  public void changedView2D(final View2D view, final View oldView, final View newView) {
    // Adapter class
  }
}
