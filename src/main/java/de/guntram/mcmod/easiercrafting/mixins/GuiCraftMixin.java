package de.guntram.mcmod.easiercrafting.mixins;

import de.guntram.mcmod.easiercrafting.ExtendedGuiCrafting;
import de.guntram.mcmod.easiercrafting.ExtendedGuiStonecutter;
import de.guntram.mcmod.easiercrafting.RecipeBook;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screens;
import net.minecraft.container.ContainerType;
import net.minecraft.container.CraftingTableContainer;
import net.minecraft.container.StonecutterContainer;

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
        if (type == ContainerType.CRAFTING) {
            CraftingTableContainer container = ContainerType.CRAFTING.create(any, client.player.inventory);
            ExtendedGuiCrafting screen = new ExtendedGuiCrafting(container, client.player.inventory, component);
            screen.setRecipeBook(new RecipeBook(screen, 1, 3, 0, 10));
            client.player.container = container;
            client.openScreen(screen);
            ci.cancel();
        } else if (type == ContainerType.STONECUTTER) {
            StonecutterContainer container = ContainerType.STONECUTTER.create(any, client.player.inventory);
            ExtendedGuiStonecutter screen = new ExtendedGuiStonecutter(container, client.player.inventory, component);
            screen.setRecipeBook(new RecipeBook(screen, 0, 1, 1, 2));
            client.player.container = container;
            client.openScreen(screen);
            ci.cancel();
        }
    }
}
