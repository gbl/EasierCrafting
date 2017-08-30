package de.guntram.mcmod.easiercrafting;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import static net.minecraftforge.common.config.Configuration.CATEGORY_CLIENT;

public class GuiConfig extends net.minecraftforge.fml.client.config.GuiConfig {
    public GuiConfig(GuiScreen parent) {
        super(parent,
                new ConfigElement(ConfigurationHandler.getConfig().getCategory(CATEGORY_CLIENT)).getChildElements(),
                EasierCrafting.MODID,
                false,
                false,
                "Easier Crafting configuration");
    }
}
