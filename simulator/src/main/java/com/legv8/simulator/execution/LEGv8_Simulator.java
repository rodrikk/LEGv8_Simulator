package com.legv8.simulator.execution;

import com.legv8.simulator.response.CPUSnapshot;
import com.legv8.simulator.response.LineError;
import com.legv8.simulator.cpu.CPU;
import com.legv8.simulator.instruction.*;
import com.legv8.simulator.lexer.TextLine;
import com.legv8.simulator.memory.Memory;
import com.legv8.simulator.response.ResultWrapper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * <code>LEGv8_Simulator</code> is the base class from which all simulator/execution modes are derived.
 *
 * @author Jonathan Wright, 2016
 */
public abstract class LEGv8_Simulator {

    /**
     * Initialises all necessary components for the simulator to run.
     * If there are no errors in the supplied code, one can immediately
     * call a method to execute an instruction in a derived class.
     *
     * @param code	the individual lines of LEGv8 source code from the text editor
     */
    public LEGv8_Simulator(ArrayList<TextLine> code) {
        this.code = code;
        branchTable = new HashMap<String, Integer>();
        cpuInstructions = new ArrayList<Instruction>();
        cpu = new CPU();
        compileErrors = new ArrayList<LineError>();
        parseCode();
        populateBranchTable();
        decodeInstructions();
        memory = new Memory(cpuInstructions.size());
    }

    /**
     * For each line of source code: attempt to generate tokens and then parse.
     */
    /*
     * Any errors are stored in the compileErrors arraylist
     */
    public void parseCode() {
        for (int i=0; i<code.size(); i++) {
            if (!code.get(i).getLine().isEmpty()) {
                code.get(i).tokenize();
                if (code.get(i).getNumTokens()>0) { // Why would there be an error message is the number of tokens is greater than 0?
                    String errorMsg = code.get(i).parse();
                    if (errorMsg != null) {
                        compileErrors.add(new LineError(errorMsg, i));
                    }
                }
            }
        }
    }

    /**
     * For each label in the source code, an entry is inserted into the branch lookup table
     * mapping from the label to the index of the instruction immediately after it.
     */
    public void populateBranchTable() {
        String label;
        Mnemonic mnem;
        int instructionCount = 0;
        for (int i=0; i<code.size(); i++) {
            label = code.get(i).getLabel();
            mnem = code.get(i).getMnemonic();
            if (label != null) {
                branchTable.put(label, instructionCount);
            }
            if (mnem != null) {
                instructionCount++;
            }
        }
    }

    /**
     * Attempt to generate a list of instructions from the parsed lines of source code
     */
    /*
     * Errors are stored in the compileErros arraylist
     */
    public void decodeInstructions() {
        TextLine line;
        for (int i=0; i<code.size(); i++) {
            line = code.get(i);
            if (line.getMnemonic() != null) {
                try {
                    cpuInstructions.add(Decoder.getInstruction(
                            line.getMnemonic(), line.getArgs(), i, branchTable));
                } catch (UndefinedLabelException ule) {
                    compileErrors.add(new LineError(ule.getMessage(), i));
                } catch (ImmediateOutOfBoundsException ioobe) {
                    compileErrors.add(new LineError(ioobe.getMessage(), i));
                }
            }
        }
    }

    /**
     * Run the cpu with the generated list of instructions until completion (or not if infinite loop)
     */
    public void runCPU() {
        ResultWrapper<CPUSnapshot,LineError> result = cpu.run(cpuInstructions, memory);
        if(result.isFailure()) {
            runtimeError = result.getError();
        }
    }

    /**
     * @return	the list of text lines from the text editor
     */
    public ArrayList<TextLine> getCode() {
        return code;
    }

    /**
     * @return	the list of compile errors found when performing lexical,
     * syntactic and semantic analysis on the user's code
     */
    public ArrayList<LineError> getCompileErrorMsgs() {
        return compileErrors;
    }

    /**
     * @return	the runtime error thrown by the CPU, <code>null</code> if none exists
     */
    public LineError getRuntimeErrorMsg() {
        return runtimeError;
    }

    /**
     * @param index	the index of any CPU register. Permitted range 0-31
     * @return		the value stored in the specified CPU register
     */
    public long getCPURegister(int index) {
        return cpu.getRegister(index);
    }

    /**
     * @return	the current value of the Z flag in the CPU
     */
    public boolean getCPUZflag() {
        return cpu.getZflag();
    }

    /**
     * @return	the current value of the N flag in the CPU
     */
    public boolean getCPUNflag() {
        return cpu.getNflag();
    }

    /**
     * @return	the current value of the C flag in the CPU
     */
    public boolean getCPUCflag() {
        return cpu.getCflag();
    }

    /**
     * @return	the current value of the V flag in the CPU
     */
    public boolean getCPUVflag() {
        return cpu.getVflag();
    }

    /**
     * @return	the current value of the PC register in the CPU
     */
    public long getPC() {
        return cpu.getPC();
    }

    /**
     * The PC register value is derived from this index
     *
     * @return	the index of the next instruction to execute in the list of program instructions
     */
    public int getInstructionIndex() {
        return cpu.getInstructionIndex();
    }

    /**
     * @return the contents of the CPU log
     */
    public String getCpuLog() {
        return cpu.getCpuLog();
    }

    /**
     * @return	the line number in the text editor of the previously executed instruction
     */
    public int getCurrentLineNumber() {
        return currentLineNumber;
    }

    public Memory getMemory() {
        return memory;
    }

    protected ArrayList<TextLine> code;
    protected int currentLineNumber;
    protected LineError runtimeError = null;
    protected ArrayList<LineError> compileErrors;
    protected Memory memory;
    protected HashMap<String, Integer> branchTable;
    protected ArrayList<Instruction> cpuInstructions;
    protected CPU cpu;
}
