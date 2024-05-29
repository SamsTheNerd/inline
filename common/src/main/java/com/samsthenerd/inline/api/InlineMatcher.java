package com.samsthenerd.inline.api;

import net.minecraft.util.Identifier;

public interface InlineMatcher {
    public InlineMatchResult match(String input);

    public MatcherInfo getInfo();

    public Identifier getId();
}
