package ir.kharazmi.minesweepersolver;

import javax.swing.*;
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
                int confirm = JOptionPane.showConfirmDialog(null, "Do you want to reset?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    imageProcessor.reset();
                    imageProcessor.updateBoard();
                    new Solver(imageProcessor).solve();
                } else
                    System.exit(1);
            }
        }
    }
}
