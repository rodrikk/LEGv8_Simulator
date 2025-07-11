package com.legv8.simulator.cpu;

import ch.qos.logback.core.encoder.JsonEscapeUtil;
import com.legv8.simulator.response.CPUSnapshot;
import com.legv8.simulator.response.LineError;
import com.legv8.simulator.instruction.Instruction;
import com.legv8.simulator.memory.Memory;
import com.legv8.simulator.memory.SegmentFaultException;
import com.legv8.simulator.response.ResultWrapper;

import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * The <code>CPU</code> class is a single cycle simulator of the LEGv8 instruction set.
 * <p>
 * It comprises a register file, N, Z, C, V flags and a PC. To execute an instruction,
 * references to a list of instructions and a data memory must be supplied.
 *
 * @author Jonathan Wright, 2016
 * @author Rodrigo Bautista Hernández, 2025
 */

/*
 * Ugly things...
 * 1) Hard coded strings for log message saying ignored attempt to assign to XZR
 * 2) The strings in the cpuLog.append(...) code for each instruction are hideous.
 * 	  This is because GWT does not support the java.util.Formatter class which provides C-style printf() string formatting
 */

public class CPU {
    private Map<Integer, SeekableByteChannel> openFiles = new HashMap<>();
    private int nextFileId = 3;

    public static final int INSTRUCTION_SIZE = 4;
    public static final int NUM_REGISTERS = 32;

    public static final int XZR = 31;
    public static final int LR = 30;
    public static final int FP = 29;
    public static final int SP = 28;
    public static final int X27 = 27;
    public static final int X26 = 26;
    public static final int X25 = 25;
    public static final int X24 = 24;
    public static final int X23 = 23;
    public static final int X22 = 22;
    public static final int X21 = 21;
    public static final int X20 = 20;
    public static final int X19 = 19;
    public static final int X18 = 18;
    public static final int IP1 = 17;
    public static final int IP0 = 16;
    public static final int X15 = 15;
    public static final int X14 = 14;
    public static final int X13 = 13;
    public static final int X12 = 12;
    public static final int X11 = 11;
    public static final int X10 = 10;
    public static final int X9 = 9;
    public static final int X8 = 8;
    public static final int X7 = 7;
    public static final int X6 = 6;
    public static final int X5 = 5;
    public static final int X4 = 4;
    public static final int X3 = 3;
    public static final int X2 = 2;
    public static final int X1 = 1;
    public static final int X0 = 0;

    private boolean branchTaken = false;
    private boolean STXRSucceed = false;
    private StringBuilder cpuLog = new StringBuilder("");
    private long[] registerFile;
    private long taggedAddress;
    private int instructionIndex;
    private boolean Nflag;
    private boolean Zflag;
    private boolean Cflag;
    private boolean Vflag;
    private final long startTime;
    private long endTime;

    /**
     * Constructs a new <code>CPU</code> object, initialising registers and flags to 0 and false respectively.
     * The SP register is then set according the definition of the LEGv8 virtual address space in Patterson and Hennessy ARM Edition.
     *
     * @see Memory
     */
    public CPU() {
        registerFile = new long[NUM_REGISTERS];
        for (int i=0; i<NUM_REGISTERS; i++) {
            registerFile[i] = 0L;
        }
        registerFile[SP] = Memory.STACK_BASE;
        Nflag = false;
        Zflag = false;
        Cflag = false;
        Vflag = false;
        startTime = System.currentTimeMillis();
    }

    /**
     * The <code>Instruction</code> executed is that pointed to by the PC.
     *
     * @param cpuInstructions	the list of <code>Instruction</code>s in the LEGv8 assembly program
     * @param memory			a reference to the data memory used in data transfer instructions
     * @return					an <code>LineError</code> object, <code>null</code> if no error occurs during execution
     *
     * @see Instruction
     * @see Memory
     * @see LineError
     */
    /*
     * The PC value is derived from the internal variable 'instructionIndex', which denotes the next instruction to execute
     * in the cpuInstructions ArrayList
     */
    public LineError executeInstruction(ArrayList<Instruction> cpuInstructions, Memory memory) {
        try {
            execute(cpuInstructions.get(instructionIndex++), memory);
        } catch (SegmentFaultException sfe) {
            return new LineError(sfe.getMessage(), cpuInstructions.get(instructionIndex-1).getLineNumber());
        } catch (PCAlignmentException pcae) {
            return new LineError(pcae.getMessage(), cpuInstructions.get(instructionIndex-1).getLineNumber());
        } catch (SPAlignmentException spae) {
            return new LineError(spae.getMessage(), cpuInstructions.get(instructionIndex-1).getLineNumber());
        } catch (IOException ioe) {
            return new LineError(ioe.getMessage(), cpuInstructions.get(instructionIndex-1).getLineNumber());
        } catch (EndExecutionException eee) {
            return new LineError(eee.getMessage(), eee.getLine());
        }
        return null;
    }

