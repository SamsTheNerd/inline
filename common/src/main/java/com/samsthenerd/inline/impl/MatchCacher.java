package com.samsthenerd.inline.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.datafixers.util.Either;
import com.samsthenerd.inline.api.client.InlineClientAPI;
import com.samsthenerd.inline.api.client.InlineClientConfig;
import com.samsthenerd.inline.api.matching.InlineMatcher;
import com.samsthenerd.inline.api.matching.MatchContext;
import net.minecraft.text.Text;

import java.time.Duration;

public class MatchCacher {
    private static final LoadingCache<Either<String, Text>, MatchContext> MATCH_CACHE = CacheBuilder.newBuilder()
            .maximumSize(250) // should be a decent size ?
            .expireAfterAccess(Duration.ofMinutes(5))
            .build(CacheLoader.from(input -> {
                MatchContext matchContext = input.map(MatchContext::forInput, MatchContext::forTextInput);

                InlineClientConfig config = InlineClientAPI.INSTANCE.getConfig();

                // run all the matchers
                for(InlineMatcher matcher : InlineClientAPI.INSTANCE.getAllMatchers()){
                    if(!config.isMatcherEnabled(matcher.getId())) continue;
                    matcher.match(matchContext);
                }

                matchContext.freeze();
                return matchContext;
            }));

    public static MatchContext getMatch(Either<String, Text> input){
        return MATCH_CACHE.getUnchecked(input);
    }

    public static void clear(){
        MATCH_CACHE.invalidateAll();
    }
}
