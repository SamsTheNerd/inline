package com.samsthenerd.inline.api.data;

import java.util.Optional;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.InlineData;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public class EntityInlineData implements InlineData{

    private Entity entity = null;
    private float uniqueOffset;

    protected static final Random random = Random.create();

    public Identifier getDataType(){
        return new Identifier(Inline.MOD_ID, "entity");
    }

    public Identifier getRendererId(){
        return new Identifier(Inline.MOD_ID, "entity");
    }

    public IDSerializer<EntityInlineData> getSerializer(){
        return Serializer.INSTANCE;
    }

    public EntityInlineData(Entity entity){
        this.entity = entity;
        this.uniqueOffset = random.nextFloat();
    }

    @Nullable
    public Entity getEntity(){
        return entity;
    }

    public float getUniqueOffset(){
        return uniqueOffset;
    }

    public static class Serializer implements InlineData.IDSerializer<EntityInlineData> {

        public static Serializer INSTANCE = new Serializer();

        public EntityInlineData deserialize(JsonObject json){
            if(json.has("entity")){
                try{
                    NbtCompound tag = StringNbtReader.parse(json.get("entity").getAsString());
                    Optional<Entity> maybeEnt = EntityType.getEntityFromNbt(tag, MinecraftClient.getInstance().world);
                    if(maybeEnt.isPresent()){
                        return new EntityInlineData(maybeEnt.get());
                    }
                } catch (Exception e){
                    return null;
                }
            }
            return null;
        }

        public JsonObject serializeData(EntityInlineData data){
            try{
                if(data.entity == null || !data.entity.getType().isSaveable()) return new JsonObject();
                NbtCompound tag = new NbtCompound();
                data.entity.saveNbt(tag);
                JsonObject json = new JsonObject();
                json.addProperty("entity", tag.asString());
                return json;
            } catch (Exception e){
                return new JsonObject();
            }
        }
    }
}

