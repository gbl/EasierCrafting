/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.easiercrafting;

import net.minecraft.client.gui.screen.ingame.LoomScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.container.LoomContainer;
import net.minecraft.container.SlotActionType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BannerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.text.Text;

/**
 *
 * @author gbl
 */
public class ExtendedGuiLoom extends LoomScreen implements SlotClickAccepter {
    
    private TextFieldWidget saveName;
    private ButtonWidget saveButton;

    public ExtendedGuiLoom(LoomContainer container, PlayerInventory inventory, Text title) {
        super(container, inventory, title);
    }
    
    @Override
    protected void init() {
        super.init();
        saveName = new TextFieldWidget(this.font, 0, -25, this.containerWidth-25, 20, "Save input pattern as...");
        saveButton = new ButtonWidget(this.containerWidth-20, -25, 20, 20, "Save", (button)->{this.saveButtonPressed();});
    }
    
    @Override
    protected void drawForeground(final int mousex, final int mousey) {
        super.drawForeground(mousex, mousey);
        if (((LoomContainer)container).getBannerSlot().hasStack()) {
            saveButton.active = !(saveName.getText().isEmpty());
            saveButton.renderButton(mousex, mousey, 0);
            saveName.renderButton(mousex, mousey, 0);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (saveName != null) {
            if (saveName.mouseClicked(mouseX-x, mouseY-y, button))
                return true;
        }
        if (saveButton != null) {
            if (saveButton.mouseClicked(mouseX-x, mouseY-y, button))
                return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (saveName != null && saveName.isFocused()) {
            return saveName.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        if (saveName != null && saveName.isFocused()) {
            return saveName.charTyped(chr, keyCode);
        }
        return super.charTyped(chr, keyCode);
    }

    @Override
    public void slotClick(int slot, int mouseButton, SlotActionType clickType) {
        this.onMouseClick(null, slot, mouseButton, clickType);
    }

    private void saveButtonPressed() {
        System.out.println("save");
        String bannerTypeName = saveName.getText();
        if (bannerTypeName.isEmpty()) {
            return;
        }
        ItemStack banners = ((LoomContainer)container).getBannerSlot().getStack();
        if (!(banners.getItem() instanceof BannerItem)) {
            return;
        }
        CompoundTag tag = banners.getSubTag("BlockEntityTag");
        if (tag == null) {
            return;
        }
        ListTag patterns = tag.getList("Patterns", 10);
        if (patterns == null) {
            return;
        }
        LoomRecipe recipe = new LoomRecipe(bannerTypeName);
        int[] colorMap = new int[patterns.size()+1];
        colorMap[0]=((BannerItem)banners.getItem()).getColor().getId();
        int colorMapUsed = 1;
        recipe.addStep(new LoomStep("bannerbase", 'A'));

        for (int i=0; i<patterns.size(); i++) {
            CompoundTag patternTag = (CompoundTag) patterns.get(i);
            String stepName = patternTag.getString("Pattern");
            int stepColor = patternTag.getInt("Color");
            int colorMapIndex = -1;
            for (int j=0; j<colorMapUsed; j++) {
                if (colorMap[j] == stepColor) {
                    colorMapIndex = j;
                    break;
                }
            }
            if (colorMapIndex == -1) {
                colorMapIndex = colorMapUsed;
                colorMap[colorMapUsed++]=stepColor;
            }
            recipe.addStep(new LoomStep(stepName, (char) ('A'+colorMapIndex)));
        }
        
        System.out.println(recipe.toSaveString());
    }
}
