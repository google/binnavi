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

#ifndef THREAD_CONTINUE_STATE_H
#define THREAD_CONTINUE_STATE_H

#include <iostream>
#include <map>

/**
 * Simple class to save state information per thread id
 */
template<typename T>
class ThreadStateInformation {
 public:
  // set the continue state to be used when the given thread is continued the
  // next time
  template<typename T>
  void setThreadState(unsigned int threadId, T continueAction) {
    threadState[threadId] = continueAction;
  }

  template<typename T>
  T getThreadState(unsigned int threadId) {
    std::map<unsigned int, T>::const_iterator cit = threadState.find(threadId);
    //if (cit == threadState.end()); // what?
    return cit->second;
  }

  // checks whether a state is associated with the given thread id
  bool hasThreadState(unsigned int threadId) {
    return threadState.find(threadId) != threadState.end();
  }

  // remove the state associated with the given thread
  void removeThreadState(unsigned int threadId) {
    std::map<unsigned int, T>::iterator it = threadState.find(threadId);

    if (it != threadState.end()) {
      threadState.erase(it);
    }
  }

 private:

  std::map<unsigned int, T> threadState;
};

#endif
