/*
Copyright 2014 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/
#include <iostream>
#include <cmath>
#include <vector>
#include <limits>
#include <sstream>

bool checkUpperBound(unsigned int upperBound)
{
	if (upperBound >= 100000)
	{
		std::cout << "Error: The upper bound you entered is too large";
		return false;
	}
	
	return true;
}

unsigned int inputUpperBound()
{
	std::cout << "Please enter the upper bound for prime numbers (max 100000): \n";
	
	unsigned int upperBound;
	
	while(!(std::cin >> upperBound))
	{
		std::cin.clear();
		std::cin.ignore(std::numeric_limits<std::streamsize>::max(),'\n');
	}
	
	return upperBound;
}

std::vector<unsigned int> calculateSieve(unsigned int upperBound)
{
	std::vector<unsigned int> sieve(upperBound, 1);
	
	sieve[0] = sieve[1] = 0;
	
	for (unsigned int i=2;i<sqrt((float) upperBound);i++)
	{
		for (unsigned int j=2*i;j<upperBound;j+=i)
		{
			sieve[j] = 0;
		}
	}
	
	return sieve;
}

void printSieve(const std::vector<unsigned int>& sieve)
{
	for (unsigned int i=0;i<sieve.size();i++)
	{
		if (sieve[i])
		{
			std::cout << "Prime: " << i << std::endl;
		}
	}
}

int main()
{
	unsigned int upperBound = inputUpperBound();
	
	if (!checkUpperBound(upperBound))
	{
		return 1;
	}
	
	printSieve(calculateSieve(upperBound));
}
