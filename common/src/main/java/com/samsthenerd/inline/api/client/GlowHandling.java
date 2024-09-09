package com.samsthenerd.inline.api.client;

public sealed class GlowHandling permits GlowHandling.None, GlowHandling.Full {
    public static final class None extends GlowHandling{}
    public static final class Full extends GlowHandling{
        public final String cacheId;
        public Full(){ cacheId = null; }
        public Full(String cacheId){ this.cacheId = cacheId;}
    }
}
