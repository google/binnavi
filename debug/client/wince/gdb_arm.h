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

#ifndef GDB_ARM_HPP
#define GDB_ARM_HPP

//NOTE: some functions are slightly modified

/* Instruction condition field values.  */
#define INST_EQ 0x0
#define INST_NE 0x1
#define INST_CS 0x2
#define INST_CC 0x3
#define INST_MI 0x4
#define INST_PL 0x5
#define INST_VS 0x6
#define INST_VC 0x7
#define INST_HI 0x8
#define INST_LS 0x9
#define INST_GE 0xa
#define INST_LT 0xb
#define INST_GT 0xc
#define INST_LE 0xd
#define INST_AL 0xe
#define INST_NV 0xf

#define FLAG_N 0x80000000
#define FLAG_Z 0x40000000
#define FLAG_C 0x20000000
#define FLAG_V 0x10000000

#define submask(x) ((1L << ((x) + 1)) - 1)
#define bit(obj, st) (((obj) >> (st)) & 1)
#define bits(obj, st, fn) (((obj) >> (st)) & submask((fn) - (st)))
#define sbits(obj, st, fn)
((long)(bits(obj, st, fn)| ((long) bit(obj, fn)* ~submask(fn - st))))
#define BranchDest(addr, instr)
    ((CPUADDRESS)(((long)(addr)) + 8 + (sbits(instr, 0, 23) << 2)))
#define ARM_PC_32 1
int arm_apcs_32 = 1;

int arm_pc_is_thumb(CPUADDRESS memaddr) {
  //TODO: write proper code
  return 0;
}

static CPUADDRESS arm_addr_bits_remove(CPUADDRESS val) {
  if (arm_apcs_32)
    return (val & (arm_pc_is_thumb(val) ? 0xfffffffe : 0xfffffffc));
  else
    return (val & 0x03fffffc);
}

static int condition_true(unsigned long cond, unsigned long status_reg) {
  if (cond == INST_AL || cond == INST_NV)
    return 1;

  switch (cond) {
    case INST_EQ:
      return ((status_reg & FLAG_Z) != 0);
    case INST_NE:
      return ((status_reg & FLAG_Z) == 0);
    case INST_CS:
      return ((status_reg & FLAG_C) != 0);
    case INST_CC:
      return ((status_reg & FLAG_C) == 0);
    case INST_MI:
      return ((status_reg & FLAG_N) != 0);
    case INST_PL:
      return ((status_reg & FLAG_N) == 0);
    case INST_VS:
      return ((status_reg & FLAG_V) != 0);
    case INST_VC:
      return ((status_reg & FLAG_V) == 0);
    case INST_HI:
      return ((status_reg & (FLAG_C | FLAG_Z)) == FLAG_C);
    case INST_LS:
      return ((status_reg & (FLAG_C | FLAG_Z)) != FLAG_C);
    case INST_GE:
      return (((status_reg & FLAG_N) == 0) == ((status_reg & FLAG_V) == 0));
    case INST_LT:
      return (((status_reg & FLAG_N) == 0) != ((status_reg & FLAG_V) == 0));
    case INST_GT:
      return (((status_reg & FLAG_Z) == 0)
          && (((status_reg & FLAG_N) == 0) == ((status_reg & FLAG_V) == 0)));
    case INST_LE:
      return (((status_reg & FLAG_Z) != 0)
          || (((status_reg & FLAG_N) == 0) != ((status_reg & FLAG_V) == 0)));
  }
  return 1;
}

static unsigned int shifted_reg_val(unsigned int inst, int carry,
                                    unsigned int pc_val,
                                    unsigned int status_reg,
                                    unsigned int* registers) {
  unsigned int res, shift;
  int rm = bits(inst, 0, 3);
  unsigned int shifttype = bits(inst, 5, 6);

  if (bit(inst, 4)) {
    int rs = bits(inst, 8, 11);
    shift = (rs == 15 ? pc_val + 8 : registers[rs]) & 0xFF;
  } else
    shift = bits(inst, 7, 11);

  res = (
      rm == 15 ?
          ((pc_val | (ARM_PC_32 ? 0 : status_reg)) + (bit(inst, 4) ? 12 : 8)) :
          registers[rm]);

  switch (shifttype) {
    case 0: /* LSL */
      res = shift >= 32 ? 0 : res << shift;
      break;

    case 1: /* LSR */
      res = shift >= 32 ? 0 : res >> shift;
      break;

    case 2: /* ASR */
      if (shift >= 32)
        shift = 31;
      res = ((res & 0x80000000L) ? ~((~res) >> shift) : res >> shift);
      break;

    case 3: /* ROR/RRX */
      shift &= 31;
      if (shift == 0)
        res = (res >> 1) | (carry ? 0x80000000L : 0);
      else
        res = (res >> shift) | (res << (32 - shift));
      break;
  }

  return res & 0xffffffff;
}

/* Return number of 1-bits in VAL.  */

static int bitcount(unsigned int val) {
  int nbits;
  for (nbits = 0; val != 0; nbits++)
    val &= val - 1; /* delete rightmost 1-bit in val */
  return nbits;
}
#endif
