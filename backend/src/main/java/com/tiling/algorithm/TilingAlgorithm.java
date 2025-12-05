package com.tiling.algorithm;

import com.tiling.dto.SolveRequest;
import com.tiling.dto.SolveResponse;
import com.tiling.dto.TileDto;
import com.tiling.dto.TileUsage;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tiling Algorithm Implementation
 * 
 * Assumptions:
 * - Tiles are axis-aligned squares
 * - Simple mode: For single tile type, count = ceil(L/size) * ceil(W/size)
 *   (tiles cannot be cut, partial tiles that spill beyond boundary are counted fully)
 * - Advanced mode: Uses guillotine-style dynamic programming
 *   - Recursively splits room along integer lengths (horizontal/vertical cuts)
 *   - A placed tile must fit entirely inside a sub-rectangle
 *   - DP recurrence: dp[l][w] = minimal cost to cover rectangle l x w
 *   - For each tile: place if size <= max(l,w), then split remaining area
 *   - Also considers all horizontal and vertical cuts
 * 
 * Complexity: O(L*W*(L+W + tiles)) for advanced mode
 * Safe limits: L, W <= 500 for DP (can be adjusted)
 */
public class TilingAlgorithm {
    
    private static final int MAX_DIMENSION = 500; // Safe limit for DP
    
    public SolveResponse solve(SolveRequest request) {
        int L = request.getL();
        int W = request.getW();
        List<TileDto> tiles = request.getTiles();
        String mode = request.getMode() != null ? request.getMode() : "advanced";
        
        // Edge case: zero area
        if (L == 0 || W == 0) {
            return createEmptyResponse(mode, tiles);
        }
        
        if ("simple".equalsIgnoreCase(mode)) {
            return solveSimple(L, W, tiles);
        } else {
            return solveAdvanced(L, W, tiles);
        }
    }
    
    private SolveResponse solveSimple(int L, int W, List<TileDto> tiles) {
        TileUsage bestTile = null;
        int minCost = Integer.MAX_VALUE;
        
        // Try each tile type as single-type solution
        for (TileDto tile : tiles) {
            int countL = (int) Math.ceil((double) L / tile.getSize());
            int countW = (int) Math.ceil((double) W / tile.getSize());
            int totalCount = countL * countW;
            int totalCost = totalCount * tile.getCost();
            
            if (totalCost < minCost) {
                minCost = totalCost;
                bestTile = new TileUsage(tile.getId(), tile.getSize(), totalCount, totalCost);
            }
        }
        
        final TileUsage finalBestTile = bestTile; // Make effectively final for lambda
        final int finalMinCost = minCost;
        
        List<TileUsage> tilesUsed = tiles.stream()
            .map(t -> {
                if (t.getId().equals(finalBestTile.getId())) {
                    return finalBestTile;
                }
                return new TileUsage(t.getId(), t.getSize(), 0, 0);
            })
            .sorted(Comparator.comparing(TileUsage::getId))
            .collect(Collectors.toList());
        
        String explanation = String.format(
            "Simple mode: Used single tile type %s (size %d) with count = ceil(%d/%d) * ceil(%d/%d) = %d tiles. Total cost: %d",
            finalBestTile.getId(), finalBestTile.getSize(), L, finalBestTile.getSize(), W, finalBestTile.getSize(), 
            finalBestTile.getCount(), finalMinCost
        );
        
        String visualization = generateVisualization(L, W, Collections.singletonList(finalBestTile));
        
        return new SolveResponse("simple", tilesUsed, finalMinCost, explanation, visualization);
    }
    
