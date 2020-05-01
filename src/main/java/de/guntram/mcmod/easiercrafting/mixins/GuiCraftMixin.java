package de.guntram.mcmod.easiercrafting.mixins;

import de.guntram.mcmod.easiercrafting.ExtendedGuiBrewingStand;
import de.guntram.mcmod.easiercrafting.ExtendedGuiCrafting;
import de.guntram.mcmod.easiercrafting.ExtendedGuiLoom;
import de.guntram.mcmod.easiercrafting.ExtendedGuiStonecutter;
import de.guntram.mcmod.easiercrafting.RecipeBook;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screens;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.container.*;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screens.class)
public class GuiCraftMixin {
    
    @Inject(method="open", at=@At("HEAD"), cancellable = true)
    private static void checkCraftScreen(ContainerType type, MinecraftClient client,
            int any, Text component, CallbackInfo ci) {

        Container container;
        AbstractContainerScreen screen;
        if (type == ContainerType.CRAFTING) {
            container = ContainerType.CRAFTING.create(any, client.player.inventory);
            screen = new ExtendedGuiCrafting((CraftingTableContainer) container, client.player.inventory, component);
            ((ExtendedGuiCrafting)screen).setRecipeBook(new RecipeBook(screen, 1, 3, 0, 10));
        } else if (type == ContainerType.STONECUTTER) {
            container = ContainerType.STONECUTTER.create(any, client.player.inventory);
            screen = new ExtendedGuiStonecutter((StonecutterContainer) container, client.player.inventory, component);
            ((ExtendedGuiStonecutter)screen).setRecipeBook(new RecipeBook(screen, 0, 1, 1, 2));
        } else if (type == ContainerType.BREWING_STAND) {
            container=ContainerType.BREWING_STAND.create(any, client.player.inventory);
            screen = new ExtendedGuiBrewingStand((BrewingStandContainer) container, client.player.inventory, component);
            ((ExtendedGuiBrewingStand)screen).setRecipeBook(new RecipeBook(screen, 0, 0, 0, 5));
        } else if (type == ContainerType.LOOM) {
            container=ContainerType.LOOM.create(any, client.player.inventory);
            screen = new ExtendedGuiLoom((LoomContainer) container, client.player.inventory, component);
        } else {
            return;
        }
        client.player.container = container;
        client.openScreen(screen);
        ci.cancel();
    }
}
