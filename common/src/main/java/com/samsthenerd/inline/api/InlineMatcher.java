package com.samsthenerd.inline.api;

@FunctionalInterface
public interface InlineMatcher {
    public InlineMatchResult match(String input);
}
