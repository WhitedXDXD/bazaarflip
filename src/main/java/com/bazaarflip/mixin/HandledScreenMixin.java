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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {

    private int bazaarflip$tickCounter = 0;

    private static final Pattern PAT_PRICE_EACH     = Pattern.compile("([\\d,]+\\.?\\d*) coins each");
    private static final Pattern PAT_PRICE_PER_UNIT = Pattern.compile("Price per unit:\\s*([\\d,]+\\.?\\d*)");
    private static final Pattern PAT_QUEUE_POS      = Pattern.compile("#(\\d+) in queue");

    @Inject(method = "render", at = @At("HEAD"))
    private void bazaarflip$onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        BazaarTracker tracker = BazaarFlipMod.TRACKER;
        if (tracker.getTrackedItem() == null) return;

        if (++bazaarflip$tickCounter < 20) return;
        bazaarflip$tickCounter = 0;

        HandledScreen<?> self = (HandledScreen<?>) (Object) this;

        double bestBuy = -1, bestSell = -1;
        double yourBuy = -1, yourSell = -1;
        int buyPos = -1, sellPos = -1;

        for (Slot slot : self.getScreenHandler().slots) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;

            String itemName = strip(stack.getName().getString());

            LoreComponent lore = stack.get(DataComponentTypes.LORE);
            if (lore == null) continue;

            StringBuilder sb = new StringBuilder();
            for (Text line : lore.lines()) sb.append(strip(line.getString())).append('\n');
            String lorePlain = sb.toString();

            if (itemName.equals("Create Buy Order")) {
                double price = parseDouble(PAT_PRICE_EACH, lorePlain);
                if (price > 0) bestBuy = price;
            }

            if (itemName.equals("Create Sell Offer")) {
                double price = parseDouble(PAT_PRICE_EACH, lorePlain);
                if (price > 0) bestSell = price;
            }

            if (itemName.startsWith("BUY ")) {
                double price = parseDouble(PAT_PRICE_PER_UNIT, lorePlain);
                int pos = parseInt(PAT_QUEUE_POS, lorePlain);
                if (price > 0) yourBuy = price;
                if (pos > 0)   buyPos  = pos;
            }

            if (itemName.startsWith("SELL ")) {
                double price = parseDouble(PAT_PRICE_PER_UNIT, lorePlain);
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
