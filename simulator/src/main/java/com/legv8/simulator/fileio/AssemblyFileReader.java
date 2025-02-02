package com.legv8.simulator.fileio;

import com.legv8.simulator.cli.CommandLineHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * <code>AssemblyFileReader</code> is used to read assembly code files from the path
 * given to the <code>CommandLineHandler</code> when the simulator is run from the CLI.
 *
 * @see    CommandLineHandler
 * @author Rodrigo Bautista Hernández, 2025
 *
 */
@Component
public class AssemblyFileReader {

    public List<String> readFile(String filePath) throws IOException {
        return Files.readAllLines(Path.of(filePath));
    }
}
