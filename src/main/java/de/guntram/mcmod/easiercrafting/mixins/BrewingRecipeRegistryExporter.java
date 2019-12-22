package de.guntram.mcmod.easiercrafting.mixins;

import de.guntram.mcmod.easiercrafting.BrewingRecipe;
import de.guntram.mcmod.easiercrafting.BrewingRecipeRegistryCache;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.BrewingRecipeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(BrewingRecipeRegistry.class)
public class BrewingRecipeRegistryExporter {

    @Inject(method = "registerItemRecipe", at = @At("HEAD"))
    private static void addItemRecipe(Item input, Item ingredient, Item output, CallbackInfo ci) {
        BrewingRecipeRegistryCache.add(new BrewingRecipe(new ItemStack(input), new ItemStack(ingredient), new ItemStack(output)));
    }

    @Inject(method = "registerPotionRecipe", at=@At("HEAD"))
    private static void addPotionRecipe(Potion input, Item ingredient, Potion output, CallbackInfo ci) {
        BrewingRecipeRegistryCache.add(new BrewingRecipe(PotionUtil.setPotion(new ItemStack(Items.POTION), input),
                                                new ItemStack(ingredient),
                                                PotionUtil.setPotion(new ItemStack(Items.POTION), output)));
    }
}
