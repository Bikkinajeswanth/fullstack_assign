package com.tiling.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolveRequest {
    @JsonProperty("L")
    @NotNull(message = "Length L is required")
    @Min(value = 1, message = "Length L must be positive")
    private Integer L;

    @JsonProperty("W")
    @NotNull(message = "Width W is required")
    @Min(value = 1, message = "Width W must be positive")
    private Integer W;

    @JsonProperty("tiles")
    @NotNull(message = "Tiles list is required")
    @Size(min = 1, message = "At least one tile must be provided")
    @Valid
    private List<TileDto> tiles;

    @JsonProperty("mode")
    private String mode = "advanced"; // default to advanced
}

