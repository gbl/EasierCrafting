package de.guntram.mcmod.easiercrafting.mixins;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientPlayNetworkHandler.class)
public class DelayedSlotClickContinue {
/*
    TODO restore this when the new way of handling slot clicks is understood,
    and fields are mapped
    
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
*/
}
