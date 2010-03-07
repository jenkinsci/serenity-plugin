package com.ikokoon.serenity.hudson.modeller;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Composite;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.model.Project;
import com.ikokoon.toolkit.Toolkit;

/**
 * This class takes the composites and generates the data arrays for the Highcharts model, and makes a big string that can be inserted into the
 * Highcharts object on the front end.
 *
 * @author Michael Couck
 * @since 01.01.10
 * @version 01.00
 */
public class HighchartsModeller implements IModeller {

    private Logger logger = Logger.getLogger(this.getClass());
    private String model;
    private String modelName;
    private Integer[] buildNumbers;
    private Map<String, IConstructor> constructors = new HashMap<String, IConstructor>();

    /**
     * Constructor takes the name of the model file, either the floating box model which is smaller or the graph model.
     *
     * @param modelName
     *            the name of the model file
     * @param buildNumbers
     *            the numbers that should be on the x-axis
     */
    public HighchartsModeller(String modelName, Integer... buildNumbers) {
        this.modelName = modelName;
        this.buildNumbers = buildNumbers;
        addConstructors();
    }

    /**
     * {@inheritDoc}
     */
    public String getModel() {
        return model;
    }

    /**
     * Visits the composites and generates a model string for the Highcharts javaScript graph.<br>
     *
     * coverageData = [49.9, 71.5, 106.4, 129.2, 144.0, 176.0, 135.6, 148.5, 216.4, 194.1, 95.6, 54.4]<br>
     * complexityData - [1016, 1016, 1015.9, 1015.5, 1012.3, 1009.5, 1009.6, 1010.2, 1013.1, 1016.9, 1018.2, 1016.7]<br>
     * stabilityData - [7.0, 6.9, 9.5, 14.5, 18.2, 21.5, 25.2, 26.5, 23.3, 18.3, 13.9, 9.6]<br>
     * categoryData - ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12']<br>
     */
    public void visit(java.lang.Class<?> klass, Composite<?, ?>... composites) {
        InputStream inputStream = null;
        if (modelName != null) {
            inputStream = this.getClass().getResourceAsStream(modelName);
        } else {
        	inputStream = this.getClass().getResourceAsStream(klass.getSimpleName());
        }
        model = Toolkit.getContents(inputStream).toString();
        String compositeName = getName(composites);
        model = Toolkit.replaceAll(model, "compositeName", compositeName);
        for (String dataName : constructors.keySet()) {
            String data = getData(constructors.get(dataName), composites);
            logger.debug("Data : " + data);
            model = Toolkit.replaceAll(model, dataName, data);
        }
    }

    @SuppressWarnings("unchecked")
	private String getName(Composite<?, ?>... composites) {
        for (Composite<?, ?> composite : composites) {
            if (composite instanceof Project) {
                return ((Project<?, ?>) composite).getName();
            } else if (composite instanceof Package) {
                return ((Package<?, ?>) composite).getName();
            } else if (composite instanceof Class) {
                return ((Class<?, ?>) composite).getName();
            } else {
                return "What am I";
            }
        }
        return "";
    }

    public interface IConstructor {
        public void construct(StringBuilder builder, Composite<?, ?> composite);
    }

    private String getData(IConstructor constructor, Composite<?, ?>... composites) {
        StringBuilder builder = new StringBuilder("[0.0,");
        for (int i = 0; i < composites.length; i++) {
            Composite<?, ?> composite = composites[i];
            constructor.construct(builder, composite);
            if (i + 1 < composites.length) {
                builder.append(",");
            }
        }
        builder.append("]");
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    private void addConstructors() {
        constructors.put("coverageData", new IConstructor() {
            public void construct(StringBuilder builder, Composite<?, ?> composite) {
                if (composite instanceof Class) {
                    builder.append(((Class<?, ?>) composite).getCoverage());
                } else if (composite instanceof Package) {
                    builder.append(((Package<?, ?>) composite).getCoverage());
                } else if (composite instanceof Project) {
                    builder.append(((Project<?, ?>) composite).getCoverage());
                }
            }
        });
        constructors.put("complexityData", new IConstructor() {
            public void construct(StringBuilder builder, Composite<?, ?> composite) {
                if (composite instanceof Class) {
                    builder.append(((Class<?, ?>) composite).getComplexity());
                } else if (composite instanceof Package) {
                    builder.append(((Package<?, ?>) composite).getComplexity());
                } else if (composite instanceof Project) {
                    builder.append(((Project<?, ?>) composite).getComplexity());
                }
            }
        });
        constructors.put("stabilityData", new IConstructor() {
            public void construct(StringBuilder builder, Composite<?, ?> composite) {
                if (composite instanceof Class) {
                    builder.append(((Class<?, ?>) composite).getStability());
                } else if (composite instanceof Package) {
                    builder.append(((Package<?, ?>) composite).getStability());
                } else if (composite instanceof Project) {
                    builder.append(((Project<?, ?>) composite).getStability());
                }
            }
        });
        constructors.put("abstractnessData", new IConstructor() {
            public void construct(StringBuilder builder, Composite<?, ?> composite) {
                if (composite instanceof Package) {
                    builder.append(((Package<?, ?>) composite).getAbstractness());
                } else if (composite instanceof Project) {
                    builder.append(((Project<?, ?>) composite).getAbstractness());
                } else {
                    builder.append("0.0");
                }
            }
        });
        constructors.put("distanceData", new IConstructor() {
            public void construct(StringBuilder builder, Composite<?, ?> composite) {
                if (composite instanceof Package) {
                    builder.append(((Package<?, ?>) composite).getDistance());
                } else if (composite instanceof Project) {
                    builder.append(((Project<?, ?>) composite).getDistance());
                } else {
                    builder.append("0.0");
                }
            }
        });
        constructors.put("categoryData", new IConstructor() {

            int i = 0;

            public void construct(StringBuilder builder, Composite<?, ?> composite) {
                builder.append("'");
                builder.append(buildNumbers[i++]);
                builder.append("'");
            }
        });
        constructors.put("interfacesData", new IConstructor() {
            public void construct(StringBuilder builder, Composite<?, ?> composite) {
                if (composite instanceof Package) {
                    builder.append(((Package<?, ?>) composite).getInterfaces());
                } else {
                    builder.append("0.0");
                }
            }
        });
        constructors.put("implementationsData", new IConstructor() {
            public void construct(StringBuilder builder, Composite<?, ?> composite) {
                if (composite instanceof Package) {
                    builder.append(((Package<?, ?>) composite).getImplementations());
                } else {
                    builder.append("0.0");
                }
            }
        });
        constructors.put("linesData", new IConstructor() {
            public void construct(StringBuilder builder, Composite<?, ?> composite) {
                if (composite instanceof Package) {
                    builder.append(((Package<?, ?>) composite).getLines());
                } else {
                    builder.append("0.0");
                }
            }
        });
    }

}