    /**
     * This method will execute the supplied LEGv8 assembly program in its entirety.
     *
     * @param cpuInstructions	the list of <code>Instruction</code>s in the LEGv8 assembly program
     * @param memory			a reference to the data memory used in data transfer instructions
     * @return					an <code>LineError</code> object, <code>null</code> if no error occurs during execution
     */
    public ResultWrapper<CPUSnapshot, LineError> run(ArrayList<Instruction> cpuInstructions, Memory memory) {
        try {
            while (instructionIndex < cpuInstructions.size()) {
                execute(cpuInstructions.get(instructionIndex++), memory);
            }
        } catch (SegmentFaultException | IOException | SPAlignmentException | PCAlignmentException sfe) {
            return ResultWrapper.failure(new LineError(sfe.getMessage(), cpuInstructions.get(instructionIndex-1).getLineNumber()));
        } catch (EndExecutionException eee) {
            return ResultWrapper.failure(new LineError(eee.getMessage(), eee.getLine()));
        }
        catch (OutOfMemoryError oome) {
            return ResultWrapper.failure(new LineError("Infinite loop prevented. Out of memory.", -1));
        }
        this.endTime = System.currentTimeMillis();
        return ResultWrapper.success(new CPUSnapshot(this));
    }

    /**
     * @param index	the register whose value to return, an integer in the range 0-31
     * @return		the value stored in the register <code>index</code>
     */
    public long getRegister(int index) {
        return registerFile[index];
    }

    /**
     * @return a string showing full CPU execution history
     */
    public String getCpuLog() {
        return cpuLog.toString();
    }

    /**
     * @return	<code>true</code> if the last instruction executed was a branch instruction and
     * the branch was taken. <code>false</code> otherwise.
     */
    public boolean getBranchTaken() {
        return branchTaken;
    }

    /**
     * @return	<code>true</code> if the last instruction executed was STXR and the store to
     * memory succeeded. <code>false</code> otherwise.
     */
    public boolean getSTXRSucceed() {
        return STXRSucceed;
    }

    /**
     * @return	the current value of PC.
     */
    public long getPC() {
        return (long) instructionIndex * INSTRUCTION_SIZE + Memory.TEXT_SEGMENT_OFFSET;
    }

    /**
     * @return	the value of <code>instructionIndex</code>, the index of the next instruction to execute.
     */
    public int getInstructionIndex() {
        return instructionIndex;
    }

    /**
     * @return	the value of the N flag.
     */
    public boolean getNflag() {
        return Nflag;
    }

    /**
     * @return	 the value of the Z flag.
     */
    public boolean getZflag() {
        return Zflag;
    }

    /**
     * @return	the value of the C flag.
     */
    public boolean getCflag() {
        return Cflag;
    }

    /**
     * @return	the value of the V flag.
     */
    public boolean getVflag() {
        return Vflag;
    }

    private void setNflag(boolean set) {
        Nflag = set;
    }

    private void setZflag(boolean set) {
        Zflag = set;
    }

    private void setCflag(boolean set) {
        Cflag = set;
    }

    private void setCflag(long result, long op1, long op2) {
        Cflag = ((MSB(~result) + MSB(op1) + MSB(op2)) & 2L) != 0;
    }

    private void setVflag(boolean set) {
        Vflag = set;
    }

    private void setVflag(long result, long op1, long op2) {
        Vflag = (((op1^~op2) & (op1^result)) & (1<<63)) != 0;
    }

    // returns most significant bit of value passed in
    private long MSB(long value) {
        return value >>> 63;
    }

    private void ADDSetFlags(long result, long op1, long op2) {
        setNflag(result < 0);
        setZflag(result == 0);
        setCflag(result, op1, op2);
        setVflag(result, op1, op2);
    }

    private void SUBSetFlags(long result, long op1, long op2) {
        ADDSetFlags(result, op1, op2);
    }

    private void ANDSetFlags(long result) {
        setNflag(result < 0);
        setZflag(result == 0);
        setCflag(false);
        setVflag(false);
    }

    private void clearExclusiveAccessTag(long address, int figureSize) {
        if (taggedAddress == 0) return;
        if ((address >= taggedAddress
                && address < taggedAddress+Memory.DOUBLEWORD_SIZE)
                || (address+figureSize-1 >= taggedAddress
                && address+figureSize-1 < taggedAddress+Memory.DOUBLEWORD_SIZE)) {
            taggedAddress = 0;
            cpuLog.append("Exclusive access address tag cleared \n");
        }
    }

    private void checkSPAlignment() throws SPAlignmentException {
        if (registerFile[SP]%16 != 0) {
            cpuLog.append("SP misaligned\n");
            throw new SPAlignmentException(registerFile[SP]);
        }
        cpuLog.append("SP aligned correctly\n");
    }

