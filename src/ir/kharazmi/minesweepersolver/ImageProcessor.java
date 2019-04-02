package ir.kharazmi.minesweepersolver;

import org.opencv.core.Point;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class ImageProcessor {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    int TileWidth;
    int TileHeight;
    private Process process;
    private List<Template> resetTile;
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
    private Location gameLocationTL;

    void init() {
        resetTile = getTemplates("reset");
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
        ArrayList<Location> boarder_locations = match(gameBoard, unknownTile, Color.red);

        ArrayList<Location> locations = new ArrayList<>();
        for (Location boarder_location : boarder_locations) {
            boolean exist = false;
            for (Location approximateLocation : getApproximateLocations(boarder_location, 1))
                if (boarder_locations.contains(approximateLocation) && !approximateLocation.equals(boarder_location))
                    exist = true;
            if (!exist)
                locations.add(boarder_location);
        }

        int h = 0;
        int w = 0;
        int temp = locations.get(0).getX();
        for (Location item : locations) {
            if (item.getX() == temp)
                h += 1;
            else
                break;
        }
        temp = locations.get(0).getY();
        for (int i = 0; i < locations.size(); i += h) {
            Location item = locations.get(i);
            if (item.getY() == temp)
                w += 1;
            else
                break;
        }

        board = new Tile[w][h];
        for (int i = 0; i < w; i++)
            for (int j = 0; j < h; j++) {
                Location location = locations.get(j + (i * h));
                board[i][j] = new Tile(-1, new Location(location.getX(), location.getY()));
            }

        TileWidth = board[1][0].getLocation().getX() - board[0][0].getLocation().getX();
        TileHeight = board[0][1].getLocation().getY() - board[0][0].getLocation().getY();
    }

    public Image toBufferedImage(Mat m) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (m.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = m.channels() * m.cols() * m.rows();
        byte[] b = new byte[bufferSize];
        m.get(0, 0, b); // get all the pixels
        BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;

    }

    void startGame() {
        try {
            process = Runtime.getRuntime().exec("resources\\game.exe");
            Thread.sleep(1000);
        } catch (IOException | InterruptedException ignored) {
        }
    }

    void reset() {
        ArrayList<Location> reset_locations = match(getScreenshot(), resetTile, Color.red);
        try {
            Robot bot = new Robot();
            bot.mouseMove(reset_locations.get(0).getX() + gameLocationTL.getX() + (TileWidth / 2), reset_locations.get(0).getY() + gameLocationTL.getY() + (TileHeight / 2));
            bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            updateBoard();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    void click(int x, int y) {
        try {
            Robot bot = new Robot();
            Location tileLocation = board[x][y].getLocation();
            bot.mouseMove(gameLocationTL.getX() + tileLocation.getX() +  + (TileWidth / 2), gameLocationTL.getY()
                    + tileLocation.getY() + (TileHeight / 2));
            bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            updateBoard();
        } catch (AWTException ignored) {
        }
    }

    void flag(int x, int y) {
        try {
            Robot bot = new Robot();
            Location tileLocation = board[x][y].getLocation();
            bot.mouseMove(gameLocationTL.getX() + tileLocation.getX() +  + (TileWidth / 2), gameLocationTL.getY()
                    + tileLocation.getY() + (TileHeight / 2));
            bot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
            bot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
            updateBoard();
        } catch (AWTException ignored) {
        }
    }

    void updateBoard() {
        Mat gameBoard = getScreenshot();

        Thread unknownTileThread = new Thread(() -> updateBoard(match(gameBoard, unknownTile, null), -1));
        Thread emptyTileThread = new Thread(() -> updateBoard(match(gameBoard, emptyTile, null), 0));
        Thread flagTileThread = new Thread(() -> updateBoard(match(gameBoard, flagTile, null), -2));
        Thread oneTileThread = new Thread(() -> updateBoard(match(gameBoard, oneTile, null), 1));
        Thread twoTileThread = new Thread(() -> updateBoard(match(gameBoard, twoTile, null), 2));
        Thread threeTileThread = new Thread(() -> updateBoard(match(gameBoard, threeTile, null), 3));
        Thread fourTileThread = new Thread(() -> updateBoard(match(gameBoard, fourTile, null), 4));
        Thread fiveTileThread = new Thread(() -> updateBoard(match(gameBoard, fiveTile, null), 5));
        Thread sixTileThread = new Thread(() -> updateBoard(match(gameBoard, sixTile, null), 6));
        Thread sevenTileThread = new Thread(() -> updateBoard(match(gameBoard, sevenTile, null), 7));
        Thread eightTileThread = new Thread(() -> updateBoard(match(gameBoard, eightTile, null), 8));

        unknownTileThread.start();
        emptyTileThread.start();
        flagTileThread.start();
        oneTileThread.start();
        twoTileThread.start();
        threeTileThread.start();
        fourTileThread.start();
        fiveTileThread.start();
        sixTileThread.start();
        sevenTileThread.start();
        eightTileThread.start();

        try {
            unknownTileThread.join();
            emptyTileThread.join();
            flagTileThread.join();
            oneTileThread.join();
            twoTileThread.join();
            threeTileThread.join();
            fourTileThread.join();
            fiveTileThread.join();
            sixTileThread.join();
            sevenTileThread.join();
            eightTileThread.join();
        } catch (InterruptedException ignored) {
            throw new RuntimeException("update board interrupted");
        }
    }

    private void updateBoard(ArrayList<Location> tileLocations, int state) {
        for (Location tileLocation : tileLocations) {
            TileLoop:
            for (Location approximateLocation : getApproximateLocations(tileLocation, 2))
                for (Tile[] yTiles : board)
                    for (Tile tile : yTiles)
                        if (approximateLocation.equals(tile.getLocation())) {
                            tile.setState(state);
                            break TileLoop;
                        }
        }
    }

    Tile[][] getBoard() {
        return board;
    }

    int getWidth() {
        return board.length;
    }

    int getHeight() {
        return board[0].length;
    }

    int[][] getTable() {
        int n = getWidth(), m = getHeight();
        int[][] ret = new int[n][m];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                ret[i][j] = board[i][j].getState();
        return ret;
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

    private ArrayList<Location> getApproximateLocations(Location location, int accuracy) {
        int i = location.getX();
        int j = location.getY();

        ArrayList<Location> locations = new ArrayList<>();

        for (int w = -1 * accuracy; w < accuracy; w++)
            for (int h = -1 * accuracy; h < accuracy; h++)
                locations.add(new Location(i + w, j + h));

        return locations;
    }

    private ArrayList<Location> extendLocations(ArrayList<Location> newLocations, ArrayList<Location>
            oldLocations) {
        ArrayList<Location> result = new ArrayList<>(oldLocations);
        for (Location newLocation : newLocations) {
            boolean exist = false;
            OldLocation:
            for (Location oldLocation : oldLocations)
                for (Location approximateLocation : getApproximateLocations(oldLocation, 2))
                    if (newLocation.equals(approximateLocation)) {
                        exist = true;
                        break OldLocation;
                    }
            if (!exist)
                result.add(newLocation);
        }
        return result;
    }

    private ArrayList<Location> match(Mat src, List<Template> templates, Color color) {
        ArrayList<Location> result = new ArrayList<>();
        for (Template template : templates)
            result = extendLocations(result, match(src, template, color));
        return result;
    }

    private ArrayList<Location> match(Mat src, Template template, Color color) {
        Mat srcGray = new Mat();
        Mat templateGray = new Mat();
        Imgproc.cvtColor(src, srcGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(template.getTelmplate(), templateGray, Imgproc.COLOR_BGR2GRAY);
        Mat res = new Mat();
        Imgproc.matchTemplate(srcGray, templateGray, res, Imgproc.TM_CCOEFF_NORMED);

        ArrayList<Location> locations = new ArrayList<>();

        for (int i = 0; i < res.width(); i++)
            for (int j = 0; j < res.height(); j++) {
                if (res.get(j, i)[0] >= template.getThreshold()) {
                    locations.add(new Location(i, j));
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
        Location gameLocationBR = null;

        try {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            BufferedImage image = new Robot().createScreenCapture(new Rectangle(screenSize));
            ImageIO.write(image, "png", new File("resources\\game.png"));
        } catch (IOException | AWTException ignored) {
        }

        Mat gameBoard = Imgcodecs.imread("resources\\game.png");
        Mat rangeMat = new Mat();
        Core.inRange(gameBoard, new Scalar(192, 192, 192), new Scalar(192, 192, 192), rangeMat);
        for (int i = 0; i < rangeMat.width(); i++)
            for (int j = 0; j < rangeMat.height(); j++)
                if (!isZero(rangeMat, i, j, 5))
                    if (gameLocationTL == null)
                        gameLocationTL = new Location(i, j);
                    else
                        gameLocationBR = new Location(i, j);
        if (gameLocationBR == null || gameLocationTL == null)
            throw new RuntimeException("I can't detect game :(");
        gameBoard = new Mat(gameBoard, new Rect(gameLocationTL.getX(), gameLocationTL.getY()
                , gameLocationBR.getX() - gameLocationTL.getX()
                , gameLocationBR.getY() - gameLocationTL.getY()));
        return gameBoard;
    }
}
