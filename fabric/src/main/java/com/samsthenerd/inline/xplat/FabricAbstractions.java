package com.samsthenerd.inline.xplat;

import net.fabricmc.loader.api.FabricLoader;

public class FabricAbstractions implements IXPlatAbstractions {
    public boolean isDevEnv(){
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
}
