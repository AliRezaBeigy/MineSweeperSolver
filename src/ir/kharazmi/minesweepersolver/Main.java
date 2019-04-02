package ir.kharazmi.minesweepersolver;

public class Main {
    public static void main(String[] args) {
        ImageProcessor imageProcessor = new ImageProcessor();
        imageProcessor.startGame();
        imageProcessor.init();
        new Solver(imageProcessor).solve();
    }
}
