//Dirección de memoria 1
MOVZ X1, #0x1000          // parte baja (bits 0–15)
MOVK X1, #0x1000, LSL #16 // parte alta (bits 16–31)
//Lectura por CLI
SVC #1 //Lee el nombre del archivo
//Abrir archivo
SVC #2
//Número de bits max
MOVZ X2, #6
//Leer del archivo
SVC #4
//Imprimir
SVC #0
//Cerrar archivo
SVC #3

//Dirección de memoria 2
MOVZ X1, #0x2000          // parte baja (bits 0–15)
MOVK X1, #0x2000, LSL #16 // parte alta (bits 16–31)
//Lectura por CLI
SVC #1 //Lee el nombre de otro archivo
//Modo escritura
MOVZ X2, #1
//Abrir archivo en modo escritura
SVC #2
//Dirección de memoria 1 (donde se leyó el primer archivo)
MOVZ X1, #0x1000          // parte baja (bits 0–15)
MOVK X1, #0x1000, LSL #16 // parte alta (bits 16–31)
//Número de bits
MOVZ X2, #6
//Escritura en archivo desde memoria
SVC #5
//Cerrar archivo
SVC #3