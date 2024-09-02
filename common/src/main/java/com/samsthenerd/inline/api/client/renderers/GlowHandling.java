package com.samsthenerd.inline.api.client.renderers;

import java.util.function.IntUnaryOperator;

public sealed class GlowHandling permits GlowHandling.None, GlowHandling.Full {
    public static final class None extends GlowHandling{}
    public static final class Full extends GlowHandling{
        public final IntUnaryOperator blender;
        public Full(){ blender = color -> color; } // TODO: make this like, work
        public Full(IntUnaryOperator blender){ this.blender = blender;}
    }
}
