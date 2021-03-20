/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.easiercrafting.mixins;

import de.guntram.mcmod.easiercrafting.accessorInterfaces.PropertyDelegateProvider;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen;
import net.minecraft.client.gui.screen.ingame.BrewingStandScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 *
 * @author gbl
 */
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {
    
    @Shadow protected int backgroundWidth = 176;
    @Shadow protected int backgroundHeight = 166;
    @Shadow protected ScreenHandler handler;
    @Shadow protected Text displayName;

    public HandledScreenMixin(ScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(title);
    }
    
    @Inject(method="drawForeground", at=@At("HEAD"), cancellable = true)
    private void patchTitleWithBurntime(MatrixStack stack, int x, int y, CallbackInfo ci) {
        Object thisCopy=this;
        if ((thisCopy instanceof AbstractFurnaceScreen)) {
            PropertyDelegateProvider pde = (PropertyDelegateProvider) handler;
            int itemsLeft=0, fuelLeftPercent=0, itemDonePercent=0;
            if (pde.getPropertyDelegate(3) != 0) {
                itemsLeft = pde.getPropertyDelegate(0)/pde.getPropertyDelegate(3);
                itemDonePercent = pde.getPropertyDelegate(2)*100/pde.getPropertyDelegate(3);
            }
            if (pde.getPropertyDelegate(1)!=0) {
                fuelLeftPercent = pde.getPropertyDelegate(0)*100/pde.getPropertyDelegate(1);
            }

            String titleText = this.title.getString() + " (" + itemsLeft + " " + I18n.translate("easiercrafting.furnace.itemsleft")+")";
            this.textRenderer.draw(stack, titleText, (float)(this.backgroundWidth / 2 - this.textRenderer.getWidth(titleText) / 2), 6.0F, 4210752);
            this.textRenderer.draw(stack, displayName, 8.0F, (float)(this.backgroundHeight - 96 + 2), 4210752);
            this.textRenderer.draw(stack, itemDonePercent+" %", 20, 22, 4210752);
            this.textRenderer.draw(stack, fuelLeftPercent+" %", 20, 58, 4210752);

            //for (int i=0; i<4; i++) {
            //    this.font.draw(""+((PropertyDelegateProvider)container).getPropertyDelegate(i), 5, 10+i*10, 0x000000);
            //}

            ci.cancel();
        } else if (thisCopy instanceof BrewingStandScreen) {
            int brewTime = ((BrewingStandScreenHandler)handler).getBrewTime();
            Text titleText;
            if (brewTime > 0) {
                int brewPercent = 100-(brewTime*100/400);
                titleText = new LiteralText(this.title.getString() + " ( " + brewPercent + "% )");
            } else {
                titleText = this.title;
            }
            this.textRenderer.draw(stack, titleText.getString(), (float)(this.backgroundWidth / 2 - this.textRenderer.getWidth(titleText) / 2), 6.0F, 4210752);
            this.textRenderer.draw(stack, displayName, 8.0F, (float)(this.backgroundHeight - 96 + 2), 4210752);
            ci.cancel();
        }
    }
}
