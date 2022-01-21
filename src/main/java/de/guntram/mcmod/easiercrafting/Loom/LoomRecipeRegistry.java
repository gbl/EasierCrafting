/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.easiercrafting.Loom;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gbl
 */
public class LoomRecipeRegistry {

    private static LoomRecipeRegistry instance;
    private static Logger LOGGER;

    private TreeMap<String, LoomRecipe> recipes;
    
    private LoomRecipeRegistry() {
        recipes = new TreeMap<>();
        LOGGER = LoggerFactory.getLogger(LoomRecipeRegistry.class);
    }
    
    public static LoomRecipeRegistry getInstance() {
        if (instance == null) {
            instance = new LoomRecipeRegistry();
        }
        return instance;
    }
    
    public static String getRecipeCollectionPath() {
        File dir = new File("config"); dir.mkdirs();
        dir = new File(dir, "loomrecipes"); dir.mkdirs();
        return dir.getAbsolutePath();
    }
    
    public static void registerRecipe(LoomRecipe recipe) {
        getInstance().recipes.put(recipe.name, recipe);
    }
    
    public static void loadRecipeFile(String path) throws IOException, IllegalArgumentException {
        String content = new String(Files.readAllBytes(Paths.get(path)));
        LoomRecipe recipe = LoomRecipe.fromSaveString(content);
        registerRecipe(recipe);
    }
    
    public static void loadRecipeCollection(String path) {
        for (String file: new File(path).list((dir, name) -> { return name.endsWith(".lr"); } )) {
            try {
                loadRecipeFile(path + File.separatorChar + file);
                LOGGER.info(file + " loaded");
            } catch (IOException | IllegalArgumentException ex) {
                LOGGER.warn("file : "+ex);
            }
        }
    }
    
    public static SortedSet<String> getKnownRecipeNames() {
        return (SortedSet<String>) getInstance().recipes.keySet();
    }
    
    public static Collection<LoomRecipe> getAllRecipes() {
        return getInstance().recipes.values();
    }
}
