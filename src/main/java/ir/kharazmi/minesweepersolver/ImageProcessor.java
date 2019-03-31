package ir.kharazmi.minesweepersolver;

import org.opencv.core.Point;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ImageProcessor {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private Process process;

    private List<Template> unknownTile;
    private List<Template> emptyTile;
    private List<Template> flagTile;
    private List<Template> oneTile;
    private List<Template> twoTile;
    private List<Template> threeTile;
    private List<Template> fourTile;
    private List<Template> fiveTile;
    private List<Template> sixTile;
    private List<Template> sevenTile;
    private List<Template> eightTile;

    private Tile[][] board;
    private Integer[] gameLocationTL;

    private int TileWidth;
    private int TileHeight;

    void init() {
        unknownTile = getTemplates("unknown");
        emptyTile = getTemplates("empty");
        flagTile = getTemplates("flag");
        oneTile = getTemplates("one");
        twoTile = getTemplates("two");
        threeTile = getTemplates("three");
        fourTile = getTemplates("four");
        fiveTile = getTemplates("five");
        sixTile = getTemplates("six");
        sevenTile = getTemplates("seven");
        eightTile = getTemplates("eight");

        Mat gameBoard = getScreenshot();

        ArrayList<Integer[]> boarder_locations = match(gameBoard, unknownTile, Color.red);

        int w = 0;
        int h = 0;
        int temp = boarder_locations.get(0)[0];
        for (Integer[] item : boarder_locations) {
            if (item[0] == temp)
                w += 1;
            else
                break;
        }
        temp = boarder_locations.get(0)[1];
        for (int i = 0; i < boarder_locations.size(); i += w) {
            Integer[] item = boarder_locations.get(i);
            if (item[1] == temp)
                h += 1;
            else
                break;
        }

        board = new Tile[w][h];
        for (int i = 0; i < w; i++)
            for (int j = 0; j < h; j++)
                board[i][j] = new Tile(-1, boarder_locations.get(i + (j * w)));

        TileWidth = board[0][1].getLocation()[0] - board[0][0].getLocation()[0];
        TileHeight = board[1][0].getLocation()[1] - board[0][0].getLocation()[1];
    }

    void startGame() {
        try {
            process = Runtime.getRuntime().exec("resources\\game.exe");
        } catch (IOException ignored) {
        }
    }

    void click(int x, int y) {
        try {
            Robot bot = new Robot();
            Integer[] tileLocation = board[x][y].getLocation();
            bot.mouseMove(tileLocation[0] + gameLocationTL[0] + (TileWidth / 2), tileLocation[1] + gameLocationTL[1] + (TileHeight / 2));
            bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        } catch (AWTException ignored) {
        }
    }

    void updateBoard() {
        Mat gameBoard = getScreenshot();

        ArrayList<Integer[]> unknownTileLocations = match(gameBoard, unknownTile, null);
        ArrayList<Integer[]> emptyTileLocations = match(gameBoard, emptyTile, null);
        ArrayList<Integer[]> flagTileLocations = match(gameBoard, flagTile, null);
        ArrayList<Integer[]> oneTileLocations = match(gameBoard, oneTile, null);
        ArrayList<Integer[]> twoTileLocations = match(gameBoard, twoTile, null);
        ArrayList<Integer[]> threeTileLocations = match(gameBoard, threeTile, null);
        ArrayList<Integer[]> fourTileLocations = match(gameBoard, fourTile, null);
        ArrayList<Integer[]> fiveTileLocations = match(gameBoard, fiveTile, null);
        ArrayList<Integer[]> sixTileLocations = match(gameBoard, sixTile, null);
        ArrayList<Integer[]> sevenTileLocations = match(gameBoard, sevenTile, null);
        ArrayList<Integer[]> eightTileLocations = match(gameBoard, eightTile, null);

        updateBoard(flagTileLocations, -2);
        updateBoard(unknownTileLocations, -1);
        updateBoard(emptyTileLocations, 0);
        updateBoard(oneTileLocations, 1);
        updateBoard(twoTileLocations, 2);
        updateBoard(threeTileLocations, 3);
        updateBoard(fourTileLocations, 4);
        updateBoard(fiveTileLocations, 5);
        updateBoard(sixTileLocations, 6);
        updateBoard(sevenTileLocations, 7);
        updateBoard(eightTileLocations, 8);
    }

    private void updateBoard(ArrayList<Integer[]> tileLocations, int state) {
        for (Integer[] tileLocation : tileLocations) {
            TileLoop:
            for (Integer[] approximateLocation : getApproximateLocations(tileLocation, 2))
                for (Tile[] yTiles : board)
                    for (Tile tile : yTiles)
                        if (Arrays.equals(approximateLocation, tile.getLocation())) {
                            tile.setState(state);
                            break TileLoop;
                        }
        }
    }

    Tile[][] getBoard() {
        return board;
    }

    Process getProcess() {
        return process;
    }

    private List<Template> getTemplates(String name) {
        List<Template> result = new ArrayList<>();
        File dir = new File("resources\\" + name);
        File[] files = dir.listFiles((dir1, filename) -> filename.endsWith(".png"));
        if (files != null)
            for (File file : files) {
                String extension = file.getName().split("\\.")[1];
                result.add(new Template(Imgcodecs.imread(file.getAbsolutePath())
                        , extension.equals("png") ? 0.8f : Float.parseFloat("0." + extension)));
            }
        return result;
    }

    private boolean isZero(Mat mat, int i, int j, int accuracy) {
        boolean result = true;
        for (int w = -1 * accuracy; w < accuracy; w++)
            try {
                double[] point = mat.get(j, i + w);
                result = point == null || point.length <= 0 || point[0] == 0;
                if (result)
                    return result;
            } catch (Exception ignored) {
            }
        return result;
    }

    private ArrayList<Integer[]> getApproximateLocations(Integer[] location, int accuracy) {
        int i = location[0];
        int j = location[1];

        ArrayList<Integer[]> locations = new ArrayList<>();

        for (int w = -1 * accuracy; w < accuracy; w++)
            for (int h = -1 * accuracy; h < accuracy; h++)
                locations.add(new Integer[]{i + w, j + h});

        return locations;
    }

    private ArrayList<Integer[]> extendLocations(ArrayList<Integer[]> newLocations, ArrayList<Integer[]> oldLocations) {
        ArrayList<Integer[]> result = new ArrayList<>(oldLocations);
        for (Integer[] newLocation : newLocations) {
            boolean exist = false;
            OldLocation:
            for (Integer[] oldLocation : oldLocations)
                for (Integer[] approximateLocation : getApproximateLocations(oldLocation, 2))
                    if (Arrays.equals(newLocation, approximateLocation)) {
                        exist = true;
                        break OldLocation;
                    }
            if (!exist)
                result.add(newLocation);
        }
        return result;
    }

    private ArrayList<Integer[]> match(Mat src, List<Template> templates, Color color) {
        ArrayList<Integer[]> result = new ArrayList<>();
        for (Template template : templates)
            result = extendLocations(result, match(src, template, color));
        return result;
    }

    private ArrayList<Integer[]> match(Mat src, Template template, Color color) {
        Mat srcGray = new Mat();
        Mat templateGray = new Mat();
        Imgproc.cvtColor(src, srcGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(template.getTelmplate(), templateGray, Imgproc.COLOR_BGR2GRAY);
        Mat res = new Mat();
        Imgproc.matchTemplate(srcGray, templateGray, res, Imgproc.TM_CCOEFF_NORMED);

        ArrayList<Integer[]> locations = new ArrayList<>();

        for (int i = 0; i < res.width(); i++)
            for (int j = 0; j < res.height(); j++) {
                if (res.get(j, i)[0] >= template.getThreshold()) {
                    locations.add(new Integer[]{i, j});
                    if (color != null)
                        Imgproc.circle(src
                                , new Point(i + new Float(template.getTelmplate().width() / 2f).intValue()
                                        , j + new Float(template.getTelmplate().height() / 2f).intValue())
                                , 2
                                , new Scalar(color.getRed(), color.getGreen(), color.getBlue()));
                }
            }
        return locations;
    }

    private Mat getScreenshot() {
        gameLocationTL = null;
        Integer[] gameLocationBR = null;

        try {
            Thread.sleep(1000);
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            BufferedImage image = new Robot().createScreenCapture(new Rectangle(screenSize));
            ImageIO.write(image, "png", new File("resources\\game.png"));
            Thread.sleep(500);
        } catch (IOException | AWTException | InterruptedException ignored) {
        }

        Mat gameBoard = Imgcodecs.imread("resources\\game.png");
        Mat rangeMat = new Mat();
        Core.inRange(gameBoard, new Scalar(192, 192, 192), new Scalar(192, 192, 192), rangeMat);
        for (int i = 0; i < rangeMat.width(); i++)
            for (int j = 0; j < rangeMat.height(); j++)
                if (!isZero(rangeMat, i, j, 5))
                    if (gameLocationTL == null)
                        gameLocationTL = new Integer[]{i, j};
                    else
                        gameLocationBR = new Integer[]{i, j};
        if (gameLocationBR == null || gameLocationTL == null)
            throw new RuntimeException("I can't detect game :(");
        gameBoard = new Mat(gameBoard, new Rect(gameLocationTL[0], gameLocationTL[1], gameLocationBR[0] - gameLocationTL[0], gameLocationBR[1] - gameLocationTL[1]));
        return gameBoard;
    }
}
