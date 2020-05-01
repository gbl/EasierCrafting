package de.guntram.mcmod.easiercrafting.mixins;

import de.guntram.mcmod.easiercrafting.ExtendedGuiBrewingStand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.packet.InventoryS2CPacket;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)

public class BrewingstandRefreshMixin {
    
    @Shadow @Final static Logger LOGGER;
    
    @Inject(method="onInventory", at=@At("RETURN"))
    private void dumpInventoryInfo(InventoryS2CPacket packet, CallbackInfo ci) {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen != null && screen instanceof ExtendedGuiBrewingStand) {
            LOGGER.info("updating brewing stand recipes");
            ((ExtendedGuiBrewingStand)screen).updateRecipes();
        }
    }
}
