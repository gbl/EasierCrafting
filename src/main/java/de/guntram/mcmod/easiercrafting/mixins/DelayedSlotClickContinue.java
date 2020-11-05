package de.guntram.mcmod.easiercrafting.mixins;

import de.guntram.mcmod.easiercrafting.delayedslotclicks.DelayedSlotClickQueue;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ConfirmScreenActionS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class DelayedSlotClickContinue {
    @Inject(method="onConfirmScreenAction", at=@At("RETURN"))
    private void nextClickOnConfirm(ConfirmScreenActionS2CPacket packet, CallbackInfo ci) {
        if (packet.wasAccepted()) {
            DelayedSlotClickQueue.execute();
        }
    }
    @Inject(method="onInventory", at=@At("RETURN"))
    private void nextClickOnInventory(InventoryS2CPacket packet, CallbackInfo ci) {
        DelayedSlotClickQueue.execute();
    }
}
