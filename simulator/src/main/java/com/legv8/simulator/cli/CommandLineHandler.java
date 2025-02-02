package com.legv8.simulator.cli;

import com.legv8.simulator.fileio.AssemblyFileReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * <code>CommandLineHandler</code> is used when the emulator is run from the CLI.
 * This makes it so the emulator can be run without a GUI.
 *
 * @author Rodrigo Bautista Hernández, 2025
 *
 */
@Component
public class CommandLineHandler implements CommandLineRunner {

    @Autowired
    private AssemblyFileReader reader;

    @Override
    public void run(String... args) {
        if (args.length == 0) {
            System.out.println("Usage: java -jar emulator.jar <path-to-file>");
            return;
        }

        String filePath = args[0];
        List<String> lines;
        try {
            lines = reader.readFile(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (lines != null && !lines.isEmpty()) {
            System.out.println(lines.get(0));
        }
        else {
            System.out.println("nothing");
        }
    }
}
