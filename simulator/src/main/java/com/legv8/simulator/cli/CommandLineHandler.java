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
        if (args.length == 0) {
            System.out.println("Usage: java -jar simulator.jar <path-to-file>");
            return;
        }
        String filePath = args[0];
        String expectedResultFilePath=null;
        boolean printMemory=false;
        if (args.length>=2) {
            printMemory = Boolean.parseBoolean(args[1]);
        }
        if (args.length>=3) {
            expectedResultFilePath = args[2];
        }


        ArrayList<TextLine> lines;
        try {
            lines = reader.readAsTextLines(filePath);
        } catch (IOException e) {
            System.err.println("Error accessing assembly file: " + filePath);
            return;
        }

        if (lines != null && !lines.isEmpty()) {
            List<String> toPrint = new ArrayList<>();

            //Initialize the simulator.
            ContinuousMode simulator = new ContinuousMode(lines);
            ResultWrapper<CPUSnapshot, LineError> result;

            //Check for compile errors
            if(!simulator.getCompileErrorMsgs().isEmpty()) {
                result = null;
                toPrint.addAll(simulator.getCompileErrorMsgs().stream()
                        .map(err -> "Line " + (err.getLineNumber()+1) + ": " + err.getMsg())
                        .peek(System.out::println)
                        .toList());
            }
            else {
                //Run the simulator
                result = simulator.runWithResult();

                if(result.isSuccess()) {
                    toPrint.add(result.getValue().toString());
                    if(printMemory) {
                        toPrint.add(simulator.getMemory().toString());
                    }
                } else {
                    toPrint.add(result.getError().toString());
                }
            }

            if(expectedResultFilePath != null && !expectedResultFilePath.isEmpty()) {
                boolean success = true;
                String failingRegisters = "";
                if(result!=null && result.isFailure()) {
                    success = false;
                }
                else {
                    Map<String, Long> registers;
                    try {
                        registers = resultReader.readExpectedRegisters(expectedResultFilePath);
                    } catch (IOException e) {
                        System.err.println("Error accessing the expected results file: " + expectedResultFilePath);
                        return;
                    }

                    if (registers != null && !registers.isEmpty()) {
                        List<String> registerNames = List.of(result.getValue().getRegisterNames());

                        for (Map.Entry<String, Long> entry : registers.entrySet()) {
                            int reg = registerNames.indexOf(entry.getKey());
                            long actual = result.getValue().getRegister(reg);

                            if(entry.getValue() != actual) {
                                success = false;
                                failingRegisters += (entry.getKey() + ", ");
                            }
                        }
                    }
                }

                if(!success) {
                    System.out.println("TESTS FAILED");
                    toPrint.add("Tests FAILED. Failing registers: " + failingRegisters);
                }
                else {
                    System.out.println("TESTS PASSED");
                    toPrint.add("Tests PASSED.");
                }
            }

            //Write results
            try {
                writer.writeToFile(toPrint);
            } catch (IOException e) {
                System.err.println("Error accessing results file: " + expectedResultFilePath);
                return;
            }
        }
        else {
            try {
                writer.writeToFile("There is no code.");
            } catch (IOException e) {
                System.err.println("Error accessing results file: " + expectedResultFilePath);
            }
        }
    }
}
