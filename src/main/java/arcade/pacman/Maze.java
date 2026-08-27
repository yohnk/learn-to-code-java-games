package arcade.pacman;

import arcade.engine.GridPos;
import java.util.ArrayList;
import java.util.List;

/**
 * Tile map, pellets, and spawn points. The layout is original-inspired, not
 * a copy of Namco's maze.
 */
public final class Maze {
    public static final String[] ROWS = {
            "#####################",
            "#.........#.........#",
            "#o##.####.#.####.##o#",
            "#.##.####.#.####.##.#",
            "#...................#",
            "#.##.#.#######.#.##.#",
            "#....#....#....#....#",
            "#####.###.#.###.#####",
            "XXXX#.#XX___XX#.#XXXX",
            "#####.#.##=##.#.#####",
            "_____#_BINC_#________",
            "#####.#.##_##.#.#####",
            "XXXX#.#XXXXXXX#.#XXXX",
            "#####.###.#.###.#####",
            "#.........#.........#",
            "#.##.####.#.####.##.#",
            "#o.#.............#.o#",
            "##.#.#.#######.#.#.##",
            "#....#....P....#....#",
            "#.######.#.######.#.#",
            "#...................#",
            "#####################"
    };

    private final int width;
    private final int height;
    private final Tile[][] tiles;
    private GridPos pacSpawn = new GridPos(1, 1);
    private final GridPos[] ghostSpawns = new GridPos[4];
    private GridPos door = new GridPos(10, 9);

    public Maze() {
        height = ROWS.length;
        width = ROWS[0].length();
        tiles = new Tile[height][width];
        for (int y = 0; y < height; y++) {
            String row = ROWS[y];
            if (row.length() != width) {
                throw new IllegalStateException("Maze row " + y + " length " + row.length() + " != " + width);
            }
            for (int x = 0; x < width; x++) {
                tiles[y][x] = parse(row.charAt(x), x, y);
            }
        }
        for (int i = 0; i < ghostSpawns.length; i++) {
            if (ghostSpawns[i] == null) {
                ghostSpawns[i] = door;
            }
        }
    }

    int width() {
        return width;
    }

    int height() {
        return height;
    }

    public Tile tile(int x, int y) {
        if (y < 0 || y >= height) {
            return Tile.WALL;
        }
        return tiles[y][wrapX(x)];
    }

    void setTile(int x, int y, Tile tile) {
        tiles[y][wrapX(x)] = tile;
    }

    int wrapX(int x) {
        if (x < 0) {
            return width - 1;
        }
        if (x >= width) {
            return 0;
        }
        return x;
    }

    boolean walkableForPac(int x, int y) {
        if (y < 0 || y >= height) {
            return false;
        }
        Tile tile = tiles[y][wrapX(x)];
        return tile != Tile.WALL && tile != Tile.DOOR;
    }

    boolean walkableForGhost(int x, int y) {
        if (y < 0 || y >= height) {
            return false;
        }
        Tile tile = tiles[y][wrapX(x)];
        return tile != Tile.WALL;
    }

    public GridPos pacSpawn() {
        return pacSpawn;
    }

    GridPos ghostSpawn(int index) {
        return ghostSpawns[index];
    }

    public GridPos door() {
        return door;
    }

    public int pelletCount() {
        int count = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (tiles[y][x] == Tile.PELLET || tiles[y][x] == Tile.POWER) {
                    count++;
                }
            }
        }
        return count;
    }

    Tile[][] copyTiles() {
        Tile[][] copy = new Tile[height][width];
        for (int y = 0; y < height; y++) {
            System.arraycopy(tiles[y], 0, copy[y], 0, width);
        }
        return copy;
    }

    List<GridPos> pellets() {
        return collect(Tile.PELLET);
    }

    List<GridPos> powerPellets() {
        return collect(Tile.POWER);
    }

    void resetPellets() {
        for (int y = 0; y < height; y++) {
            String row = ROWS[y];
            for (int x = 0; x < width; x++) {
                char cell = row.charAt(x);
                if (cell == '.') {
                    tiles[y][x] = Tile.PELLET;
                } else if (cell == 'o') {
                    tiles[y][x] = Tile.POWER;
                }
            }
        }
    }

    boolean inHouse(int x, int y) {
        return x >= 6 && x <= 12 && y >= 9 && y <= 11;
    }

    private List<GridPos> collect(Tile wanted) {
        List<GridPos> list = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (tiles[y][x] == wanted) {
                    list.add(new GridPos(x, y));
                }
            }
        }
        return List.copyOf(list);
    }

    private Tile parse(char cell, int x, int y) {
        return switch (cell) {
            case '#' -> Tile.WALL;
            case 'X' -> Tile.WALL;
            case '.' -> Tile.PELLET;
            case 'o' -> Tile.POWER;
            case '=' -> {
                door = new GridPos(x, y);
                yield Tile.DOOR;
            }
            case 'P' -> {
                pacSpawn = new GridPos(x, y);
                yield Tile.EMPTY;
            }
            case 'B' -> {
                ghostSpawns[0] = new GridPos(x, y);
                yield Tile.EMPTY;
            }
            case 'I' -> {
                ghostSpawns[1] = new GridPos(x, y);
                yield Tile.EMPTY;
            }
            case 'N' -> {
                ghostSpawns[2] = new GridPos(x, y);
                yield Tile.EMPTY;
            }
            case 'C' -> {
                ghostSpawns[3] = new GridPos(x, y);
                yield Tile.EMPTY;
            }
            case '_' -> Tile.EMPTY;
            default -> Tile.EMPTY;
        };
    }
}
