# LEGv8 Educational Simulator

This is a simple educational simulator for the LEGv8 architecture (a teaching subset of ARMv8), built in Java.  
It's designed to help students experiment with assembly code execution, including memory usage, registers, exceptions, system calls (SVC), and file handling — all from the command line.

## 🚀 Features

- Executes `.s` assembly files (LEGv8)
- Step-by-step or full execution
- Supports pseudo-instructions like `ADR`
- Basic SVC system call support:
    - `#0` print string
    - `#1` read string
    - `#2-#7` file operations (open, close, read, write, rename, delete)
    - `#8` clock
    - `#9` terminate program
- Built-in error detection: invalid memory, infinite loops, bad instructions
- Integration with expected result files for automated testing
- Bulk execution of multiple student submissions

## 📦 How to run

### Parameters
| Position | Description                                                                                                      | Example                              |
| -------- |------------------------------------------------------------------------------------------------------------------|--------------------------------------|
| 0        | Path to `.s` file or folder with assembly code                                                                   | `./examples/program.s`, `./examples` |
| 1        | Bulk mode? (`true` or `false`). Must be true if running multiple programs                                        | `false`                              |
| 2        | Print memory? (`true` or `false`). If true, print out used memory addresses and their values in the results file | `true`                               |
| 3        | (Optional) Expected results file path. This text file will contain the expected end value for registers          | `./expected/program1.txt`            |
| 4        | (Optional) Compact output (`true/false`). Limit verbosity of the results file.                                   | `false`                              |
| 5        | (Optional) Where to save output. Default is ./output/default_simulation_results.txt                              | `./results/output.txt`               |

### Input files
 - The files with assembly code should have the .s extension.
 - The assembly code should be formated like normal assembly code files.
 - The expected result files should have different registers in different lines (separated by line breaks) </br> with spaces and an equals in between like: ```X1 = 25```.


```bash
# Run a single program
java -jar simulator.jar path/to/program.s false true path/to/expected.txt true path/to/results.txt

# Run multiple programs in a folder (bulk mode)
java -jar simulator.jar path/to/folder true true path/to/expected.txt false path/to/results.txt
```
