//// Copyright 2011-2016 Google Inc. All Rights Reserved.
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

// Provides BitReference class, which allows user to create a reference
// to selected range of bits in a variable.
#ifndef BITREFERENCE_H_
#define BITREFERENCE_H_

#include <cassert>

// Creates a reference to bits [first_bit_pos; first_bit_pos + bit_size - 1]
// from source variable
//
// Bit position X corresponds to the bit with value 2^X (X-th from the right,
// counting from 0)
//
// It doesn't free source pointer in destructor
//
// T should be an unsigned integer type (unsigned is needed because we can
// underflow in BitMask() method.
template <class T>
class BitReference {
 public:
  // Same as BitReference(source, 0, sizeof(T)*8)
  explicit BitReference(T* source);

  BitReference(T* source, int first_bit_pos, int bit_size);

  void SetReference(T* source, int first_bit_pos, int bit_size);

  T value() const;
  void set_value(T val);

 private:
  T* source_;
  int first_bit_pos_;
  int bit_size_;

  // Creates a bitmask with lowest bit_cnt bits set to 1
  static T BitMask(int bit_cnt);
};

template <class T>
BitReference<T>::BitReference(T* source) {
  SetReference(source, 0, sizeof(T) * 8);
}

template <class T>
BitReference<T>::BitReference(T* source, int first_bit_pos, int bit_size) {
  SetReference(source, first_bit_pos, bit_size);
}

template <class T>
void BitReference<T>::SetReference(T* source, int first_bit_pos, int bit_size) {
  assert(first_bit_pos >= 0 && first_bit_pos < sizeof(T) * 8);
  assert(first_bit_pos + bit_size <= sizeof(T) * 8);
  source_ = source;
  first_bit_pos_ = first_bit_pos;
  bit_size_ = bit_size;
}

template <class T>
T BitReference<T>::value() const {
  return ((*source_) >> first_bit_pos_) & BitMask(bit_size_);
}

template <class T>
void BitReference<T>::set_value(T val) {
  T val_mask = BitMask(bit_size_);
  T mask = ~(val_mask << first_bit_pos_);
  (*source_) = (((*source_) & mask) | ((val & val_mask) << first_bit_pos_));
}

template <class T>
T BitReference<T>::BitMask(int bit_cnt) {
  // We need it to solve the problem with bit_cnt >= 32 (1 << 32 == 1)
  if (bit_cnt >= sizeof(T) * 8)
    return (T)0 - 1;
  else
    return ((T)1 << bit_cnt) - 1;
}

#endif  // BITREFERENCE_H_
