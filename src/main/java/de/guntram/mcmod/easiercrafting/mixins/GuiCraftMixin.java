package de.guntram.mcmod.easiercrafting.mixins;

import de.guntram.mcmod.easiercrafting.ExtendedGuiCrafting;
import de.guntram.mcmod.easiercrafting.RecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.world.IInteractionObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public class GuiCraftMixin extends AbstractClientPlayer {
    
    public GuiCraftMixin() {
        super(null, null);
    }
    @Shadow protected Minecraft mc;
    
    @Inject(method="displayGui", at=@At("HEAD"), cancellable = true)
    public void displayExtendedCraftGUI(IInteractionObject owner, CallbackInfo ci) {
        String s=owner.getGuiID();
        System.out.println("EGC mixin with object "+s);
        if ("minecraft:crafting_table".equals(s)) {
            ExtendedGuiCrafting egc = new ExtendedGuiCrafting(this.inventory, this.world);
            egc.setRecipeBook(new RecipeBook(egc, 1, 3, 0, 10));
            this.mc.displayGuiScreen(egc);
            ci.cancel();
        }
    }
}
