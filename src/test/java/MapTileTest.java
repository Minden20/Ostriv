import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.minden.entity.MapTile;

public class MapTileTest {

    @Test
    void shouldReturnCost1ForForest() {
        MapTile forestTile = MapTile.builder().x(0).y(0).terrainType("Forest").build();
        assertEquals(1, forestTile.getMovementCost(), "Ліс повинен вимагати 1 одиницю енергії");
    }

    @Test
    void shouldReturnCost2ForSand() {
        MapTile sandTile = MapTile.builder().x(0).y(0).terrainType("Sand").build();
        assertEquals(2, sandTile.getMovementCost(), "Пісок повинен вимагати 2 одиниці енергії");
    }

    @Test
    void shouldReturnCost5ForMountain() {
        MapTile mountainTile = MapTile.builder().x(0).y(0).terrainType("Mountain").build();
        assertEquals(5, mountainTile.getMovementCost(), "Гори повинні вимагати 5 одиниць енергії");
    }

    @Test
    void shouldReturnCost999ForWater() {
        MapTile waterTile = MapTile.builder().x(0).y(0).terrainType("Water").build();
        assertEquals(999, waterTile.getMovementCost(), "Вода має бути непрохідною (вартість 999)");
    }

    @Test
    void shouldBeCaseInsensitive() {
        MapTile forestLower = MapTile.builder().x(0).y(0).terrainType("forest").build();
        MapTile sandUpper = MapTile.builder().x(0).y(0).terrainType("SAND").build();
        
        assertEquals(1, forestLower.getMovementCost(), "Логіка ландшафту має бути нечутливою до регістру (forest)");
        assertEquals(2, sandUpper.getMovementCost(), "Логіка ландшафту має бути нечутливою до регістру (SAND)");
    }

    @Test
    void shouldReturnDefaultCost1ForUnknownTerrain() {
        MapTile unknownTile = MapTile.builder().x(0).y(0).terrainType("UnknownLava").build();
        assertEquals(1, unknownTile.getMovementCost(), "Невідомий тип місцевості повинен повертати дефолтне значення 1");
    }
}
