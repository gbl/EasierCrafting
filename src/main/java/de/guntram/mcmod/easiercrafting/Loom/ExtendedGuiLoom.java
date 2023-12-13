/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.easiercrafting.Loom;

import de.guntram.mcmod.easiercrafting.SlotClickAccepter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import net.minecraft.block.BannerBlock;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.LoomScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BannerItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.LoomScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

/**
 *
 * @author gbl
 */
public class ExtendedGuiLoom extends LoomScreen implements SlotClickAccepter {
    
    private TextFieldWidget saveName;
    private ButtonWidget saveButton;
    private ColorButtonWidget colorButtons[];
    private static int savedColorCode[];
    private static final int BUTTONCOUNT = 7;
    private final Logger LOGGER;
    
    private LoomRecipeBook recipeBook;

    public ExtendedGuiLoom(LoomScreenHandler container, PlayerInventory inventory, Text title) {
        super(container, inventory, title);
        LOGGER=LogManager.getLogger();
    }
    
    @Override
    protected void init() {
        super.init();
        saveName = new TextFieldWidget(this.textRenderer, 0, -25, this.backgroundWidth-25, 20, Text.literal("Save input pattern as..."));
        saveButton = ButtonWidget.builder(Text.translatable("easiercrafting.loom.save"), (button)->{this.saveButtonPressed();})
                .dimensions(this.backgroundWidth-20, -25, 20, 20)
                .build();
        if (savedColorCode == null) {
            savedColorCode = new int[BUTTONCOUNT];
            for (int i=0; i<savedColorCode.length; i++) {
                savedColorCode[i]=i;
            }
        }
        colorButtons = new ColorButtonWidget[BUTTONCOUNT];
        for (int i=0; i<colorButtons.length; i++) {
            colorButtons[i] = new ColorButtonWidget(26*i, -25, 20, 20, i, savedColorCode[i]);
        }
        this.recipeBook.afterInitGui();
    }
    
    public void setRecipeBook(LoomRecipeBook recipeBook) {
        this.recipeBook=recipeBook;
    }
    
