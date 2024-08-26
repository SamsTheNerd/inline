package com.samsthenerd.inline.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.client.InlineClientConfig;

import me.shedaniel.cloth.clothconfig.shadowed.com.moandjiezana.toml.Toml;
import me.shedaniel.cloth.clothconfig.shadowed.com.moandjiezana.toml.TomlWriter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

// still should only really be on the client i think ?
public class InlineClientConfigImpl implements InlineClientConfig {

    private static final String clientConfigSubPath = "config" + File.separator + "inline" + File.separator + "client-config.toml";

    public static final TomlWriter TOML_WRITER = new TomlWriter.Builder()
        .indentValuesBy(2)
        .indentTablesBy(4)
        .padArrayDelimitersBy(3)
        .build();

    private File configFile;

    private static InlineClientConfigImpl INSTANCE;

    private Set<Identifier> disabledMatchers = new HashSet<>();
    private boolean renderModIcon = true;
    private boolean doCreateMixin = true;
    private double chatScaleCap = 1.5;

    private boolean dirty = false;


    public static InlineClientConfigImpl getInstance(){
        if(INSTANCE == null){
            INSTANCE = new InlineClientConfigImpl(new File(MinecraftClient.getInstance().runDirectory, clientConfigSubPath));
        }
        return INSTANCE;
    }

    private InlineClientConfigImpl(File configFile){
        this.configFile = configFile;
        if(!this.configFile.exists()){
            File dir = this.configFile.getParentFile();
            if(!dir.exists()){
                dir.mkdirs();
            }
            writeToFile();
        } else {
            reloadFromFile();
        }
    }

    protected void reloadFromFile(){
        try{
            Toml config = (new Toml()).read(configFile);
            List<String> dMatchers = config.getList("disabledMatchers", List.of());
            disabledMatchers.clear();
            for(String mId : dMatchers){
                disabledMatchers.add(new Identifier(mId));                
            }

            renderModIcon = config.getBoolean("modIconInTooltip", true);
            dirty = false;
        } catch (Exception e){
            Inline.LOGGER.error("Failed to read inline client config file: " + configFile.getAbsolutePath(), e);
        }
    }

    protected void writeToFile(){
        Map<String, Object> configMap = new HashMap<>();
        List<String> disabledMatcherIds = new ArrayList<>();
        for(Identifier matcherId : disabledMatchers){
            disabledMatcherIds.add(matcherId.toString());
        }
        configMap.put("disabledMatchers", disabledMatcherIds);
        configMap.put("modIconInTooltip", renderModIcon);
        dirty = false;
        try{
            TOML_WRITER.write(configMap, configFile);
        } catch (Exception e){
            Inline.LOGGER.error("Failed to write inline client config file: " + configFile.getAbsolutePath(), e);
        }
    }

    protected void save(){
        if(dirty){
            writeToFile();
        }
    }

    protected void enableMatcher(Identifier matcherId){
        disabledMatchers.remove(matcherId);
        dirty = true;
    }

    protected void disableMatcher(Identifier matcherId){
        disabledMatchers.add(matcherId);
        dirty = true;
    }

    protected void someableMatcher(Identifier matcherId, boolean enableOrDisable){
        if(enableOrDisable){
            enableMatcher(matcherId);
        } else {
            disableMatcher(matcherId);
        }
    }

    @Override
    public boolean isMatcherEnabled(Identifier matcherId){
        return !disabledMatchers.contains(matcherId);
    }

    @Override
    public boolean shouldRenderModIcons(){
        return renderModIcon;
    }

    public void setShouldRenderModIcons(boolean should){
        renderModIcon = should;
        dirty = true;
    }

    @Override
    public boolean shouldDoCreateMixins(){
        return doCreateMixin;
    }

    public void setShouldDoCreateMixins(boolean should){
        doCreateMixin = should;
        dirty = true;
    }

    @Override
    public double maxChatSizeModifier(){
        return chatScaleCap;
    }

    public void setChatScaleCap(double newCap){
        chatScaleCap = newCap;
        dirty = true;
    }
}
