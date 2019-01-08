/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.easiercrafting;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/**
 *
 * @author gbl
 */
public class InventoryGeneratedRecipe implements IRecipe {
    
    ItemStack result;
    NonNullList<Ingredient> ingredients;

    public InventoryGeneratedRecipe(ItemStack result, ItemStack firstInput, ItemStack... inputs) {
        this.result=result;

        ingredients=NonNullList.create();
        ingredients.add(Ingredient.fromStacks(firstInput));
        for (ItemStack stack:inputs) 
            ingredients.add(Ingredient.fromStacks(stack));
    }

    @Override
    public boolean matches(IInventory ii, World world) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ItemStack getCraftingResult(IInventory inv) {
        return result;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width*height >= ingredients.size();
    }

    @Override
    public ItemStack getRecipeOutput() {
        return result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public ResourceLocation getId() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
