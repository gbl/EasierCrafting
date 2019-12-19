package de.guntram.mcmod.easiercrafting.mixins;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.packet.ConfirmGuiActionS2CPacket;
import net.minecraft.client.network.packet.InventoryS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class GuiActionConfirmDebug {
    @Inject(method="onGuiActionConfirm", at=@At("HEAD"))
    private void dumpActionConfirmInfo(ConfirmGuiActionS2CPacket packet, CallbackInfo ci) {
        System.out.println("confirm: id="+packet.getId()+", action="+packet.getActionId()+", accepted="+packet.wasAccepted());
    }
    @Inject(method="onInventory", at=@At("HEAD"))
    private void dumpInventoryInfo(InventoryS2CPacket packet, CallbackInfo ci) {
        System.out.println("inventory: guiid="+packet.getGuiId()+", slotcount="+packet.getSlotStacks().size());
    }
}
