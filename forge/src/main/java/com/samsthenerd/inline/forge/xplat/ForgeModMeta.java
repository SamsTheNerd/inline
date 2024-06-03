package com.samsthenerd.inline.forge.xplat;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.samsthenerd.inline.xplat.IModMeta;

import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;

public class ForgeModMeta implements IModMeta {
    private final ModContainer container;
    private final IModInfo info;
    
    public ForgeModMeta(String id) {
        this.container = ModList.get().getModContainerById(id).orElseThrow();
        this.info = ModList.get().getMods().stream()
                .filter(modInfo -> Objects.equals(modInfo.getModId(), id))
                .findAny()
                .orElseThrow();
    }

    public static Optional<IModMeta> getMod(String modid){
        try{
            return Optional.of(new ForgeModMeta(modid));
        } catch (NoSuchElementException e){
            return Optional.empty();
        }
    }
    
    @Override
    @NotNull
    public String getModId() {
        return info.getModId();
    }
    
    @Override
    @NotNull
    public String getVersion() {
        return info.getVersion().toString();
    }
    
    @Override
    @NotNull
    public String getName() {
        return info.getDisplayName();
    }
    
    @Override
    @NotNull
    public String getDescription() {
        return info.getDescription();
    }
    
    @Override
    public Optional<String> getLogoFile(int i) {
        return this.info.getLogoFile();
    }
    
    @Override
    public List<Path> getFilePaths() {
        return List.of(getFilePath());
    }
    
    @Override
    public Path getFilePath() {
        return this.info.getOwningFile().getFile().getSecureJar().getRootPath();
    }
    
    @Override
    public Optional<Path> findResource(String... path) {
        return Optional.of(this.info.getOwningFile().getFile().findResource(path)).filter(Files::exists);
    }
    
    @Override
    public Collection<String> getAuthors() {
        Optional<String> optional = this.info.getConfig().getConfigElement("authors")
                .map(String::valueOf);
        return optional.isPresent() ? Collections.singleton(optional.get()) : Collections.emptyList();
    }
    
    @Override
    public @Nullable Collection<String> getLicense() {
        return Collections.singleton(this.info.getOwningFile().getLicense());
    }
    
    @Override
    public Optional<String> getHomepage() {
        return this.info.getConfig().getConfigElement("displayURL")
                .map(String::valueOf);
    }
    
    @Override
    public Optional<String> getSources() {
        return Optional.empty();
    }
    
    @Override
    public Optional<String> getIssueTracker() {
        IModFileInfo owningFile = this.info.getOwningFile();
        if (owningFile instanceof ModFileInfo info) {
            return Optional.ofNullable(info.getIssueURL())
                    .map(URL::toString);
        }
        return Optional.empty();
    }
}
