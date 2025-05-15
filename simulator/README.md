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
| Position | Description                                                                                                   | Example                              |
| -------- |---------------------------------------------------------------------------------------------------------------|--------------------------------------|
| 0        | Path to `.s` file or folder with assembly code                                                                | `./examples/program.s`, `./examples` |
| 1        | Bulk mode? (`true` or `false`). Must be true if running multiple programs                                     | `false`                              |
| 2        | Print memory? (`true` or `false`). If true, print out used memory addresses and their values in the results file | `true`                               |
| 3        | Expected results file path. This text file will contain the expected end value for registers          | `./expected/program1.txt`            |
| 4        | Compact output (`true/false`). Limit verbosity of the results file.                                | `false`                              |
| 5        | Where to save output. Default is ./output/default_simulation_results.txt                           | `./results/output.txt`               |

### Input files
 - The files with assembly code should have the .s extension.
 - The assembly code should be formated like normal assembly code files.
 - The expected result files should have different registers in different lines (separated by line breaks) </br> with spaces and an equals in between like: ```X1 = 25```.

### Running from the CLI

```bash
# Run a single program
java -jar simulator.jar path/to/program.s false true path/to/expected.txt true path/to/results.txt

# Run multiple programs in a folder (bulk mode)
java -jar simulator.jar path/to/folder true true path/to/expected.txt false path/to/results.txt
```

## Using software interrupts
## 🛠 System Calls (SVC)

| IMM | Function            | Inputs                                                                                     | Output / Effect                                              | Notes                                   |
|-----|---------------------|--------------------------------------------------------------------------------------------|--------------------------------------------------------------|-----------------------------------------|
| 0   | Print string        | `X1`: Memory address of string<br>`X2`: Length of string in bytes                          | Prints the string to console                                 | Must be null-terminated or bounded by X2 |
| 1   | Read string         | `X1`: Destination memory address                                                           | `X2`: Number of bytes written (including null terminator)    | Converts `\\n` into real newline        |
| 2   | Open file           | `X1`: Memory address of filename (null-terminated)<br>`X2`: 1 for write, 0 for read        | `X0`: File ID or -1 on failure                               | Creates the file if it doesn't exist    |
| 3   | Close file          | `X1`: File ID                                                                              | -                                                            | -                                       |
| 4   | Read file           | `X0`: File ID<br>`X1`: Destination memory<br>`X2`: Number of bytes to read (-1 = all)      | `X0`: Number of bytes read, or -1 on failure                 | Content is null-terminated in memory    |
| 5   | Write file          | `X0`: File ID<br>`X1`: Source memory address<br>`X2`: Number of bytes to write from memory | `X0`: Number of bytes written, or -1 on failure              | -                                       |
| 6   | Rename file         | `X1`: Address of current filename<br>`X2`: Address of new filename                         | `X0`: 0 if success, -1 if failure                            | Uses Java `Files.move()`                |
| 7   | Delete file         | `X1`: Address of filename                                                                  | `X0`: 0 if success, -1 if failure                            | Uses Java `Files.delete()`              |
| 8   | Clock (time elapsed)| -                                                                                          | `X0`: Milliseconds since start of execution                  | Timer starts with program execution     |
| 9   | Terminate program   | -                                                                                          | Halts the simulator                                          | Internally throws `EndExecutionException` |
