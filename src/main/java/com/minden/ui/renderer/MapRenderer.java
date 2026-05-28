package com.minden.ui.renderer;

import java.util.List;

import com.minden.dto.TreasureDto;
import com.minden.entity.MapTile;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Responsible for rendering all graphic components of the game map, including
 * ground tiles, unexplored regions, treasures, and the player sprite.
 */
public class MapRenderer {

    /**
     * Map cell size in pixels on screen.
     */
    private static final int TILE_SIZE = 50;

    /**
     * Size of a single tile within the tile sheets in pixels.
     */
    private static final int SHEET_TILE_SIZE = 16;

    /**
     * Slicing coordinates for terrain types.
     */
    private static final int FOREST_TILE_COL = 1;
    private static final int FOREST_TILE_ROW = 10;
    private static final int SAND_TILE_COL = 7;
    private static final int SAND_TILE_ROW = 14;
    private static final int CITY_TILE_COL = 17;
    private static final int CITY_TILE_ROW = 0;
    private static final int WATER_TILE_COL = 2;
    private static final int WATER_TILE_ROW = 11;

    /**
     * Sprite sheets for the player animations.
     */
    private Image idleDownSheet;
    private Image idleUpSheet;
    private Image idleSideSheet;
    private Image walkDownSheet;
    private Image walkUpSheet;
    private Image walkSideSheet;

    /**
     * Floor tile atlas.
     */
    private Image floorsTilesSheet;

    /**
     * Chest sprite sheet.
     */
    private Image chestSheet;

    /**
     * Water tile atlas.
     */
    private Image waterTilesSheet;

    /**
     * Constructs a new MapRenderer and loads all required graphic assets.
     */
    public MapRenderer() {
        try {
            idleDownSheet = new Image(getClass().getResourceAsStream("/image/idle/Idle_Down-Sheet.png"));
            idleUpSheet = new Image(getClass().getResourceAsStream("/image/idle/Idle_Up-Sheet.png"));
            idleSideSheet = new Image(getClass().getResourceAsStream("/image/idle/Idle_Side-Sheet.png"));
            walkDownSheet = new Image(getClass().getResourceAsStream("/image/walk/Walk_Down-Sheet.png"));
            walkUpSheet = new Image(getClass().getResourceAsStream("/image/walk/Walk_Up-Sheet.png"));
            walkSideSheet = new Image(getClass().getResourceAsStream("/image/walk/Walk_Side-Sheet.png"));
            floorsTilesSheet = new Image(getClass().getResourceAsStream("/image/tile_texture/Floors_Tiles.png"));
            waterTilesSheet = new Image(getClass().getResourceAsStream("/image/tile_texture/Water_tiles.png"));
            chestSheet = new Image(getClass().getResourceAsStream("/image/chest/chests_1px_padding.png"));
        } catch (Exception e) {
            System.err.println("Failed to load graphic assets: " + e.getMessage());
        }
    }

