package de.guntram.mcmod.easiercrafting;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = EasierCrafting.MODID, 
        version = EasierCrafting.VERSION,
	clientSideOnly = true, 
	guiFactory = "de.guntram.mcmod.easiercrafting.GuiFactory",
	acceptedMinecraftVersions = "[1.12]"
)

public class EasierCrafting
{
    static final String MODID="easiercrafting";
    static final String VERSION="1.2";
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(OpenCraftEventHandler.getInstance());
    }

    @EventHandler
    public void preInit(final FMLPreInitializationEvent event) {
        ConfigurationHandler confHandler = ConfigurationHandler.getInstance();
        confHandler.load(event.getSuggestedConfigurationFile());
        MinecraftForge.EVENT_BUS.register(confHandler);
    }
}
