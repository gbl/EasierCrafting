package de.guntram.mcmod.easiercrafting;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class BrewingRecipe<C extends Inventory> implements Recipe<C> {

    private final ItemStack inputPotion, ingredient, outputPotion;
    private final boolean isPotionRecipe;
    public final static RecipeType recipeType = RecipeType.register("easiercrafting:brewing_recipe");

    public BrewingRecipe(boolean isPotionRecipe, ItemStack inputPotion, ItemStack ingredient, ItemStack outputPotion) {
        this.isPotionRecipe = isPotionRecipe;
        this.inputPotion = inputPotion;
        this.ingredient = ingredient;
        this.outputPotion = outputPotion;
    }

    @Override
    public boolean matches(C inv, World world) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ItemStack craft(C inv) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getOutput() {
        return outputPotion;
    }

    @Override
    public DefaultedList<Ingredient> getPreviewInputs() {
        DefaultedList<Ingredient> ingredients = DefaultedList.of();
        ingredients.add(Ingredient.ofStacks(inputPotion));
        ingredients.add(Ingredient.ofStacks(ingredient));
        return ingredients;
    }

    @Override
    public Identifier getId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public RecipeType<?> getType() {
        return recipeType;
    }

    public String getCategory() {
        return isPotionRecipe ? "Effect" : "Improvement";
    }

    public boolean isPotionRecipe() {
        return isPotionRecipe;
    }

    public boolean isItemRecipe() {
        return !isPotionRecipe;
    }
}
