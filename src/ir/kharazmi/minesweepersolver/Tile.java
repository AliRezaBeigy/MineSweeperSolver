package ir.kharazmi.minesweepersolver;

class Tile {
    private int state;
    private Location location;

    Tile(int state, Location location) {
        this.state = state;
        this.location = location;
    }

    int getState() {
        return state;
    }

    void setState(int state) {
        this.state = state;
    }

    Location getLocation() {
        return location;
    }
}
