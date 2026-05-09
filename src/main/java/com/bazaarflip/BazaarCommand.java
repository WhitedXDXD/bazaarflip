package com.bazaarflip;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

/**
 * Registers the /bflip command (client-side only, no server needed).
 *
 *  /bflip <item name>   — start tracking an item
 *  /bflip               — stop tracking / show current item
 */
public class BazaarCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {

        dispatcher.register(
            ClientCommandManager.literal("bflip")

                // /bflip <item name>  →  start tracking
                .then(ClientCommandManager.argument("item", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        String item = StringArgumentType.getString(ctx, "item");
                        BazaarFlipMod.TRACKER.setTrackedItem(item);
                        ctx.getSource().sendFeedback(Text.literal(
                            "§6[BazaarFlip] §aNow tracking: §f" + item
                            + "\n§7Open the Bazaar and navigate to that item."
                        ));
                        return 1;
                    })
                )

                // /bflip  →  stop or show status
                .executes(ctx -> {
                    BazaarTracker t = BazaarFlipMod.TRACKER;
                    if (t.getTrackedItem() == null) {
                        ctx.getSource().sendFeedback(Text.literal(
                            "§6[BazaarFlip] §7No item tracked. Use §f/bflip <item name>§7."
                        ));
                    } else {
                        ctx.getSource().sendFeedback(Text.literal(
                            "§6[BazaarFlip] §7Stopped tracking §f" + t.getTrackedItem() + "§7."
                        ));
                        BazaarFlipMod.TRACKER.setTrackedItem(null);
                    }
                    return 1;
                })
        );
    }
}