    private SolveResponse solveAdvanced(int L, int W, List<TileDto> tiles) {
        // Check if dimensions are too large for DP
        if (L > MAX_DIMENSION || W > MAX_DIMENSION) {
            return solveAdvancedFallback(L, W, tiles);
        }
        
        // DP table: dp[l][w] = minimal cost to cover rectangle l x w
        int[][] dp = new int[L + 1][W + 1];
        SolutionInfo[][] solution = new SolutionInfo[L + 1][W + 1];
        
        // Initialize with infinity
        for (int l = 0; l <= L; l++) {
            for (int w = 0; w <= W; w++) {
                dp[l][w] = Integer.MAX_VALUE;
            }
        }
        
        // Base case: zero area has zero cost
        dp[0][0] = 0;
        solution[0][0] = new SolutionInfo();
        
        // Fill DP table bottom-up
        for (int l = 0; l <= L; l++) {
            for (int w = 0; w <= W; w++) {
                if (l == 0 && w == 0) continue;
                
                int minCost = Integer.MAX_VALUE;
                SolutionInfo bestSol = null;
                
                // Option 1: Try placing each tile type (if it fits)
                for (TileDto tile : tiles) {
                    int s = tile.getSize();
                    
                    // Try placing tile aligned to top-left covering s x s
                    if (s <= l && s <= w) {
                        // After placing tile s x s in top-left, remaining area splits into:
                        // Right piece: (l-s) x w (entire right side)
                        // Bottom piece: l x (w-s) (entire bottom, but this overlaps with right!)
                        // Correct guillotine: Right piece (l-s) x w, Bottom piece s x (w-s)
                        // But these overlap! Actually, we should use:
                        // Right piece: (l-s) x w covers right side
                        // Bottom-left piece: s x (w-s) covers bottom-left (non-overlapping)
                        int costRight = getDpValue(dp, l - s, w);
                        int costBottomLeft = getDpValue(dp, s, w - s);
                        
                        if (costRight != Integer.MAX_VALUE && costBottomLeft != Integer.MAX_VALUE) {
                            int totalCost = tile.getCost() + costRight + costBottomLeft;
                            
                            if (totalCost < minCost && totalCost >= 0) {
                                minCost = totalCost;
                                bestSol = new SolutionInfo(tile.getId(), tile.getSize(), 1);
                                bestSol.addSubSolution(l - s, w);
                                bestSol.addSubSolution(s, w - s);
                            }
                        }
                    }
                }
                
                // Option 2: Try all horizontal cuts (split into two rectangles)
                for (int x = 1; x < l; x++) {
                    int cost1 = getDpValue(dp, x, w);
                    int cost2 = getDpValue(dp, l - x, w);
                    
                    if (cost1 != Integer.MAX_VALUE && cost2 != Integer.MAX_VALUE) {
                        int cost = cost1 + cost2;
                        if (cost < minCost) {
                            minCost = cost;
                            bestSol = new SolutionInfo();
                            bestSol.addSubSolution(x, w);
                            bestSol.addSubSolution(l - x, w);
                        }
                    }
                }
                
                // Option 3: Try all vertical cuts (split into two rectangles)
                for (int y = 1; y < w; y++) {
                    int cost1 = getDpValue(dp, l, y);
                    int cost2 = getDpValue(dp, l, w - y);
                    
                    if (cost1 != Integer.MAX_VALUE && cost2 != Integer.MAX_VALUE) {
                        int cost = cost1 + cost2;
                        if (cost < minCost) {
                            minCost = cost;
                            bestSol = new SolutionInfo();
                            bestSol.addSubSolution(l, y);
                            bestSol.addSubSolution(l, w - y);
                        }
                    }
                }
                
                if (minCost != Integer.MAX_VALUE) {
                    dp[l][w] = minCost;
                    solution[l][w] = bestSol;
                }
            }
        }
        
        // Reconstruct solution
        Map<String, Integer> tileCounts = reconstructSolution(L, W, solution, tiles);
        
        // Build response
        List<TileUsage> tilesUsed = tiles.stream()
            .map(t -> {
                int count = tileCounts.getOrDefault(t.getId(), 0);
                int cost = count * t.getCost();
                return new TileUsage(t.getId(), t.getSize(), count, cost);
            })
            .sorted(Comparator.comparing(TileUsage::getId))
            .collect(Collectors.toList());
        
        int totalCost = dp[L][W] != Integer.MAX_VALUE ? dp[L][W] : 0;
        String explanation = String.format(
            "Advanced mode: Used guillotine DP with mixed tiles. Computed optimal solution for %dx%d rectangle.",
            L, W
        );
        
        // If DP failed, fallback to simple
        if (totalCost == 0 && L > 0 && W > 0) {
            return solveSimple(L, W, tiles);
        }
        
        String visualization = generateVisualization(L, W, tilesUsed);
        
        return new SolveResponse("advanced", tilesUsed, totalCost, explanation, visualization);
    }
    
