package de.guntram.mcmod.easiercrafting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.GuiOpenEvent;

public class OpenCraftEventHandler {
    
    static private OpenCraftEventHandler instance;
    private Minecraft mc;
    
    private OpenCraftEventHandler() {
        // do not call, use getInstance()
    }
    
    public static OpenCraftEventHandler getInstance() {
        if (instance==null) {
            instance=new OpenCraftEventHandler();
            instance.mc=Minecraft.getMinecraft();
        }
        return instance;
    }

    @SubscribeEvent
    public void guiOpenEvent(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiCrafting) {
            ExtendedGuiCrafting egc = new ExtendedGuiCrafting(mc.player.inventory, mc.world);
            egc.setRecipeBook(new RecipeBook(egc, 1, 3, 0));
            event.setGui(egc);
        } else if (event.getGui() instanceof GuiInventory) {
            ExtendedGuiInventory egi = new ExtendedGuiInventory(mc.player);
            egi.setRecipeBook(new RecipeBook(egi, 1, 2, 0));
            event.setGui(egi);
        } else {
            if (event.getGui() != null)
                System.out.println("opened "+event.getGui().getClass().getCanonicalName());
        }
    }
}
