package com.samsthenerd.inline.api;

public interface InlineMatcher {
    public InlineMatchResult match(String input);

    public MatcherInfo getInfo();
}
