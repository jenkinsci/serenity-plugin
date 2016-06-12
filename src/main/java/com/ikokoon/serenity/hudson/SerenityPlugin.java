package com.ikokoon.serenity.hudson;

import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.toolkit.LoggingConfigurator;
import hudson.Extension;
import hudson.Plugin;
import hudson.model.Api;
import hudson.model.Item;
import hudson.model.labels.LabelAtom;
import jenkins.model.Jenkins;
import org.apache.log4j.Logger;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Entry point of a plugin.
 * <p>
 * <p>
 * There must be one class in each plugin. Actually not any more. If there is no plugin in the plugin then Hudson will create one it seems.. See
 * JavaDoc of for more about what can be done on this class.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 25.07.09
 */
@Extension
@ExportedBean
public class SerenityPlugin extends Plugin {

    public Api getApi() {
        return new Api(this);
    }

    /**
     * Constructor initialises the logging and the database.
     */
    public SerenityPlugin() {
        LoggingConfigurator.configure();
        Logger logger = Logger.getLogger(SerenityPlugin.class);
        logger.debug("Loaded plugin : " + this.getClass().getName());
    }

    /**
     * We need to stop all the databases. This releases memory and allows the databases to be committed in the case any objects were modified, which
     * generally they shouldn't be of course.
     */
    @Override
    public void stop() {
        Map<String, IDataBase> dataBases = IDataBase.DataBaseManager.getDataBases();
        IDataBase[] dataBasesArray = dataBases.values().toArray(new IDataBase[dataBases.values().size()]);
        for (IDataBase dataBase : dataBasesArray) {
            dataBase.close();
        }
    }

    @Exported(name = "label")
    public Set<LabelAtom> getLabels() {
        return Jenkins.getInstance().getLabelAtoms();
    }

    @Exported(name = "item")
    public List<Item> getItems() {
        return Jenkins.getInstance().getAllItems();
    }

}
