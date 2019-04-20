package de.guntram.mcmod.easiercrafting.mixins;

import de.guntram.mcmod.easiercrafting.ExtendedGuiCrafting;
import de.guntram.mcmod.easiercrafting.RecipeBook;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ContainerScreenRegistry;
import net.minecraft.container.ContainerType;
import net.minecraft.container.CraftingTableContainer;
import net.minecraft.text.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ContainerScreenRegistry.class)
public class GuiCraftMixin {
    
    @Inject(method="openScreen", at=@At("HEAD"), cancellable = true)
    private static void checkCraftScreen(ContainerType type, MinecraftClient client,
            int any, TextComponent component, CallbackInfo ci) {
        if (type == ContainerType.CRAFTING) {
            CraftingTableContainer container = ContainerType.CRAFTING.create(any, client.player.inventory);
            ExtendedGuiCrafting screen = new ExtendedGuiCrafting(container, client.player.inventory, component);
            screen.setRecipeBook(new RecipeBook(screen, 1, 3, 0, 10));
            client.player.container = container;
            client.openScreen(screen);
            ci.cancel();
        }
    }
}
