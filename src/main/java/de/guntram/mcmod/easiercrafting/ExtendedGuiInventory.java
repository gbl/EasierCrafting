package de.guntram.mcmod.easiercrafting;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;

public class ExtendedGuiInventory extends GuiInventory {
    
    private RecipeBook recipeBook;

    public ExtendedGuiInventory(EntityPlayer player) {
        super(player);
    }
    
    @Override
    public void initGui() {
        super.initGui();
        if (!ConfigurationHandler.getAllowMinecraftRecipeBook())
            this.buttonList.clear();
    }

    void setRecipeBook(RecipeBook recipeBook) {
        this.recipeBook=recipeBook;
    }
    
    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        recipeBook.drawRecipeList(fontRenderer, itemRender, xSize, ySize, mouseX-guiLeft, mouseY-guiTop);
    }
    
    @Override
    protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        recipeBook.mouseClicked(mouseX, mouseY, mouseButton, guiLeft, guiTop);
    }

    @Override
    public void keyTyped(char c, int i) throws IOException {
        if (c==27)
            super.keyTyped(c, i);
        else if (recipeBook.keyTyped(c, i))
            ;
        else
            super.keyTyped(c, i);
    }
}
