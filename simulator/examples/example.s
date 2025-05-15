// PROGRAM_1: Basic Math, and Branch test

// 1.1)
// Load values into:
// x0 = -1 *
// x1 = 1
// x2 = 2
//
// * Subtract x2 from x1 to get answer in x0 (Because you cannot use a negative number as an immediate value):
// 1 - 2 = -1
// Add x0 and x2 to get an answer in x3:
// -1 + 2 = 1

MOVZ x1, #1
MOVZ x2, #2
SUB x0, x1, x2
ADD x3, x0, x2

// 1.2)
// Store contents of register x3 into memory. Then load from memory into register x4.
// Load x2 into register x5. Compare register x5 and register x2, branch if equal.

MOVZ x27, #0x1
LSL x27, x27, #28
STURW x3, [x27, #5]
LDURSW x4, [x27, #5]
MOVZ x5, #2
SUB x6, x5, x2
CBZ x6, label1
MOVZ x23, #28
label1: ADDI x0, x0, #2
MOVZ x26, #1
ANDS x26, x0, x26