    private void execute(Instruction ins, Memory memory)
            throws SegmentFaultException, PCAlignmentException, SPAlignmentException, IOException, EndExecutionException {
        int[] args = ins.getArgs();
        branchTaken = false; // rather ugly but... set to false by default as most instructions are not branches.
        //if a branch instruction is executed and the branch is taken, will be set to true in that instruction method
        switch (ins.getMnemonic()) {
            case ADD -> ADD(args[0], args[1], args[2]);
            case ADDS -> ADDS(args[0], args[1], args[2]);
            case ADDI -> ADDI(args[0], args[1], args[2]);
            case ADDIS -> ADDIS(args[0], args[1], args[2]);
            case SUB -> SUB(args[0], args[1], args[2]);
            case SUBS -> SUBS(args[0], args[1], args[2]);
            case SUBI -> SUBI(args[0], args[1], args[2]);
            case SUBIS -> SUBIS(args[0], args[1], args[2]);
            case AND -> AND(args[0], args[1], args[2]);
            case ANDS -> ANDS(args[0], args[1], args[2]);
            case ANDI -> ANDI(args[0], args[1], args[2]);
            case ANDIS -> ANDIS(args[0], args[1], args[2]);
            case ORR -> ORR(args[0], args[1], args[2]);
            case ORRI -> ORRI(args[0], args[1], args[2]);
            case EOR -> EOR(args[0], args[1], args[2]);
            case EORI -> EORI(args[0], args[1], args[2]);
            case LSL -> LSL(args[0], args[1], args[2]);
            case LSR -> LSR(args[0], args[1], args[2]);
            case LDUR -> LDUR(args[0], args[1], args[2], memory);
            case STUR -> STUR(args[0], args[1], args[2], memory);
            case LDURSW -> LDURSW(args[0], args[1], args[2], memory);
            case STURW -> STURW(args[0], args[1], args[2], memory);
            case LDURH -> LDURH(args[0], args[1], args[2], memory);
            case STURH -> STURH(args[0], args[1], args[2], memory);
            case LDURB -> LDURB(args[0], args[1], args[2], memory);
            case STURB -> STURB(args[0], args[1], args[2], memory);
            case LDXR -> LDXR(args[0], args[1], args[2], memory);
            case STXR -> STXR(args[0], args[1], args[2], args[3], memory);
            case MOVZ -> MOVZ(args[0], args[1], args[2]);
            case MOVK -> MOVK(args[0], args[1], args[2]);
            case CBZ -> CBZ(args[0], args[1]);
            case CBNZ -> CBNZ(args[0], args[1]);
            case BEQ -> BEQ(args[0]);
            case BNE -> BNE(args[0]);
            case BHS -> BHS(args[0]);
            case BLO -> BLO(args[0]);
            case BHI -> BHI(args[0]);
            case BLS -> BLS(args[0]);
            case BGE -> BGE(args[0]);
            case BLT -> BLT(args[0]);
            case BGT -> BGT(args[0]);
            case BLE -> BLE(args[0]);
            case BMI -> BMI(args[0]);
            case BPL -> BPL(args[0]);
            case BVS -> BVS(args[0]);
            case BVC -> BVC(args[0]);
            case B -> B(args[0]);
            case BR -> BR(args[0], memory);
            case BL -> BL(args[0]);
            case SVC -> SVC(args[0], memory);
            default -> {
            }
        }
    }

    private void ADD(int destReg, int op1Reg, int op2Reg) {
        if (destReg == XZR) {
            cpuLog.append("Ignored attempted assignment to XZR. \n");
        } else {
            registerFile[destReg] = registerFile[op1Reg] + registerFile[op2Reg];
            cpuLog.append("ADD \t X" + destReg + ", X" + op1Reg + ", X" + op2Reg + "\n");
        }
    }

    private void ADDS(int destReg, int op1Reg, int op2Reg) {
        long result = registerFile[op1Reg] + registerFile[op2Reg];
        if (destReg == XZR) {
            cpuLog.append("Ignored attempted assignment to XZR. \n");
        } else {
            registerFile[destReg] = result;
            cpuLog.append("ADDS \t X" + destReg + ", X" + op1Reg + ", X" + op2Reg + "\n");
        }
        ADDSetFlags(result, registerFile[op1Reg], registerFile[op2Reg]);
        cpuLog.append("Set flags + \n");
    }

    private void ADDI(int destReg, int op1Reg, int op2Imm) {
        if (destReg == XZR) {
            cpuLog.append("Ignored attempted assignment to XZR. \n");
        } else {
            registerFile[destReg] = registerFile[op1Reg] + op2Imm;
            cpuLog.append("ADDI \t X" + destReg + ", X" + op1Reg + ", #" + op2Imm + "\n");
        }
    }

    private void ADDIS(int destReg, int op1Reg, int op2Imm) {
        long result = registerFile[op1Reg] + op2Imm;
        if (destReg == XZR) {
            cpuLog.append("Ignored attempted assignment to XZR. \n");
        } else {
            registerFile[destReg] = result;
            cpuLog.append("ADDIS \t X" + destReg + ", X" + op1Reg + ", #" + op2Imm + "\n");
        }
        ADDSetFlags(result, registerFile[op1Reg], op2Imm);
        cpuLog.append("Set flags + \n");
    }

