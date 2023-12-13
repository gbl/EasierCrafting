/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.easiercrafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author gbl
 */
public class InventoryRecipeScanner {
    
    private static final Logger LOGGER = LogManager.getLogger();
    
    static List<Recipe<?>> findUnusualRecipes(ScreenHandler inventory, int firstInventorySlotNo) {
        ArrayList<Recipe<?>> result=new ArrayList<>();

        // needed for various recipes; count the number of each dye type
        Map<DyeColor, Integer> hasDye = new HashMap<>();
        
        boolean hasShulkerBox=false;
        ItemStack shulkerBoxItemStack=null;
        boolean hasDyeableBanner=false;
        boolean hasCopyableBanner=false;
        boolean hasHelmet=false, hasChest=false, hasPants=false, hasBoots=false;        // leather!
        Map<Potion, Integer> availablePotions=new HashMap<>();
        int     availableArrows=0;
        boolean hasPaper=false;
        int     availableGunPowder=0;
        boolean hasWrittenBook=false;
        boolean hasWritableBook=false;
        boolean hasFilledMap=false;
        boolean hasMap=false;
        Map<Item, Integer> hasRepairable = new HashMap<>();
        
        for (int i=0; i<36; i++) {
            Slot invitem=inventory.getSlot(i+firstInventorySlotNo);
            ItemStack stack=invitem.getStack();
            Item item = stack.getItem();

            if (item.isDamageable()&& stack.isDamaged()) {
                NbtList enchantments = stack.getEnchantments();
                if (enchantments.size() <= ConfigurationHandler.getMaxEnchantsAllowedForRepair()) {
                    Integer previous=hasRepairable.get(item);
                    hasRepairable.put(item, previous == null ? 1 : previous+1);
                }
            }
            else if (item == Items.LINGERING_POTION) {
                availablePotions.put(PotionUtil.getPotion(stack), i);
            }
            else if (stack.hasEnchantments()) {
                continue;
            }
            else if (item instanceof DyeItem) {
                DyeColor color=((DyeItem)item).getColor();
                if (hasDye.containsKey(color)) {
                    hasDye.put(color, hasDye.get(color)+stack.getCount());
                } else {
                    hasDye.put(color, stack.getCount());
                }
            }
            else if (Block.getBlockFromItem(item) instanceof ShulkerBoxBlock) {
                hasShulkerBox=true;
                shulkerBoxItemStack=stack;
            }
            /* TODO 
            else if (item == Items.BANNER) {
                int patterns=TileEntityBanner.getPatterns(stack);
                if (patterns<6)
                    hasDyeableBanner=true;
                if (patterns>0)
                    hasCopyableBanner=true;
            }
            */
            else if (item == Items.LEATHER_HELMET) {
                hasHelmet=true;
            }
            else if (item == Items.LEATHER_CHESTPLATE) {
                hasChest=true;
            }
            else if (item == Items.LEATHER_LEGGINGS) {
                hasPants=true;
            }
            else if (item == Items.LEATHER_BOOTS) {
                hasBoots=true;
            }
            else if (item == Items.ARROW) {
                availableArrows+=stack.getCount();
            }
            else if (item == Items.GUNPOWDER) {
                availableGunPowder+=stack.getCount();
            }
            else if (item == Items.PAPER) {
                hasPaper=true;
            }
            else if (item == Items.WRITTEN_BOOK) {
                hasWrittenBook=true;
            }
            else if (item == Items.WRITABLE_BOOK) {
                hasWritableBook=true;
            }
            else if (item == Items.FILLED_MAP) {
                hasFilledMap=true;
            }
            else if (item == Items.MAP) {
                hasMap=true;
            }
        }
        
        if (hasShulkerBox) {
            for (DyeColor dye: hasDye.keySet()) {
                Block resultingBox=ShulkerBoxBlock.get(dye);
                result.add(new InventoryGeneratedRecipe(
                        new ItemStack(resultingBox, 1),
                        new ItemStack(DyeItem.byColor(dye)),
                        shulkerBoxItemStack
                ));
            }
        }

        // do not implement banners at the moment
        // do not implement colored leather at the moment

        //System.out.println("Arrows: "+availableArrows);
        if (availableArrows >= 8) {
            for (Potion type: availablePotions.keySet()) {
                //System.out.println("Potion "+type.getNamePrefixed("?")+" at "+availablePotions.get(type));
                ItemStack potion = inventory.getSlot(firstInventorySlotNo+availablePotions.get(type)).getStack();
                ItemStack resultArrow = new ItemStack(Items.TIPPED_ARROW, 8);
                PotionUtil.setPotion(resultArrow, PotionUtil.getPotion(potion));
                PotionUtil.setCustomPotionEffects(resultArrow, PotionUtil.getPotionEffects(potion));

                result.add(new InventoryGeneratedRecipe(
                        resultArrow,              // result
                        new ItemStack(Items.ARROW, 1), new ItemStack(Items.ARROW, 1), new ItemStack(Items.ARROW, 1), new ItemStack(Items.ARROW, 1),
                        inventory.getSlot(firstInventorySlotNo+availablePotions.get(type)).getStack(),
                        new ItemStack(Items.ARROW, 1), new ItemStack(Items.ARROW, 1), new ItemStack(Items.ARROW, 1), new ItemStack(Items.ARROW, 1)
                ));
            }
        }

        //System.out.println("Paper: "+hasPaper);
        //System.out.println("Gunpowder: "+availableGunPowder);

        if (hasPaper && availableGunPowder > 0) {
            ItemStack paper=new ItemStack(Items.PAPER);

            for (int power=1; power<=3; power++) {
                if (availableGunPowder>=power) {
                    ItemStack resultItem = new ItemStack(Items.FIREWORK_ROCKET, 3);
                    NbtCompound nbttagcompound = resultItem.getOrCreateSubNbt("Fireworks");
                    nbttagcompound.putByte("Flight", (byte)power);
                    resultItem.setCustomName(Text.literal("Strength "+power));

                    ItemStack[] gunPowder = new ItemStack[power];
                    for (int k=0; k<power; k++)
                        gunPowder[k]=new ItemStack(Items.GUNPOWDER);
                    //System.out.println("adding fireworks with "+power+" powder");
                    result.add(new InventoryGeneratedRecipe(resultItem, paper, gunPowder));
                }
            }
        }
        
        for (Item item:hasRepairable.keySet()) {
            LOGGER.debug("repairable "+item.getTranslationKey()+": "+hasRepairable.get(item));
            if (hasRepairable.get(item)>=2)
                result.add(new RepairRecipe(item));
        }

        //System.out.println("returning "+result.size()+" custom recipes");
        return result;
    }
}
