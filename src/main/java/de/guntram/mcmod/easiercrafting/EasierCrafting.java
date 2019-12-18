package de.guntram.mcmod.easiercrafting;

import de.guntram.mcmod.fabrictools.ConfigurationProvider;
import net.fabricmc.api.ClientModInitializer;

public class EasierCrafting implements ClientModInitializer 
{
    static final String MODID="easiercrafting";
    static final String MODNAME="EasierCrafting";
    static final String VERSION="1.4.0";

    @Override
    public void onInitializeClient() {
        ConfigurationHandler confHandler = ConfigurationHandler.getInstance();
        ConfigurationProvider.register("EasierCrafting", confHandler);
        confHandler.load(ConfigurationProvider.getSuggestedFile(MODID));
    }
}
