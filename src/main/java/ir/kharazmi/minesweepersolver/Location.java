package ir.kharazmi.minesweepersolver;

public class Location {

    private int x;
    private int y;

    Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Location) {
            Location location = (Location) obj;
            return location.getX() == getX() && location.getY() == getY();
        }
        return false;
    }
}
