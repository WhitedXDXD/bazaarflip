package com.bazaarflip;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

import java.text.DecimalFormat;

public class HudRenderer {

    private static final DecimalFormat DF = new DecimalFormat("#,##0.0");
    private static final int LINE = 11;
    private static final int PAD  = 5;

    private static final int COLOR_GOLD    = 0xFFAA00;
    private static final int COLOR_WHITE   = 0xFFFFFF;
    private static final int COLOR_GREEN   = 0x55FF55;
    private static final int COLOR_RED     = 0xFF5555;
    private static final int COLOR_YELLOW  = 0xFFFF55;
    private static final int COLOR_GRAY    = 0xAAAAAA;

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (mc.currentScreen != null) return;

        BazaarTracker t = BazaarFlipMod.TRACKER;
        if (t.getTrackedItem() == null) return;

        boolean buyUndercut  = t.isBuyUndercut();
        boolean sellUndercut = t.isSellUndercut();

        int x = PAD;
        int y = PAD;

        int lineCount = 5;
        if (buyUndercut)  lineCount++;
        if (sellUndercut) lineCount++;

        ctx.fill(x - 2, y - 2, x + 250, y + lineCount * LINE + PAD * 2, 0xBB000000);

        draw(ctx, mc, "Bazaar Flip: " + t.getTrackedItem(), x, y, COLOR_GOLD);   y += LINE + 1;
        draw(ctx, mc, "Top Buy:  " + fmt(t.getBestBuyPrice()),  x, y, COLOR_GRAY);  y += LINE;
        draw(ctx, mc, "Top Sell: " + fmt(t.getBestSellPrice()), x, y, COLOR_GRAY);  y += LINE + 1;

        String buyStatus  = "Your Buy:  " + fmt(t.getYourBuyOrderPrice())  + queueTag(t.getBuyQueuePosition())  + (buyUndercut  ? "  UNDERCUT!" : "  OK");
        String sellStatus = "Your Sell: " + fmt(t.getYourSellOrderPrice()) + queueTag(t.getSellQueuePosition()) + (sellUndercut ? "  UNDERCUT!" : "  OK");

        draw(ctx, mc, buyStatus,  x, y, buyUndercut  ? COLOR_RED : COLOR_GREEN); y += LINE;
        draw(ctx, mc, sellStatus, x, y, sellUndercut ? COLOR_RED : COLOR_GREEN); y += LINE;

        if (buyUndercut)  { draw(ctx, mc, ">> Cancel & set buy to  " + DF.format(t.getRecommendedBuyPrice())  + " coins", x, y, COLOR_YELLOW); y += LINE; }
        if (sellUndercut) { draw(ctx, mc, ">> Cancel & set sell to " + DF.format(t.getRecommendedSellPrice()) + " coins", x, y, COLOR_YELLOW); }
    }

    private static void draw(DrawContext ctx, MinecraftClient mc, String text, int x, int y, int color) {
        ctx.drawText(mc.textRenderer, Text.literal(text), x, y, color, true);
    }

    private static String fmt(double v) {
        return v > 0 ? DF.format(v) + " coins" : "N/A";
    }

    private static String queueTag(int pos) {
        return pos > 0 ? " (#" + pos + ")" : "";
    }
}
