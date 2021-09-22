package de.guntram.mcmod.easiercrafting.mixins;


import de.guntram.mcmod.easiercrafting.ExtendedGuiInventory;
import de.guntram.mcmod.easiercrafting.RecipeBook;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class GuiInventoryMixin {
    
    @Shadow public ClientPlayerEntity player;
    @Shadow public void setScreen(Screen screenIn) {}
    
    @Inject(method="handleInputEvents", at=@At(value="INVOKE",
            target="Lnet/minecraft/client/tutorial/TutorialManager;onInventoryOpened()V"), cancellable = true)

    public void displayExtendedInventory(CallbackInfo ci) {
        ExtendedGuiInventory egi = new ExtendedGuiInventory(this.player);
        egi.setRecipeBook(new RecipeBook(egi, 1, 2, 0, 9));
        this.setScreen(egi);
        ci.cancel();
    }
}
