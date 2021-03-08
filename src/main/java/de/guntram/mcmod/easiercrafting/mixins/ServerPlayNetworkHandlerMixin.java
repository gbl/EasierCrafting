package de.guntram.mcmod.easiercrafting.mixins;

import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
 * Version 1.6.7:
 * This is probably not needed any more with the new useInventoryRefreshHack
 * functionality, but left in for reference if we need it again. Not referenced
 * in the mixins json file.
 */
@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow public ServerPlayerEntity player;
    
    @Inject(method="onClickSlot", at=@At("RETURN"))
    private void clickWindowSendsCraftResult(ClickSlotC2SPacket packet, CallbackInfo ci) {
        if (packet.getSlot() == 0 && packet.getActionType() == SlotActionType.QUICK_MOVE) {
            this.player.refreshScreenHandler(this.player.currentScreenHandler);
        }
    }
}
