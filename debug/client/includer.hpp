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

#ifndef INCLUDER_HPP
#define INCLUDER_HPP

#ifdef NAVI_WINDOWS_SOCKET
#include "conns/WindowsSocket.hpp"
#include "windows/WindowsSystem.hpp"
#include "windows/misc.hpp"

#define CONNECTION_POLICY WindowsSocket
#define SYSTEM_POLICY WindowsSystem
#endif

#ifdef WIN32
#include "../../../../third_party/gflags/src/windows/gflags/gflags.h"
#else
#include <gflags/gflags.h>
#endif

#ifdef NAVI_WINCE_SOCKET
#include "wince/WinCESystem.hpp"
#include "conns/WinCESocket.hpp"

#define CONNECTION_POLICY WinCESocket
#define SYSTEM_POLICY WinCESystem
#endif

#ifdef NAVI_LINUX_SOCKET
#include "linux/LinuxSystem.hpp"
#include "conns/LinuxSocket.hpp"
#include "linux/misc.hpp"

#define CONNECTION_POLICY LinuxSocket
#define SYSTEM_POLICY LinuxSystem
#endif

#ifdef NAVI_GDB_WINDOWS
#include "gdb/GdbSystem.hpp"
#include "conns/WindowsSocket.hpp"
#include "gdb/misc.hpp"

#define CONNECTION_POLICY WindowsSocket
#define SYSTEM_POLICY GdbSystem
#endif

#ifdef NAVI_GDB_LINUX
#include "gdb/GdbSystem.hpp"
#include "conns/LinuxSocket.hpp"
#include "gdb/misc.hpp"

#define CONNECTION_POLICY LinuxSocket
#define SYSTEM_POLICY GdbSystem
#endif

#ifdef NAVI_GDB_OSX
#include "gdb/GdbSystem.hpp"
#include "conns/OSXSocket.hpp"
#include "gdb/misc.hpp"

#define CONNECTION_POLICY OSXSocket
#define SYSTEM_POLICY GdbSystem
#endif

#ifdef NAVI_DYNAMORIO_WINDOWS
#include "conns/WindowsSocket.hpp"
#include "windynamorio/WinDynamoRioSystem.hpp"

#define CONNECTION_POLICY WindowsSocket
#define SYSTEM_POLICY WinDynamoRioSystem
#endif

#ifdef NAVI_WINDBG_WINDOWS
#include "windbg/WinDbgSystem.hpp"
#include "conns/WindowsSocket.hpp"
#include "windbg/misc.hpp"

#define CONNECTION_POLICY WindowsSocket
#define SYSTEM_POLICY WinDbgSystem
#endif

#ifndef SYSTEM_POLICY
#error No System Policy selected
#endif

#ifndef CONNECTION_POLICY
#error No Connection Policy selected
#endif

#endif
