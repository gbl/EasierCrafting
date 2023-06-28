package de.guntram.mcmod.easiercrafting;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
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
    public ItemStack craft(C inv, DynamicRegistryManager registryManager) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        return outputPotion;
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        DefaultedList<Ingredient> ingredients = DefaultedList.of();
        ingredients.add(Ingredient.ofStacks(inputPotion));
        ingredients.add(Ingredient.ofStacks(ingredient));
        return ingredients;
    }
    
    public ItemStack getInputPotion() {
        return inputPotion;
    }
    
    public ItemStack getIngredient() {
        return ingredient;
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
        return I18n.translate(isPotionRecipe ? "easiercrafting.brewingcategory.effect" : "easiercrafting.brewingcategory.improvement");
    }

    public boolean isPotionRecipe() {
        return isPotionRecipe;
    }

    public boolean isItemRecipe() {
        return !isPotionRecipe;
    }
    
    @Override
    public String toString () {
        StringBuilder result=new StringBuilder();
        result.append(getCategory())
                .append(": input ")
                .append(inputPotion.getItem().getName().getString()) . append("/")
                .append(PotionUtil.getPotion(inputPotion).finishTranslationKey(""))
                .append(" with ingredient ")
                .append(ingredient.getItem().getName().getString())
                .append(" yields ")
                .append(outputPotion.getItem().getName().getString()) . append("/")
                .append(PotionUtil.getPotion(outputPotion).finishTranslationKey(""))
                ;
        return result.toString();
    }
}
