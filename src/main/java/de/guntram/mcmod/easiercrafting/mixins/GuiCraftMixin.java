package de.guntram.mcmod.easiercrafting.mixins;

import de.guntram.mcmod.easiercrafting.ExtendedGuiBrewingStand;
import de.guntram.mcmod.easiercrafting.ExtendedGuiCrafting;
import de.guntram.mcmod.easiercrafting.ExtendedGuiStonecutter;
import de.guntram.mcmod.easiercrafting.Loom.ExtendedGuiLoom;
import de.guntram.mcmod.easiercrafting.Loom.LoomRecipeBook;
import de.guntram.mcmod.easiercrafting.RecipeBook;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.LoomScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.StonecutterScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreens.class)
public class GuiCraftMixin {
    
    @Inject(method="open", at=@At("HEAD"), cancellable = true)
    private static void checkCraftScreen(ScreenHandlerType type, MinecraftClient client,
            int any, Text component, CallbackInfo ci) {

        ScreenHandler handler;
        HandledScreen screen;
        if (type == ScreenHandlerType.CRAFTING) {
            handler = ScreenHandlerType.CRAFTING.create(any, client.player.getInventory());
            screen = new ExtendedGuiCrafting((CraftingScreenHandler) handler, client.player.getInventory(), component);
            ((ExtendedGuiCrafting)screen).setRecipeBook(new RecipeBook(screen, 1, 3, 0, 10));
        } else if (type == ScreenHandlerType.STONECUTTER) {
            handler = ScreenHandlerType.STONECUTTER.create(any, client.player.getInventory());
            screen = new ExtendedGuiStonecutter((StonecutterScreenHandler) handler, client.player.getInventory(), component);
            ((ExtendedGuiStonecutter)screen).setRecipeBook(new RecipeBook(screen, 0, 1, 1, 2));
        } else if (type == ScreenHandlerType.BREWING_STAND) {
            handler=ScreenHandlerType.BREWING_STAND.create(any, client.player.getInventory());
            screen = new ExtendedGuiBrewingStand((BrewingStandScreenHandler) handler, client.player.getInventory(), component);
            ((ExtendedGuiBrewingStand)screen).setRecipeBook(new RecipeBook(screen, 0, 0, 0, 5));
        } else if (type == ScreenHandlerType.LOOM) {
            handler=ScreenHandlerType.LOOM.create(any, client.player.getInventory());
            screen = new ExtendedGuiLoom((LoomScreenHandler) handler, client.player.getInventory(), component);
            ((ExtendedGuiLoom)screen).setRecipeBook(new LoomRecipeBook(screen));
        } else {
            return;
        }
        client.player.currentScreenHandler = handler;
        client.setScreen(screen);
        ci.cancel();
    }
}
