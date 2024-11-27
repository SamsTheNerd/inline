package com.samsthenerd.inline.neoforge.xplat;

import com.samsthenerd.inline.xplat.IXPlatAbstractions;
import net.neoforged.fml.loading.FMLLoader;


public class ForgeAbstractions implements IXPlatAbstractions {
    public boolean isDevEnv(){
        return !FMLLoader.isProduction();
    }
}
