package com.legv8.simulator.cpu;

/**
 * Thrown when the <code>CPU</code> receives a Supervisor call to shut down.
 *
 * @author Rodrigo Bautista Hernández, 2025
 */
public class EndExecutionException extends Exception {

    /**
     * @param line The line in the code that made the shut down call.
     */
    public EndExecutionException(int line) {
        super();
        this.line = line;
    }

    @Override
    public String getMessage() {
        return "There was a supervisor call to shut down the program on this line.";
    }

    public int getLine() {
        return this.line;
    }

    private int line;
}
