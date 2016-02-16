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

#include <string>

namespace zylib
{
	namespace zycon
	{
		class ByteReader
		{
			private:
				const char* start;
				const char* p;
				unsigned int toRead;
				bool error_;

			public:
				ByteReader(const char* start, unsigned int toRead) : start(start), p(start), toRead(toRead), error_(false) { }

			bool isDone() const
			{
				return start + toRead == p;
			}

			bool isError() const
			{
				return error_;
			}

			unsigned int readInt()
			{
				if (p + sizeof(unsigned int) > start + toRead)
				{
					error_ = true;

					return 0;
				}

				unsigned int value = *(unsigned int*) p;

				p += sizeof(unsigned int);

				return value;
			}

			std::string readString(unsigned int chars)
			{
				if (p + chars > start + toRead)
				{
					error_ = true;

					return "";
				}

				std::string str(p, chars);

				p += chars;

				return str;
			}
		};
	}
}
