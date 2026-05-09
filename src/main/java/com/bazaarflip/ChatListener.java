package com.bazaarflip;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.text.DecimalFormat;

/**
 * Listens to incoming chat messages from Hypixel and:
 *  1. Detects order-filled notifications
 *  2. Periodically sends in-game alerts when undercut is detected
 */
public class ChatListener {

    private static final DecimalFormat DF = new DecimalFormat("#,##0.0");

    public static void onMessage(String rawMessage) {
        // Strip Minecraft color codes
        String msg = rawMessage.replaceAll("§[0-9a-fk-orA-FK-OR]", "");

        BazaarTracker tracker = BazaarFlipMod.TRACKER;
        if (tracker.getTrackedItem() == null) return;

        // ── Hypixel Bazaar order-completed messages ───────────────────────────
        // Example: "[Bazaar] Completed buy order for 50x Wheat for 500 coins!"
        if (msg.contains("[Bazaar]")) {
            if (msg.contains("Completed buy order") || msg.contains("Completed sell order")) {
                sendHint("§a✔ Order filled! Go re-place it at the best price.");
            }
        }

        // ── Periodic undercut alert (throttled inside shouldAlert()) ──────────
        if (tracker.shouldAlert()) {
            if (tracker.isBuyUndercut()) {
                sendHint("§c⚠ BUY undercut! Cancel & set to §a§l"
                        + DF.format(tracker.getRecommendedBuyPrice()) + " coins");
            }
            if (tracker.isSellUndercut()) {
                sendHint("§c⚠ SELL undercut! Cancel & set to §a§l"
                        + DF.format(tracker.getRecommendedSellPrice()) + " coins");
            }
        }
    }

    /** Sends a client-side message (only visible to the local player). */
    private static void sendHint(String text) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("§6[BazaarFlip] " + text), false);
        }
    }
}
