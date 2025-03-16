package com.samsthenerd.inline.neoforge.xplat;

import com.samsthenerd.inline.xplat.XPlatInstances;

public class XPlat {
    private static final XPlatInstances PLAT = new XPlatInstances(
      ForgeModMeta::getMod,
      new ForgeAbstractions()
    );

    public static XPlatInstances getPlat() {
        return PLAT;
    }
}
