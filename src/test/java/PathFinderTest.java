import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import com.minden.entity.MapTile;
import com.minden.service.PathFinder;

public class PathFinderTest {

    private MapTile[][] createMockGrid(int width, int height, String defaultTerrain) {
        MapTile[][] grid = new MapTile[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid[x][y] = MapTile.builder()
                        .x(x)
                        .y(y)
                        .terrainType(defaultTerrain)
                        .build();
            }
        }
        return grid;
    }

    @Test
    void shouldFindDirectPathOnFlatTerrain() {
        // Arrange: Створюємо плоску мапу 3x3 з трави/лісу (вартість = 1)
        MapTile[][] grid = createMockGrid(3, 3, "Forest");
        PathFinder pathFinder = new PathFinder();
        
        MapTile start = grid[0][0];
        MapTile end = grid[2][2];

        // Act: Шукаємо шлях
        List<String> path = pathFinder.findPath(grid, start, end);

        // Assert: Шлях має існувати та містити початкову та кінцеву точки
        assertNotNull(path, "Шлях не повинен бути null");
        assertFalse(path.isEmpty(), "Шлях не повинен бути порожнім");
        
        assertEquals("0,0", path.get(0), "Перша точка має бути стартом");
        assertEquals("2,2", path.get(path.size() - 1), "Остання точка має бути фінішем");
        
        // Оскільки це 4-напрямний рух на сітці 3x3, довжина найкоротшого шляху (манхеттенська відстань) має бути 5 тайлів (включаючи старт)
        assertEquals(5, path.size(), "Кількість кроків у найкоротшому шляху має дорівнювати 5");
    }

    @Test
    void shouldAvoidWaterTile() {
        // Arrange: Створюємо мапу 3x3 з лісу
        MapTile[][] grid = createMockGrid(3, 3, "Forest");
        PathFinder pathFinder = new PathFinder();
        
        // Встановлюємо тайл води (непрохідний) по центру (1,1)
        grid[1][1].setTerrainType("Water");
        
        MapTile start = grid[0][0];
        MapTile end = grid[2][2];

        // Act: Шукаємо шлях
        List<String> path = pathFinder.findPath(grid, start, end);

        // Assert: Шлях має оминути центральний тайл (1,1)
        assertNotNull(path, "Шлях має існувати");
        assertFalse(path.isEmpty(), "Шлях не має бути порожнім");
        assertFalse(path.contains("1,1"), "Шлях повинен повністю оминати непрохідний тайл води (1,1)");
        
        // Приклади валідних шляхів:
        // (0,0) -> (0,1) -> (0,2) -> (1,2) -> (2,2)
        // (0,0) -> (1,0) -> (2,0) -> (2,1) -> (2,2)
        assertEquals(5, path.size(), "Довжина шляху в обхід води має складати 5 кроків");
    }

    @Test
    void shouldReturnEmptyPathWhenTargetIsWater() {
        // Arrange: Створюємо мапу 3x3
        MapTile[][] grid = createMockGrid(3, 3, "Forest");
        PathFinder pathFinder = new PathFinder();
        
        // Кінцева точка є водою
        grid[2][2].setTerrainType("Water");
        
        MapTile start = grid[0][0];
        MapTile end = grid[2][2];

        // Act: Шукаємо шлях
        List<String> path = pathFinder.findPath(grid, start, end);

        // Assert: Оскільки вода непрохідна, шлях має бути порожнім
        assertNotNull(path, "Шлях не має бути null");
        assertTrue(path.isEmpty(), "Шлях має бути порожнім, оскільки ціль недосяжна");
    }
}
