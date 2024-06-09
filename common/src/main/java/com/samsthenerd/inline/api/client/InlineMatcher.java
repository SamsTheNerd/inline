package com.samsthenerd.inline.api.client;

import net.minecraft.util.Identifier;

public interface InlineMatcher {
    public void match(MatchContext matchContext);

    public MatcherInfo getInfo();

    public Identifier getId();
}
