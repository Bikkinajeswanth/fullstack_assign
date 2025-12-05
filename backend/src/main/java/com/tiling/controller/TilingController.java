package com.tiling.controller;

import com.tiling.dto.SolveRequest;
import com.tiling.dto.SolveResponse;
import com.tiling.dto.TileDto;
import com.tiling.service.TilingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@Validated
public class TilingController {

    private final TilingService tilingService;

    public TilingController(TilingService tilingService) {
        this.tilingService = tilingService;
    }

    @PostMapping("/solve")
    public ResponseEntity<?> solve(@Valid @RequestBody SolveRequest request) {
        try {
            // Default mode must be advanced if omitted
            if (request.getMode() == null || request.getMode().isBlank()) {
                request.setMode("advanced");
            }
            SolveResponse response = tilingService.solve(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body("Invalid input: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing request: " + e.getMessage());
        }
    }
    
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(org.springframework.web.bind.MethodArgumentNotValidException ex) {
        StringBuilder errors = new StringBuilder();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String errorMessage = error.getDefaultMessage();
            errors.append(errorMessage).append("; ");
        });
        return ResponseEntity.badRequest()
            .body("Validation failed: " + errors.toString());
    }
    
    @GetMapping("/sample")
    public ResponseEntity<SolveRequest> getSample() {
        SolveRequest sample = new SolveRequest();
        sample.setL(6);
        sample.setW(4);
        sample.setTiles(Arrays.asList(
            new TileDto("A", 1, 2),
            new TileDto("B", 2, 3),
            new TileDto("C", 3, 6)
        ));
        sample.setMode("advanced");
        return ResponseEntity.ok(sample);
    }
}

