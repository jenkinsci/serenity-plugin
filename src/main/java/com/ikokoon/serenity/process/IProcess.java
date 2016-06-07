package com.ikokoon.serenity.process;

/**
 * @author Michael Couck
 * @version 01.00
 * @see AProcess for more information on the end processing strategy.
 * @since 23.08.09
 */
public interface IProcess {

    /**
     * @see AProcess
     */
    void execute();

    /**
     * @param child the process to execute after the parent
     * @see AProcess
     */
    void setChild(IProcess child);

}
