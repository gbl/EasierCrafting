package de.guntram.mcmod.easiercrafting;

import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;


public class ExtendedGuiInventory extends InventoryScreen implements SlotClickAccepter {

    private RecipeBook recipeBook;
    // temp kludge -- field_2776 and field_2800 seem to have been renamed with 21w13a
    public ExtendedGuiInventory(PlayerEntity player) {
        super(player);
    }
    
    @Override
    public void init() {
        super.init();
        if (!ConfigurationHandler.getAllowMinecraftRecipeBook())
            this.children().clear();
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
        // System.out.println("Clicking slot "+slot+" "+(mouseButton==0 ? "left" : "right")+" type:"+clickType.toString());
        this.onMouseClick(null, slot, mouseButton, clickType);
        // mc.playerController.windowClick(mc.player.openContainer.windowId, slot, mouseButton, clickType, mc.player);
    }

}
