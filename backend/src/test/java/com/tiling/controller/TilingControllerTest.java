package com.tiling.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiling.dto.SolveRequest;
import com.tiling.dto.SolveResponse;
import com.tiling.dto.TileDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
class TilingControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @Test
    void testSampleEndpoint() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        mockMvc.perform(get("/api/sample"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.L").value(6))
            .andExpect(jsonPath("$.W").value(4))
            .andExpect(jsonPath("$.tiles.length()").value(3));
    }

    @Test
    void testSolveSimpleMode() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        SolveRequest request = new SolveRequest();
        request.setL(6);
        request.setW(4);
        request.setTiles(Arrays.asList(
            new TileDto("A", 1, 2),
            new TileDto("B", 2, 3),
            new TileDto("C", 3, 6)
        ));
        request.setMode("simple");

        mockMvc.perform(post("/api/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.solutionMode").value("simple"))
            .andExpect(jsonPath("$.totalCost").exists());
    }

    @Test
    void testSolveAdvancedMode() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        SolveRequest request = new SolveRequest();
        request.setL(6);
        request.setW(4);
        request.setTiles(Arrays.asList(
            new TileDto("A", 1, 2),
            new TileDto("B", 2, 3),
            new TileDto("C", 3, 6)
        ));
        request.setMode("advanced");

        mockMvc.perform(post("/api/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.solutionMode").value("advanced"))
            .andExpect(jsonPath("$.totalCost").exists())
            .andExpect(jsonPath("$.tilesUsed.length()").value(3));
    }

    @Test
    void testInvalidInputNegativeLength() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        SolveRequest request = new SolveRequest();
        request.setL(-1);
        request.setW(4);
        request.setTiles(Arrays.asList(new TileDto("A", 1, 2)));

        mockMvc.perform(post("/api/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testInvalidInputZeroTileSize() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        SolveRequest request = new SolveRequest();
        request.setL(6);
        request.setW(4);
        request.setTiles(Arrays.asList(new TileDto("A", 0, 2)));

        mockMvc.perform(post("/api/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testEdgeCase1x1Room() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        SolveRequest request = new SolveRequest();
        request.setL(1);
        request.setW(1);
        request.setTiles(Arrays.asList(
            new TileDto("A", 1, 1),
            new TileDto("B", 2, 5)
        ));
        request.setMode("simple");

        mockMvc.perform(post("/api/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCost").value(1));
    }

    @Test
    void testMixedTileCase() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        SolveRequest request = new SolveRequest();
        request.setL(10);
        request.setW(8);
        request.setTiles(Arrays.asList(
            new TileDto("A", 2, 5),
            new TileDto("B", 3, 8),
            new TileDto("C", 4, 12)
        ));
        request.setMode("advanced");

        mockMvc.perform(post("/api/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.solutionMode").value("advanced"))
            .andExpect(jsonPath("$.totalCost").exists());
    }
}

