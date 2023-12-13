package de.guntram.mcmod.easiercrafting;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.BrewingStandScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ExtendedGuiBrewingStand extends BrewingStandScreen implements SlotClickAccepter {

    private RecipeBook recipeBook;

    public ExtendedGuiBrewingStand(BrewingStandScreenHandler container, PlayerInventory lowerInv, Text title) {
        super(container, lowerInv, title);
    }
    
    @Override
    protected void init() {
        super.init();
        if (!ConfigurationHandler.hideBrewingStandTakeButton()) {
            ButtonWidget button = ButtonWidget.builder(
                    Text.translatable("easiercrafting.brewing.takeall"),
                    (widget) -> {
                        // for some reason, this order seems to work better than 0 1 2
                        slotClick(1, 0, SlotActionType.QUICK_MOVE);
                        slotClick(2, 0, SlotActionType.QUICK_MOVE);
                        slotClick(0, 0, SlotActionType.QUICK_MOVE);
                    })
                    .position(x + 130, y + 50)
                    .size(40, 20)
                    .build();
            this.addDrawableChild(button);
        }
        this.recipeBook.afterInitGui();
    }

    public void setRecipeBook(RecipeBook recipeBook) {
        this.recipeBook=recipeBook;
    }
    
    public void updateRecipes() {
        if (recipeBook != null) {
            recipeBook.updateRecipes();
        }
    }
    
    public void updateRecipesIn(int ms) {
        if (recipeBook != null) {
            recipeBook.updateRecipesIn(ms);
        }
    }
    
    @Override
    protected void drawForeground(DrawContext context, final int mouseX, final int mouseY) {
        super.drawForeground(context, mouseX, mouseY);
        recipeBook.drawRecipeList(context, textRenderer, backgroundWidth, backgroundHeight, mouseX-x, mouseY-y);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double xdelta, double ydelta) {
        recipeBook.scrollBy((int) ydelta);
        return super.mouseScrolled(mouseX, mouseY, xdelta, ydelta);
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