    private void SUB(int destReg, int op1Reg, int op2Reg) {
        if (destReg == XZR) {
            cpuLog.append("Ignored attempted assignment to XZR. \n");
        } else {
            registerFile[destReg] = registerFile[op1Reg] - registerFile[op2Reg];
            cpuLog.append("SUB \t X" + destReg + ", X" + op1Reg + ", X" + op2Reg + "\n");
        }
    }

    private void SUBS(int destReg, int op1Reg, int op2Reg) {
        long result = registerFile[op1Reg] - registerFile[op2Reg];
        if (destReg == XZR) {
            cpuLog.append("Ignored attempted assignment to XZR. \n");
        } else {
            registerFile[destReg] = result;
            cpuLog.append("SUBS \t X" + destReg + ", X" + op1Reg + ", X" + op2Reg + "\n");
        }
        SUBSetFlags(result, registerFile[op1Reg], registerFile[op2Reg]);
        cpuLog.append("Set flags + \n");
    }

    private void SUBI(int destReg, int op1Reg, int op2Imm) {
        if (destReg == XZR) {
            cpuLog.append("Ignored attempted assignment to XZR. \n");
        } else {
            registerFile[destReg] = registerFile[op1Reg] - op2Imm;
            cpuLog.append("SUBI \t X" + destReg + ", X" + op1Reg + ", #" + op2Imm + "\n");
        }
    }

    private void SUBIS(int destReg, int op1Reg, int op2Imm) {
        long result = registerFile[op1Reg] - op2Imm;
        if (destReg == XZR) {
            cpuLog.append("Ignored attempted assignment to XZR. \n");
        } else {
            registerFile[destReg] = result;
            cpuLog.append("SUBIS \t X" + destReg + ", X" + op1Reg + ", #" + op2Imm + "\n");
        }
        SUBSetFlags(result, registerFile[op1Reg], op2Imm);
        cpuLog.append("Set flags + \n");
    }

    private void AND(int destReg, int op1Reg, int op2Reg) {
        if (destReg == XZR) {
            cpuLog.append("Ignored attempted assignment to XZR. \n");
        } else {
            registerFile[destReg] = registerFile[op1Reg] & registerFile[op2Reg];
            cpuLog.append("AND \t X" + destReg + ", X" + op1Reg + ", X" + op2Reg + "\n");
        }
    }

    private void ANDS(int destReg, int op1Reg, int op2Reg) {
        long result = registerFile[op1Reg] & registerFile[op2Reg];
        if (destReg == XZR) {
            cpuLog.append("Ignored attempted assignment to XZR. \n");
        } else {
            registerFile[destReg] = result;
            cpuLog.append("ANDS \t X" + destReg + ", X" + op1Reg + ", X" + op2Reg + "\n");
        }
        ANDSetFlags(result);
        cpuLog.append("Set flags + \n");
    }

    private void ANDI(int destReg, int op1Reg, int op2Imm) {
        if (destReg == XZR) {
            cpuLog.append("Ignored attempted assignment to XZR. \n");
        } else {
            registerFile[destReg] = registerFile[op1Reg] & op2Imm;
            cpuLog.append("ANDI \t X" + destReg + ", X" + op1Reg + ", #" + op2Imm + "\n");
        }
    }

    private void ANDIS(int destReg, int op1Reg, int op2Imm) {
        long result = registerFile[op1Reg] & op2Imm;
        if (destReg == XZR) {
            cpuLog.append("Ignored attempted assignment to XZR. \n");
        } else {
            registerFile[destReg] = result;
            cpuLog.append("ANDIS \t X" + destReg + ", X" + op1Reg + ", #" + op2Imm + "\n");
        }
        ANDSetFlags(result);
        cpuLog.append("Set flags + \n");
    }

    private void ORR(int destReg, int op1Reg, int op2Reg) {
        if (destReg == XZR) {
            cpuLog.append("Ignored attempted assignment to XZR. \n");
        } else {
            registerFile[destReg] = registerFile[op1Reg] | registerFile[op2Reg];
            cpuLog.append("ORR \t X" + destReg + ", X" + op1Reg + ", X" + op2Reg + "\n");
        }
    }

    private void ORRI(int destReg, int op1Reg, int op2Imm) {
        if (destReg == XZR) {
            cpuLog.append("Ignored attempted assignment to XZR. \n");
        } else {
            registerFile[destReg] = registerFile[op1Reg] | op2Imm;
            cpuLog.append("ORRI \t X" + destReg + ", X" + op1Reg + ", #" + op2Imm + "\n");
        }
    }

    private void EOR(int destReg, int op1Reg, int op2Reg) {
        if (destReg == XZR) {
            cpuLog.append("Ignored attempted assignment to XZR. \n");
        } else {
            registerFile[destReg] = registerFile[op1Reg] ^ registerFile[op2Reg];
            cpuLog.append("EOR \t X" + destReg + ", X" + op1Reg + ", X" + op2Reg + "\n");
        }
    }

