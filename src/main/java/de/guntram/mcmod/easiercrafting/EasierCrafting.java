package de.guntram.mcmod.easiercrafting;

import de.guntram.mcmod.easiercrafting.Loom.LoomRecipeRegistry;
import de.guntram.mcmod.fabrictools.ConfigurationProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.recipe.Recipe;

public class EasierCrafting implements ClientModInitializer 
{
    static public final String MODID="easiercrafting";
    static public final String MODNAME="EasierCrafting";
    static public final String VERSION="@VERSION@";

    @Override
    public void onInitializeClient() {
        ConfigurationHandler confHandler = ConfigurationHandler.getInstance();
        ConfigurationProvider.register("EasierCrafting", confHandler);
        confHandler.load(ConfigurationProvider.getSuggestedFile(MODID));
        
        File localRecipes = new File(ConfigurationProvider.getSuggestedFile(MODID).getParentFile(), "localrecipes.zip");
        if (!localRecipes.exists()) {
            extractDefaultLocalRecipesFile(localRecipes);
        }

        LocalRecipeManager.addZipfile(localRecipes.getPath());
        LocalRecipeManager.load();
        
        LoomRecipeRegistry.loadRecipeCollection(LoomRecipeRegistry.getRecipeCollectionPath());
    }
    
    public static String recipeDisplayName(Recipe recipe) {
        String display = recipe.getGroup();
        if (display.startsWith(MODID+":")) {
            display = I18n.translate(display);
            if (display.startsWith(MODID+":")) {
                display=display.substring(MODID.length()+1);
            }
        } else {
            display = recipe.getOutput().getName().asString();
        }
        return display;
    }
    
    private void extractDefaultLocalRecipesFile(File target) {
        InputStream is;
        try {
            CodeSource src = EasierCrafting.class.getProtectionDomain().getCodeSource();
            if (src != null) {
                URL jar = src.getLocation();
                is = jar.openStream();
                ZipInputStream zis = new ZipInputStream(is);
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.getName().equalsIgnoreCase("localrecipes.zip")) {
                        FileOutputStream fos = new FileOutputStream(target);
                        byte[] buf=new byte[16384];
                        int length;
                        while ((length=zis.read(buf, 0, buf.length))>=0) {
                            fos.write(buf, 0, length);
                        }
                        fos.close();
                        break;
                    }
                }
                zis.close();
            }
        } catch (IOException ex) {
        }
    }
}
