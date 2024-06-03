package com.samsthenerd.inline.xplat;

import java.util.Optional;
import java.util.function.Function;

public class XPlatInstances {
    public final Function<String, Optional<IModMeta>> modFactory;

    public XPlatInstances(Function<String, Optional<IModMeta>> modFactory){
        this.modFactory = modFactory;
    }

}
