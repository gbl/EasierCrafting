/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.easiercrafting;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

/**
 *
 * @author gbl
 */
public class InventoryGeneratedRecipe<C extends Inventory> implements Recipe<C> {
    
    ItemStack result;
    DefaultedList<Ingredient> ingredients;

    public InventoryGeneratedRecipe(ItemStack result, ItemStack firstInput, ItemStack... inputs) {
        this.result=result;

        ingredients=DefaultedList.of();
        ingredients.add(Ingredient.ofStacks(firstInput));
        for (ItemStack stack:inputs) 
            ingredients.add(Ingredient.ofStacks(stack));
    }

    @Override
    public boolean matches(C ii, World world) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ItemStack craft(C inv) {
        return result;
    }

    @Override
    public boolean fits(int width, int height) {
        return width*height >= ingredients.size();
    }

    @Override
    public ItemStack getOutput() {
        return result;
    }

    @Override
    public DefaultedList<Ingredient> getPreviewInputs() {
        return ingredients;
    }

    @Override
    public Identifier getId() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }
}
