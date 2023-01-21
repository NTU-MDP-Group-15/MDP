package arena;

public enum ArenaDirections {

    UP, LEFT, DOWN, RIGHT;

    public static ArenaDirections getAntiClockwise(ArenaDirections currDirection) {
        return values()[(currDirection.ordinal() + 1) % values().length];
    }

    public static ArenaDirections getClockwise(ArenaDirections currDirection) {
        return values()[(currDirection.ordinal() + values().length- 1) % values().length];
    }

    public static ArenaDirections getOpposite(ArenaDirections currDirection) {
        return values()[(currDirection.ordinal() + 2) % values().length];
    }
}