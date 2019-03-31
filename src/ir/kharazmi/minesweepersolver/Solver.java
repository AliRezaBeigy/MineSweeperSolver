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

    void solve() {
        //TODO first random click
        //TODO mainOperation.update
        while (true) {
            mainOperation.mineFinder();
            mainOperation.clickFinder();
            if (mainOperation.clickable() > 0) {
                //TODO click
                //TODO erase clicked
                //TODO mainOperation.update
            } else {
                boolean find = false;
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        if (mainOperation.table[i][j] - mainOperation.getAdj(i, j, -2) == 1){
                            for (int k = -1; k < 2; k++) {
                                for (int l = -1; l < 2; l++) {
                                    int ii = i+k, jj = j+l;
                                    if (ii > 0 && jj > 0 && ii < width && jj < height && mainOperation.table[ii][jj] == -1) {
                                        Operation draft = new Operation(width, height, mainOperation.table);
                                        draft.table[ii][jj] = -2;
                                        int clicked;
                                        draft.clickFinder();
                                        do{
                                            clicked = draft.clickable();
                                            draft.mineFinder();
                                            draft.clickFinder();
                                        }while(draft.clickable() > clicked);
                                        if (draft.checkContradiction()) {
                                            mainOperation.click[ii][jj] = true;
                                            find = true;
                                            break;
                                        }
                                    }
                                }
                                if (find)
                                    break;
                            }
                        }//hala bar axesh
                        if (find)
                            break;
                    }
                    if (find)
                        break;
                }
                if (!find){
                    //fuck this table
                }

            }
        }
    }
}