    private void EORI(int destReg, int op1Reg, int op2Imm) {
        if (destReg == XZR) {
            cpuLog.append("Ignored attempted assignment to XZR. \n");
        } else {
            registerFile[destReg] = registerFile[op1Reg] ^ op2Imm;
            cpuLog.append("EORI \t X" + destReg + ", X" + op1Reg + ", #" + op2Imm + "\n");
        }
    }

    private void LSL(int destReg, int op1Reg, int op2Imm) {
        if (destReg == XZR) {
            cpuLog.append("Ignored attempted assignment to XZR. \n");
        } else {
            registerFile[destReg] = registerFile[op1Reg] << op2Imm;
            cpuLog.append("LSL \t X" + destReg + ", X" + op1Reg + ", #" + op2Imm + "\n");
        }
    }

    private void LSR(int destReg, int op1Reg, int op2Imm) {
        if (destReg == XZR) {
            cpuLog.append("Ignored attempted assignment to XZR. \n");
        } else {
            registerFile[destReg] = registerFile[op1Reg] >>> op2Imm;
            cpuLog.append("LSR \t X" + destReg + ", X" + op1Reg + ", #" + op2Imm + "\n");
        }
    }

    private void LDUR(int destReg, int baseAddressReg, int offset, Memory memory)
            throws SegmentFaultException, SPAlignmentException {
        if (baseAddressReg == SP) checkSPAlignment();
        if (destReg == XZR) {
            cpuLog.append("Ignored attempted assignment to XZR. \n");
        } else {
            registerFile[destReg] = memory.loadDoubleword(registerFile[baseAddressReg]+offset);
            cpuLog.append("LDUR \t X" + destReg + ", [X" + baseAddressReg + ", #" + offset + "] \n");
        }
    }

    private void STUR(int valReg, int baseAddressReg, int offset, Memory memory)
            throws SegmentFaultException, SPAlignmentException {
        if (baseAddressReg == SP) checkSPAlignment();
        memory.storeDoubleword(registerFile[baseAddressReg]+offset, registerFile[valReg]);
        clearExclusiveAccessTag(registerFile[baseAddressReg]+offset, Memory.DOUBLEWORD_SIZE);
        cpuLog.append("STUR \t X" + valReg + ", [X" + baseAddressReg + ", #" + offset + "] \n");
    }

    private void LDURSW(int destReg, int baseAddressReg, int offset, Memory memory)
            throws SegmentFaultException, SPAlignmentException {
        if (baseAddressReg == SP) checkSPAlignment();
        if (destReg == XZR) {
            cpuLog.append("Ignored attempted assignment to XZR. \n");
        } else {
            registerFile[destReg] = memory.loadSignedWord(registerFile[baseAddressReg]+offset);
            cpuLog.append("LDURSW \t X" + destReg + ", [X" + baseAddressReg + ", #" + offset + "] \n");
        }
    }

    private void STURW(int valReg, int baseAddressReg, int offset, Memory memory)
            throws SegmentFaultException, SPAlignmentException {
        if (baseAddressReg == SP) checkSPAlignment();
        memory.storeWord(registerFile[baseAddressReg]+offset, registerFile[valReg]);
        clearExclusiveAccessTag(registerFile[baseAddressReg]+offset, Memory.WORD_SIZE);
        cpuLog.append("STURW \t X" + valReg + ", [X" + baseAddressReg + ", #" + offset + "] \n");
    }

    private void LDURH(int destReg, int baseAddressReg, int offset, Memory memory)
            throws SegmentFaultException, SPAlignmentException {
        if (baseAddressReg == SP) checkSPAlignment();
        if (destReg == XZR) {
            cpuLog.append("Ignored attempted assignment to XZR. \n");
        } else {
            registerFile[destReg] = memory.loadHalfword(registerFile[destReg]+offset);
            cpuLog.append("LDURH \t X" + destReg + ", [X" + baseAddressReg + ", #" + offset + "] \n");
        }
    }

    private void STURH(int valReg, int baseAddressReg, int offset, Memory memory)
            throws SegmentFaultException, SPAlignmentException {
        if (baseAddressReg == SP) checkSPAlignment();
        memory.storeHalfword(registerFile[baseAddressReg]+offset, registerFile[valReg]);
        clearExclusiveAccessTag(registerFile[baseAddressReg]+offset, Memory.HALFWORD_SIZE);
        cpuLog.append("STURH \t X" + valReg + ", [X" + baseAddressReg + ", #" + offset + "] \n");
    }

    private void LDURB(int destReg, int baseAddressReg, int offset, Memory memory)
            throws SegmentFaultException, SPAlignmentException {
        if (baseAddressReg == SP) checkSPAlignment();
        if (destReg == XZR) {
            cpuLog.append("Ignored attempted assignment to XZR. \n");
        } else {
            registerFile[destReg] = memory.loadByte(registerFile[baseAddressReg]+offset);
            cpuLog.append("LDURB \t X" + destReg + ", [X" + baseAddressReg + ", #" + offset + "] \n");
        }
    }

