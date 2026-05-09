package com.bazaarflip;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BazaarFlipMod implements ClientModInitializer {

    public static final String MOD_ID = "bazaarflip";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final BazaarTracker TRACKER = new BazaarTracker();

    @Override
    public void onInitializeClient() {
        LOGGER.info("[BazaarFlip] Loaded! Use /bflip <item name> to start tracking.");

        // Register client-side command /bflip
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                BazaarCommand.register(dispatcher)
        );

        // Register HUD overlay
        HudRenderCallback.EVENT.register(HudRenderer::render);

        // Listen to chat messages from Hypixel (order fills, etc.)
        ClientReceiveMessageEvents.GAME.register((message, overlay) ->
                ChatListener.onMessage(message.getString())
        );
    }
}