    /**
     * Відображає карту на наданому графічному контексті з використанням
     * відсікання невидимих елементів.
     *
     * @param gc цільовий графічний контекст
     * @param canvasWidth ширина полотна
     * @param canvasHeight висота полотна
     * @param viewX горизонтальне зміщення видимої області
     * @param viewY вертикальне зміщення видимої області
     * @param viewportWidth ширина видимої області
     * @param viewportHeight висота видимої області
     * @param grid сітка тайлів карти
     * @param exploredTiles матриця досліджених тайлів для туману війни
     * @param treasures список скарбів на карті
     * @param hasPlayer чи потрібно малювати гравця
     * @param targetX логічна цільова координата X гравця
     * @param targetY логічна цільова координата Y гравця
     * @param vpx плавна візуальна координата X гравця
     * @param vpy плавна візуальна координата Y гравця
     * @param facingDirection поточний напрямок погляду гравця
     */
    public void drawMap(GraphicsContext gc, double canvasWidth, double canvasHeight,
            double viewX, double viewY, double viewportWidth, double viewportHeight,
            MapTile[][] grid, boolean[][] exploredTiles, List<TreasureDto> treasures,
            boolean hasPlayer, int targetX, int targetY, double vpx, double vpy, String facingDirection) {

        /**
         * Очищаємо лише видиму прямокутну область для підвищення продуктивності
         */
        gc.setFill(Color.web("#18110b"));
        gc.fillRect(viewX, viewY, viewportWidth, viewportHeight);

        if (grid == null) {
            return;
        }

        int width = grid.length;
        int height = grid[0].length;

        /**
         * Обчислюємо межі видимих тайлів із безпечним відступом у 2 клітинки
         */
        int minX = Math.max(0, (int) (viewX / TILE_SIZE) - 2);
        int maxX = Math.min(width - 1, (int) ((viewX + viewportWidth) / TILE_SIZE) + 2);
        int minY = Math.max(0, (int) (viewY / TILE_SIZE) - 2);
        int maxY = Math.min(height - 1, (int) ((viewY + viewportHeight) / TILE_SIZE) + 2);

        gc.setLineWidth(0.5);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                if (!exploredTiles[x][y]) {
                    continue;
                }

                MapTile tile = grid[x][y];
                if (tile == null) {
                    continue;
                }

                boolean isVisibleNow = false;
                if (hasPlayer) {
                    double dx = x - vpx;
                    double dy = y - vpy;
                    isVisibleNow = (dx * dx + dy * dy <= 16.0);
                }

                Image sheet = null;
                int col = 0;
                int row = 0;

                if (tile.getTerrainType() != null) {
                    switch (tile.getTerrainType()) {
                        case "Water":
                            sheet = waterTilesSheet;
                            col = WATER_TILE_COL;
                            row = WATER_TILE_ROW;
                            break;
                        case "Forest":
                            sheet = floorsTilesSheet;
                            col = FOREST_TILE_COL;
                            row = FOREST_TILE_ROW;
                            break;
                        case "Sand":
                            sheet = floorsTilesSheet;
                            col = SAND_TILE_COL;
                            row = SAND_TILE_ROW;
                            break;
                        case "City":
                            sheet = floorsTilesSheet;
                            col = CITY_TILE_COL;
                            row = CITY_TILE_ROW;
                            break;
                    }
                }

                if (sheet != null && sheet.getWidth() > 0) {
                    int sx = col * SHEET_TILE_SIZE;
                    int sy = row * SHEET_TILE_SIZE;
                    gc.drawImage(
                            sheet,
                            sx, sy, SHEET_TILE_SIZE, SHEET_TILE_SIZE,
                            x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE
                    );
                    if (!isVisibleNow) {
                        gc.setFill(Color.rgb(0, 0, 0, 0.5));
                        gc.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    }
                } else {
                    Color terrainColor = getColorForTerrain(tile.getTerrainType());
                    if (isVisibleNow) {
                        gc.setFill(terrainColor);
                    } else {
                        gc.setFill(terrainColor.deriveColor(0, 1.0, 0.40, 1.0));
                    }
                    gc.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }

                gc.setStroke(Color.web("#5e4531", isVisibleNow ? 0.35 : 0.15));
                gc.strokeRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        if (treasures != null) {
            for (TreasureDto treasure : treasures) {
                if (!treasure.getIsCollected()) {
                    int tx = treasure.getX();
                    int ty = treasure.getY();

                    /**
                     * Відсікаємо скарби, які знаходяться поза межами видимості
                     */
                    if (tx < minX || tx > maxX || ty < minY || ty > maxY) {
                        continue;
                    }

                    if (hasPlayer) {
                        double dx = tx - vpx;
                        double dy = ty - vpy;
                        if (dx * dx + dy * dy <= 16.0) {
                            if (chestSheet != null && chestSheet.getWidth() > 0) {
                                gc.drawImage(
                                        chestSheet,
                                        1, 1, 16, 16,
                                        tx * TILE_SIZE, ty * TILE_SIZE, TILE_SIZE, TILE_SIZE
                                );
                            } else {
                                gc.setFill(Color.web("#8B4513"));
                                double padding = 6.0;
                                gc.fillOval(
                                        tx * TILE_SIZE + padding,
                                        ty * TILE_SIZE + padding,
                                        TILE_SIZE - padding * 2.0,
                                        TILE_SIZE - padding * 2.0
                                );
                                gc.setStroke(Color.web("#1e1e2e"));
                                gc.setLineWidth(1.0);
                                gc.strokeOval(
                                        tx * TILE_SIZE + padding,
                                        ty * TILE_SIZE + padding,
                                        TILE_SIZE - padding * 2.0,
                                        TILE_SIZE - padding * 2.0
                                );
                            }
                        }
                    }
                }
            }
        }

        if (hasPlayer) {
            boolean isMoving = Math.sqrt((targetX - vpx) * (targetX - vpx) + (targetY - vpy) * (targetY - vpy)) > 0.01;

            Image activeSheet = null;
            boolean isLeft = false;

            if (isMoving) {
                switch (facingDirection) {
                    case "UP":
                        activeSheet = walkUpSheet;
                        break;
                    case "DOWN":
                        activeSheet = walkDownSheet;
                        break;
                    case "LEFT":
                        activeSheet = walkSideSheet;
                        isLeft = true;
                        break;
                    case "RIGHT":
                        activeSheet = walkSideSheet;
                        break;
                }
            } else {
                switch (facingDirection) {
                    case "UP":
                        activeSheet = idleUpSheet;
                        break;
                    case "DOWN":
                        activeSheet = idleDownSheet;
                        break;
                    case "LEFT":
                        activeSheet = idleSideSheet;
                        isLeft = true;
                        break;
                    case "RIGHT":
                        activeSheet = idleSideSheet;
                        break;
                }
            }

            double padding = 2.0;

            if (activeSheet != null && activeSheet.getWidth() > 0) {
                int sheetWidth = (int) activeSheet.getWidth();
                int sheetHeight = (int) activeSheet.getHeight();
                int frameWidth = sheetHeight;
                int frameHeight = sheetHeight;
                int totalFrames = sheetWidth / sheetHeight;

                int frameIndex = (int) ((System.currentTimeMillis() / 120) % totalFrames);
                int sx = frameIndex * frameWidth;

                double aspect = (double) frameWidth / frameHeight;
                double scaleFactor = 2;
                double destHeight = TILE_SIZE * scaleFactor;
                double destWidth = destHeight * aspect;

                double offsetX = (TILE_SIZE - destWidth) / 2.0;
                double offsetY = TILE_SIZE - destHeight;

                double drawX = vpx * TILE_SIZE + offsetX;
                double drawY = vpy * TILE_SIZE + offsetY;

                if (isLeft) {
                    gc.save();
                    gc.translate(drawX + destWidth / 2.0, drawY + destHeight / 2.0);
                    gc.scale(-1, 1);
                    gc.drawImage(
                            activeSheet,
                            sx, 0, frameWidth, frameHeight,
                            -destWidth / 2.0, -destHeight / 2.0, destWidth, destHeight
                    );
                    gc.restore();
                } else {
                    gc.drawImage(
                            activeSheet,
                            sx, 0, frameWidth, frameHeight,
                            drawX, drawY, destWidth, destHeight
                    );
                }
            } else {
                gc.setFill(Color.web("#c63d2f"));
                double size = TILE_SIZE - padding * 2.0;
                gc.fillOval(
                        vpx * TILE_SIZE + padding,
                        vpy * TILE_SIZE + padding,
                        size,
                        size
                );
                gc.setStroke(Color.web("#dfaf64"));
                gc.setLineWidth(1.5);
                gc.strokeOval(
                        vpx * TILE_SIZE + padding,
                        vpy * TILE_SIZE + padding,
                        size,
                        size
                );
            }
        }
    }

    /**
     * Resolves the color of the terrain for solid color fallbacks.
     *
     * @param terrainType the terrain type string
     * @return the representing Color
     */
    private Color getColorForTerrain(String terrainType) {
        if (terrainType == null) {
            return Color.BLACK;
        }

        switch (terrainType) {
            case "Water":
                return Color.web("#4b779a");
            case "Forest":
                return Color.web("#4b6d4c");
            case "Sand":
                return Color.web("#ebd8b0");
            case "City":
                return Color.web("#c87a53");
            default:
                return Color.GRAY;
        }
    }
}
