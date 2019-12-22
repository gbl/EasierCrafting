package de.guntram.mcmod.easiercrafting;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class BrewingRecipe<C extends Inventory> implements Recipe<C> {

    private ItemStack inputPotion, ingredient, outputPotion;
    public final static RecipeType recipeType = RecipeType.register("easiercrafting:brewing_recipe");

    public BrewingRecipe(ItemStack inputPotion, ItemStack ingredient, ItemStack outputPotion) {
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
}
