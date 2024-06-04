package com.samsthenerd.inline.xplat;

import com.samsthenerd.inline.Inline;

public interface IXPlatAbstractions {

    public static IXPlatAbstractions getInstance(){
        return Inline.getXPlats().abs;
    }

    public boolean isDevEnv();
}
