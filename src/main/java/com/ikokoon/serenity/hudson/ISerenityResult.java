package com.ikokoon.serenity.hudson;

/**
 * This is the interface for the Serenity result that will be presented to the front end via Stapler.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 18.08.09
 */
public interface ISerenityResult {

    int HISTORY = 8;

    String getLastBuildProjectId();

    String getName();

}
