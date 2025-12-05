# Optimal Tiling for Cost-Efficient Flooring

A full-stack solution (Java Spring Boot backend + Angular frontend) that solves the optimal tiling problem using two algorithms: simple single-type baseline and advanced guillotine-style dynamic programming.

## Problem Statement

Given a rectangular room of dimensions L x W and a set of tile types (each with size and cost), find the optimal combination of tiles that covers the room with minimum total cost.

## Assumptions

- **Tile placement**: Tiles are axis-aligned squares
- **Simple mode**: For a single tile type covering the whole room, the number of tiles required = `ceil(L / size) * ceil(W / size)`. This reflects that tiles cannot be cut and any partial tile that spills beyond the room boundary is counted fully.
- **Advanced mode**: Uses guillotine-style dynamic programming:
  - Recursively splits the room along integer lengths (horizontal or vertical cuts)
  - A placed tile must fit entirely inside a sub-rectangle
  - DP recurrence: `dp[l][w] = minimal cost to cover rectangle l x w`
  - For each tile: place if `size <= max(l,w)`, then split remaining area into sub-rectangles
  - Also considers all horizontal and vertical cuts

## Algorithm Complexity

- **Simple mode**: O(tiles) - linear in number of tile types
- **Advanced mode**: O(L * W * (L + W + tiles)) - polynomial in dimensions and tile count
- **Safe limits**: L, W <= 500 for DP (configurable via `MAX_DIMENSION` constant)
- **Fallback**: For dimensions > 500, automatically falls back to simple mode

## Project Structure

```
/
├── backend/              # Spring Boot backend
│   ├── src/
│   │   ├── main/java/com/tiling/
│   │   └── test/java/com/tiling/
│   └── pom.xml
├── frontend/             # Angular frontend
│   ├── src/
│   ├── package.json
│   └── angular.json
├── README.md
├── sample_input.json
└── sample_output.json
```

## Building and Running

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Node.js 18+ and npm
- Angular CLI (install via `npm install -g @angular/cli`)

### Backend

1. Navigate to backend directory:
   ```bash
   cd backend
   ```

2. Build the project:
   ```bash
   mvn clean package
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

   The backend will start on `http://localhost:8080`

### Frontend

1. Navigate to frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   ng serve
   ```

   The frontend will be available at `http://localhost:4200`

## API Endpoints

### POST /api/solve

Solves the optimal tiling problem.

**Request:**
```json
{
  "L": 6,
  "W": 4,
  "tiles": [
    {"id": "A", "size": 1, "cost": 2},
    {"id": "B", "size": 2, "cost": 3},
    {"id": "C", "size": 3, "cost": 6}
  ],
  "mode": "advanced"
}
```

**Response:**
```json
{
  "solutionMode": "advanced",
  "tilesUsed": [
    {"id": "A", "size": 1, "count": 0, "cost": 0},
    {"id": "B", "size": 2, "count": 12, "cost": 36},
    {"id": "C", "size": 3, "count": 0, "cost": 0}
  ],
  "totalCost": 36,
  "explanation": "Advanced mode: Used guillotine DP with mixed tiles...",
  "visualization": "Grid visualization..."
}
```

### GET /api/sample

Returns a sample input JSON for testing.

## Example Usage

### Using curl

```bash
curl -X POST http://localhost:8080/api/solve \
  -H "Content-Type: application/json" \
  -d '{
    "L": 6,
    "W": 4,
    "tiles": [
      {"id": "A", "size": 1, "cost": 2},
      {"id": "B", "size": 2, "cost": 3},
      {"id": "C", "size": 3, "cost": 6}
    ],
    "mode": "advanced"
  }'
```

### Testing Example

**Input:**
```
6 4
1 2
2 3
3 6
```

**Expected Output Format:**
```
Tiles of size A: 0
Tiles of size B: 12
Tiles of size C: 0
Total Cost: 36
```

**Note:** The actual solution may differ based on the algorithm mode and implementation assumptions. The sample provided uses ceil-mode for simple mode, while advanced mode uses guillotine DP which may produce different results.

## Running Tests

### Backend Tests

```bash
cd backend
mvn test
```

The test suite includes:
- Sample input validation
- Simple mode tests
- Advanced mode tests
- Edge cases (1x1 room, empty input)
- Invalid input handling

## Implementation Details

### Simple Mode Algorithm

1. For each tile type, compute:
   - `countL = ceil(L / tile.size)`
   - `countW = ceil(W / tile.size)`
   - `totalCount = countL * countW`
   - `totalCost = totalCount * tile.cost`
2. Select the tile type with minimum total cost
3. Return solution with only that tile type used

### Advanced Mode Algorithm

1. Initialize DP table `dp[l][w]` for all dimensions up to L x W
2. For each rectangle (l, w):
   - Try placing each tile type (if it fits)
   - Try all horizontal cuts: `dp[x][w] + dp[l-x][w]`
   - Try all vertical cuts: `dp[l][y] + dp[l][w-y]`
   - Select minimum cost option
3. Reconstruct solution by tracking tile placements
4. Return optimal mixed-tile solution

## Edge Cases Handled

- Empty or zero area → 0 cost, all counts 0
- Duplicate tile sizes → handled normally
- Multiple solutions with same cost → prefer fewer total tiles, then lexicographic by tile id
- Large dimensions (>500) → automatic fallback to simple mode

## Notes

- The visualization is simplified for large grids (>50x50)
- Input validation ensures all values are positive integers
- CORS is enabled for frontend-backend communication
- Error messages are clear and descriptive

## License

This project is provided as-is for educational purposes.

