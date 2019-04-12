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
        jFrame.setSize(550, 420);

        imageProcessor.setUpdateBoardListener(board -> {
            StringBuilder result = new StringBuilder();
            Tile[][] tiles1 = new Tile[board[0].length][board.length];
            for (int i = 0; i < board.length; i++) {
                Tile[] tiles = board[i];
                for (int i1 = 0; i1 < tiles.length; i1++) {
                    tiles1[i1][i] = board[i][i1];
                }
            }
            for (Tile[] tiles : tiles1) {
                for (Tile tile : tiles) {
                    result.append(String.format("%4s", tile.getState()));
                }
                result.append("\n");
            }
            textArea1.setText(result.toString());
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        });

        updateBoardButton.addActionListener(e -> {
            imageProcessor.updateBoard();

            StringBuilder result = new StringBuilder();
            Tile[][] board = imageProcessor.getBoard();
            Tile[][] tiles1 = new Tile[board[0].length][board.length];
            for (int i = 0; i < board.length; i++) {
                Tile[] tiles = board[i];
                for (int i1 = 0; i1 < tiles.length; i1++) {
                    tiles1[i1][i] = board[i][i1];
                }
            }
            for (Tile[] tiles : tiles1) {
                for (Tile tile : tiles) {
                    result.append(String.format("%4s", tile.getState()));
                }
                result.append("\n");
            }
            textArea1.setText(result.toString());
        });

        clickButton.addActionListener(e -> {
            imageProcessor.click(Integer.parseInt(xField.getText()), Integer.parseInt(yField.getText()));
        });

        resetGameButton.addActionListener(e -> imageProcessor.reset());
        StringBuilder result = new StringBuilder();
        Tile[][] board = imageProcessor.getBoard();
        Tile[][] tiles1 = new Tile[board[0].length][board.length];
        for (int i = 0; i < board.length; i++) {
            Tile[] tiles = board[i];
            for (int i1 = 0; i1 < tiles.length; i1++) {
                tiles1[i1][i] = board[i][i1];
            }
        }
        for (Tile[] tiles : tiles1) {
            for (Tile tile : tiles) {
                result.append(String.format("%4s", tile.getState()));
            }
            result.append("\n");
        }
        textArea1.setText(result.toString());
    }
}
