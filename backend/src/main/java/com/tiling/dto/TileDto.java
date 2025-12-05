package com.tiling.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TileDto {
    @NotBlank(message = "Tile id is required")
    private String id;
    
    @NotNull(message = "Tile size is required")
    @Min(value = 1, message = "Tile size must be positive")
    private Integer size;
    
    @NotNull(message = "Tile cost is required")
    @Min(value = 1, message = "Tile cost must be positive")
    private Integer cost;
}

