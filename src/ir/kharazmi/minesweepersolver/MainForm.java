package ir.kharazmi.minesweepersolver;

import javax.swing.*;

public class MainForm {
    private JPanel panel1;
    private JTextArea textArea1;
    private JButton updateBoardButton;
    private JTextField xField;
    private JTextField yField;
    private JButton clickButton;
    private JButton resetGameButton;

    MainForm(ImageProcessor imageProcessor) {
        JFrame jFrame = new JFrame();
        jFrame.setContentPane(panel1);
        jFrame.setVisible(true);
        jFrame.setSize(550, 700);

        updateBoardButton.addActionListener(e -> {
            imageProcessor.updateBoard();

            StringBuilder result = new StringBuilder();
            Tile[][] board = imageProcessor.getBoard();
            for (Tile[] tiles : board) {
                for (Tile tile : tiles) {
                    result.append(String.format("%4s", tile.getState()));
                }
                result.append("\n");
            }
            textArea1.setText(result.toString());
        });

        clickButton.addActionListener(e -> {
            imageProcessor.flag(Integer.parseInt(xField.getText()), Integer.parseInt(yField.getText()));
        });

        resetGameButton.addActionListener(e -> imageProcessor.reset());

        StringBuilder result = new StringBuilder();
        Tile[][] board = imageProcessor.getBoard();
        for (Tile[] tiles : board) {
            for (Tile tile : tiles) {
                result.append(String.format("%4s", tile.getState()));
            }
            result.append("\n");
        }
        textArea1.setText(result.toString());
    }
}
