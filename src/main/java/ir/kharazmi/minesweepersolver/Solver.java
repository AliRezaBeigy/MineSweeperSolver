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
                            imageProcessor.flag(i, j);
                            mainOperation.flag[i][j] = false;
                        }
                    }
                }
            }
            mainOperation.update(imageProcessor);
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
                boolean find = false;
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        if (mainOperation.table[i][j] - mainOperation.getAdj(i, j, -2) == 1) {
                            //System.out.println("dore:" + i + " " + j);
                            for (int k = -1; k < 2; k++) {
                                for (int l = -1; l < 2; l++) {
                                    int ii = i + k, jj = j + l;
                                    if (ii > 0 && jj > 0 && ii < width && jj < height && mainOperation.table[ii][jj] == -1) {
                                        Operation draft = new Operation(width, height, mainOperation.table);
                                        draft.table[ii][jj] = -2;
                                        int clicked;
                                        do {
                                            clicked = draft.clickable();
                                            draft.mineFinder();
                                            draft.clickFinder();
                                        } while (draft.clickable() > clicked);
                                        if (draft.checkContradiction()) {
                                            System.out.println("ooooffff" + ii + " " + jj);
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
                if (!find) {
                    if (mainOperation.remain() == 0) {
                        System.out.println("win!!!");
                        break;
                    }
                    //fuck this table
                    System.out.println("random needed");
                    mainOperation.randomClick(imageProcessor);
                    mainOperation.update(imageProcessor);
                    mainOperation.mineFinder();
                    mainOperation.clickFinder();
                }
//                int confirm = JOptionPane.showConfirmDialog(null, "Do you want to reset?", "Confirm", JOptionPane.YES_NO_OPTION);
//                if (confirm == JOptionPane.YES_OPTION) {
//                    imageProcessor.reset();
//                    imageProcessor.updateBoard();
//                    new Solver(imageProcessor).solve();
//                } else
//                    System.exit(1);
            }
        }
    }
}
