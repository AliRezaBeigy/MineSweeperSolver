package ir.kharazmi.minesweepersolver;

public class Location {

    private int x;
    private int y;
    private double threshold;

    Location(int x, int y, double threshold) {
        this.x = x;
        this.y = y;
        this.threshold = threshold;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
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