    private void STURB(int valReg, int baseAddressReg, int offset, Memory memory)
            throws SegmentFaultException, SPAlignmentException {
        if (baseAddressReg == SP) checkSPAlignment();
        memory.storeByte(registerFile[baseAddressReg]+offset, registerFile[valReg]);
        clearExclusiveAccessTag(registerFile[baseAddressReg]+offset, Memory.BYTE_SIZE);
        cpuLog.append("STURB \t X" + valReg + ", [X" + baseAddressReg + ", #" + offset + "] \n");
    }

    private void LDXR(int destReg, int baseAddressReg, int offset, Memory memory)
            throws SegmentFaultException, SPAlignmentException {
        if (baseAddressReg == SP) checkSPAlignment();
        long address = registerFile[baseAddressReg] + offset;
        if (destReg == XZR) {
            cpuLog.append("Ignored attempted assignment to XZR. \n");
        } else {
            registerFile[destReg] = memory.loadDoubleword(address);
            taggedAddress = address;
            cpuLog.append("LDXR \t X" + destReg + ", [X" + baseAddressReg + ", #" + offset + "] \n");
        }
    }

    private void STXR(int valReg, int outcomeReg, int baseAddressReg, int offset, Memory memory)
            throws SegmentFaultException, SPAlignmentException {
        if (baseAddressReg == SP) checkSPAlignment();
        long address = registerFile[baseAddressReg] + offset;
        if (taggedAddress == address) {
            memory.storeDoubleword(address, registerFile[valReg]);
            registerFile[outcomeReg] = 0;
            taggedAddress = 0;
            STXRSucceed = true;
        } else {
            registerFile[outcomeReg] = 1;
            STXRSucceed = false;
        }
        cpuLog.append("STXR \t X" + valReg + ", X" + outcomeReg + ", [X" + baseAddressReg + ", #" + offset + "] \n");
    }

    private void MOVZ(int destReg, int immediate, int quadrantShift) {
        if (destReg == XZR) {
            cpuLog.append("Ignored attempted assignment to XZR. \n");
        } else {
            registerFile[destReg] = immediate << quadrantShift;
            cpuLog.append("MOVZ \t X" + destReg + ", #" + immediate + ", LSL #" + quadrantShift + " \n");
        }
    }

    private void MOVK(int destReg, int immediate, int quadrantShift) {
        if (destReg == XZR) {
            cpuLog.append("Ignored attempted assignment to XZR. \n");
        } else {
            registerFile[destReg] = registerFile[destReg] | (immediate << quadrantShift);
            cpuLog.append("MOVK \t X" + destReg + ", #" + immediate + ", LSL #" + quadrantShift + " \n");
        }
    }

    private void CBZ(int conditionReg, int branchIndex) {
        if (registerFile[conditionReg] == 0) {
            instructionIndex = branchIndex;
        }
        cpuLog.append("CBZ \t X" + conditionReg + ", " + "0x" + Long.toHexString(getPC()) + " \n");
        branchTaken = (registerFile[conditionReg] == 0);
    }

    private void CBNZ(int conditionReg, int branchIndex) {
        if (registerFile[conditionReg] != 0) {
            instructionIndex = branchIndex;
        }
        cpuLog.append("CBNZ \t X" + conditionReg + ", " + "0x" + Long.toHexString(getPC()) + " \n");
        branchTaken = (registerFile[conditionReg] != 0);
    }

    private void BEQ(int branchIndex) {
        if (Zflag) {
            instructionIndex = branchIndex;
        }
        cpuLog.append("B.EQ \t" + "0x" + Long.toHexString(getPC()) + " \n");
        branchTaken = (Zflag);
    }

    private void BNE(int branchIndex) {
        if (!Zflag) {
            instructionIndex = branchIndex;
        }
        cpuLog.append("B.NE \t" + "0x" + Long.toHexString(getPC()) + " \n");
        branchTaken = (!Zflag);
    }

    private void BHS(int branchIndex) {
        if (Cflag) {
            instructionIndex = branchIndex;
        }
        cpuLog.append("B.HS \t" + "0x" + Long.toHexString(getPC()) + " \n");
        branchTaken = (Cflag);
    }

    private void BLO(int branchIndex) {
        if (!Cflag) {
            instructionIndex = branchIndex;
        }
        cpuLog.append("B.LO \t" + "0x" + Long.toHexString(getPC()) + " \n");
        branchTaken = (!Cflag);
    }

    private void BHI(int branchIndex) {
        if (!Zflag && Cflag) {
            instructionIndex = branchIndex;
        }
        cpuLog.append("B.HI \t" + "0x" + Long.toHexString(getPC()) + " \n");
        branchTaken = (!Zflag && Cflag);
    }

