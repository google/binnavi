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

#include "zycon.h"

#include <algorithm>
#include <string>
#include <functional>
#include <cctype>

namespace zylib
{
	namespace zycon
	{
		/**
		* Determines whether a given character is a lower case hexadecimal character (0-9, a-f).
		*
		* @param c The character to test.
		*
		* @return True, if the given character is a lower case hexadecimal character. False, otherwise.
		**/
		bool isLowerHex(char c)
		{
			return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f');
		}

		/**
		* Determines whether a given character is an upper case hexadecimal character (0-9, A-F).
		*
		* @param c The character to test.
		*
		* @return True, if the given character is an upper case hexadecimal character. False, otherwise.
		**/
		bool isUpperHex(char c)
		{
			return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F');
		}

		/**
		* Determines whether a given character is an a hexadecimal character (0-9, A-F).
		*
		* @param c The character to test.
		*
		* @return True, if the given character is a hexadecimal character. False, otherwise.
		**/
		bool isHex(char c)
		{
			return isLowerHex(c) || isUpperHex(c);
		}

		/**
		* Tests whether a string is a valid representation of a positive number (all characters of the
		* string are digits).
		*
		* @param str The string to test.
		*
		* @return True, if the string is a positive number. False, otherwise.
		**/
		bool isPositiveNumber(const std::string& str)
		{
			return std::find_if(str.begin(), str.end(), std::not1(std::ptr_fun(isdigit))) == str.end();
		}

		std::string toLower(const std::string& str)
		{
			std::string lowerString = str;
			std::transform(lowerString.begin(), lowerString.end(), lowerString.begin(), ::tolower);

			return lowerString;
		}

		/**
		* \brief Converts char values to hex strings.
		*
		* Don't use this function. It's a helper function.
		**/
		template<typename T>
		std::string charFormat(const T& x, bool pad)
		{
		    std::ostringstream streamOut;

		    streamOut << std::hex;

		    if (pad)
		    {
			    streamOut << std::setw(2) << std::setfill('0');
		    }

		    // This is necessary; otherwise the ASCII char is added to the stream.
			streamOut << (x & 0xFF);

		    return streamOut.str();
		}

		/**
		* \brief Converts a char value into a hex string.
		*
		* Converts a char value into a hex string.
		*
		* @param x The value to convert.
		* @param pad Pad value with 0 bytes if necessary.
		*
		* @return The converted value.
		**/
		template<>
		std::string toHexString(const char& x, bool pad)
		{
			return charFormat(x, pad);
		}

		/**
		* \brief Converts an unsigned char value into a hex string.
		*
		* Converts an unsigned char value into a hex string.
		*
		* @param x The value to convert.
		* @param pad Pad value with 0 bytes if necessary.
		*
		* @return The converted value.
		**/
		template<>
		std::string toHexString(const unsigned char& x, bool pad)
		{
			return charFormat(x, pad);
		}

		/**
		* \brief Converts a signed char value into a hex string.
		*
		* Converts a signed char value into a hex string.
		*
		* @param x The value to convert.
		* @param pad Pad value with 0 bytes if necessary.
		*
		* @return The converted value.
		**/
		template<>
		std::string toHexString(const signed char& x, bool pad)
		{
			return charFormat(x, pad);
		}

		/**
		* Converts a boolean value into a string.
		*
		* Note: we do not use template specialization
		* in order to not break existing code which relies upon
		* the implicit conversion of bool to int.
		*
		* @param x The value to convert.
		*
		* @return The converted value.
		**/
		std::string toBoolString(bool x)
		{
			return x ? "true" : "false";
		}
	}
}

