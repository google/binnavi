# Copyright 2011-2016 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

cmake_minimum_required (VERSION 2.8)
# Make file for Dynamorio debugger client DLL.
project(dynamorio_client)

# Find dynamoRIO and set global options.
include(FindProtobuf)
find_package(Protobuf REQUIRED)
find_package(DynamoRIO REQUIRED)

include_directories(${PROTOBUF_INCLUDE_DIR})

set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} /MT")
set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} /MTd")

PROTOBUF_GENERATE_CPP(PROTO_SRCS PROTO_HDRS drdebug.proto)

foreach(PROTO_HDR ${PROTO_HDRS})
  get_filename_component(HDR_DIR ${PROTO_HDR} DIRECTORY)
  include_directories(${HDR_DIR})
endforeach(PROTO_HDR)

# Makes dynamic libraries (e.g. dr. memory) known to clients without having to
# copy them to the bin32 directory.
set(DynamoRIO_RPATH ON)

add_library(dynamorio_client SHARED
            dynamorio_client.cc
            common.h
            WinPipeCommandReader.cc
            WinPipeCommandReader.h
            AbstractCommandReader.cc
            AbstractCommandReader.h
            BitReference.h
            DebugState.h
            DebugState.cc
            ScopedLocker.h
            BreakpointInfo.cc
            BreakpointInfo.h
            ${PROTO_HDRS}
            ${PROTO_SRCS})

target_link_libraries(dynamorio_client ${PROTOBUF_LIBRARIES})

# Configure dynamorio_client client.
configure_DynamoRIO_client(dynamorio_client)
