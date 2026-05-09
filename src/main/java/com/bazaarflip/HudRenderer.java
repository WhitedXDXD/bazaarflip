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

    // Culori cu alpha (0xFF = complet vizibil)
    private static final int COLOR_GOLD   = 0xFFFFAA00;
    private static final int COLOR_WHITE  = 0xFFFFFFFF;
    private static final int COLOR_GREEN  = 0xFF55FF55;
    private static final int COLOR_RED    = 0xFFFF5555;
    private static final int COLOR_YELLOW = 0xFFFFFF55;
    private static final int COLOR_GRAY   = 0xFFAAAAAA;

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

        String buyStatus  = "Your Buy:  " + fmt(t.getYourBuyOrderPrice())
                + queueTag(t.getBuyQueuePosition()) + (buyUndercut ? "  UNDERCUT!" : "  OK");
        String sellStatus = "Your Sell: " + fmt(t.getYourSellOrderPrice())
                + queueTag(t.getSellQueuePosition()) + (sellUndercut ? "  UNDERCUT!" : "  OK");

        draw(ctx, mc, buyStatus,  x, y, buyUndercut  ? COLOR_RED : COLOR_GREEN); y += LINE;
        draw(ctx, mc, sellStatus, x, y, sellUndercut ? COLOR_RED : COLOR_
