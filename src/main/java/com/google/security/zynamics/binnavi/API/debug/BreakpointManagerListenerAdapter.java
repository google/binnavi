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
package com.google.security.zynamics.binnavi.API.debug;

// / Adapter class for breakpoint managers
/**
 * Adapter class that can be used by objects that want to listen on breakpoint managers but only
 * need to process few events.
 */
public class BreakpointManagerListenerAdapter implements IBreakpointManagerListener {
  @Override
  public void addedBreakpoint(
      final BreakpointManager breakpointManager, final Breakpoint breakpoint) {
    // Empty default implementation
  }

  @Override
  public void addedEchoBreakpoint(
      final BreakpointManager breakpointManager, final Breakpoint breakpoint) {
    // Empty default implementation
  }

  @Override
  public void removedBreakpoint(
      final BreakpointManager breakpointManager, final Breakpoint breakpoint) {
    // Empty default implementation
  }

  @Override
  public void removedEchoBreakpoint(
      final BreakpointManager breakpointManager, final Breakpoint breakpoint) {
    // Empty default implementation
  }
}
