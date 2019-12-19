package de.guntram.mcmod.easiercrafting.mixins;

import net.minecraft.container.SlotActionType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.packet.ClickWindowC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClickWindowC2SPacket.class)
public class ClickWindowC2SPacketMixin {
    @Inject(method="<init>(IIILnet/minecraft/container/SlotActionType;Lnet/minecraft/item/ItemStack;S)V", at=@At("RETURN"))
    private void dumpC2SNewInfo(int syncid, int slot, int button, SlotActionType actionType, ItemStack stack,
            short transaction, CallbackInfo ci) {
        System.out.println("new ClickWindow C2S: syncid="+syncid+", slot="+slot+", button="+button+
                ", action="+actionType.toString()+", item="+stack.getCount()+" of "+stack.getName().asString()+
                ", transaction="+transaction);
    }
}
