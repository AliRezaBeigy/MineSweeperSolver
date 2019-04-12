package ir.kharazmi.minesweepersolver;

public class Main {

    public static void main(String[] args) {
        ImageProcessor imageProcessor = new ImageProcessor();
        imageProcessor.startGame();
        imageProcessor.init();
        //ImageProcessor Debugger
        new MainForm(imageProcessor);
        new Solver(imageProcessor).solve();
    }
}
