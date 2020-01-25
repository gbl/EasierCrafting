package de.guntram.mcmod.easiercrafting;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BrewingRecipeRegistryCache {

    private static final Logger LOGGER = LogManager.getLogger(BrewingRecipeRegistryCache.class);
    private static List<BrewingRecipe> recipes = new ArrayList<>();

    public static void add(BrewingRecipe recipe) {
        recipes.add(recipe);
    }

    static List<BrewingRecipe> registeredBrewingRecipes() {
        return recipes;
    }
    
    static List<BrewingRecipe> registeredPotionRecipes() {
        return recipes.stream().filter(r -> r.isPotionRecipe()).collect(Collectors.toList());
    }

    static List<BrewingRecipe> registeredItemRecipes() {
        return recipes.stream().filter(r -> r.isItemRecipe()).collect(Collectors.toList());
    }
}
