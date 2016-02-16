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

#ifndef ZYCON_HPP
#define ZYCON_HPP

#include <string>
#include <sstream>
#include <iomanip>

namespace zylib
{
	/**
	* The zycon namespace contains a collection of functions for converting between different datatypes
	* and for checking whether values have certain structures.
	**/
	namespace zycon
	{
		//! Tests whether a character is a lower case hexadecimal character.
		bool isLowerHex(char c);

		//! Tests whether a character is a upper case hexadecimal character.
		bool isUpperHex(char c);

		//! Tests whether a character is a hexadecimal character.
		bool isHex(char c);

		//! Tests whether a string is a positive number.
		bool isPositiveNumber(const std::string& str);

		std::string toLower(const std::string& str);

		/**
		 * \brief Converts a string into a value.
		 *
		 * Converts a string into a variable of type T.
		 *
		 * @param s The string to parse.
		 *
		 * @return The parsed value.
		 **/
		template<typename T>
				T parseString(const std::string& s)
		{
			std::istringstream i(s);
			T x;

			if (!(i >> x))
			{
				// TODO: Throw an exception instead
				return 0;
			}
			else
			{
				return x;
			}
		}

		/**
		* Converts a hex string into a variable of type T.
		*
		* @param s The hex string to parse.
		*
		* @return The parsed value.
		**/
		template<typename T>
		T parseHexString(const std::string& s)
		{
			std::istringstream i(s);
			T x;

			if (!(i >> std::hex >> x))
			{
				return 0;
			}
			else
			{
				return x;
			}
		}

		/**
		* \brief Converts a value into a string.
		*
		* Converts a value of type T into a string.
		*
		* @param x The value to convert.
		*
		* @return The converted value.
		**/
		template<typename T>
		std::string toString(const T& x)
		{
		    std::ostringstream streamOut;

		    streamOut << x;

		    return streamOut.str();
		}

		/**
		* \brief Converts a given value into a hex string.
		*
		* Converts a given value into a hex string.
		*
		* @param x The value to convert.
		* @param pad Pad value with 0 bytes if necessary.
		*
		* @return The converted value.
		**/
		template<typename T>
		std::string toHexString(const T& x, bool pad = false)
		{
		    std::ostringstream streamOut;

		    streamOut << std::hex;

		    if (pad)
		    {
			    streamOut << std::setw(2 * sizeof(T)) << std::setfill('0');
		    }

		    streamOut << x;

		    return streamOut.str();
		}

		template<>
		std::string toHexString(const char& x, bool pad);

		template<>
		std::string toHexString(const unsigned char& x, bool pad);

		template<>
		std::string toHexString(const signed char& x, bool pad);

		std::string toBoolString(bool x);
	}
}

#endif
