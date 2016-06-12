package com.ikokoon.serenity.hudson.modeller;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.model.Composite;
import org.junit.Test;
import org.mockito.Spy;

/**
 * @author Michael Couck
 * @version 1.0
 * @since 11-06-2016
 */
public class GoogleChartModellerTest extends ATest {

    @Spy
    private GoogleChartModeller googleChartModeller;

    @Test
    public void visit() {
        googleChartModeller.setBuildNumbers(1, 2, 3);
        Composite<?, ?>[] composites = {getPackage(), getPackage(), getPackage()};
        googleChartModeller.visit(Package.class, composites);
        String model = googleChartModeller.getModel();
        // TODO: Complete me...
    }

}
