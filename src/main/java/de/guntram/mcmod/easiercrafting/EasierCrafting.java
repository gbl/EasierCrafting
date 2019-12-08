package de.guntram.mcmod.easiercrafting;

// import de.guntram.mcmod.fabrictools.ConfigurationProvider;
import net.fabricmc.api.ClientModInitializer;

public class EasierCrafting implements ClientModInitializer 
{
    static final String MODID="easiercrafting";
    static final String VERSION="@VERSION@";

    @Override
    public void onInitializeClient() {
        ConfigurationHandler confHandler = ConfigurationHandler.getInstance();
//        ConfigurationProvider.register("EasierCrafting", confHandler);
//        confHandler.load(ConfigurationProvider.getSuggestedFile(MODID));
        confHandler.load(null);
    }
}
