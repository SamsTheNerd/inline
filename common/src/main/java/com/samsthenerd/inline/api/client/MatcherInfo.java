package com.samsthenerd.inline.api.client;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MatcherInfo {
    private final Text rawTitle;
    private final Text styledTitle;
    private final Text example;
    private final Text description;

    public MatcherInfo(Text rawTitle, Text styledTitle, Text example, Text description){
        this.rawTitle = rawTitle;
        this.styledTitle = styledTitle;
        this.example = example;
        this.description = description;
    }

    public static MatcherInfo fromId(Identifier id){
        return new MatcherInfo(
            Text.translatable("matcher." + id.getNamespace() + "." + id.getPath() + ".title"),
            Text.translatable("matcher." + id.getNamespace() + "." + id.getPath() + ".title.styled"),
            Text.translatable("matcher." + id.getNamespace() + "." + id.getPath() + ".example"),
            Text.translatable("matcher." + id.getNamespace() + "." + id.getPath() + ".description")
        );
    }

    public Text getTitle(boolean styled){
        if(styled){
            return styledTitle;
        }
        return rawTitle;
    }

    public Text getExample(){
        return example;
    }

    public Text getDescription(){
        return description;
    }
}