    @Override
    protected void drawForeground(DrawContext context, final int mouseX, final int mouseY) {
        super.drawForeground(context, mouseX, mouseY);
        if (((LoomScreenHandler)handler).getBannerSlot().hasStack()) {
            saveButton.active = !(saveName.getText().isEmpty());
            saveButton.render(context, mouseX, mouseY, 0);
            saveName.renderButton(context, mouseX, mouseY, 0);
        } else {
            saveName.setText("");
            for (int i=0; i<colorButtons.length; i++) {
                colorButtons[i].renderButton(context, mouseX, mouseY, 0);
            }
            for (int i=0; i<colorButtons.length; i++) {
                colorButtons[i].renderButtonTooltip(context, mouseX, mouseY, 0);
            }
        }
        recipeBook.drawRecipeList(context, textRenderer, backgroundWidth, backgroundHeight, mouseX-x, mouseY-y);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (((LoomScreenHandler)handler).getBannerSlot().hasStack()) {
            if (saveName != null) {
                if (saveName.mouseClicked(mouseX-x, mouseY-y, button))
                    return true;
            }
            if (saveButton != null) {
                if (saveButton.mouseClicked(mouseX-x, mouseY-y, button))
                    return true;
            }
        } else {
            for (int i=0; i<colorButtons.length; i++) {
                if (colorButtons[i].mouseClicked(mouseX-x, mouseY-y, button))
                    return true;
            }
        }
        super.mouseClicked(mouseX, mouseY, button);
        recipeBook.mouseClicked((int)mouseX, (int)mouseY, button, x, y);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode==GLFW.GLFW_KEY_ESCAPE) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (recipeBook.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (saveName != null && saveName.isFocused()) {
            boolean result = saveName.keyPressed(keyCode, scanCode, modifiers);
            adjustFilenameColor();
            return result;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        if (saveName != null && saveName.isFocused()) {
            boolean result = saveName.charTyped(chr, keyCode);
            adjustFilenameColor();
            return result;
        }
        if (recipeBook.charTyped(chr, keyCode)) {
            return true;
        }
        return super.charTyped(chr, keyCode);
    }

    @Override
    public void slotClick(int slot, int mouseButton, SlotActionType clickType) {
        this.onMouseClick(null, slot, mouseButton, clickType);
    }
    
    private void adjustFilenameColor() {
        if (saveName.getText().isEmpty()) {
            saveName.setEditableColor(0xffffff);
            return;
        }
        File file = new File(LoomRecipeRegistry.getRecipeCollectionPath(), saveName.getText()+".lr");
        if (file.exists()) {
            saveName.setEditableColor(0xff8000);
        } else {
            saveName.setEditableColor(0x00ff00);
        }
    }

    private void saveButtonPressed() {
        LOGGER.debug("save");
        String loomRecipeName = saveName.getText();
        if (loomRecipeName.isEmpty()) {
            return;
        }
        ItemStack banners = ((LoomScreenHandler)handler).getBannerSlot().getStack();
        if (!(banners.getItem() instanceof BannerItem)) {
            return;
        }
        NbtCompound tag = banners.getSubNbt("BlockEntityTag");
        if (tag == null) {
            return;
        }
        NbtList patterns = tag.getList("Patterns", 10);
        if (patterns == null) {
            return;
        }
        LoomRecipe recipe = new LoomRecipe(loomRecipeName);
        int[] colorMap = new int[patterns.size()+1];
        colorMap[0]=((BannerItem)banners.getItem()).getColor().getId();
        int colorMapUsed = 1;
        recipe.addStep(new LoomStep("bannerbase", 'A'));

        for (int i=0; i<patterns.size(); i++) {
            NbtCompound patternTag = (NbtCompound) patterns.get(i);
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
        
        String path = LoomRecipeRegistry.getRecipeCollectionPath()+File.separator+loomRecipeName+".lr";
        try {
            Files.write(Paths.get(path), recipe.toSaveString().getBytes());
            LoomRecipeRegistry.registerRecipe(recipe);
            // System.out.println(recipe.toSaveString());
            saveName.setText("");
            recipeBook.updateRecipes();
            recipeBook.updatePatternMatch();
        } catch (IOException ex) {
            LOGGER.warn(ex);
        }
    }
    
    private void  colorButtonPressed(int index) {
        colorButtons[index].switchCurrentColor();
        savedColorCode[index]=colorButtons[index].currentColor;
    }
    
    public ItemStack getBannerItemStack() {
        return colorButtons[0].getRenderStack();
    }
    
    public int getColor(int index) {
        return savedColorCode[index];
    }
    
    private class ColorButtonWidget extends ButtonWidget {
        
        private int currentColor;
        private boolean useBannerItem;
        private int clickButton;

        ColorButtonWidget(int x, int y, int width, int height, int index, int color) {
            super(x, y, width, height, Text.literal(""), new PressAction() {
                @Override
                public void onPress(ButtonWidget button) {
                    colorButtonPressed(index);
                }
            }, DEFAULT_NARRATION_SUPPLIER);
            // using a lambda here results in a ClassFormatError:
            // (button) -> { colorButtonPressed(index); } );
            useBannerItem = (index == 0);
            currentColor = color;
        }

        public ItemStack getRenderStack() {
            Item item;
            if (useBannerItem) {
                item = BannerBlock.getForColor(DyeColor.byId(currentColor)).asItem();
            } else {
                item = DyeItem.byColor(DyeColor.byId(currentColor));
            }
            ItemStack stack = new ItemStack(item);
            return stack;
        }
        
        public int getColor() {
            return currentColor;
        }

        @Override
        public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderButton(context, mouseX, mouseY, delta);
            ItemStack items = getRenderStack();
            //setBlitOffset(100);
            context.drawItem(items, x+2, y+2);
            //setBlitOffset(0);
        }

        public void renderButtonTooltip(DrawContext context, int mouseX, int mouseY, float delta) {
            ItemStack items = getRenderStack();
            mouseX-=ExtendedGuiLoom.this.x;
            mouseY-=ExtendedGuiLoom.this.y;
            if (mouseX > x && mouseX < x+width && mouseY > y && mouseY < y+width) {
                context.drawItemTooltip(textRenderer, items, mouseX, mouseY);
            }
        }

        public void switchCurrentColor() {
            if (clickButton == 0) {
                currentColor = (currentColor+1) % 16;
            } else {
                currentColor = (currentColor+15) % 16;
            }
        }

        @Override
        protected boolean isValidClickButton(int i) {
            clickButton = i;
            return true;
        }
    }
}
