package ir.kharazmi.minesweepersolver;

public class Solver {
    int width, height;
    int[][] mainTable;
    Operation mainOperation;

    public Solver(int width, int height, int[][] mainTable) {
        this.width = width;
        this.height = height;
        this.mainTable = mainTable;
        mainOperation = new Operation(width, height, mainTable);
    }

    boolean clickable() {
        int sum = 0;
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                if (mainOperation.click[i][j])
                    return true;
        return false;
    }

    void solve() {
        //TODO first random click
        //TODO mainOperation.update
        while (true) {
            mainOperation.mineFinder();
            mainOperation.clickFinder();
            if (clickable()) {
                //TODO click
                //TODO erase clicked
            } else {

            }
        }
    }
}
