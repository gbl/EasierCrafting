/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.easiercrafting.mixins;

import de.guntram.mcmod.easiercrafting.PropertyDelegateProvider;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen;
import net.minecraft.container.Container;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 *
 * @author gbl
 */
@Mixin(AbstractFurnaceScreen.class)
public abstract class AbstractFurnaceScreenMixin extends AbstractContainerScreen {

    public AbstractFurnaceScreenMixin(Container container_1, PlayerInventory playerInventory_1, Text text_1) {
        super(container_1, playerInventory_1, text_1);
    }
    
    @Inject(method="drawForeground", at=@At("HEAD"), cancellable = true)
    private void patchTitleWithBurntime(int x, int y, CallbackInfo ci) {
        PropertyDelegateProvider pde = (PropertyDelegateProvider) container;
        int itemsLeft=0, fuelLeftPercent=0, itemDonePercent=0;
        if (pde.getPropertyDelegate(3) != 0) {
            itemsLeft = pde.getPropertyDelegate(0)/pde.getPropertyDelegate(3);
            itemDonePercent = pde.getPropertyDelegate(2)*100/pde.getPropertyDelegate(3);
        }
        if (pde.getPropertyDelegate(1)!=0) {
            fuelLeftPercent = pde.getPropertyDelegate(0)*100/pde.getPropertyDelegate(1);
        }

        String titleText = this.title.asFormattedString() + " (" + itemsLeft + " more items)";
        this.font.draw(titleText, (float)(this.containerWidth / 2 - this.font.getStringWidth(titleText) / 2), 6.0F, 4210752);
        this.font.draw(this.playerInventory.getDisplayName().asFormattedString(), 8.0F, (float)(this.containerHeight - 96 + 2), 4210752);
        this.font.draw(itemDonePercent+" %", 20, 22, 4210752);
        this.font.draw(fuelLeftPercent+" %", 20, 58, 4210752);
        
        //for (int i=0; i<4; i++) {
        //    this.font.draw(""+((PropertyDelegateProvider)container).getPropertyDelegate(i), 5, 10+i*10, 0x000000);
        //}
        
        ci.cancel();
    }
}
