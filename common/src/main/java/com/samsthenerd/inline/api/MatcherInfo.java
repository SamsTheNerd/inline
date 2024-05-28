package com.samsthenerd.inline.api;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MatcherInfo {
    private final Text title;
    private final Text example;
    private final Text description;

    public MatcherInfo(Text title, Text example, Text description){
        this.title = title;
        this.example = example;
        this.description = description;
    }

    public static MatcherInfo fromId(Identifier id){
        return new MatcherInfo(
            Text.translatable("matcher." + id.getNamespace() + "." + id.getPath() + ".title"),
            Text.translatable("matcher." + id.getNamespace() + "." + id.getPath() + ".example"),
            Text.translatable("matcher." + id.getNamespace() + "." + id.getPath() + ".description")
        );
    }

    public Text getTitle(){
        return title;
    }

    public Text getExample(){
        return example;
    }

    public Text getDescription(){
        return description;
    }
}