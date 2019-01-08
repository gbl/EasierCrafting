package de.guntram.mcmod.easiercrafting;

import java.io.File;

public class ConfigurationHandler {

    private static ConfigurationHandler instance;

    private String configFileName;
    
    private boolean autoFocusSearch;
    private int autoUpdateRecipeTimer;
    private boolean allowRecipeBook;
    private boolean showGuiRight;
    private boolean allowGeneratedRecipes;
    private int maxEnchantsAllowedForRepair;

    public static ConfigurationHandler getInstance() {
        if (instance==null)
            instance=new ConfigurationHandler();
        return instance;
    }
    

    public void load(final File configFile) {
        loadConfig();
    }

    private void loadConfig() {
        autoUpdateRecipeTimer=5;
        autoFocusSearch=false;
        allowRecipeBook=true;
        showGuiRight=true;
        allowGeneratedRecipes=true;
        maxEnchantsAllowedForRepair=1;
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
}
