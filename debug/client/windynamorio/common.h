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

#ifndef COMMON_H_
#define COMMON_H_

#include <vector>

template <class T, class T2>
inline T Bit(T num, T2 pos) {
  return (num >> pos) & 1;
}

template <class T, class T2>
inline T ClearBit(T num, T2 pos) {
  return num & ~(1 << pos);
}

template <class T, class T2, class T3>
inline T SetBit(T num, T2 pos, T3 val) {
  return ClearBit(num, pos) | ((val & 1) << pos);
}

typedef size_t cpuaddress_t;
typedef int breakpoint_id_t;
typedef unsigned int exception_code_t;

enum class ErrorCode {
  SUCCESS = 0,

  ADDRESS_NOT_MAPPED,
  ADDRESS_NOT_READABLE,
  ADDRESS_NOT_WRITABLE,
  ADDRESS_NOT_ALIGNED,
  SIZE_NOT_ALIGNED,
  CANNOT_CHANGE_PROTECTION,
  WRONG_PROTECTION_FLAGS,

  BREAKPOINT_ALREADY_PRESENT,
  NO_SUCH_BREAKPOINT,

  TERMINATE_PROCESS_FAILED,
  UNKNOWN_THREAD,
  SUSPEND_COUNT_NEGATIVE,  // someone tried to resume already running thread
  UNKNOWN_REGISTER,
  REGISTER_NOT_WRITABLE,
};

template <class _Cont, class _Ty>
bool stl_contains(const _Cont& cont, const _Ty& val) {
  return std::find(cont.begin(), cont.end(), val) != cont.end();
}

template <class _Ty>
void vector_erase(std::vector<_Ty>* v, _Ty val) {
  v->erase(find(v->begin(), v->end(), val));
}

// Erases given element by swapping with the last and removing it.
template <class _Ty>
void vector_fast_erase(std::vector<_Ty>* v,
                       typename const std::vector<_Ty>::iterator& it) {
  std::swap(*it, *(--(v->end())));
  v->erase(--(v->end()));
}

#endif  // COMMON_H_
