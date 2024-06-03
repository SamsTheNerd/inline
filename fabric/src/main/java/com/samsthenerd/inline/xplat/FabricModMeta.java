package com.samsthenerd.inline.xplat;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;

/*
 * Again, literally just copied from arch api
 */
public class FabricModMeta implements IModMeta {
    private final ModContainer container;
    private final ModMetadata metadata;
    
    private FabricModMeta(String id) {
        this.container = FabricLoader.getInstance().getModContainer(id).orElseThrow();
        this.metadata = this.container.getMetadata();
    }

    public static Optional<IModMeta> getMod(String modid){
        try{
            return Optional.of(new FabricModMeta(modid));
        } catch(NoSuchElementException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public String getModId() {
        return metadata.getId();
    }
    
    @Override
    public String getVersion() {
        return metadata.getVersion().getFriendlyString();
    }
    
    @Override
    public String getName() {
        return metadata.getName();
    }
    
    @Override
    public String getDescription() {
        return metadata.getDescription();
    }
    
    @Override
    public Optional<String> getLogoFile(int preferredSize) {
        return metadata.getIconPath(preferredSize);
    }

    @Override
    public List<Path> getFilePaths() {
        return container.getRootPaths();
    }

    @Override
    public Path getFilePath() {
        return container.getRootPath();
    }

    @Override
    public Optional<Path> findResource(String... path) {
        return container.findPath(String.join("/", path));
    }

    @Override
    public Collection<String> getAuthors() {
        return metadata.getAuthors().stream()
                .map(Person::getName)
                .collect(Collectors.toList());
    }
    
    @Override
    public @Nullable Collection<String> getLicense() {
        return metadata.getLicense();
    }
    
    @Override
    public Optional<String> getHomepage() {
        return metadata.getContact().get("homepage");
    }
    
    @Override
    public Optional<String> getSources() {
        return metadata.getContact().get("issues");
    }
    
    @Override
    public Optional<String> getIssueTracker() {
        return metadata.getContact().get("sources");
    }
}
