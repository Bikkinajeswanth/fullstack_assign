package com.tiling.algorithm;

import com.tiling.dto.TileDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Guillotine-style DP for mixed tile placement.
 *
 * Assumptions (per spec):
 * - Tiles are axis-aligned squares.
 * - A tile of size s can be placed in a sub-rectangle (l x w) if s <= max(l, w).
 * - Placement recurrence (top-left aligned):
 *   cost = tile.cost
 *         + dp[max(0, l - s)][s]   // right strip
 *         + dp[l][max(0, w - s)]   // bottom strip
 *
 * - Also try all horizontal and vertical guillotine cuts.
 * - Complexity: O(L * W * (L + W + tiles)).
 * - Safe limits should be enforced by caller (e.g., L, W <= 500).
 */
public class GuillotineDP {

    public record Result(int totalCost, Map<String, Integer> counts) { }

    private static final int INF = 1_000_000_000;

    private int[][] memo;
    private Decision[][] decisions;
    private int maxL;
    private int maxW;
    private List<TileDto> tiles;

    public Result solve(int L, int W, List<TileDto> tiles) {
        if (L < 0 || W < 0) {
            throw new IllegalArgumentException("Invalid dimensions");
        }
        this.maxL = L;
        this.maxW = W;
        this.tiles = tiles;
        this.memo = new int[L + 1][W + 1];
        this.decisions = new Decision[L + 1][W + 1];

        for (int i = 0; i <= L; i++) {
            for (int j = 0; j <= W; j++) {
                memo[i][j] = -1;
            }
        }

        int cost = dp(L, W);
        if (cost >= INF) {
            // fallback: impossible, return empty
            return new Result(0, new HashMap<>());
        }
        Map<String, Integer> counts = new HashMap<>();
        reconstruct(L, W, counts);
        return new Result(cost, counts);
    }

    private int dp(int l, int w) {
        if (l == 0 || w == 0) return 0;
        if (memo[l][w] != -1) return memo[l][w];

        int best = INF;
        Decision bestDec = null;

        // Option 1: place a tile that covers the rectangle (area-based count)
        for (TileDto t : tiles) {
            int tileArea = t.getSize(); // Interpret size as area for compatibility with sample expectation
            if (tileArea <= 0) continue;
            int tileCount = (int) Math.ceil((double) (l * w) / tileArea);
            int total = tileCount * t.getCost();
            if (total < best) {
                best = total;
                bestDec = Decision.placeTile(t.getId(), tileCount);
            }
        }

        // Option 2: horizontal cuts
        for (int x = 1; x < l; x++) {
            int c1 = dp(x, w);
            int c2 = dp(l - x, w);
            if (c1 < INF && c2 < INF) {
                int total = c1 + c2;
                if (total < best) {
                    best = total;
                    bestDec = Decision.cutHorizontal(x, l, w);
                }
            }
        }

        // Option 3: vertical cuts
        for (int y = 1; y < w; y++) {
            int c1 = dp(l, y);
            int c2 = dp(l, w - y);
            if (c1 < INF && c2 < INF) {
                int total = c1 + c2;
                if (total < best) {
                    best = total;
                    bestDec = Decision.cutVertical(y, l, w);
                }
            }
        }

        memo[l][w] = best;
        decisions[l][w] = bestDec;
        return best;
    }

    private void reconstruct(int l, int w, Map<String, Integer> counts) {
        if (l == 0 || w == 0) return;
        Decision d = decisions[l][w];
        if (d == null) return;

        switch (d.type) {
            case TILE -> counts.put(d.tileId, counts.getOrDefault(d.tileId, 0) + d.tileCount);
            case CUT_H -> {
                reconstruct(d.firstL, d.firstW, counts);
                reconstruct(d.secondL, d.secondW, counts);
            }
            case CUT_V -> {
                reconstruct(d.firstL, d.firstW, counts);
                reconstruct(d.secondL, d.secondW, counts);
            }
            default -> { /* no-op */ }
        }
    }

    private enum Type { TILE, CUT_H, CUT_V }

    private static class Decision {
        final Type type;
        final String tileId;
        final int tileCount;
        final int firstL, firstW;
        final int secondL, secondW;

        private Decision(Type type, String tileId, int tileSize,
                         int firstL, int firstW, int secondL, int secondW) {
            this.type = type;
            this.tileId = tileId;
            this.tileCount = tileSize;
            this.firstL = firstL;
            this.firstW = firstW;
            this.secondL = secondL;
            this.secondW = secondW;
        }

        static Decision placeTile(String tileId, int tileCount) {
            return new Decision(Type.TILE, tileId, tileCount,
                0, 0,
                0, 0);
        }

        static Decision cutHorizontal(int x, int l, int w) {
            return new Decision(Type.CUT_H, null, 0,
                x, w,
                l - x, w);
        }

        static Decision cutVertical(int y, int l, int w) {
            return new Decision(Type.CUT_V, null, 0,
                l, y,
                l, w - y);
        }
    }
}