    private void BLS(int branchIndex) {
        if (!(!Zflag && Cflag)) {
            instructionIndex = branchIndex;
        }
        cpuLog.append("B.LS \t" + "0x" + Long.toHexString(getPC()) + " \n");
        branchTaken = (!(!Zflag && Cflag));
    }

    private void BGE(int branchIndex) {
        if (Nflag == Vflag) {
            instructionIndex = branchIndex;
        }
        cpuLog.append("B.GE \t" + "0x" + Long.toHexString(getPC()) + " \n");
        branchTaken = (Nflag == Vflag);
    }

    private void BLT(int branchIndex) {
        if (Cflag != Vflag) {
            instructionIndex = branchIndex;
        }
        cpuLog.append("B.LT \t" + "0x" + Long.toHexString(getPC()) + " \n");
    }

    private void BGT(int branchIndex) {
        if (!Zflag && Nflag == Vflag) {
            instructionIndex = branchIndex;
        }
        cpuLog.append("B.GT \t" + "0x" + Long.toHexString(getPC()) + " \n");
        branchTaken = (!Zflag && Nflag == Vflag);
    }

    private void BLE(int branchIndex) {
        if (!(!Zflag && Nflag == Vflag)) {
            instructionIndex = branchIndex;
        }
        cpuLog.append("B.LE \t" + "0x" + Long.toHexString(getPC()) + " \n");
        branchTaken = (!(!Zflag && Nflag == Vflag));
    }

    private void BMI(int branchIndex) {
        if (Nflag) {
            instructionIndex = branchIndex;
        }
        cpuLog.append("B.MI \t" + "0x" + Long.toHexString(getPC()) + " \n");
        branchTaken = (Nflag);
    }

    private void BPL(int branchIndex) {
        if (!Nflag) {
            instructionIndex = branchIndex;
        }
        cpuLog.append("B.PL \t" + "0x" + Long.toHexString(getPC()) + " \n");
        branchTaken = (!Nflag);
    }

    private void BVS(int branchIndex) {
        if (Vflag) {
            instructionIndex = branchIndex;
        }
        cpuLog.append("B.VS \t" + "0x" + Long.toHexString(getPC()) + " \n");
        branchTaken = (Vflag);
    }

    private void BVC(int branchIndex) {
        if (!Vflag) {
            instructionIndex = branchIndex;
        }
        cpuLog.append("B.VC \t" + "0x" + Long.toHexString(getPC()) + " \n");
        branchTaken = (!Vflag);
    }

    private void B(int branchIndex) {
        instructionIndex = branchIndex;
        cpuLog.append("B \t" + "0x" + Long.toHexString(getPC()) + " \n");
        branchTaken = true;
    }

    private void BR(int branchReg, Memory memory) throws SegmentFaultException, PCAlignmentException {
        if (registerFile[branchReg]%Memory.WORD_SIZE != 0) {
            throw new PCAlignmentException(registerFile[branchReg]);
        }
        if (registerFile[branchReg] < Memory.TEXT_SEGMENT_OFFSET
                || registerFile[branchReg] > memory.getStaticDataSegmentOffset()-Memory.WORD_SIZE) {
            throw new SegmentFaultException(registerFile[branchReg], "text");
        }
        instructionIndex = (int) (registerFile[branchReg] - Memory.TEXT_SEGMENT_OFFSET) / INSTRUCTION_SIZE;
        cpuLog.append("BR \t X" + "0x" + Long.toHexString(getPC()) + " \n");
    }

    private void BL(int branchIndex) {
        instructionIndex = branchIndex;
        registerFile[LR] = instructionIndex * INSTRUCTION_SIZE + Memory.TEXT_SEGMENT_OFFSET;
        cpuLog.append("BL \t" + "0x" + Long.toHexString(registerFile[LR]) + " \n");
    }

