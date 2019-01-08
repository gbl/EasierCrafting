package de.guntram.mcmod.easiercrafting;

import java.io.File;
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
        ConfigurationHandler confHandler = ConfigurationHandler.getInstance();
        confHandler.load(new File("easiercrafting.json"));
    }
}
