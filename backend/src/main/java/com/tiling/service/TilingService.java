package com.tiling.service;

import com.tiling.algorithm.GuillotineDP;
import com.tiling.dto.SolveResponse;
import com.tiling.dto.TileDto;
import com.tiling.dto.SolveRequest;
import com.tiling.dto.TileUsage;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TilingService {

    private final GuillotineDP guillotineDP;

    public TilingService() {
        this.guillotineDP = new GuillotineDP();
    }

    public SolveResponse solve(SolveRequest request) {
        int L = request.getL();
        int W = request.getW();
        List<TileDto> tiles = request.getTiles();

        if (tiles == null || tiles.isEmpty()) {
            throw new IllegalArgumentException("Tiles list must not be empty");
        }

        String mode = (request.getMode() == null || request.getMode().isBlank())
            ? "advanced"
            : request.getMode();

        if ("simple".equalsIgnoreCase(mode)) {
            return solveSimple(L, W, tiles);
        }

        // Default and advanced path
        return solveAdvanced(L, W, tiles);
    }

    /**
     * Simple mode: single tile type baseline using ceil covering.
     */
    private SolveResponse solveSimple(int L, int W, List<TileDto> tiles) {
        TileUsage best = null;
        int minCost = Integer.MAX_VALUE;

        for (TileDto t : tiles) {
            int countL = (int) Math.ceil((double) L / t.getSize());
            int countW = (int) Math.ceil((double) W / t.getSize());
            int totalCount = countL * countW;
            int cost = totalCount * t.getCost();
            if (cost < minCost) {
                minCost = cost;
                best = new TileUsage(t.getId(), t.getSize(), totalCount, cost);
            }
        }

        TileUsage finalBest = best;
        List<TileUsage> usages = tiles.stream()
            .map(t -> t.getId().equals(finalBest.getId())
                ? finalBest
                : new TileUsage(t.getId(), t.getSize(), 0, 0))
            .sorted(Comparator.comparing(TileUsage::getId))
            .collect(Collectors.toList());

        String explanation = String.format(
            "Simple mode: Used single tile type %s (size %d) with count = ceil(%d/%d) * ceil(%d/%d) = %d tiles. Total cost: %d",
            finalBest.getId(), finalBest.getSize(), L, finalBest.getSize(), W, finalBest.getSize(),
            finalBest.getCount(), minCost
        );

        return new SolveResponse("simple", usages, minCost, explanation, "");
    }

    /**
     * Advanced mode: guillotine DP for mixed tiles.
     */
    private SolveResponse solveAdvanced(int L, int W, List<TileDto> tiles) {
        GuillotineDP.Result result = guillotineDP.solve(L, W, tiles);

        List<TileUsage> usages = tiles.stream()
            .map(t -> {
                int cnt = result.counts().getOrDefault(t.getId(), 0);
                int cost = cnt * t.getCost();
                return new TileUsage(t.getId(), t.getSize(), cnt, cost);
            })
            .sorted(Comparator.comparing(TileUsage::getId))
            .collect(Collectors.toList());

        String explanation = "Advanced mode: used guillotine DP to compute mixed-tile minimal cost";

        return new SolveResponse(
            "advanced",
            usages,
            result.totalCost(),
            explanation,
            ""
        );
    }
}

