package com.samsthenerd.inline.xplat;

import java.util.Optional;
import java.util.function.Function;

public class XPlatInstances {
    public final Function<String, Optional<IModMeta>> modFactory;
    public final IXPlatAbstractions abs;

    public XPlatInstances(Function<String, Optional<IModMeta>> modFactory, IXPlatAbstractions abs){
        this.modFactory = modFactory;
        this.abs = abs;
    }

}
