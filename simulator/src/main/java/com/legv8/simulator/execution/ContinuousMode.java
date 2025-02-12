package com.legv8.simulator.execution;

import com.legv8.simulator.lexer.TextLine;
import com.legv8.simulator.response.CPUSnapshot;
import com.legv8.simulator.response.LineError;
import com.legv8.simulator.response.ResultWrapper;

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

    /**
     * Run the cpu with the generated list of instructions until completion (or not if infinite loop)
     */
    public ResultWrapper<CPUSnapshot, LineError> runWithResult() {
        ResultWrapper<CPUSnapshot, LineError> result = cpu.run(cpuInstructions, memory);
        if(result.isFailure()) {
            runtimeError = result.getError();
        }
        return result;
    }
}
