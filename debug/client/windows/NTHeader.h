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

#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#include <iostream>

class NTHeader {
 public:

  NTHeader(std::string fileName);
  ~NTHeader();

  unsigned int getImageSize() const {
    return imageSize;
  };

 private:

  void parsePEFile(const std::string& fileName);

  HANDLE hFile_;
  HANDLE hFileMapping_;
  PBYTE mappedView_;
  PIMAGE_NT_HEADERS pNTHeader_;
  unsigned int imageSize;
};
