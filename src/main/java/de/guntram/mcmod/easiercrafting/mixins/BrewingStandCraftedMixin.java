package de.guntram.mcmod.easiercrafting.mixins;

import de.guntram.mcmod.easiercrafting.ExtendedGuiBrewingStand;
import static de.guntram.mcmod.easiercrafting.RecipeBook.LOGGER;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandBlockEntity.class) 

public class BrewingStandCraftedMixin {
    @Inject(method="craft", at=@At("RETURN"))
    private void updateRecipesWhenCrafted(CallbackInfo info) {
        LOGGER.info("brewing stand playing done sound");
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen != null && screen instanceof ExtendedGuiBrewingStand) {
            LOGGER.info("updating brewing stand recipes");
            ((ExtendedGuiBrewingStand)screen).updateRecipes();
        }
    }
}
