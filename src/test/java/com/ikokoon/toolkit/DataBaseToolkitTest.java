package com.ikokoon.toolkit;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Composite;
import com.ikokoon.serenity.persistence.DataBaseOdb;
import com.ikokoon.serenity.persistence.DataBaseToolkit;
import com.ikokoon.serenity.persistence.IDataBase;
import org.junit.Ignore;
import org.junit.Test;

import static com.ikokoon.serenity.persistence.IDataBase.DataBaseManager.getDataBase;

/**
 * @author Michael Couck
 * @version 1.0
 * @since 12-06-2016
 */
public class DataBaseToolkitTest extends ATest {

    @Test
    @Ignore
    public void dump() {
        IDataBase dataBase = getDataBase(DataBaseOdb.class,
                "/home/laptop/Workspace/serenity/work/jobs/i-discover/builds/31/serenity/serenity.odb", null);
        DataBaseToolkit.dump(dataBase, new DataBaseToolkit.ICriteria() {
            @SuppressWarnings("rawtypes")
            public boolean satisfied(final Composite<?, ?> composite) {
                if (Class.class.isAssignableFrom(composite.getClass())) {
                    return ((Class) composite).getName().equals("discover.connect.DatabaseConnector");
                }
                return Boolean.FALSE;
            }
        }, "Database dump : ");
        dataBase.close();
    }

}
