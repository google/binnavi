//// Copyright 2011-2016 Google Inc. All Rights Reserved.
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

// Provides ScopedLocker, which is used to maintain proper DebugState locking
// and unlocking.
#ifndef SCOPEDLOCKER_H_
#define SCOPEDLOCKER_H_

#include "DebugState.h"

struct ScopedLocker {
  explicit ScopedLocker(DebugState* state) : state_(state) { state->Lock(); }
  ~ScopedLocker() { state_->Unlock(); }

  // Disallow copying/assignment.
  ScopedLocker(const ScopedLocker&) = delete;
  ScopedLocker& operator=(const ScopedLocker&) = delete;

 private:
  DebugState* state_;
};

#endif  // SCOPEDLOCKER_H_
