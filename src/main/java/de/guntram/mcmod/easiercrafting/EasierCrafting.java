package de.guntram.mcmod.easiercrafting;

import de.guntram.mcmod.rifttools.ConfigurationProvider;
import org.dimdev.riftloader.listener.InitializationListener;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

public class EasierCrafting implements InitializationListener 
{
    static final String MODID="easiercrafting";
    static final String VERSION="@VERSION@";

    @Override
    public void onInitialization() {
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.easiercrafting.json");
        Mixins.addConfiguration("mixins.rifttools-de-guntram.json");
        ConfigurationHandler confHandler = ConfigurationHandler.getInstance();
        ConfigurationProvider.register("EasierCrafting", confHandler);
        confHandler.load(ConfigurationProvider.getSuggestedFile(MODID));
    }
}
