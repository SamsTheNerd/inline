package com.samsthenerd.inline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samsthenerd.inline.api.InlineAPI;
import com.samsthenerd.inline.api.data.EntityInlineData.EntityDataType;
import com.samsthenerd.inline.api.data.ItemInlineData.ItemDataType;
import com.samsthenerd.inline.api.data.ModIconData.ModIconDataType;
import com.samsthenerd.inline.api.data.PlayerHeadData.PlayerHeadDataType;
import com.samsthenerd.inline.api.data.SpriteInlineData.SpriteDataType;
import com.samsthenerd.inline.xplat.XPlatInstances;

// this will probably be bumped out into its own mod Soon, but i want to get it working in this test environment first
public class Inline {

    public static final String MOD_ID = "inline";

    public static final Logger LOGGER = LoggerFactory.getLogger("inline");

    public static XPlatInstances getXPlats(){
        return xPlats;
    }

	public static final void logPrint(String message){
        // if(IXPlatAbstractions.getInstance().isDevEnv())
			LOGGER.info(message);
	}

    private static XPlatInstances xPlats;

    public static void onInitialize(XPlatInstances xPlats){
        // nothing yet !
        Inline.xPlats = xPlats;
        registerDataTypes();
    }

    private static void registerDataTypes(){
        InlineAPI.INSTANCE.addDataType(EntityDataType.INSTANCE);
        InlineAPI.INSTANCE.addDataType(ItemDataType.INSTANCE);
        InlineAPI.INSTANCE.addDataType(ModIconDataType.INSTANCE);
        InlineAPI.INSTANCE.addDataType(PlayerHeadDataType.INSTANCE);
        InlineAPI.INSTANCE.addDataType(SpriteDataType.INSTANCE);
    }
}
