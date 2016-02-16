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

#include "NTHeader.h"
#include <string>

#define MakePtr(cast, ptr, addValue) \
    (cast)((DWORD_PTR)(ptr) + (DWORD_PTR)(addValue))

NTHeader::NTHeader(std::string fileName)
    : pNTHeader_(NULL),
      imageSize(0) {
  parsePEFile(fileName);
}

NTHeader::~NTHeader() {
  UnmapViewOfFile (mappedView_);
  CloseHandle (hFileMapping_);
  CloseHandle (hFile_);
}

void NTHeader::parsePEFile(const std::string& fileName) {
  hFile_ = CreateFile(fileName.c_str(), GENERIC_READ, FILE_SHARE_READ, NULL,
                      OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, 0);
  if (hFile_ == INVALID_HANDLE_VALUE)
    throw std::exception("Unable to open file!");

  hFileMapping_ = CreateFileMapping(hFile_, NULL, PAGE_READONLY, 0, 0, NULL);
  if (hFileMapping_ == 0) {
    CloseHandle (hFile_);
    throw std::runtime_error(
        "Unable to create file mapping for PE file (" + fileName + ")");
  }

  mappedView_ = (PBYTE) MapViewOfFile(hFileMapping_, FILE_MAP_READ, 0, 0, 0);
  if (mappedView_ == 0) {
    CloseHandle (hFileMapping_);
    CloseHandle (hFile_);
    throw std::runtime_error("Unable to map view of file!");
  }

  PIMAGE_DOS_HEADER pDosHeader = (PIMAGE_DOS_HEADER) mappedView_;
  if (IsBadReadPtr(mappedView_, sizeof(IMAGE_DOS_HEADER))) {
    throw new std::runtime_error(
        "Unable to parse DOS header of debug executable (" + fileName + ")");
  }

  pNTHeader_ = MakePtr(PIMAGE_NT_HEADERS, pDosHeader, pDosHeader->e_lfanew);

  if (IsBadReadPtr(pNTHeader_, sizeof(IMAGE_NT_HEADERS))) {
    throw new std::runtime_error(
        "Unable to parse PE header of debug executable (" + fileName + ")");
  }

  imageSize = pNTHeader_->OptionalHeader.SizeOfImage;
}