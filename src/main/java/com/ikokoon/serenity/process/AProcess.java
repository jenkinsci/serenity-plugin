package com.ikokoon.serenity.process;

import java.util.logging.Logger;

/**
 * This class is the process that calls the child processes in the chain. At the end of the coverage processing, i.e. when the unit tests are finished
 * the processes for collecting the dependency and aggregating the data are started. Each process is chained to the next. The order of the processes
 * is somewhat important. The accumulator should run first, then the aggregator then the writer.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 23.08.09
 */
public abstract class AProcess implements IProcess {

    /**
     * The LOGGER for the class.
     */
    protected Logger logger = Logger.getLogger(this.getClass().getName());
    /**
     * The child process in the chain if there is one.
     */
    private IProcess child;

    /**
     * Constructor takes the parent, and the child calls the parent to add it's self to the parent.
     *
     * @param parent the parent of this process
     */
    public AProcess(IProcess parent) {
        if (parent != null) {
            parent.setChild(this);
        }
    }

    /**
     * This is the method that executes the logic for the process. It will be called by the parent after the parent has executed it's own logic.
     */
    public void execute() {
        if (child != null) {
            child.execute();
        }
    }

    /**
     * Sets the child of this process. Typically the child calls the parenht to set it's self as the child, in the constructor
     *
     * @param child the child to set in the parent
     */
    public void setChild(IProcess child) {
        this.child = child;
    }

}
