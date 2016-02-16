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

#include "../logger.hpp"
#include "WindowsCommon.hpp"
#include <Windows.h>

namespace windowscommon {
NaviError getFileSystems(std::vector<boost::filesystem::path>& roots) {


  const unsigned int BUFFER_SIZE = 512;

  char buffer[BUFFER_SIZE] = { 0 };

  DWORD written = GetLogicalDriveStrings(sizeof(buffer) - 1, buffer);
  if (!written || written > BUFFER_SIZE) {
    return NaviErrors::COULDNT_GET_ROOTS;
  }

  for (unsigned int i = 0; i < written; i += 4) {
    roots.push_back(&buffer[i]);
  }

  return 0;
}

NaviError getSystemRoot(boost::filesystem::path& root) {


  char buffer[4] = { 0 };

  if (GetEnvironmentVariable("SystemDrive", buffer, sizeof(buffer))) {
    // Bug in boost: buffer needs to be converted to string first,
    // otherwise appending the path separator does not work
    std::string r(buffer);
    root = boost::filesystem::path(r) / "/";
  } else {
    // This should never actually happen on valid Windows installations because
    // the environment variable SystemDrive should always exist.
    root = "C:/";
  }

  return NaviErrors::SUCCESS;
}
}