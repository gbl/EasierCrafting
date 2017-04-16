package de.guntram.mcmod.easiercrafting;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import java.io.File;
import net.minecraftforge.common.config.Configuration;

public class ConfigurationHandler {

    private static ConfigurationHandler instance;

    private Configuration config;
    private String configFileName;
    
    private int autoUpdateRecipeTimer;

    public static ConfigurationHandler getInstance() {
        if (instance==null)
            instance=new ConfigurationHandler();
        return instance;
    }

    public void load(final File configFile) {
        if (config == null) {
            config = new Configuration(configFile);
            configFileName=configFile.getPath();
            loadConfig();
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equalsIgnoreCase(EasierCrafting.MODID)) {
            loadConfig();
        }
    }
    
    private void loadConfig() {
//        allowUpload=config.getBoolean("Allow Upload", Configuration.CATEGORY_CLIENT, allowUpload, "Allow Upload to central database");
//        allowDownload=config.getBoolean("Allow Download", Configuration.CATEGORY_CLIENT, allowDownload, "Allow Download from central database (only if Upload is enabled as well)");
//        saveEveryXMinutes=config.getInt("Save every X minutes", Configuration.CATEGORY_CLIENT, 1, 1, 60, "How often sign data will be saved locally");
//        uploadEveryXMinutes=config.getInt("Upload every X minutes", Configuration.CATEGORY_CLIENT, 5, 5, 60, "How often sign data will be uploaded");
        autoUpdateRecipeTimer=config.getInt("Auto update recipe timer", Configuration.CATEGORY_CLIENT, 5, 0, 300, "Update recipe list after this many seconds after last click");
        
        if (config.hasChanged())
            config.save();
    }
    
    public static Configuration getConfig() {
        return getInstance().config;
    }
    
    public static String getConfigFileName() {
        return getInstance().configFileName;
    }
    
    public static int getAutoUpdateRecipeTimer() {
        return getInstance().autoUpdateRecipeTimer;
    }
}
