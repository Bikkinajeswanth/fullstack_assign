package com.tiling.algorithm;

import com.tiling.dto.SolveRequest;
import com.tiling.dto.SolveResponse;
import com.tiling.dto.TileDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TilingAlgorithmTest {

    private TilingAlgorithm algorithm;

    @BeforeEach
    void setUp() {
        algorithm = new TilingAlgorithm();
    }

    @Test
    void testSampleInputSimpleMode() {
        SolveRequest request = new SolveRequest();
        request.setL(6);
        request.setW(4);
        request.setTiles(Arrays.asList(
            new TileDto("A", 1, 2),
            new TileDto("B", 2, 3),
            new TileDto("C", 3, 6)
        ));
        request.setMode("simple");

        SolveResponse response = algorithm.solve(request);

        assertNotNull(response);
        assertEquals("simple", response.getSolutionMode());
        assertTrue(response.getTotalCost() > 0);
        assertEquals(3, response.getTilesUsed().size());
        
        // Verify counts format matches expected output
        for (var tile : response.getTilesUsed()) {
            assertNotNull(tile.getId());
            assertNotNull(tile.getSize());
            assertNotNull(tile.getCount());
            assertNotNull(tile.getCost());
        }
    }

    @Test
    void testSampleInputAdvancedMode() {
        SolveRequest request = new SolveRequest();
        request.setL(6);
        request.setW(4);
        request.setTiles(Arrays.asList(
            new TileDto("A", 1, 2),
            new TileDto("B", 2, 3),
            new TileDto("C", 3, 6)
        ));
        request.setMode("advanced");

        SolveResponse response = algorithm.solve(request);

        assertNotNull(response);
        assertEquals("advanced", response.getSolutionMode());
        assertTrue(response.getTotalCost() > 0);
        assertEquals(3, response.getTilesUsed().size());
    }

    @Test
    void testPureSingleTypeOptimal() {
        SolveRequest request = new SolveRequest();
        request.setL(4);
        request.setW(4);
        request.setTiles(Arrays.asList(
            new TileDto("A", 2, 1),
            new TileDto("B", 4, 10)
        ));
        request.setMode("simple");

        SolveResponse response = algorithm.solve(request);

        assertEquals("simple", response.getSolutionMode());
        // Tile A (2x2) should be optimal: 2*2 = 4 tiles * 1 = 4 cost
        // Tile B (4x4) needs 1 tile * 10 = 10 cost
        // So A should be chosen
        assertEquals(4, response.getTotalCost());
    }

    @Test
    void testEdgeCase1x1Room() {
        SolveRequest request = new SolveRequest();
        request.setL(1);
        request.setW(1);
        request.setTiles(Arrays.asList(
            new TileDto("A", 1, 1)
        ));
        request.setMode("simple");

        SolveResponse response = algorithm.solve(request);

        assertEquals(1, response.getTotalCost());
        assertEquals(1, response.getTilesUsed().get(0).getCount());
    }

    @Test
    void testAdvancedModeWithSmallInput() {
        SolveRequest request = new SolveRequest();
        request.setL(4);
        request.setW(4);
        request.setTiles(Arrays.asList(
            new TileDto("A", 2, 1),
            new TileDto("B", 1, 1)
        ));
        request.setMode("advanced");

        SolveResponse response = algorithm.solve(request);

        assertNotNull(response);
        assertEquals("advanced", response.getSolutionMode());
        assertTrue(response.getTotalCost() > 0);
    }

    @Test
    void testLargeDimensionsFallback() {
        SolveRequest request = new SolveRequest();
        request.setL(1000);
        request.setW(1000);
        request.setTiles(Arrays.asList(
            new TileDto("A", 10, 1)
        ));
        request.setMode("advanced");

        SolveResponse response = algorithm.solve(request);

        // Should fallback to simple mode for large dimensions
        assertNotNull(response);
        assertTrue(response.getTotalCost() > 0);
    }
}

