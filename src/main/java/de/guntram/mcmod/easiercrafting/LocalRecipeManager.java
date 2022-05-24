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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

/* disabled for now -- I don't know how to port this


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
        Profiler profiler = new EmptyProfiler();
        Map<Identifier, JsonElement> map = instance.prepare(instance, profiler);
        instance.apply(map, instance, profiler);
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
    public Optional<Resource> getResource(Identifier id) {
        String[] parts = id.getPath().split(SEPARATOR);
        String zipFile = parts[0];
        String zipPath = parts[1];
        return Optional.of(new LocalRecipeResource(zipFile, zipPath));
    }

    @Override
    public List<Resource> getAllResources(Identifier id) {
        List<Resource> result = new ArrayList<>();
        result.add(getResource(id).get());
        return result;
    }
    
    @Override
    public Map<Identifier, Resource> findResources(String resourceType, Predicate<Identifier> pathPredicate) {
        final Map<Identifier, Resource> result = new HashMap<>();
        
        for (String filename: forcedZips) {
            try {
                final ZipFile zip = new ZipFile(new File(filename));
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    Identifier id = new Identifier(EasierCrafting.MODID, filename.replace('\\', '/') + SEPARATOR + entry.getName());
                    if (pathPredicate.test(id)) {
                        result.put(id, getResource(id).get());
                    }
                }
            } catch (IOException ex) {
                LOGGER.info("In "+filename, ex);
            }
        }
        return result;
    }
}

class LocalRecipeResource extends Resource {

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

*/