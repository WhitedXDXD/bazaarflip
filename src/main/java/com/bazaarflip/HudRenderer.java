package com.bazaarflip;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.text.DecimalFormat;

/**
 * Renders a compact HUD in the top-left corner showing:
 *  - Tracked item
 *  - Current best buy / sell prices
 *  - Your active order prices
 *  - UNDERCUT warnings + recommended prices
 */
public class HudRenderer {

    private static final DecimalFormat DF = new DecimalFormat("#,##0.0");
    private static final int LINE = 11;
    private static final int PAD  = 5;

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        // Don't draw while a screen (like the Bazaar GUI) is open
        if (mc.currentScreen != null) return;

        BazaarTracker t = BazaarFlipMod.TRACKER;
        if (t.getTrackedItem() == null) return;

        boolean buyUndercut  = t.isBuyUndercut();
        boolean sellUndercut = t.isSellUndercut();

        // ── Build lines ──────────────────────────────────────────────────────

        String title   = "§6§lBazaar Flip §8│ §f" + t.getTrackedItem();
        String buyBest = "§7Top Buy:  " + fmt(t.getBestBuyPrice());
        String selBest = "§7Top Sell: " + fmt(t.getBestSellPrice());

        String yourBuyLine = "§7Your Buy:  §e" + fmt(t.getYourBuyOrderPrice())
                + queueTag(t.getBuyQueuePosition())
                + (buyUndercut ? " §c§l⚠ UNDERCUT" : " §a✔");

        String yourSelLine = "§7Your Sell: §e" + fmt(t.getYourSellOrderPrice())
                + queueTag(t.getSellQueuePosition())
                + (sellUndercut ? " §c§l⚠ UNDERCUT" : " §a✔");

        // Optional action lines
        String recBuy  = buyUndercut
                ? "§c▶ Cancel & set buy to  §a§l" + DF.format(t.getRecommendedBuyPrice())  + " coins" : null;
        String recSell = sellUndercut
                ? "§c▶ Cancel & set sell to §a§l" + DF.format(t.getRecommendedSellPrice()) + " coins" : null;

        // ── Count lines for background ────────────────────────────────────────
        int lineCount = 5;
        if (recBuy  != null) lineCount++;
        if (recSell != null) lineCount++;

        int x = PAD;
        int y = PAD;
        int bgW = 240;
        int bgH = lineCount * LINE + PAD * 2;

        // Background box
        ctx.fill(x - 2, y - 2, x + bgW, y + bgH, 0xBB000000);

        // Draw each line
        ctx.drawText(mc.textRenderer, title,      x, y, 0xFFFFFF, true); y += LINE + 1;
        ctx.drawText(mc.textRenderer, buyBest,    x, y, 0xFFFFFF, true); y += LINE;
        ctx.drawText(mc.textRenderer, selBest,    x, y, 0xFFFFFF, true); y += LINE + 1;
        ctx.drawText(mc.textRenderer, yourBuyLine,x, y, 0xFFFFFF, true); y += LINE;
        ctx.drawText(mc.textRenderer, yourSelLine,x, y, 0xFFFFFF, true); y += LINE;

        if (recBuy  != null) { ctx.drawText(mc.textRenderer, recBuy,  x, y, 0xFFFFFF, true); y += LINE; }
        if (recSell != null) { ctx.drawText(mc.textRenderer, recSell, x, y, 0xFFFFFF, true); }
    }

    private static String fmt(double v) {
        return v > 0 ? "§a" + DF.format(v) + " coins" : "§8N/A";
    }

    private static String queueTag(int pos) {
        if (pos <= 0) return "";
        return " §8(#" + pos + ")";
    }
}
