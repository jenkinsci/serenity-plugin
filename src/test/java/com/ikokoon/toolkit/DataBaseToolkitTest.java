package com.ikokoon.toolkit;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Composite;
import com.ikokoon.serenity.model.Efferent;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.persistence.DataBaseOdb;
import com.ikokoon.serenity.persistence.DataBaseToolkit;
import com.ikokoon.serenity.persistence.IDataBase;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        // /home/laptop/Workspace/ronin-microservice/serenity/serenity.odb
        // /home/laptop/Workspace/serenity/work/jobs/ronin-microservice/builds/4/./serenity/serenity.odb
        IDataBase dataBase = getDataBase(DataBaseOdb.class,
                "/home/laptop/Workspace/ronin-microservice/serenity/serenity.odb", null);
        DataBaseToolkit.dump(dataBase, new DataBaseToolkit.ICriteria() {
            @SuppressWarnings("rawtypes")
            public boolean satisfied(final Composite<?, ?> composite) {
                /*if (Class.class.isAssignableFrom(composite.getClass())) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;*/
                return Boolean.TRUE;
            }
        }, "Database dump : ");
        dataBase.close();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void collectEfferentAndAfferent() {
        Class source = new Class();
        source.setName("io.ronin.packageOne.ClassOne");
        source.setId(Toolkit.hash(source.getName()));

        Class target = new Class();
        target.setName("io.ronin.packageTwo.ClassTwo");
        target.setEfferent(Collections.singletonList(new Efferent("<e:io.ronin.packageOne>")));
        target.setId(Toolkit.hash(target.getName()));

        List<Package> targetPackages = new ArrayList<>();
        Package targetPackage = new Package();
        targetPackage.setChildren(Collections.singletonList(target));
        targetPackages.add(targetPackage);

        dataBase.persist(source);
        dataBase.persist(target);

        DataBaseToolkit.collectEfferentAndAfferent(source, targetPackages);

        /*DataBaseToolkit.dump(dataBase, new DataBaseToolkit.ICriteria() {
            public boolean satisfied(final Composite<?, ?> composite) {
                return Boolean.TRUE;
            }
        }, "Database dump : ");*/

        Class sourceWithEfferent = dataBase.find(Class.class, source.getId());
        // We expect that the efferent item in the target class has created an afferent item in 'our' class
        Assert.assertEquals(1, sourceWithEfferent.getEfferent().size());
    }

}