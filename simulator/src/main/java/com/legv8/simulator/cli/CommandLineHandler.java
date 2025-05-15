package com.legv8.simulator.cli;

import com.legv8.simulator.execution.ContinuousMode;
import com.legv8.simulator.execution.LEGv8_Simulator;
import com.legv8.simulator.fileio.AssemblyFileReader;
import com.legv8.simulator.fileio.ExpectedResulFileReader;
import com.legv8.simulator.fileio.ResultFileWriter;
import com.legv8.simulator.lexer.TextLine;
import com.legv8.simulator.response.CPUSnapshot;
import com.legv8.simulator.response.LineError;
import com.legv8.simulator.response.ResultWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <code>CommandLineHandler</code> is used when the simulator is run from the CLI.
 * This makes it so the simulator can be run without a GUI.
 *
 * @see LEGv8_Simulator
 * @author Rodrigo Bautista Hernández, 2025
 */
@Component
public class CommandLineHandler implements CommandLineRunner {

    @Autowired
    private AssemblyFileReader reader;
    @Autowired
    private ExpectedResulFileReader resultReader;
    @Autowired
    private ResultFileWriter writer;

    @Override
    public void run(String... args) {
        if (args.length < 3) {
            System.out.println("Usage: java -jar simulator.jar <path-to-file-or-folder> <bulk:true|false> <print-memory:true|false> <expected-results-path> <compact-results:true|false> <path-to-print-results-file>");
            return;
        }

        String path = args[0];
        boolean isBulk = Boolean.parseBoolean(args[1]);
        boolean printMemory = Boolean.parseBoolean(args[2]);
        String expectedResultFilePath = (args.length >= 4) ? args[3] : null;
        boolean compactResults = (args.length >= 5) ? Boolean.parseBoolean(args[4]) : false;
        String whereToPrint = (args.length >= 6) ? args[5] : null;
        try {
            if(whereToPrint != null)
                Files.delete(Path.of(whereToPrint));
            else
                Files.delete(Path.of(ResultFileWriter.DEFAULT_FILE_PATH));
        } catch (IOException e) {
            //Do nothing, it is fine
        }

        if (isBulk) {
            try (var files = Files.list(Path.of(path))) {
                files.filter(p -> p.toString().endsWith(".s"))
                        .forEach(file -> executeFile(file.toString(), printMemory, expectedResultFilePath, whereToPrint, compactResults));
            } catch (IOException e) {
                System.err.println("Error reading directory: " + path);
            }
        } else {
            executeFile(path, printMemory, expectedResultFilePath, whereToPrint, compactResults);
        }
    }


    private void executeFile(String filePath, boolean printMemory, String expectedResultFilePath, String whereToPrint, boolean compactResults) {
        System.out.println("Executing: " + filePath);
        ArrayList<TextLine> lines;
        try {
            lines = reader.readAsTextLines(filePath);
        } catch (IOException e) {
            System.err.println("Error accessing assembly file: " + filePath);
            return;
        }

        if (lines != null && !lines.isEmpty()) {
            List<String> toPrint = new ArrayList<>();
            toPrint.add("=== " + filePath + " ===");

            ContinuousMode simulator = new ContinuousMode(lines);
            ResultWrapper<CPUSnapshot, LineError> result;

            if (!simulator.getCompileErrorMsgs().isEmpty()) {
                result = null;
                toPrint.addAll(simulator.getCompileErrorMsgs().stream()
                        .map(err -> "Line " + (err.getLineNumber() + 1) + ": " + err.getMsg())
                        .peek(System.out::println)
                        .toList());
            } else {
                result = simulator.runWithResult();
                if (result.isSuccess()) {
                    if(!compactResults) {
                        toPrint.add(result.getValue().toString());
                    }
                    else {
                        toPrint.add(result.getValue().getRunTimeString());
                    }
                    if (printMemory) {
                        toPrint.add(simulator.getMemory().toString());
                    }
                } else {
                    toPrint.add(result.getError().toString());
                }
            }

            if (expectedResultFilePath != null && !expectedResultFilePath.isEmpty()) {
                boolean success = true;
                String failingRegisters = "";
                if (result == null || result.isFailure()) {
                    toPrint.add("Tests FAILED. Run or compile failure.");
                } else {
                    Map<String, Long> registers;
                    try {
                        registers = resultReader.readExpectedRegisters(expectedResultFilePath);
                    } catch (IOException e) {
                        System.err.println("Error accessing expected results file: " + expectedResultFilePath);
                        return;
                    }

                    if (registers != null && !registers.isEmpty()) {
                        List<String> registerNames = List.of(result.getValue().getRegisterNames());
                        for (Map.Entry<String, Long> entry : registers.entrySet()) {
                            int reg = registerNames.indexOf(entry.getKey());
                            long actual = result.getValue().getRegister(reg);
                            if (entry.getValue() != actual) {
                                success = false;
                                failingRegisters += (failingRegisters.length()==0) ? entry.getKey() : (", " + entry.getKey());
                            }
                        }
                    }
                    if (!success) {
                        toPrint.add("Tests FAILED. Failing registers: " + failingRegisters);
                    } else {
                        toPrint.add("Tests PASSED.");
                    }
                }
            }

            try {
                if(whereToPrint!=null && !whereToPrint.isEmpty()) {
                    writer.writeToFile(whereToPrint,toPrint);
                }
                else {
                    writer.writeToFile(toPrint);
                }
            } catch (IOException e) {
                System.err.println("Error writing results for file: " + filePath);
            }
        } else {
            try {
                writer.writeToFile("No code found in file: " + filePath);
            } catch (IOException e) {
                System.err.println("Error writing empty result for file: " + filePath);
            }
        }
    }
}
