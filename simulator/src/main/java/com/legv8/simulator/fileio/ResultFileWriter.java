package com.legv8.simulator.fileio;

import com.legv8.simulator.cli.CommandLineHandler;
import com.legv8.simulator.execution.ContinuousMode;
import com.legv8.simulator.lexer.TextLine;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>ResultFileWriter</code> is used to write files containing the results
 * of executing the simulator in continuous mode.
 *
 * @see CommandLineHandler
 * @see ContinuousMode
 * @author Rodrigo Bautista Hernández, 2025
 *
 */
@Component
public class ResultFileWriter {

    // Default file path
    private static final String DEFAULT_FILE_PATH = "output/default_simulation_results.txt";

    /**
     * Writes a list of strings to a file. Each string will be written as a new line.
     * If no file path is provided, the default file path will be used.
     *
     * @param lines List of strings to write.
     * @throws IOException if an I/O error occurs.
     */
    public void writeToFile(List<String> lines) throws IOException {
        writeToFile(DEFAULT_FILE_PATH, lines);
    }

    /**
     * Writes a single string to a file. The string will be written as one line.
     * If no file path is provided, the default file path will be used.
     *
     * @param content The string to write.
     * @throws IOException if an I/O error occurs.
     */
    public void writeToFile(String content) throws IOException {
        writeToFile(DEFAULT_FILE_PATH, content);
    }

    /**
     * Writes a list of strings to a file. Each string will be written as a new line.
     *
     * @param filePath Path of the file to write to.
     * @param lines    List of strings to write.
     * @throws IOException if an I/O error occurs.
     */
    public void writeToFile(String filePath, List<String> lines) throws IOException {
        Path path = Path.of(filePath);

        // Ensure the parent directory exists
        if (Files.notExists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        // Write all lines to the file, overwriting any existing content
        Files.write(path, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Writes a single string to a file. The string will be written as one line.
     *
     * @param filePath Path of the file to write to.
     * @param content  The string to write.
     * @throws IOException if an I/O error occurs.
     */
    public void writeToFile(String filePath, String content) throws IOException {
        Path path = Path.of(filePath);

        // Ensure the parent directory exists
        if (Files.notExists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        // Write the content to the file, overwriting any existing content
        Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
