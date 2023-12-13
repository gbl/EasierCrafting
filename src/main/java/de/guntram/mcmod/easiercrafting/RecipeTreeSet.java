package de.guntram.mcmod.easiercrafting;

import java.util.Comparator;
import java.util.TreeSet;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;

public class RecipeTreeSet extends TreeSet<Recipe> {
    public RecipeTreeSet() {
        super(new Comparator<Recipe>() {
            @Override
            public int compare(Recipe a, Recipe b) {
                int sameName = EasierCrafting.recipeDisplayName(a).compareToIgnoreCase(EasierCrafting.recipeDisplayName(b));
                if (a.getType() == RecipeType.STONECUTTING && b.getType() == RecipeType.STONECUTTING) {
                    if (sameName != 0) {
                        return sameName;
                    } else {
                        return ((Ingredient)(a.getIngredients().get(0))).getMatchingStacks()[0].getItem().getName().getString()
                        .compareToIgnoreCase(
                               ((Ingredient)(b.getIngredients().get(0))).getMatchingStacks()[0].getItem().getName().getString()
                        );
                    }
                } else if (a.getType() == BrewingRecipe.recipeType || b.getType() == BrewingRecipe.recipeType) {
                    if (sameName != 0) {
                        return sameName;
                    }
                    if (PotionUtil.getPotion(a.getResult(null)) == PotionUtil.getPotion(b.getResult(null))
                    ||  PotionUtil.getPotionEffects(a.getResult(null)).isEmpty()
                    ||  PotionUtil.getPotionEffects(b.getResult(null)).isEmpty()
                    ) {
                        return 0;
                    }
                    //LOGGER.info("comparing potions seems equal, name="+a.getOutput().getName().getString());
                    //LOGGER.info("first potion is "+PotionUtil.getPotion(a.getOutput()).getName(""));
                    //try { LOGGER.info("First dur. "+PotionUtil.getPotionEffects(a.getOutput()).get(0).getDuration()); } catch (Exception ex) {}
                    //LOGGER.info("secnd potion is "+PotionUtil.getPotion(b.getOutput()).getName(""));
                    //try { LOGGER.info("Secnd dur. "+PotionUtil.getPotionEffects(b.getOutput()).get(0).getDuration()); } catch (Exception ex) {}

                    return PotionUtil.getPotionEffects(a.getResult(null)).get(0).getDuration() - PotionUtil.getPotionEffects(b.getResult(null)).get(0).getDuration();
                } else if (a.getType() == RecipeType.CRAFTING && b.getType() == RecipeType.CRAFTING) {
                    if (sameName != 0) {
                        return sameName;
                    } else {
                        return 0;
                        // return a.getId().compareTo(b.getId());
                    }
                } else {
                    return sameName;
                }
            }
        });
    }
}
