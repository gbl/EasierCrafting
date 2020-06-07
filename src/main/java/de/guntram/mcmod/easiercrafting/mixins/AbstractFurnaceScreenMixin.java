/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.easiercrafting.mixins;

import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

/**
 *
 * @author gbl
 */
@Mixin(AbstractFurnaceScreen.class)
public abstract class AbstractFurnaceScreenMixin extends HandledScreen {

    public AbstractFurnaceScreenMixin(ScreenHandler handler, PlayerInventory playerInventory_1, Text text_1) {
        super(handler, playerInventory_1, text_1);
    }
    
/* TODO injection point doesn't exist any more

    @Inject(method="drawForeground", at=@At("HEAD"), cancellable = true)
    private void patchTitleWithBurntime(MatrixStack stack, int x, int y, CallbackInfo ci) {
        PropertyDelegateProvider pde = (PropertyDelegateProvider) handler;
        int itemsLeft=0, fuelLeftPercent=0, itemDonePercent=0;
        if (pde.getPropertyDelegate(3) != 0) {
            itemsLeft = pde.getPropertyDelegate(0)/pde.getPropertyDelegate(3);
            itemDonePercent = pde.getPropertyDelegate(2)*100/pde.getPropertyDelegate(3);
        }
        if (pde.getPropertyDelegate(1)!=0) {
            fuelLeftPercent = pde.getPropertyDelegate(0)*100/pde.getPropertyDelegate(1);
        }

        String titleText = this.title.getString() + " (" + itemsLeft + " more items)";
        this.textRenderer.draw(stack, titleText, (float)(this.backgroundWidth / 2 - this.textRenderer.getWidth(titleText) / 2), 6.0F, 4210752);
        this.textRenderer.draw(stack, this.playerInventory.getDisplayName().getString(), 8.0F, (float)(this.backgroundHeight - 96 + 2), 4210752);
        this.textRenderer.draw(stack, itemDonePercent+" %", 20, 22, 4210752);
        this.textRenderer.draw(stack, fuelLeftPercent+" %", 20, 58, 4210752);
        
        //for (int i=0; i<4; i++) {
        //    this.font.draw(""+((PropertyDelegateProvider)container).getPropertyDelegate(i), 5, 10+i*10, 0x000000);
        //}
        
        ci.cancel();
    }
*/

}
