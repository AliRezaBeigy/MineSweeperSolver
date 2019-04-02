package ir.kharazmi.minesweepersolver;

import java.security.SecureRandom;

public class Solver {
    int width, height;
    int[][] mainTable;
    Operation mainOperation;
    ImageProcessor imageProcessor;

    public Solver(ImageProcessor imageProcessor) {
        this.imageProcessor = imageProcessor;
        this.width = imageProcessor.getWidth();
        this.height = imageProcessor.getHeight();
        this.mainTable = imageProcessor.getTable();
        mainOperation = new Operation(width, height, mainTable);
    }

    void solve() {
        SecureRandom rand = new SecureRandom();
        int cnt = rand.nextInt(height * width);
        imageProcessor.click(cnt % width, cnt / width);
        mainOperation.update(imageProcessor);
        while (true) {
            mainOperation.mineFinder();
            while (mainOperation.flagable() > 0) {
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        if (mainOperation.flag[i][j]) {
                            imageProcessor.rightClick(i, j);
                            mainOperation.flag[i][j] = false;
                        }
                    }
                }
            }
            mainOperation.clickFinder();
            if (mainOperation.clickable() > 0) {
                while (mainOperation.clickable() > 0) {
                    for (int i = 0; i < width; i++) {
                        for (int j = 0; j < height; j++) {
                            if (mainOperation.click[i][j]) {
                                imageProcessor.click(i, j);
                                mainOperation.click[i][j] = false;
                            }
                        }
                    }
                }
                mainOperation.update(imageProcessor);
            } else {
//                boolean find = false;
//                for (int i = 0; i < width; i++) {
//                    for (int j = 0; j < height; j++) {
//                        if (mainOperation.table[i][j] - mainOperation.getAdj(i, j, -2) == 1) {
//                            for (int k = -1; k < 2; k++) {
//                                for (int l = -1; l < 2; l++) {
//                                    int ii = i + k, jj = j + l;
//                                    if (ii > 0 && jj > 0 && ii < width && jj < height && mainOperation.table[ii][jj] == -1) {
//                                        Operation draft = new Operation(width, height, mainOperation.table);
//                                        draft.table[ii][jj] = -2;
//                                        int clicked;
//                                        draft.clickFinder();
//                                        do {
//                                            clicked = draft.clickable();
//                                            draft.mineFinder();
//                                            draft.clickFinder();
//                                        } while (draft.clickable() > clicked);
//                                        if (draft.checkContradiction()) {
//                                            mainOperation.click[ii][jj] = true;
//                                            find = true;
//                                            break;
//                                        }
//                                    }
//                                }
//                                if (find)
//                                    break;
//                            }
//                        }//hala bar axesh
//                        if (find)
//                            break;
//                    }
//                    if (find)
//                        break;
//                }
//                if (!find) {
//                    //fuck this table
//                    break;
//                }
            }
        }
    }
}
