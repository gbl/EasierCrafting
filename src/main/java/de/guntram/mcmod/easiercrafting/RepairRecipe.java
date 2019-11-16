/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

/**
 *
 * @author gbl
 */
public class RepairRecipe<C  extends Inventory> implements Recipe<C> {
    
    private Item item;

    public RepairRecipe(Item item) {
        this.item=item;
    }
    
    public Item getItem() {
        return item;
    }
    
    @Override
    public boolean matches(C inv, World worldIn) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ItemStack craft(C inv) {
        return new ItemStack(item);
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }
    
    @Override
    public ItemStack getOutput() {
        return new ItemStack(item);
    }

    @Override
    public DefaultedList<Ingredient> getPreviewInputs() {
        DefaultedList<Ingredient> ingredients = DefaultedList.of();
        ingredients.add(Ingredient.ofStacks(new ItemStack(item)));
        ingredients.add(Ingredient.ofStacks(new ItemStack(item)));
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