    private void SVC(int imm, Memory memory) throws SegmentFaultException, IOException, EndExecutionException {
        switch (imm) {
            case 0 -> {
                long address = registerFile[X1];
                int maxBytes = (int) registerFile[X2];
                StringBuilder sb = new StringBuilder();

                try {
                    long b;
                    for (int i=0; i<maxBytes; i++) {
                        b = memory.loadByte(address+i);
                        sb.append((char) b);
                    }
                    System.out.print(sb);
                } catch (SegmentFaultException e) {
                    System.err.println("Memory access error during string print: " + e.getMessage());
                    throw(e);
                }
            }
            case 1 -> {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Debug channel listening...");
                String input = scanner.nextLine().replace("\\n", "\n");;
                byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
                for (int i = 0; i < bytes.length; i++) {
                    try {
                        memory.storeByte(registerFile[X1] + i, bytes[i]);
                    } catch (SegmentFaultException e) {
                        System.err.println("Memory access error during string read: " + e.getMessage());
                        throw(e);
                    }
                }
                try {
                    memory.storeByte(registerFile[X1] + bytes.length, 0);
                } catch (SegmentFaultException e) {
                    System.err.println("Memory access error during string read: " + e.getMessage());
                    throw(e);
                }
                registerFile[X2] = bytes.length+1;
                try {
                    memory.storeByte((registerFile[X1]+bytes.length), (byte) 0);
                } catch (SegmentFaultException e) {
                    System.err.println("Memory access error during string read: " + e.getMessage());
                    throw(e);
                }
            }
            case 2 -> {
                long address = registerFile[X1];
                boolean write = registerFile[X2] == 1;
                StringBuilder filenameBuilder = new StringBuilder();

                try {
                    long b;
                    while ((b = memory.loadByte(address)) != 0) {
                        filenameBuilder.append((char) b);
                        address++;
                    }

                    String filename = filenameBuilder.toString();
                    Path path = Path.of(filename);
                    SeekableByteChannel channel = write ? Files.newByteChannel(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE) : Files.newByteChannel(path, StandardOpenOption.READ);
                    int fileId = nextFileId++;
                    openFiles.put(fileId, channel);
                    registerFile[X0] = fileId;
                } catch (Exception e) {
                    System.err.println("Failed to open file: " + e.getMessage());
                    registerFile[X0] = -1;
                    throw(new IOException("Failed to open file: " + e.getMessage()));
                }
            }
            case 3 -> {
                int fileId = (int) registerFile[X1];
                SeekableByteChannel channel = openFiles.remove(fileId);
                try {
                    if (channel != null) channel.close();
                } catch (IOException e) {
                    System.err.println("Failed to close file: " + e.getMessage());
                    throw(new IOException("Failed to close file: " + e.getMessage()));
                }
            }
            case 4 -> {
                int fileId = (int) registerFile[X0];
                long destAddress = registerFile[X1]; // dirección de destino en memoria
                int maxBytes = (int) registerFile[X2]; // cuántos bytes leer

                SeekableByteChannel channel = openFiles.get(fileId);
                if (channel == null) {
                    System.err.println("Invalid file descriptor");
                    registerFile[X0] = -1;
                    break;
                }

                try {
                    ByteBuffer buffer = (maxBytes==-1) ? ByteBuffer.allocate((int)channel.size()) : ByteBuffer.allocate(maxBytes);
                    int bytesRead = channel.read(buffer);
                    buffer.flip();
                    for (int i = 0; i < bytesRead; i++) {
                        memory.storeByte(destAddress + i, buffer.get(i));
                    }
                    memory.storeByte(destAddress+bytesRead, 0);
                    registerFile[X0] = bytesRead+1;
                } catch (Exception e) {
                    System.err.println("File read failed: " + e.getMessage());
                    registerFile[X0] = -1;
                    throw(e);
                }
            }
            case 5 -> {
                int fileId = (int) registerFile[X0];
                long srcAddress = registerFile[X1];
                int byteCount = (int) registerFile[X2];

                SeekableByteChannel channel = openFiles.get(fileId);
                if (channel == null) {
                    System.err.println("Invalid file descriptor for write");
                    registerFile[X0] = -1;
                    break;
                }

                ByteBuffer buffer = ByteBuffer.allocate(byteCount);
                try {
                    for (int i = 0; i < byteCount; i++) {
                        byte b = (byte) memory.loadByte(srcAddress + i);
                        buffer.put(b);
                    }
                    buffer.flip();
                    int bytesWritten = channel.write(buffer);
                    registerFile[X0] = bytesWritten;
                } catch (Exception e) {
                    System.err.println("File write failed: " + e.getMessage());
                    registerFile[X0] = -1;
                    throw(new IOException("File write failed: " + e.getMessage()));
                }
            }
            case 6 -> {
                try {
                    String oldName = readStringFromMemory(registerFile[X1], memory);
                    String newName = readStringFromMemory(registerFile[X2], memory);
                    Files.move(Path.of(oldName), Path.of(newName), StandardCopyOption.REPLACE_EXISTING);
                    registerFile[X0] = 0;
                } catch (Exception e) {
                    System.err.println("File rename failed: " + e.getMessage());
                    registerFile[X0] = -1;
                    throw(e);
                }
            }
            case 7 -> {
                try {
                    String filename = readStringFromMemory(registerFile[X1], memory);
                    Files.delete(Path.of(filename));
                    registerFile[X0] = 0;
                } catch (Exception e) {
                    System.err.println("File deletion failed: " + e.getMessage());
                    registerFile[X0] = -1;
                    throw(e);
                }
            }
            case 8 -> {
                long now = System.currentTimeMillis();
                registerFile[X0] = now - startTime;
            }
            case 9 -> {
                throw new EndExecutionException(this.instructionIndex-1);
            }
            default -> System.err.println("SVC immediate code operation not implemented.");
        }
    }

    private String readStringFromMemory(long address, Memory memory) throws SegmentFaultException {
        StringBuilder sb = new StringBuilder();
        long b;
        while ((b = memory.loadByte(address)) != 0) {
            sb.append((char) b);
            address++;
        }
        return sb.toString();
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }
}
