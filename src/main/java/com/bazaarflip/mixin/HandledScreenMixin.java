package com.bazaarflip.mixin;

import com.bazaarflip.BazaarFlipMod;
import com.bazaarflip.BazaarTracker;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hooks into every HandledScreen render tick to detect when the player is
 * inside a Hypixel Bazaar GUI and read the price information from item lore.
 *
 * Hypixel sends the Bazaar as a vanilla-style container (GenericContainerScreenHandler),
 * so we inspect ItemStack lore using 1.21 DataComponents.
 */
@Mixin(HandledScreen.class)
public class HandledScreenMixin {

    // Regex patterns for Hypixel Bazaar lore strings (color codes already stripped)
    private static final Pattern PAT_BUY_PRICE      = Pattern.compile("Top Buy Order:\\s*([\\d,]+\\.?\\d*)");
    private static final Pattern PAT_SELL_PRICE     = Pattern.compile("Top Sell Order:\\s*([\\d,]+\\.?\\d*)");
    private static final Pattern PAT_ORDER_AT       = Pattern.compile("at ([\\d,]+\\.?\\d*) coins");
    private static final Pattern PAT_QUEUE_POS      = Pattern.compile("Position:\\s*#(\\d+)");
    private static final Pattern PAT_BUYING         = Pattern.compile("Buying");
    private static final Pattern PAT_SELLING        = Pattern.compile("Selling");

    // Throttle parsing to once every 20 render ticks (~1 s) to avoid GC pressure
    private int bazaarflip$tickCounter = 0;

    @Inject(method = "render", at = @At("HEAD"))
    private void bazaarflip$onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        BazaarTracker tracker = BazaarFlipMod.TRACKER;
        if (tracker.getTrackedItem() == null) return;

        // Throttle
        if (++bazaarflip$tickCounter < 20) return;
        bazaarflip$tickCounter = 0;

        // Check window title contains "Bazaar" (Hypixel sets this for all Bazaar GUIs)
        HandledScreen<?> self = (HandledScreen<?>) (Object) this;
        String title = strip(self.getTitle().getString());
        if (!title.contains("Bazaar")) return;

        bazaarflip$parsePrices(tracker);
    }

    private void bazaarflip$parsePrices(BazaarTracker tracker) {
        HandledScreen<?> self = (HandledScreen<?>) (Object) this;
        if (self.getScreenHandler() == null) return;

        double bestBuy   = -1;
        double bestSell  = -1;
        double yourBuy   = -1;
        double yourSell  = -1;
        int    buyPos    = -1;
        int    sellPos   = -1;
        boolean inBuyOrder  = false;
        boolean inSellOrder = false;

        for (Slot slot : self.getScreenHandler().slots) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;

            // Build a single plain-text lore string
            LoreComponent lore = stack.get(DataComponentTypes.LORE);
            if (lore == null) continue;

            StringBuilder sb = new StringBuilder();
            for (Text line : lore.lines()) {
                sb.append(strip(line.getString())).append('\n');
            }
            String lorePlain = sb.toString();

            // ── Top-level Bazaar product page ─────────────────────────────────
            // Item name is usually the product, lore shows top orders
            double bp = parseDouble(PAT_BUY_PRICE, lorePlain);
            double sp = parseDouble(PAT_SELL_PRICE, lorePlain);
            if (bp > 0) bestBuy  = bp;
            if (sp > 0) bestSell = sp;

            // ── "Your Orders" page ────────────────────────────────────────────
            // Each slot is one of your active orders
            // Lore: "Buying 100x Sugar\nat 20,505.0 coins each\nPosition: #1 in queue"
            if (PAT_BUYING.matcher(lorePlain).find()) {
                double price = parseDouble(PAT_ORDER_AT, lorePlain);
                int    pos   = parseInt(PAT_QUEUE_POS, lorePlain);
                if (price > 0) yourBuy = price;
                if (pos   > 0) buyPos  = pos;
            }
            if (PAT_SELLING.matcher(lorePlain).find()) {
                double price = parseDouble(PAT_ORDER_AT, lorePlain);
                int    pos   = parseInt(PAT_QUEUE_POS, lorePlain);
                if (price > 0) yourSell = price;
                if (pos   > 0) sellPos  = pos;
            }
        }

        // Push results to tracker
        tracker.updateBestPrices(bestBuy, bestSell);
        tracker.updateYourOrders(yourBuy, yourSell, buyPos, sellPos);
    }

    // ── Utility ──────────────────────────────────────────────────────────────

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
