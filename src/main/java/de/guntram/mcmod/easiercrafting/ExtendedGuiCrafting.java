package de.guntram.mcmod.easiercrafting;

import java.io.IOException;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.world.World;
import org.lwjgl.input.Mouse;

class ExtendedGuiCrafting extends GuiCrafting {

    private RecipeBook recipeBook;

    public ExtendedGuiCrafting(InventoryPlayer playerInv, World worldIn) {
        super(playerInv, worldIn);
    }
    
    @Override
    public void initGui() {
        super.initGui();
        if (!ConfigurationHandler.getAllowMinecraftRecipeBook())
            this.buttonList.clear();
        this.recipeBook.afterInitGui();
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
    public void handleMouseInput() throws IOException {
        recipeBook.scrollBy(Mouse.getDWheel());
        super.handleMouseInput();
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
