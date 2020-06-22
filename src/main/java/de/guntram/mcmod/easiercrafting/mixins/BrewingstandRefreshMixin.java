package de.guntram.mcmod.easiercrafting.mixins;

import de.guntram.mcmod.easiercrafting.ExtendedGuiBrewingStand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
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
        updateBrewingStandIfOpen(0);
    }
    
    @Inject(method="onWorldEvent", at=@At("RETURN"))
    private void onBrewingStandDone(WorldEventS2CPacket packet, CallbackInfo ci) {
        if (packet.getEventId() == 1035) { // 1035 from BrewingStandBlockEntity
            // For some reason, the done event gets sent before the inventory
            // update, so we have to delay the update a bit .. this sucks,
            // would be better to attach it to the update packet.
            updateBrewingStandIfOpen(200);
        }
    }

    private void updateBrewingStandIfOpen(int delay) {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen != null && screen instanceof ExtendedGuiBrewingStand) {
            LOGGER.info("updating brewing stand recipes");
            if (delay == 0) {
                ((ExtendedGuiBrewingStand)screen).updateRecipes();
            } else {
                ((ExtendedGuiBrewingStand)screen).updateRecipesIn(delay);
            }
        }
    }
}
