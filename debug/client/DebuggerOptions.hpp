// Copyright 2011-2016 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#ifndef DEBUGGEROPTIONS_HPP
#define DEBUGGEROPTIONS_HPP

#include "DebugException.hpp"

/**
 * Small struct that is used to specify the
 * debug commands supported by a debug client.
 */
struct DebuggerOptions {
  bool canAttach;
  bool canDetach;
  bool canTerminate;
  bool canMemmap;
  bool canValidMemory;
  bool canMultithread;
  bool canSoftwareBreakpoint;
  int breakpointCount;
  bool canHalt;
  bool haltBeforeCommunicating;
  bool hasStack;
  int pageSize;

  // Specifies whether the debugger is able to handle breakpoint hit counts
  // which are greater than one.
  bool canTraceCount;

  // Specifies that the debugger is able to halt the process whenever a library
  // is mapped into the address space.
  bool canBreakOnModuleLoad;

  // Specifies that the debugger is able to halt the process whenever a library
  // is unmapped from the address space.
  bool canBreakOnModuleUnload;

  // list of supported exceptions on the specific platform
  DebugExceptionContainer exceptions;

  DebuggerOptions()
      : canAttach(true),
        canDetach(true),
        canTerminate(true),
        canMemmap(true),
        canValidMemory(true),
        canMultithread(true),
        canSoftwareBreakpoint(true),
        breakpointCount(-1),
        canHalt(false),
        haltBeforeCommunicating(false),
        hasStack(true),
        canTraceCount(true),
        pageSize(0),
        canBreakOnModuleLoad(true),
        canBreakOnModuleUnload(true) {
  }
};

#endif
