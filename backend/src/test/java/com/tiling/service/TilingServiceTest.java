package com.tiling.service;

import com.tiling.dto.SolveRequest;
import com.tiling.dto.SolveResponse;
import com.tiling.dto.TileDto;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TilingServiceTest {

    @Test
    void testAdvancedModeSampleCase() {
        TilingService service = new TilingService();
        SolveRequest req = new SolveRequest();
        req.setL(6);
        req.setW(4);
        req.setTiles(Arrays.asList(
            new TileDto("A", 1, 2),
            new TileDto("B", 2, 3),
            new TileDto("C", 3, 6)
        ));
        req.setMode("advanced");

        SolveResponse resp = service.solve(req);

        assertEquals("advanced", resp.getSolutionMode());
        assertTrue(resp.getExplanation().toLowerCase().contains("advanced"));
        assertEquals(36, resp.getTotalCost());
        assertEquals(12, resp.getTilesUsed().stream()
            .filter(t -> t.getId().equals("B"))
            .findFirst().orElseThrow().getCount());
    }
}

