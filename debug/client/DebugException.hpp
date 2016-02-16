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

#ifndef DEBUGEXCEPTIONS_HPP
#define DEBUGEXCEPTIONS_HPP

#include "defs.hpp"
#include "DebugExceptionHandlingAction.hpp"
#include <vector>

/**
 * Defines a platform specific exception which can occur while the debugger is
 * running.
 */
struct DebugException {
  DebugException(const std::string& name, CPUADDRESS code,
                 DebugExceptionHandlingAction action)
      : exceptionName(name),
        exceptionCode(code),
        handlingAction(action) {
  }

  std::string exceptionName;
  CPUADDRESS exceptionCode;

  // defines how the debugger should handle this specific exception
  DebugExceptionHandlingAction handlingAction;
};

typedef std::vector<DebugException> DebugExceptionContainer;

#endif
