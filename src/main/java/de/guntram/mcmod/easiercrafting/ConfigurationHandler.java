package de.guntram.mcmod.easiercrafting;

import de.guntram.mcmod.fabrictools.ConfigChangedEvent;
import de.guntram.mcmod.fabrictools.Configuration;
import de.guntram.mcmod.fabrictools.ModConfigurationHandler;
import java.io.File;

public class ConfigurationHandler implements ModConfigurationHandler {

    private static ConfigurationHandler instance;

    private Configuration config;
    private String configFileName;
    
    private boolean autoFocusSearch;
    private int autoUpdateRecipeTimer;
    private boolean allowRecipeBook;
    private boolean showGuiRight;
    private boolean allowGeneratedRecipes;
    private int maxEnchantsAllowedForRepair;
    private boolean categorizeRecipes;
    private boolean hideWhenReiShown;

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
    
    @Override
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equalsIgnoreCase(EasierCrafting.MODID)) {
            loadConfig();
        }
    }
    
    private void loadConfig() {
        
        config.migrate("Auto update recipe timer", "easiercrafting.config.autoupdate");
        config.migrate("Auto focus search text", "easiercrafting.config.autofocus");
        config.migrate("Allow MC internal recipe book", "easiercrafting.config.allowinternalbutton");
        config.migrate("Show GUI right of inventory", "easiercrafting.config.guiright");
        config.migrate("Allow special recipes", "easiercrafting.config.specialrecipes");
        config.migrate("Max. enchants", "easiercrafting.config.maxenchants");
        config.migrate("Categorize recipes", "easiercrafting.config.categorize");
        
        autoUpdateRecipeTimer=config.getInt("easiercrafting.config.autoupdate", Configuration.CATEGORY_CLIENT, 5, 0, 30, "easiercrafting.config.tt.autoupdate");
        autoFocusSearch=config.getBoolean("easiercrafting.config.autofocus", Configuration.CATEGORY_CLIENT, false, "easiercrafting.config.tt.autofocus");
        allowRecipeBook=config.getBoolean("easiercrafting.config.allowinternalbutton", Configuration.CATEGORY_CLIENT, true, "easiercrafting.config.tt.allowinternalbutton");
        showGuiRight=config.getBoolean("easiercrafting.config.guiright", Configuration.CATEGORY_CLIENT, true, "easiercrafting.config.tt.guiright");
        allowGeneratedRecipes=config.getBoolean("easiercrafting.config.specialrecipes", Configuration.CATEGORY_CLIENT, true, "easiercrafting.config.tt.specialrecipes");
        maxEnchantsAllowedForRepair=config.getInt("easiercrafting.config.maxenchants", Configuration.CATEGORY_CLIENT, 0, 0, 10, "easiercrafting.config.tt.maxenchants");
        categorizeRecipes=config.getBoolean("easiercrafting.config.categorize", Configuration.CATEGORY_CLIENT, true, "easiercrafting.config.tt.categorize");
        hideWhenReiShown=config.getBoolean("easiercrafting.config.hidewithrei", Configuration.CATEGORY_CLIENT, true, "easiercrafting.config.tt.hidewithrei");
        
        if (config.hasChanged())
            config.save();
    }
    
    @Override
    public Configuration getConfig() {
        return config;
    }

    public static String getConfigFileName() {
        return getInstance().configFileName;
    }
    
    public static int getAutoUpdateRecipeTimer() {
        return getInstance().autoUpdateRecipeTimer;
    }

    public static boolean getAutoFocusSearch() {
        return getInstance().autoFocusSearch;
    }

    public static boolean getAllowMinecraftRecipeBook() {
        return getInstance().allowRecipeBook;
    }
    
    public static boolean getShowGuiRight() {
        return getInstance().showGuiRight;
    }

    public static boolean getAllowGeneratedRecipes() {
        return getInstance().allowGeneratedRecipes;
    }
    
    public static int getMaxEnchantsAllowedForRepair() {
        return getInstance().maxEnchantsAllowedForRepair;
    }
    
    public static boolean getCategorizeRecipes() {
        return getInstance().categorizeRecipes;
    }

    public static boolean hideWhenReiShown() {
        return getInstance().hideWhenReiShown;
    }
}