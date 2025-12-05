package com.tiling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolveResponse {
    private String solutionMode;
    private List<TileUsage> tilesUsed;
    private Integer totalCost;
    private String explanation;
    private String visualization;
}

