package com.bazaarflip.mixin;

import com.bazaarflip.BazaarFlipMod;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {

    private boolean bazaarflip$debugPrinted = false;

    @Inject(method = "render", at = @At("HEAD"))
    private void bazaarflip$onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        HandledScreen<?> self = (HandledScreen<?>) (Object) this;
        String title = self.getTitle().getString().replaceAll("§[0-9a-fk-orA-FK-OR]", "");

        if (!title.contains("Bazaar")) {
            bazaarflip$debugPrinted = false;
            return;
        }

        // Print lore debug info once per screen open
        if (!bazaarflip$debugPrinted) {
            bazaarflip$debugPrinted = true;
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) return;

            mc.player.sendMessage(Text.literal("§6[BazaarFlip Debug] §eTitle: " + title), false);

            int count = 0;
            for (Slot slot : self.getScreenHandler().slots) {
                ItemStack stack = slot.getStack();
                if (stack.isEmpty()) continue;
                LoreComponent lore = stack.get(DataComponentTypes.LORE);
                if (lore == null || lore.lines().isEmpty()) continue;

                mc.player.sendMessage(Text.literal("§a[Slot " + slot.id + "] §f" + stack.getName().getString()), false);
                for (Text line : lore.lines()) {
                    mc.player.sendMessage(Text.literal("§7  |" + line.getString() + "|"), false);
                }
                if (++count >= 6) break;
            }
        }

        // Still update tracker prices
        if (BazaarFlipMod.TRACKER.getTrackedItem() != null) {
            bazaarflip$parsePrices();
        }
    }

    private void bazaarflip$parsePrices() {
        // placeholder — will fix after seeing debug output
    }
}
