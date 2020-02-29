package com.ikokoon.toolkit;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.model.*;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.persistence.DataBaseOdb;
import com.ikokoon.serenity.persistence.DataBaseToolkit;
import com.ikokoon.serenity.persistence.IDataBase;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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

    @SuppressWarnings("unchecked")
    @Test
    public void collectEfferentAndAfferent() {
        DataBaseToolkit.dump(dataBase, new DataBaseToolkit.ICriteria() {
            @SuppressWarnings("rawtypes")
            public boolean satisfied(final Composite<?, ?> composite) {
                return Boolean.TRUE;
            }
        }, "Database dump : ");

        Class class_ = new Class();
        class_.setId(new Random().nextLong());
        dataBase.remove(Class.class, class_.getId());
        class_.setName("io.ronin.packageOne.ClassOne");
        class_.setEfferent(Collections.singletonList(new Efferent("<e:io.ronin.packageTwo>")));

        Class child = new Class();
        child.setId(new Random().nextLong());
        dataBase.remove(Class.class, child.getId());
        child.setName("io.ronin.packageTwo.ClassTwo");
        child.setAfferent(Collections.singletonList(new Afferent("<a:io.ronin.packageOne>")));

        List<Package> packages = new ArrayList<>();
        Package package_ = new Package();
        package_.setChildren(Collections.singletonList(child));
        packages.add(package_);

        DataBaseToolkit.collectEfferentAndAfferent(class_, packages);

        dataBase.remove(Class.class, class_.getId());
        dataBase.remove(Class.class, child.getId());

        // TODO: Verify the result, still looks strange
    }

}