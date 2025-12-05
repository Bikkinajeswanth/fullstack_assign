package com.tiling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TileUsage {
    private String id;
    private Integer size;
    private Integer count;
    private Integer cost;
}

