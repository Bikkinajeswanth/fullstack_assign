import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

interface Tile {
  id: string;
  size: number;
  cost: number;
}

interface TileUsage {
  id: string;
  size: number;
  count: number;
  cost: number;
}

interface SolveRequest {
  L: number;
  W: number;
  tiles: Tile[];
  mode: string;
}

interface SolveResponse {
  solutionMode: string;
  tilesUsed: TileUsage[];
  totalCost: number;
  explanation: string;
  visualization: string;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <h1>Optimal Tiling for Cost-Efficient Flooring</h1>
      
      <div class="form-section">
        <div class="form-group">
          <label>Room Length (L):</label>
          <input type="number" [(ngModel)]="request.L" min="1" required>
        </div>
        
        <div class="form-group">
          <label>Room Width (W):</label>
          <input type="number" [(ngModel)]="request.W" min="1" required>
        </div>
        
        <div class="form-group">
          <label>Solution Mode:</label>
          <select [(ngModel)]="request.mode">
            <option value="simple">Simple (Single-type baseline)</option>
            <option value="advanced">Advanced (Mixed tiles with DP)</option>
          </select>
        </div>
        
        <div class="tiles-section">
          <h3>Tile Definitions</h3>
          <div class="tile-input" *ngFor="let tile of request.tiles; let i = index">
            <div>
              <label>Tile ID:</label>
              <input type="text" [(ngModel)]="tile.id" placeholder="A, B, C...">
            </div>
            <div>
              <label>Size:</label>
              <input type="number" [(ngModel)]="tile.size" min="1" placeholder="Size">
            </div>
            <div>
              <label>Cost:</label>
              <input type="number" [(ngModel)]="tile.cost" min="1" placeholder="Cost">
            </div>
          </div>
        </div>
        
        <button (click)="loadSample()">Load Sample</button>
        <button (click)="solve()" [disabled]="loading">Compute</button>
      </div>
      
      <div *ngIf="error" class="error">{{ error }}</div>
      
      <div *ngIf="loading" class="loading">Computing optimal solution...</div>
      
      <div *ngIf="response && !loading" class="results-section">
        <h2>Results</h2>
        
        <div *ngFor="let tile of response.tilesUsed" class="tile-result">
          <strong>Tiles of size {{ tile.id }}:</strong> {{ tile.count }}
          <span *ngIf="tile.count > 0">(Cost: {{ tile.cost }})</span>
        </div>
        
        <div class="total-cost">
          Total Cost: {{ response.totalCost }}
        </div>
        
        <div class="explanation">
          <strong>Explanation:</strong> {{ response.explanation }}
        </div>
        
        <div *ngIf="response.visualization" class="visualization">
          <strong>Visualization:</strong><br>
          {{ response.visualization }}
        </div>
        
        <button class="download-btn" (click)="downloadResult()">Download Result (JSON)</button>
      </div>
    </div>
  `,
  styles: []
})
export class AppComponent {
  request: SolveRequest = {
    L: 6,
    W: 4,
    tiles: [
      { id: 'A', size: 1, cost: 2 },
      { id: 'B', size: 2, cost: 3 },
      { id: 'C', size: 3, cost: 6 }
    ],
    mode: 'advanced'
  };
  
  response: SolveResponse | null = null;
  loading = false;
  error: string | null = null;
  
  constructor(private http: HttpClient) {}
  
  loadSample() {
    this.http.get<SolveRequest>('http://localhost:8080/api/sample').subscribe({
      next: (data) => {
        this.request = data;
        this.error = null;
      },
      error: (err) => {
        this.error = 'Failed to load sample: ' + err.message;
      }
    });
  }
  
  solve() {
    // Validation
    if (!this.request.L || !this.request.W || this.request.L < 1 || this.request.W < 1) {
      this.error = 'Length and Width must be positive integers';
      return;
    }
    
    if (!this.request.tiles || this.request.tiles.length === 0) {
      this.error = 'At least one tile must be provided';
      return;
    }
    
    for (let tile of this.request.tiles) {
      if (!tile.id || !tile.size || !tile.cost || tile.size < 1 || tile.cost < 1) {
        this.error = 'All tiles must have valid id, size, and cost (all positive)';
        return;
      }
    }
    
    this.loading = true;
    this.error = null;
    this.response = null;
    
    this.http.post<SolveResponse>('http://localhost:8080/api/solve', this.request).subscribe({
      next: (data) => {
        this.response = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error: ' + (err.error?.message || err.message || 'Unknown error');
        this.loading = false;
      }
    });
  }
  
  downloadResult() {
    if (!this.response) return;
    
    const dataStr = JSON.stringify(this.response, null, 2);
    const dataBlob = new Blob([dataStr], { type: 'application/json' });
    const url = URL.createObjectURL(dataBlob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'tiling_result.json';
    link.click();
    URL.revokeObjectURL(url);
  }
}

