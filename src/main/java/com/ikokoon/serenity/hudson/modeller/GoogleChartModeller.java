package com.ikokoon.serenity.hudson.modeller;

import com.ikokoon.serenity.model.Composite;
import net.sf.json.JSONObject;

import java.util.*;

/**
 * @author Michael Couck
 * @version 1.0
 * @since 11-06-2016
 */
public class GoogleChartModeller implements IModeller {

    private String model;
    private Integer[] buildNumbers;

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public void visit(final Class<?> klass, final Composite<?, ?>... composites) {
        Map<String, Object> data = new HashMap<>();
        @SuppressWarnings("ConfusingArgumentToVarargsMethod")
        List<Map<String, Object>> columns = getColumns(
                new String[]{"id", "label", "type"},
                new String[][]{
                        {"Build", "Build", "string"},
                        {"Coverage", "Coverage", "number"},
                        {"Complexity", "Complexity", "number"},
                        {"Stability", "Stability", "number"},
                        {"Abstractness", "Abstractness", "number"},
                        {"Distance", "Distance", "number"}
                });
        data.put("cols", columns);

        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        JSONObject json = new JSONObject();

        List<Map<String, List<Map<String, Object>>>> rows = getRows(composites);
        data.put("rows", rows);

        json.accumulateAll(data);

        model = json.toString();
    }

    @SuppressWarnings("WeakerAccess")
    List<Map<String, List<Map<String, Object>>>> getRows(final Composite<?, ?>... composites) {
        List<Map<String, List<Map<String, Object>>>> rows = new ArrayList<>();
        int index = 0;
        for (final Composite<?, ?> composite : composites) {
            Map<String, List<Map<String, Object>>> row = new HashMap<>();
            List<Map<String, Object>> values = new ArrayList<>();

            values.add(getValue(buildNumbers[index]));
            values.add(getValue(composite.getCoverage()));
            values.add(getValue(composite.getComplexity()));
            values.add(getValue(composite.getStability() * 100));
            values.add(getValue(composite.getAbstractness() * 100));
            values.add(getValue(composite.getDistance() * 100));

            row.put("c", values);
            rows.add(row);

            index++;
        }

        return rows;
    }

    @SuppressWarnings("WeakerAccess")
    Map<String, Object> getValue(final double metric) {
        Map<String, Object> value = new HashMap<>();
        value.put("v", metric);
        return value;
    }

    @SuppressWarnings("WeakerAccess")
    List<Map<String, Object>> getColumns(final String[] keys, final Object[][] values) {
        List<Map<String, Object>> columns = new ArrayList<>();
        for (final Object[] value : values) {
            Map<String, Object> column = new HashMap<>();
            for (int i = 0; i < keys.length; i++) {
                column.put(keys[i], value[i]);
            }
            columns.add(column);
        }
        return columns;
    }

    public void setBuildNumbers(final Integer... buildNumbers) {
        this.buildNumbers = buildNumbers;
    }
}