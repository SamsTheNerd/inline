package com.samsthenerd.inline.utils;

public record IntPair(int first, int second) {
    public int width(){ return first(); }
    public int height(){ return second(); }

    public int x(){ return first(); }
    public int y(){ return second(); }
}
