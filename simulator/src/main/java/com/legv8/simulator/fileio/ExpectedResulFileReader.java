package com.legv8.simulator.fileio;

import com.legv8.simulator.cli.CommandLineHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <code>AssemblyFileReader</code> is used to read text files containing expected results
 * for the assembly program. The file is read from the path given to the
 * <code>CommandLineHandler</code> when the simulator is run from the CLI.
 *
 * @see    CommandLineHandler
 * @author Rodrigo Bautista Hernández, 2025
 *
 */
@Component
public class ExpectedResulFileReader {

    public Map<String, Long> readExpectedRegisters(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(filePath)).stream()
                .map(String::trim)
                .toList();
        Map<String, Long> registers = new HashMap<>();

        for (String line : lines) {
            String[] fragments = line.split(" ");
            if (fragments.length < 3) continue;
            registers.put(fragments[0], Long.parseLong(fragments[2]));
        }
        return registers;
    }
}
