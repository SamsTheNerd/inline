package com.samsthenerd.inline.impl.data;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.api.data.EntityInlineData;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class EntityTypeInlineData extends EntityInlineData{
    private EntityType<?> type;
    private Entity cachedEntity;

    protected static final Random random = Random.create();

    @Override
    public Identifier getDataType(){
        return new Identifier(Inline.MOD_ID, "entitynbt");
    }

    @Override
    public IDSerializer<EntityTypeInlineData> getSerializer(){
        return Serializer.INSTANCE;
    }

    public EntityTypeInlineData(EntityType<?> type){
        this.type = type;
        this.uniqueOffset = random.nextFloat();
    }

    @Override
    @Nullable
    public Entity getEntity(World world){
        if(cachedEntity != null){
            return cachedEntity;
        }
        try{
            Entity newEnt = type.create(world);
            cachedEntity = newEnt;
            return cachedEntity;
        } catch (Exception e){
            return null;
        }
    }

    public static class Serializer implements InlineData.IDSerializer<EntityTypeInlineData> {

        public static Serializer INSTANCE = new Serializer();

        public EntityTypeInlineData deserialize(JsonObject json){
            if(json.has("entitytype")){
                try{
                    String typeId = json.get("entitytype").getAsString();
                    EntityType type = EntityType.get(typeId).orElse(null);
                    if(type == null) return null;
                    return new EntityTypeInlineData(type);
                } catch (Exception e){
                    return null;
                }
            }
            return null;
        }

        public JsonObject serializeData(EntityTypeInlineData data){
            try{
                JsonObject json = new JsonObject();
                json.addProperty("entitytype", EntityType.getId(data.type).toString());
                return json;
            } catch (Exception e){
                return new JsonObject();
            }
        }
    }
}
