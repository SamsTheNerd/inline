package com.samsthenerd.inline.impl.data;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.api.data.EntityInlineData;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class NbtEntityInlineData extends EntityInlineData{
    private NbtCompound entNbt;
    private Entity cachedEntity;

    protected static final Random random = Random.create();

    @Override
    public Identifier getDataType(){
        return new Identifier(Inline.MOD_ID, "entitynbt");
    }

    @Override
    public IDSerializer<NbtEntityInlineData> getSerializer(){
        return Serializer.INSTANCE;
    }

    public NbtEntityInlineData(Entity entity){
        this.cachedEntity = entity;
        this.entNbt = new NbtCompound();
        entity.saveNbt(entNbt);
        this.uniqueOffset = random.nextFloat();
    }

    public NbtEntityInlineData(NbtCompound tag){
        this.entNbt = tag;
        this.uniqueOffset = random.nextFloat();
    }

    @Override
    @Nullable
    public Entity getEntity(World world){
        if(cachedEntity != null){
            return cachedEntity;
        }
        try{
            Entity newEnt = EntityType.getEntityFromNbt(entNbt, world).orElse(null);
            cachedEntity = newEnt;
            return cachedEntity;
        } catch (Exception e){
            return null;
        }
    }

    public static class Serializer implements InlineData.IDSerializer<NbtEntityInlineData> {

        public static Serializer INSTANCE = new Serializer();

        public NbtEntityInlineData deserialize(JsonObject json){
            if(json.has("entity")){
                try{
                    NbtCompound tag = StringNbtReader.parse(json.get("entity").getAsString());
                    return new NbtEntityInlineData(tag);
                } catch (Exception e){
                    return null;
                }
            }
            return null;
        }

        public JsonObject serializeData(NbtEntityInlineData data){
            try{
                JsonObject json = new JsonObject();
                json.addProperty("entity", data.entNbt.asString());
                return json;
            } catch (Exception e){
                return new JsonObject();
            }
        }
    }
}
