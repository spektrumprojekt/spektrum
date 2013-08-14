package de.spektrumprojekt.commons.computer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Aggregates several computers that will be invoked in sequence. If the execution of one computer
 * fails the computation will be stopped.
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class ComputerAggregation implements Computer {

    private final List<Computer> computers;

    /**
     * 
     * @param computers
     *            the computers in order to invoke in sequence.
     */
    public ComputerAggregation(Computer... computers) {
        this(Arrays.asList(computers));
    }

    /**
     * 
     * @param computers
     *            the computers in order to invoke in sequence.
     */
    public ComputerAggregation(List<Computer> computers) {

        List<Computer> me = new ArrayList<Computer>(computers.size());
        me.addAll(computers);
        this.computers = Collections.unmodifiableList(me);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());
        sb.append(" computers=");
        String prefix = "[";
        for (Computer computer : computers) {
            sb.append(prefix + computer.getConfigurationDescription());
            prefix = ", ";
        }
        sb.append("]");

        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() throws Exception {
        for (Computer computer : computers) {
            computer.run();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ComputerAggregation [computers=");
        builder.append(computers);
        builder.append("]");
        return builder.toString();
    }

}
