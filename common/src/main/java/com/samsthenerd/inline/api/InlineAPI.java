package com.samsthenerd.inline.api;

import java.util.Set;

import com.samsthenerd.inline.impl.InlineImpl;

import net.minecraft.util.Identifier;

public abstract class InlineAPI {

    public static final InlineAPI INSTANCE = new InlineImpl();

    public abstract void addMatcher(Identifier id, InlineMatcher matcher);

    public abstract InlineMatcher getMatcher(Identifier id);

    public abstract Set<InlineMatcher> getAllMatchers();

}
