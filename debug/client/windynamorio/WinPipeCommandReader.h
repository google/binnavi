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

// Provides WinPipeCommandReader class, which implements
// communication with dynamorio client using Windows named pipes.
#ifndef WINPIPECOMMANDREADER_H_
#define WINPIPECOMMANDREADER_H_

#include <Windows.h>

#include <memory>
#include <queue>
#include <string>
#include <vector>

#include "AbstractCommandReader.h"

class WinPipeCommandReader : public AbstractCommandReader {
 public:
  WinPipeCommandReader();

  ~WinPipeCommandReader();

  bool Connect(const std::string& pipe_name);

  void Disconnect();

  // empty unique_ptr if failed
  virtual std::unique_ptr<security::drdebug::Command> WaitForCommand();

  virtual bool SendResponse(const security::drdebug::Response& response);

 private:
  HANDLE pipe_;
  std::queue<security::drdebug::Command> received_queue_;

  // variables holding status of reading currently incoming message
  int packet_len_;
  int buf_pos_;
  std::vector<char> in_buf_;
  std::vector<security::drdebug::Response> response_queue_;
};

#endif  // WINPIPECOMMANDREADER_H_
