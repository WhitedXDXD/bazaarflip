package com.bazaarflip;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

public class BazaarCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {

        dispatcher.register(
            ClientCommandManager.literal("bflip")
                .then(ClientCommandManager.argument("item", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        String item = StringArgumentType.getString(ctx, "item");
                        BazaarFlipMod.TRACKER.setTrackedItem(item);
                        ctx.getSource().sendFeedback(Text.literal(
                            "§6[BazaarFlip] §aNow tracking: §f" + item
                        ));
                        return 1;
                    })
                )
                .executes(ctx -> {
                    BazaarTracker t = BazaarFlipMod.TRACKER;
                    if (t.getTrackedItem() == null) {
                        ctx.getSource().sendFeedback(Text.literal(
                            "§6[BazaarFlip] §7Use §f/bflip <item>§7 to start."
                        ));
                    } else {
                        ctx.getSource().sendFeedback(Text.literal(
                            "§6[BazaarFlip] §7Stopped tracking §f" + t.getTrackedItem()
                        ));
                        BazaarFlipMod.TRACKER.setTrackedItem(null);
                    }
                    return 1;
                })
        );

        // /bflipdebug — prints lore of all GUI items to chat
        dispatcher.register(
            ClientCommandManager.literal("bflipdebug")
                .executes(ctx -> {
                    MinecraftClient mc = MinecraftClient.getInstance();
                    if (mc.currentScreen == null) {
                        ctx.getSource().sendFeedback(Text.literal("§cOpen the Bazaar GUI first, then run this command... wait, open GUI then run in chat? Use F3+D to clear chat first, open Bazaar, then close it and run /bflipdebug"));
                        return 1;
                    }
                    if (!(mc.currentScreen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen<?>)) {
                        ctx.getSource().sendFeedback(Text.literal("§cNot in a handled screen!"));
                        return 1;
                    }
                    var screen = (net.minecraft.client.gui.screen.ingame.HandledScreen<?>) mc.currentScreen;
                    ctx.getSource().sendFeedback(Text.literal("§eTitle: " + screen.getTitle().getString()));
                    int count = 0;
                    for (Slot slot : screen.getScreenHandler().slots) {
                        ItemStack stack = slot.getStack();
                        if (stack.isEmpty()) continue;
                        LoreComponent lore = stack.get(DataComponentTypes.LORE);
                        if (lore == null || lore.lines().isEmpty()) continue;
                        ctx.getSource().sendFeedback(Text.literal("§a[Slot " + slot.id + "] §f" + stack.getName().getString()));
                        for (Text line : lore.lines()) {
                            ctx.getSource().sendFeedback(Text.literal("§7  " + line.getString()));
                        }
                        if (++count >= 5) break; // primele 5 itemuri cu lore
                    }
                    return 1;
                })
        );
    }
}
