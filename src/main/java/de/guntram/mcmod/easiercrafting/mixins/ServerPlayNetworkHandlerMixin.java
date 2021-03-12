package de.guntram.mcmod.easiercrafting.mixins;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;

/*
 * Version 1.6.7:
 * This is probably not needed any more with the new useInventoryRefreshHack
 * functionality, but left in for reference if we need it again. Not referenced
 * in the mixins json file.
 *
 * And from 21w10a things change again so disable it.
 */
@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
/*
    @Shadow public ServerPlayerEntity player;
    
    @Inject(method="onClickSlot", at=@At("RETURN"))
    private void clickWindowSendsCraftResult(ClickSlotC2SPacket packet, CallbackInfo ci) {
        if (packet.getSlot() == 0 && packet.getActionType() == SlotActionType.QUICK_MOVE) {
            this.player.refreshScreenHandler(this.player.currentScreenHandler);
        }
    }
*/
}
