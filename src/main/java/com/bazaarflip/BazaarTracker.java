package com.bazaarflip;

/**
 * Tracks the state of the currently-flipped item:
 * - What item we're watching
 * - Current best prices seen in the Bazaar GUI
 * - The player's own active order prices
 * - Whether the player has been undercut
 */
public class BazaarTracker {

    private String trackedItem = null;

    // Best prices currently visible in the Bazaar GUI
    private double bestBuyPrice  = -1; // highest buy order (someone wants to buy at this)
    private double bestSellPrice = -1; // lowest sell order (someone wants to sell at this)

    // Player's active order prices (read from "Your Orders" GUI)
    private double yourBuyOrderPrice  = -1;
    private double yourSellOrderPrice = -1;

    // Position in queue (1 = top / best)
    private int buyQueuePosition  = -1;
    private int sellQueuePosition = -1;

    // Alert cooldown so chat isn't spammed
    private long lastAlertTime = 0;
    private static final long ALERT_COOLDOWN_MS = 6_000;

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public void setTrackedItem(String item) {
        this.trackedItem = item;
        reset();
    }

    /** Called from the screen mixin whenever the Bazaar GUI is open. */
    public void updateBestPrices(double buyPrice, double sellPrice) {
        if (buyPrice  > 0) this.bestBuyPrice  = buyPrice;
        if (sellPrice > 0) this.bestSellPrice = sellPrice;
    }

    /** Called from the screen mixin when "Your Orders" data is found. */
    public void updateYourOrders(double buyOrderPrice, double sellOrderPrice,
                                  int buyPos, int sellPos) {
        if (buyOrderPrice  > 0) this.yourBuyOrderPrice  = buyOrderPrice;
        if (sellOrderPrice > 0) this.yourSellOrderPrice = sellOrderPrice;
        if (buyPos  > 0) this.buyQueuePosition  = buyPos;
        if (sellPos > 0) this.sellQueuePosition = sellPos;
    }

    // -------------------------------------------------------------------------
    // Undercut detection
    // -------------------------------------------------------------------------

    /**
     * You are undercut on buys when someone placed a HIGHER buy order than yours
     * (meaning they are now #1 in queue and will be matched first).
     * Queue position > 1 means you are NOT the best offer.
     */
    public boolean isBuyUndercut() {
        if (yourBuyOrderPrice <= 0) return false;
        // If we know queue position, use that (most reliable)
        if (buyQueuePosition > 0) return buyQueuePosition > 1;
        // Fallback: compare prices
        return bestBuyPrice > yourBuyOrderPrice + 0.05;
    }

    /**
     * You are undercut on sells when someone placed a LOWER sell order than yours.
     */
    public boolean isSellUndercut() {
        if (yourSellOrderPrice <= 0) return false;
        if (sellQueuePosition > 0) return sellQueuePosition > 1;
        return bestSellPrice < yourSellOrderPrice - 0.05;
    }

    // -------------------------------------------------------------------------
    // Recommended prices (+0.1 / -0.1 over the current best)
    // -------------------------------------------------------------------------

    /** Place your buy order 0.1 above the current best to be #1. */
    public double getRecommendedBuyPrice() {
        if (bestBuyPrice > 0) return bestBuyPrice + 0.1;
        return yourBuyOrderPrice + 0.1;
    }

    /** Place your sell order 0.1 below the current best to be #1. */
    public double getRecommendedSellPrice() {
        if (bestSellPrice > 0) return bestSellPrice - 0.1;
        return yourSellOrderPrice - 0.1;
    }

    // -------------------------------------------------------------------------
    // Alert throttling
    // -------------------------------------------------------------------------

    /** Returns true if enough time has passed to send another alert. */
    public boolean shouldAlert() {
        if (!isBuyUndercut() && !isSellUndercut()) return false;
        long now = System.currentTimeMillis();
        if (now - lastAlertTime >= ALERT_COOLDOWN_MS) {
            lastAlertTime = now;
            return true;
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void reset() {
        bestBuyPrice = bestSellPrice = -1;
        yourBuyOrderPrice = yourSellOrderPrice = -1;
        buyQueuePosition = sellQueuePosition = -1;
        lastAlertTime = 0;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String getTrackedItem()       { return trackedItem; }
    public double getBestBuyPrice()      { return bestBuyPrice; }
    public double getBestSellPrice()     { return bestSellPrice; }
    public double getYourBuyOrderPrice() { return yourBuyOrderPrice; }
    public double getYourSellOrderPrice(){ return yourSellOrderPrice; }
    public int    getBuyQueuePosition()  { return buyQueuePosition; }
    public int    getSellQueuePosition() { return sellQueuePosition; }
}
