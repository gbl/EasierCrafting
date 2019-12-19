package de.guntram.mcmod.easiercrafting.mixins;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.ClickWindowC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow public ServerPlayerEntity player;
    
    @Inject(method="onClickWindow", at=@At("RETURN"))
    private void clickWindowSendsCraftResult(ClickWindowC2SPacket packet, CallbackInfo ci) {
        if (packet.getSlot() == 0) {
            this.player.openContainer(this.player.container);
        }
    }
}
