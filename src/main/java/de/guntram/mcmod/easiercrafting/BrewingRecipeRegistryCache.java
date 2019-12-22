package de.guntram.mcmod.easiercrafting;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class BrewingRecipeRegistryCache {

    private static final Logger LOGGER = LogManager.getLogger(BrewingRecipeRegistryCache.class);
    private static List<BrewingRecipe> recipes = new ArrayList<>();

    public static void add(BrewingRecipe recipe) {
        recipes.add(recipe);
    }

    static List<BrewingRecipe> findBrewingRecipesFromRegistry() {
        return recipes;
    }
}
