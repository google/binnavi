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

#include "WinPipeCommandReader.h"

#include <cassert>

WinPipeCommandReader::WinPipeCommandReader() {
  pipe_ = INVALID_HANDLE_VALUE;
  packet_len_ = -1;
  buf_pos_ = -1;
}

WinPipeCommandReader::~WinPipeCommandReader() {
  Disconnect();
}

bool WinPipeCommandReader::Connect(const std::string& pipe_name) {
  Disconnect();
  if (!WaitNamedPipeA(pipe_name.c_str(), 3000 /* timeout */)) {
    // Waiting for server's ConnectNamedPipe() timeouted.
    return false;
  }
  pipe_ = CreateFileA(pipe_name.c_str(),
                      GENERIC_READ | GENERIC_WRITE,
                      0,     /* dwSharedMode */
                      NULL,  /* lpSecurityAttributes */
                      OPEN_EXISTING,
                      0,     /* dwFlagsAndAttributes */
                      NULL   /* hTemplateFile */
                      );
  if (pipe_ == INVALID_HANDLE_VALUE) {
    return false;
  }
  return true;
}

void WinPipeCommandReader::Disconnect() {
  if (pipe_ != INVALID_HANDLE_VALUE) {
    CloseHandle(pipe_);
    pipe_ = INVALID_HANDLE_VALUE;
  }
}

// TODO(mkow): Add timeout. We don't want to block main loop, we want to be able
// to stop it without using TerminateThread().
std::unique_ptr<security::drdebug::Command>
WinPipeCommandReader::WaitForCommand() {
  if (received_queue_.size() > 0) {
    // Pop previously received command.
    auto cmd = std::unique_ptr<security::drdebug::Command>(
        new security::drdebug::Command(received_queue_.front()));
    received_queue_.pop();
    return cmd;
  }

  assert(pipe_ != INVALID_HANDLE_VALUE);

  DWORD bytes_read;
  if (packet_len_ == -1) {
    // Assembling new packet, read len.
    if (!ReadFile(pipe_, &packet_len_, sizeof(packet_len_), &bytes_read,
                  NULL /* lpOverlapped */) ||
        bytes_read != sizeof(packet_len_)) {
      // Reading length failed.
      return nullptr;
    }
    in_buf_.resize(packet_len_);
    buf_pos_ = 0;
  }
  for (buf_pos_; buf_pos_ < packet_len_;) {
    if (!ReadFile(pipe_, in_buf_.data() + buf_pos_, packet_len_ - buf_pos_,
                  &bytes_read, NULL)) {
      return nullptr;
    }
    buf_pos_ += bytes_read;
  }
  security::drdebug::CommandPacket packet;
  if (!packet.ParseFromArray(in_buf_.data(), packet_len_)) {
    return nullptr;
  }
  auto cmd = std::unique_ptr<security::drdebug::Command>(
      new security::drdebug::Command(packet.command().Get(0)));
  for (int i = 1; i < packet.command_size(); i++) {
    received_queue_.push(packet.command().Get(i));
  }

  packet_len_ = -1;
  return cmd;
}

bool WinPipeCommandReader::SendResponse(
    const security::drdebug::Response& response) {
  response_queue_.push_back(response);
  if (received_queue_.size() > 0) {
    // Performance optimization: Wait with sending response until all commands
    // from queue are processed
    return true;
  }
  assert(pipe_ != INVALID_HANDLE_VALUE);
  security::drdebug::ResponsePacket packet;
  for (const auto& resp : response_queue_) {
    *(packet.add_response()) = resp;
  }
  response_queue_.clear();

  int size = packet.ByteSize();
  char* out_buf = new char[size];
  DWORD bytes_written;

  if (!WriteFile(pipe_, &size, sizeof(size), &bytes_written, NULL) ||
      bytes_written != sizeof(size)) {
    return false;
  }

  packet.SerializeToArray(out_buf, size);
  for (int buf_pos = 0; buf_pos < size;) {
    if (!WriteFile(pipe_, out_buf + buf_pos, size - buf_pos, &bytes_written,
                   NULL)) {
      return false;
    }
    buf_pos += bytes_written;
  }

  return true;
}
