/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.easiercrafting;

import com.google.gson.JsonElement;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.SampleType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author gbl
 */
public class LocalRecipeManager extends RecipeManager implements ResourceManager {
    
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Set<String> namespaces = Collections.singleton( EasierCrafting.MODID );
    private static final List<String> forcedZips = new ArrayList<>();
    private static final LocalRecipeManager instance = new LocalRecipeManager();
    static final String SEPARATOR = "---";
    
    public static void addZipfile(String name) {
        forcedZips.add(name);
    }
    
    static class EmptyProfiler implements Profiler {
            @Override public void startTick() {}
            @Override public void endTick() {}
            @Override public void push(String location) {}
            @Override public void push(Supplier<String> locationGetter) {}
            @Override public void pop() {}
            @Override public void swap(String location) {}
            @Override public void swap(Supplier<String> locationGetter) {}
            @Override public void markSampleType(SampleType type) {}
            @Override public void visit(String marker) {}
            @Override public void visit(Supplier<String> markerGetter) {}
            @Override public void visit(String marker, int i) {}
            @Override public void visit(Supplier<String> markerGetter, int i) {}
    }

    public static void load() {
        Map<Identifier, JsonElement> map = instance.prepare(instance, null);
        instance.apply(map, instance, new EmptyProfiler());
    }
    
    @Override
    public Stream<ResourcePack> streamResourcePacks() {
        return Stream.of();
    }

    public static void dumpAll() {
        for (Recipe r: instance.values()) {
            LOGGER.info(r.getId());
            // System.out.println(r.getId() + " produces " + r.getOutput().getTranslationKey());
        }
    }
    
    public static LocalRecipeManager getInstance() {
        return instance;
    }

    @Override
    public Set<String> getAllNamespaces() {
        return namespaces;
    }

    @Override
    public Resource getResource(Identifier id) throws IOException {
        String[] parts = id.getPath().split(SEPARATOR);
        String zipFile = parts[0];
        String zipPath = parts[1];
        return new LocalRecipeResource(zipFile, zipPath);

    }

    @Override
    public boolean containsResource(Identifier id) {
        return true;
    }

    @Override
    public List<Resource> getAllResources(Identifier id) throws IOException {
        List<Resource> result = new ArrayList<>();
        result.add(getResource(id));
        return result;
    }
    
    @Override
    public Collection<Identifier> findResources(String resourceType, Predicate<String> pathPredicate) {
        final Set<Identifier> result = new HashSet<>();
        
        for (String filename: forcedZips) {
            try {
                final ZipFile zip = new ZipFile(new File(filename));
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (pathPredicate.test(entry.getName())) {
                        result.add(new Identifier(EasierCrafting.MODID, filename.replace('\\', '/') + SEPARATOR + entry.getName()));
                    }
                }
            } catch (IOException ex) {
                LOGGER.info("In "+filename, ex);
            }
        }
        return result;
    }
}

class LocalRecipeResource implements Resource {

    String zipName, entryName;
    LocalRecipeResource(String zipName, String entryName) {
        this.zipName = zipName;
        this.entryName = entryName;
    }
    
    @Override
    public Identifier getId() {
        return new Identifier(EasierCrafting.MODID, zipName.replace('\\', '/')+LocalRecipeManager.SEPARATOR+entryName);
    }

    @Override
    public InputStream getInputStream() {
        try {
            ZipFile file = new ZipFile(this.zipName);
            ZipEntry entry = file.getEntry(this.entryName);
            return file.getInputStream(entry);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    @Override
    public boolean hasMetadata() { 
        return false;
    }

    @Override
    public <T> T getMetadata(ResourceMetadataReader<T> metaReader) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getResourcePackName() {
        return this.zipName;
    }

    @Override
    public void close() throws IOException {

    }
}