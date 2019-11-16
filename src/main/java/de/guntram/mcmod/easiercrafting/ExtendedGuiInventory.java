package de.guntram.mcmod.easiercrafting;


public class ExtendedGuiInventory /* extends ContainerScreen  */ {
/*
    private RecipeBook recipeBook;

    public ExtendedGuiInventory(EntityPlayer player) {
        super(player);
    }
    
    @Override
    public void initGui() {
        super.initGui();
        if (!ConfigurationHandler.getAllowMinecraftRecipeBook())
            this.buttons.clear();
        this.recipeBook.afterInitGui();
    }

    public void setRecipeBook(RecipeBook recipeBook) {
        this.recipeBook=recipeBook;
    }
    
    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        recipeBook.drawRecipeList(fontRenderer, itemRender, xSize, ySize, mouseX-guiLeft, mouseY-guiTop);
    }
    
    @Override
    public boolean mouseScrolled(double delta) {
        recipeBook.scrollBy((int) delta);
        return super.mouseScrolled(delta);
    }    
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        recipeBook.mouseClicked((int)mouseX, (int)mouseY, mouseButton, guiLeft, guiTop);
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
    } */
}
