package com.legv8.simulator.response;

import com.legv8.simulator.cpu.CPU;

/**
 * <code>CPUSnpashot</code> provides a deep copy of the <code>CPU</code> state for use in the pipeline simulator.
 *
 * @see CPU
 *
 * @author Jonathan Wright, 2016
 * @author Rodrigo Bautista Hernández, 2025
 */
public class CPUSnapshot {
    private long[] registerFile = new long[CPU.NUM_REGISTERS];
    private String[] registerNames = new String[]{
            "X0",  "X1",  "X2",  "X3",  "X4",  "X5",  "X6",  "X7",
            "X8",  "X9",  "X10", "X11", "X12", "X13", "X14", "X15",
            "IP0", "IP1", "X18", "X19", "X20", "X21", "X22", "X23",
            "X24", "X25", "X26", "X27" , "SP",  "FP",  "LR", "XZR"
    };
    private boolean Nflag;
    private boolean Zflag;
    private boolean Cflag;
    private boolean Vflag;

    private long startTime;
    private long endTime;

    /**
     * @param cpu	the <code>CPU</code> whose state is to be copied
     */
    public CPUSnapshot(CPU cpu) {
        for (int i=0; i<registerFile.length; i++) {
            registerFile[i] = cpu.getRegister(i);
        }
        Nflag = cpu.getNflag();
        Zflag = cpu.getZflag();
        Cflag = cpu.getCflag();
        Vflag = cpu.getVflag();
        startTime = cpu.getStartTime();
        endTime = cpu.getEndTime();
    }

    /**
     * @param index the register whose value to return, an integer in the range 0-31
     * @return		the value stored in the register <code>index</code>
     */
    public long getRegister(int index) {
        return registerFile[index];
    }

    /**
     * @return	the value of the N flag
     */
    public boolean getNflag() {
        return Nflag;
    }

    /**
     * @return	the value of the Z flag
     */
    public boolean getZflag() {
        return Zflag;
    }

    /**
     * @return	the value of the C flag
     */
    public boolean getCflag() {
        return Cflag;
    }

    /**
     * @return	the value of the V flag
     */
    public boolean getVflag() {
        return Vflag;
    }

    public String[] getRegisterNames() {
        return registerNames;
    }

    public long getTotalMillis() {
        return (endTime - startTime);
    }

    public String getRunTimeString() {
        return "\nRuntime in milliseconds: " + this.getTotalMillis() + "ms";
    }

    @Override
    public String toString() {
        String ret = "Registers:";

        for (int i = 0; i<CPU.NUM_REGISTERS; i++) {
            ret += "\n" + registerNames[i] + " = " + registerFile[i];
        }

        ret += "\nRuntime in milliseconds: " + this.getTotalMillis() + "ms";

        return ret;
    }
}
