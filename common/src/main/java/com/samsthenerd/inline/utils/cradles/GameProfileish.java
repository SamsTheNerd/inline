package com.samsthenerd.inline.utils.cradles;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.util.Uuids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

// somewhere between a GameProfile and an either. it *can* have both, is *guaranteed* to have one
public class GameProfileish {
    @Nullable
    private final String maybeName;
    @Nullable
    private final UUID maybeID;

    // only keep one ig? whatever it's fine
    public static final Codec<GameProfileish> GAME_PROFILEISH_CODEC = Codec.either(
        Codec.STRING, Uuids.CODEC
    ).xmap(GameProfileish::new, gpish -> gpish.map(Either::left, Either::right));


    // atleast one of these needs to be non-null
    private GameProfileish(@Nullable String maybeName, @Nullable UUID maybeID){
        if(maybeName == null && maybeID == null){
            throw new IllegalArgumentException("one of name or id must be non-empty");
        }
        this.maybeName = maybeName;
        this.maybeID = maybeID;
    }

    public GameProfileish(@NotNull String name){
        this(name, null);
    }

    public GameProfileish(@NotNull UUID id){
        this(null, id);
    }

    public GameProfileish(Either<String, UUID> nameOrID){
        this(nameOrID.left().orElse(null), nameOrID.right().orElse(null));
    }

    public <T> T map(Function<String, ? extends T> nameMap, Function<UUID, ? extends T> idMap){
        if(maybeName != null){
            return nameMap.apply(maybeName);
        } else {
            return idMap.apply(maybeID);
        }
    }

    public CompletableFuture<Optional<GameProfile>> fetchProfile(){
        return map(SkullBlockEntity::fetchProfileByName, SkullBlockEntity::fetchProfileByUuid);
    }

    private GameProfile makeupProfile(){
        return map(Uuids::getOfflinePlayerProfile, id -> new GameProfile(id, "UnknownPlayerName"));
    }

    public Optional<String> getName(){
        return Optional.ofNullable(maybeName);
    }

    public Optional<UUID> getID(){
        return Optional.ofNullable(maybeID);
    }

    // gets some profile, either the proper one or a close enough one
    public GameProfile fetchSomeProfile(){
        return fetchProfile().getNow(Optional.empty()).orElse(makeupProfile());
    }
}
