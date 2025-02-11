package com.legv8.simulator.execution;

import com.legv8.simulator.lexer.TextLine;

import java.util.ArrayList;

/**
 * <code>ContinuousMode</code> simulator execution mode that runs
 * the cpu with the generated list of instructions until completion.
 *
 * @author Rodrigo Bautista Hernández, 2025
 *
 */
public class ContinuousMode extends LEGv8_Simulator {

    /**
     * Initialises all necessary components for the simulator to run.
     * If there are no errors in the supplied code, one can immediately
     * call a method to execute an instruction in a derived class.
     *
     * @param code the individual lines of LEGv8 source code from the text editor
     */
    public ContinuousMode(ArrayList<TextLine> code) {
        super(code);
    }


}
