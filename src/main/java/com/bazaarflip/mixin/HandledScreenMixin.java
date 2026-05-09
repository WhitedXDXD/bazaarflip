package com.bazaarflip.mixin;

import com.bazaarflip.BazaarFlipMod;
import com.bazaarflip.BazaarTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {

    private String bazaarflip$lastTitle = "";
    private int bazaarflip$tickCounter = 0;

    // Updated patterns based on real Hypixel lore format
    private static final Pattern PAT_BUY_PRICE  = Pattern.compile("Buy price:\\s*([\\d,]+\\.?\\d*)");
    private static final Pattern PAT_SELL_PRICE  = Pattern.compile("Sell price:\\s*([\\d,]+\\.?\\d*)");
    private static final Pattern PAT_ORDER_AT    = Pattern.compile("at ([\\d,]+\\.?\\d*) coins");
    private static final Pattern PAT_QUEUE_POS   = Pattern.compile("Position:\\s*#(\\d+)");
    private static final Pattern PAT_BUYING      = Pattern.compile("(?i)buying");
    private static final Pattern PAT_SELLING     = Pattern.compile("(?i)selling");

    @Inject(method = "render", at = @At("HEAD"))
    private void bazaarflip$onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        HandledScreen<?> self = (HandledScreen<?>) (Object) this;
        String title = strip(self.getTitle().getString());

        if (!title.contains("Bazaar")) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        // Debug: print lore whenever we navigate to a new Bazaar page
        if (!title.equals(bazaarflip$lastTitle)) {
            bazaarflip$lastTitle = title;
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
                if (++count >= 8) break;
            }
        }

        // Parse prices every 20 ticks
        if (++bazaarflip$tickCounter < 20) return;
        bazaarflip$tickCounter = 0;

        BazaarTracker tracker = BazaarFlipMod.TRACKER;
        if (tracker.getTrackedItem() == null) return;

        double bestBuy = -1, bestSell = -1;
        double yourBuy = -1, yourSell = -1;
        int buyPos = -1, sellPos = -1;

        for (Slot slot : self.getScreenHandler().slots) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;
            LoreComponent lore = stack.get(DataComponentTypes.LORE);
            if (lore == null) continue;

            StringBuilder sb = new StringBuilder();
            for (Text line : lore.lines()) sb.append(strip(line.getString())).append('\n');
            String lorePlain = sb.toString();

            double bp = parseDouble(PAT_BUY_PRICE, lorePlain);
            double sp = parseDouble(PAT_SELL_PRICE, lorePlain);
            if (bp > 0) bestBuy  = bp;
            if (sp > 0) bestSell = sp;

            if (PAT_BUYING.matcher(lorePlain).find()) {
                double price = parseDouble(PAT_ORDER_AT, lorePlain);
                int pos = parseInt(PAT_QUEUE_POS, lorePlain);
                if (price > 0) yourBuy = price;
                if (pos > 0)   buyPos  = pos;
            }
            if (PAT_SELLING.matcher(lorePlain).find()) {
                double price = parseDouble(PAT_ORDER_AT, lorePlain);
                int pos = parseInt(PAT_QUEUE_POS, lorePlain);
                if (price > 0) yourSell = price;
                if (pos > 0)   sellPos  = pos;
            }
        }

        tracker.updateBestPrices(bestBuy, bestSell);
        tracker.updateYourOrders(yourBuy, yourSell, buyPos, sellPos);
    }

    private static String strip(String s) {
        return s.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
    }

    private static double parseDouble(Pattern p, String text) {
        Matcher m = p.matcher(text);
        if (m.find()) {
            try { return Double.parseDouble(m.group(1).replace(",", "")); }
            catch (NumberFormatException ignored) {}
        }
        return -1;
    }

    private static int parseInt(Pattern p, String text) {
        Matcher m = p.matcher(text);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); }
            catch (NumberFormatException ignored) {}
        }
        return -1;
    }
}
