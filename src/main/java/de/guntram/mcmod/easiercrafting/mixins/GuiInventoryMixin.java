package de.guntram.mcmod.easiercrafting.mixins;

import de.guntram.mcmod.easiercrafting.ExtendedGuiInventory;
import de.guntram.mcmod.easiercrafting.RecipeBook;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class GuiInventoryMixin {
    
    @Shadow public EntityPlayerSP player;
    @Shadow public void displayGuiScreen(@Nullable GuiScreen guiScreenIn) {}
    
    @Inject(method="processKeyBinds", at=@At(value="INVOKE",
            target="Lnet/minecraft/client/tutorial/Tutorial;openInventory()V"), cancellable = true)

    public void displayExtendedInventory(CallbackInfo ci) {
        ExtendedGuiInventory egi = new ExtendedGuiInventory(this.player);
        egi.setRecipeBook(new RecipeBook(egi, 1, 2, 0, 9));
        this.displayGuiScreen(egi);
        ci.cancel();
    }
}
