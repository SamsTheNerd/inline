package com.samsthenerd.inline.xplat;

public class XPlat {
    private static final XPlatInstances PLAT = new XPlatInstances(
      FabricModMeta::getMod,
      new FabricAbstractions()
    );

    public static XPlatInstances getPlat() {
        return PLAT;
    }
}
