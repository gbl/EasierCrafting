package de.guntram.mcmod.easiercrafting;

import de.guntram.mcmod.fabrictools.ConfigurationProvider;
import de.guntram.mcmod.fabrictools.GuiModOptions;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;

public class MMConfigurationHandler implements ModMenuApi
{
    @Override
    public String getModId() {
        return EasierCrafting.MODID;
    }

    @Override
    public ConfigScreenFactory getModConfigScreenFactory() {
        return screen -> new GuiModOptions(screen, EasierCrafting.MODNAME, ConfigurationProvider.getHandler(EasierCrafting.MODNAME));
    }
}
