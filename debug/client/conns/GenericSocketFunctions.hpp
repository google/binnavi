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

#ifndef GENERICSOCKETFUNCTIONS_HPP
#define GENERICSOCKETFUNCTIONS_HPP

#ifdef NAVI_GDB_WINDOWS
#define SOME_GDB_AGENT
#elif NAVI_GDB_LINUX
#define SOME_GDB_AGENT
#elif NAVI_GDB_OSX
#define SOME_GDB_AGENT
#endif

#if defined(NAVI_WINDBG_WINDOWS) || defined(NAVI_WINDOWS_SOCKET) \
    || defined(NAVI_GDB_WINDOWS) || defined(NAVI_DYNAMORIO_WINDOWS)
#include "WindowsSocket.hpp"

#define LAST_ERROR_FUNCTION WSAGetLastError()

#endif

#ifdef NAVI_GDB_LINUX
#include <errno.h>
#include <sys/types.h>
#include <netdb.h>
#include "LinuxSocket.hpp"
#define LAST_ERROR_FUNCTION errno
#endif

#ifdef NAVI_GDB_OSX
#include <errno.h>
#include <sys/types.h>
#include <netdb.h>
#include "OsxSocket.hpp"
#define LAST_ERROR_FUNCTION errno
#endif

#ifdef NAVI_WINCE_SOCKET
#include "WinCESocket.hpp"

#define LAST_ERROR_FUNCTION WSAGetLastError()
#endif

#ifdef NAVI_LINUX_SOCKET
#include <errno.h>
#include <sys/types.h>
#include <netdb.h>
#include "LinuxSocket.hpp"
#define LAST_ERROR_FUNCTION errno
#endif

namespace SocketFunctions {
bool hasData(SOCKET socket);

NaviError read(SOCKET socket, char* buffer, unsigned int size);

unsigned int send(SOCKET socket, const char* buffer, unsigned int size);

#ifdef SOME_GDB_AGENT
SOCKET connect(const std::string& host, unsigned int port);

void close(SOCKET socket);
#endif
}

#endif
