package com.samsthenerd.inline.xplat;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.samsthenerd.inline.Inline;

/*
 * Literally just copied from architectury API
 */
public interface IModMeta {

    public static Optional<IModMeta> getMod(String modid){
        return Inline.getXPlats().modFactory.apply(modid);
    }

    String getModId();
    
    String getVersion();
    
    String getName();
    
    String getDescription();
    
    /**
     * Gets the logo file path of the mod
     *
     * @param preferredSize the preferred logo size, only used in fabric
     * @return the logo file path relative to the file
     */
    Optional<String> getLogoFile(int preferredSize);
    
    /**
     * Gets a list of all possible root paths for the mod.
     * This is especially relevant on Fabric, as a single mod may have multiple source sets
     * (such as client / server-specific ones), each corresponding to one root path.
     *
     * @return A list of root paths belonging to the mod
     */
    List<Path> getFilePaths();
    
    /**
     * @deprecated Use {@link #getFilePaths()} instead
     */
    @Deprecated(forRemoval = true)
    Path getFilePath();
    
    /**
     * Gets an NIO Path to the given resource contained within the mod file / folder.
     * The path is verified to exist, and an empty optional is returned if it doesn't.
     *
     * @param path The resource to search for
     * @return The path of the resource if it exists, or {@link Optional#empty()} if it doesn't
     */
    Optional<Path> findResource(String... path);
    
    Collection<String> getAuthors();
    
    @Nullable
    Collection<String> getLicense();
    
    Optional<String> getHomepage();
    
    Optional<String> getSources();
    
    Optional<String> getIssueTracker();
}
