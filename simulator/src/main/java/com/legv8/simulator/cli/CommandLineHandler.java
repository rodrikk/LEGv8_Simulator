package com.legv8.simulator.cli;

import com.legv8.simulator.execution.ContinuousMode;
import com.legv8.simulator.execution.LEGv8_Simulator;
import com.legv8.simulator.fileio.AssemblyFileReader;
import com.legv8.simulator.lexer.TextLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;

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

    @Override
    public void run(String... args) {
        if (args.length == 0) {
            System.out.println("Usage: java -jar simulator.jar <path-to-file>");
            return;
        }

        String filePath = args[0];
        ArrayList<TextLine> lines;
        try {
            lines = reader.readAsTextLines(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (lines != null && !lines.isEmpty()) {
            ContinuousMode simulator = new ContinuousMode(lines);
            simulator.runCPU();
        }
        else {
            System.out.println("nothing");
        }
    }
}
