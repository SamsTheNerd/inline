package com.samsthenerd.inline.api.client.matchers;

import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.samsthenerd.inline.api.client.InlineMatch;
import com.samsthenerd.inline.api.client.InlineMatch.TextMatch;
import com.samsthenerd.inline.api.client.MatcherInfo;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

public interface RegexMatcher extends ContinuousMatcher {
    public default ContinuousMatchResult match(String input){
        Matcher regexMatcher = getRegex().matcher(input);
        ContinuousMatchResult result = new ContinuousMatchResult();
        while(regexMatcher.find()){
            MatchResult mr = regexMatcher.toMatchResult();
            Pair<InlineMatch, Integer> matchAndGroup = getMatchAndGroup(mr);
            if(matchAndGroup.getLeft() == null) continue;
            result.addMatch(mr.start(matchAndGroup.getRight()), mr.end(matchAndGroup.getRight()), matchAndGroup.getLeft());
        }
        return result;
    }

    public Pattern getRegex();

    @NotNull
    public default Pair<InlineMatch, Integer> getMatchAndGroup(MatchResult regexMatch){
        return new Pair<>(getMatch(regexMatch), 0);
    }

    @Nullable
    public InlineMatch getMatch(MatchResult regexMatch);

    public static class Simple implements RegexMatcher {
        private Pattern regex;
        private Function<MatchResult, InlineMatch> matcher;
        private MatcherInfo info;
        private Identifier id;

        public Simple(Pattern regex, Identifier id, Function<MatchResult, InlineMatch> matcher, MatcherInfo info){
            this.regex = regex;
            this.matcher = matcher;
            this.info = info;
            this.id = id;
        }

        public Simple(String regex, Identifier id, Function<MatchResult, InlineMatch> matcher, MatcherInfo info){
            this(Pattern.compile(regex), id, matcher, info);
        }

        public Pattern getRegex(){
            return regex;
        }

        public InlineMatch getMatch(MatchResult regexMatch){
            return matcher.apply(regexMatch);
        }

        public MatcherInfo getInfo(){
            return info;
        }

        public Identifier getId(){
            return id;
        }
    }

    public static class Standard implements RegexMatcher{

        public static String IDENTIFIER_REGEX = "(?:[0-9a-z._-]+:)?[0-9a-z._\\/-]+";

        private final Pattern regex;
        private final Function<String, InlineMatch> matcher;
        private final MatcherInfo info;
        private final Identifier id;

        public Standard(String namespace, String innerRegex, Identifier id, Function<String, InlineMatch> matcher, MatcherInfo info){
            regex = Pattern.compile("(\\\\)?\\[" + namespace + ":(" + innerRegex + ")\\]");
            this.id = id;
            this.info = info;
            this.matcher = matcher;
        }

        public Pattern getRegex(){
            return regex;
        }

        public InlineMatch getMatch(MatchResult regexMatch){
            return matcher.apply(regexMatch.group(2));
        }

        @NotNull
        public Pair<InlineMatch, Integer> getMatchAndGroup(MatchResult regexMatch){
            if(regexMatch.group(1) != null){
                return new Pair<>(new TextMatch(Text.literal("")), 1);
            }
            return new Pair<>(getMatch(regexMatch), 0);
        }

        public MatcherInfo getInfo(){
            return info;
        }

        public Identifier getId(){
            return id;
        }
    }
}
