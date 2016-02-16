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

#ifndef BYTEBUFFER_HPP
#define BYTEBUFFER_HPP

#include <vector>

namespace zylib
{
	namespace zycon
	{
		/**
		* Helper class that can easily fill a byte buffer with arbitrary values.
		*
		* Note: Should only be used in combination with STATIC_ASSERT to ensure
		* proper endianness and structure packing of objects placed in the buffer.
		**/
		class ByteBuffer
		{
			private:
				//! The buffer that is filled when calling the add functions
				std::vector<char> buffer;

			public:

				/**
				* Adds size bytes from a character buffer to the byte buffer.
				*
				* @param data The source buffer.
				* @param size The number of characters to copy from the source buffer.
				**/
				void add(const char* data, unsigned int size)
				{
					buffer.insert(buffer.end(), data, data + size);
				}

				/**
				* Adds an object to the byte buffer.
				*
				* @param data The object to add to the buffer.
				**/
				template<typename T>
				void add(const T& data)
				{
					buffer.insert(buffer.end(), (char*)&data, (char*)&data + sizeof(T));
				}

				/**
				* Returns a pointer to the filled buffer.
				**/
				const char* data() const { return &buffer[0]; }

				/**
				* Returns the number of bytes that were written to the buffer.
				**/
				unsigned int size() const { return buffer.size(); }
		};
	}
}

#endif

