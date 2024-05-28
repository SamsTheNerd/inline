package com.samsthenerd.inline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.architectury.platform.Platform;

// this will probably be bumped out into its own mod Soon, but i want to get it working in this test environment first
public class Inline {

    public static final String MOD_ID = "inline";

    public static final Logger LOGGER = LoggerFactory.getLogger("inline");

	public static final void logPrint(String message){
		if(Platform.isDevelopmentEnvironment())
			LOGGER.info(message);
	}

    public static void onInitialize(){
        // nothing yet !
    }
}
