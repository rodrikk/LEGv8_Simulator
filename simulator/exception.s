MOVZ X1, #0x1000          // parte baja (bits 0–15)
MOVK X1, #0x1000, LSL #16 // parte alta (bits 16–31)
SVC #1
SVC #0               // System call (Supervisor Call)
