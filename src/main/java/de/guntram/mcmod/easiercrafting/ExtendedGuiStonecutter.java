package de.guntram.mcmod.easiercrafting;

import net.minecraft.client.gui.screen.ingame.StonecutterScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.StonecutterScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ExtendedGuiStonecutter extends StonecutterScreen implements SlotClickAccepter {

    private RecipeBook recipeBook;

    public ExtendedGuiStonecutter(StonecutterScreenHandler container, PlayerInventory lowerInv, Text title) {
        super(container, lowerInv, title);
    }
    
    @Override
    protected void init() {
        super.init();
        this.recipeBook.afterInitGui();
    }

    public void setRecipeBook(RecipeBook recipeBook) {
        this.recipeBook=recipeBook;
    }
    
    @Override
    protected void drawForeground(MatrixStack stack, final int mouseX, final int mouseY) {
        super.drawForeground(stack, mouseX, mouseY);
        recipeBook.drawRecipeList(stack, textRenderer, itemRenderer, backgroundWidth, backgroundHeight, mouseX-x, mouseY-y);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        recipeBook.scrollBy((int) delta);
        return super.mouseScrolled(mouseX, mouseY, delta);
    }    
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        recipeBook.mouseClicked((int)mouseX, (int)mouseY, mouseButton, x, y);
        return true;
    }

    @Override
    public boolean keyPressed(int c, int scancode, int modifiers) {
        if (c==GLFW.GLFW_KEY_ESCAPE)
            return super.keyPressed(c, scancode, modifiers);
        else if (recipeBook.keyPressed(c, scancode, modifiers))
            return true;
        else
            return super.keyPressed(c, scancode, modifiers);
    }
    
    @Override
    public boolean charTyped(char codepoint, int modifiers) {
        if (!recipeBook.charTyped(codepoint, modifiers))
            return super.charTyped(codepoint, modifiers);
        return true;
    }

    @Override
    public void slotClick(int slot, int mouseButton, SlotActionType clickType) {
        this.onMouseClick(null, slot, mouseButton, clickType);
    }
}
