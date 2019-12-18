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
        autoUpdateRecipeTimer=config.getInt("Auto update recipe timer", Configuration.CATEGORY_CLIENT, 5, 0, 30, "Update recipe list after this many seconds after last click");
        autoFocusSearch=config.getBoolean("Auto focus search text", Configuration.CATEGORY_CLIENT, false, "Automatically focus the search box when opening craft GUI");
        allowRecipeBook=config.getBoolean("Allow MC internal recipe book", Configuration.CATEGORY_CLIENT, true, "Allow opening the MC internal recipe book (since 1.12)");
        showGuiRight=config.getBoolean("Show GUI right of inventory", Configuration.CATEGORY_CLIENT, true, "Show the GUI right of the inventory, where it could conflict with Just Enough Items, instead of left, where it conflicts with active buffs");
        allowGeneratedRecipes=config.getBoolean("Allow special recipes", Configuration.CATEGORY_CLIENT, true, "Add Shulker box coloring, tipped arrows, fireworks, repairs to the craftable list");
        maxEnchantsAllowedForRepair=config.getInt("Max. enchants", Configuration.CATEGORY_CLIENT, 0, 0, 10, "Don't consider items for workbench repair if they have more than this number of enchants");
        categorizeRecipes=config.getBoolean("Categorize recipes", Configuration.CATEGORY_CLIENT, true, "Categorize recipes by their creative mode tab");
        
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
}