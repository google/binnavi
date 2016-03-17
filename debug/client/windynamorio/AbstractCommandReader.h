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

// Defines the interface that must be implemented by classes that want to
// receive IPC commands from the broker.
#ifndef ABSTRACTCOMMANDREADER_H_
#define ABSTRACTCOMMANDREADER_H_

#include <memory>

#include "drdebug.pb.h"

class AbstractCommandReader {
 public:
  virtual std::unique_ptr<security::drdebug::Command> WaitForCommand() = 0;
  virtual bool SendResponse(const security::drdebug::Response& response) = 0;
};

#endif  // ABSTRACTCOMMANDREADER_H_
