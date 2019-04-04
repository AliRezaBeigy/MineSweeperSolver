package ir.kharazmi.minesweepersolver;

public class Tile {
    private int state;
    private Location location;

    Tile(int state, Location location) {
        this.state = state;
        this.location = location;
    }

    int getState() {
        return state;
    }

    Location getLocation() {
        return location;
    }

    void setState(int state) {
        this.state = state;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
