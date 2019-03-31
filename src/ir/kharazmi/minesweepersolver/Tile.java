package ir.kharazmi.minesweepersolver;

class Tile {
    private int state;
    private Integer[] location;

    Tile(int state, Integer[] location) {
        this.state = state;
        this.location = location;
    }

    int getState() {
        return state;
    }

    void setState(int state) {
        this.state = state;
    }

    Integer[] getLocation() {
        return location;
    }
}