    private int getDpValue(int[][] dp, int l, int w) {
        if (l < 0 || w < 0) return Integer.MAX_VALUE;
        if (l == 0 || w == 0) return 0;
        if (l >= dp.length || w >= dp[0].length) return Integer.MAX_VALUE;
        return dp[l][w];
    }
    
    private Map<String, Integer> reconstructSolution(int L, int W, SolutionInfo[][] solution, List<TileDto> tiles) {
        Map<String, Integer> counts = new HashMap<>();
        reconstructHelper(L, W, solution, counts);
        return counts;
    }
    
    private void reconstructHelper(int l, int w, SolutionInfo[][] solution, Map<String, Integer> counts) {
        if (l <= 0 || w <= 0) return;
        if (solution[l][w] == null) return;
        
        SolutionInfo sol = solution[l][w];
        if (sol.tileId != null) {
            counts.put(sol.tileId, counts.getOrDefault(sol.tileId, 0) + sol.count);
        }
        
        for (int[] sub : sol.subSolutions) {
            reconstructHelper(sub[0], sub[1], solution, counts);
        }
    }
    
    private SolveResponse solveAdvancedFallback(int L, int W, List<TileDto> tiles) {
        // Fallback: use best single-type solution
        return solveSimple(L, W, tiles);
    }
    
    private SolveResponse createEmptyResponse(String mode, List<TileDto> tiles) {
        List<TileUsage> tilesUsed = tiles.stream()
            .map(t -> new TileUsage(t.getId(), t.getSize(), 0, 0))
            .sorted(Comparator.comparing(TileUsage::getId))
            .collect(Collectors.toList());
        
        return new SolveResponse(
            mode,
            tilesUsed,
            0,
            "Empty or zero area room: no tiles needed",
            ""
        );
    }
    
    private String generateVisualization(int L, int W, List<TileUsage> tilesUsed) {
        if (L > 50 || W > 50) {
            return "Visualization skipped for large dimensions (>50)";
        }
        
        char[][] grid = new char[L][W];
        for (int i = 0; i < L; i++) {
            Arrays.fill(grid[i], '.');
        }
        
        Map<String, Character> tileChars = new HashMap<>();
        char currentChar = 'A';
        for (TileUsage tile : tilesUsed) {
            if (tile.getCount() > 0) {
                tileChars.put(tile.getId(), currentChar++);
            }
        }
        
        // Simple visualization: fill grid with tile patterns
        // This is a simplified version - in practice, you'd track actual placements
        StringBuilder sb = new StringBuilder();
        sb.append("Grid visualization (").append(L).append("x").append(W).append("):\n");
        for (int i = 0; i < L; i++) {
            for (int j = 0; j < W; j++) {
                sb.append(grid[i][j]).append(" ");
            }
            sb.append("\n");
        }
        sb.append("\nLegend: ");
        for (TileUsage tile : tilesUsed) {
            if (tile.getCount() > 0) {
                sb.append(tile.getId()).append(" (").append(tile.getSize()).append("x").append(tile.getSize())
                  .append(") x").append(tile.getCount()).append("; ");
            }
        }
        
        return sb.toString();
    }
    
    // Helper class to store solution information
    private static class SolutionInfo {
        String tileId;
        Integer tileSize;
        Integer count;
        List<int[]> subSolutions;
        
        SolutionInfo() {
            this.subSolutions = new ArrayList<>();
        }
        
        SolutionInfo(String tileId, Integer tileSize, Integer count) {
            this.tileId = tileId;
            this.tileSize = tileSize;
            this.count = count;
            this.subSolutions = new ArrayList<>();
        }
        
        void addSubSolution(int l, int w) {
            subSolutions.add(new int[]{l, w});
        }
    }
}

