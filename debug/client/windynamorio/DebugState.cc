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

#include "DebugState.h"

DebugState::DebugState() : state_(NOT_SET) {
  mutex_ = dr_mutex_create();
}

DebugState::~DebugState() {
  dr_mutex_destroy(mutex_);
}

void DebugState::Lock() {
  dr_mutex_lock(mutex_);
}

void DebugState::Unlock() {
  dr_mutex_unlock(mutex_);
}

security::drdebug::ExceptionAction DebugState::GetExceptionAction(
  exception_code_t exc_code) {
  auto it = exception_actions_.find(exc_code);
  if (it == exception_actions_.end()) {
    return security::drdebug::ExceptionAction::HALT;
  }
  return it->second;
}

void DebugState::SetExceptionAction(exception_code_t exc_code,
                                    security::drdebug::ExceptionAction action) {
  exception_actions_[exc_code] = action;
}
