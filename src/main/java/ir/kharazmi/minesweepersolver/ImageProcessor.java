package ir.kharazmi.minesweepersolver;

import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;
import org.opencv.core.Point;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

class ImageProcessor {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private final int ACCURACY_UNKNOWN_LOCATION = 3;
    private final double DEFAULT_THRUSHOLD = 0.8;

    private int TileWidth;
    private int TileHeight;

    private double ratioX = 1;
    private double ratioY = 1;

    private int bomb_count;
//    private int wrong_flag_count;

    private WinDef.HWND gameHwnd;

    private Tile[][] board;
//    private Mat gameBoardBackup;

    private Location gameLocationTL;

    private List<Template> resetTile;
    private List<Template> unknownTile;
    private List<Template> bombTile;
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
    private List<Template> wrongFlagTile;

    private UpdateBoardListener updateBoardListener;

//    private int ks;

    void setUpdateBoardListener(UpdateBoardListener updateBoardListener) {
        this.updateBoardListener = updateBoardListener;
    }

    void init() {
        resetTile = getTemplates("reset");
        bombTile = getTemplates("bomb");
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
        wrongFlagTile = getTemplates("wrong_flag");

        Mat gameBoard = getScreenshot();
        ArrayList<Location> boarder_locations = match(gameBoard, unknownTile, Color.blue);

        ArrayList<Location> locations = new ArrayList<>();
        for (Location boarder_location : boarder_locations) {
            boolean exist = false;
            for (Location approximateLocation : getApproximateLocations(boarder_location, ACCURACY_UNKNOWN_LOCATION))
                if (locations.contains(approximateLocation))
                    exist = true;
            if (!exist)
                locations.add(boarder_location);
        }

        ArrayList<Integer> xs = new ArrayList<>();
        for (Location location : locations)
            if (!xs.contains(location.getX())
                    && !xs.contains(location.getX() - 1)
                    && !xs.contains(location.getX() + 1)
                    && !xs.contains(location.getX() - 2)
                    && !xs.contains(location.getX() + 2)
                    && !xs.contains(location.getX() - 3)
                    && !xs.contains(location.getX() + 3))
                xs.add(location.getX());
        ArrayList<Integer> ys = new ArrayList<>();
        for (Location location : locations)
            if (!ys.contains(location.getY())
                    && !ys.contains(location.getY() - 1)
                    && !ys.contains(location.getY() + 1)
                    && !ys.contains(location.getY() - 2)
                    && !ys.contains(location.getY() + 2)
                    && !ys.contains(location.getY() - 3)
                    && !ys.contains(location.getY() + 3))
                ys.add(location.getY());

        for (int i = 0; i < xs.size(); i++) {
            Integer x = xs.get(i);
            int xCount = 0;
            for (Location location : locations) {
                if (location.getX() == x) {
                    xCount++;
                    if (xCount > 3)
                        break;
                }
            }
            if (xCount <= 3) {
                xs.remove(x);
                i--;
            }
        }
        for (int i = 0; i < ys.size(); i++) {
            Integer y = ys.get(i);
            int yCount = 0;
            for (Location location : locations) {
                if (location.getY() == y) {
                    yCount++;
                    if (yCount > 3)
                        break;
                }
            }
            if (yCount <= 3) {
                ys.remove(y);
                i--;
            }
        }

        Collections.sort(xs);
        Collections.sort(ys);
        int w = xs.size();
        int h = ys.size();

        board = new Tile[w][h];
        for (Location location : locations) {
            int i = indexOfLocation(location.getX(), xs);
            int j = indexOfLocation(location.getY(), ys);
            if (i == -1 || j == -1)
                continue;
            board[i][j] = new Tile(-1, new Location(location.getX(), location.getY(), 0));
        }

        TileWidth = board[1][0].getLocation().getX() - board[0][0].getLocation().getX();
        TileHeight = board[0][1].getLocation().getY() - board[0][0].getLocation().getY();
    }

    private int indexOfLocation(int o, ArrayList<Integer> s) {
        int i = s.indexOf(o);
        int f = -1 * ACCURACY_UNKNOWN_LOCATION;
        while (i == -1 && f <= ACCURACY_UNKNOWN_LOCATION) {
            i = s.indexOf(o + f);
            f++;
        }
        return i;
    }

    //Convert Mat to BufferedImage
    public BufferedImage toBufferedImage(Mat m) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (m.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = m.channels() * m.cols() * m.rows();
        byte[] b = new byte[bufferSize];
        m.get(0, 0, b);
        BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;
    }

    void startGame() {
        try {
            Process process = Runtime.getRuntime().exec("resources\\Minesweeper.exe");
            Thread.sleep(1000);
            Method pidMethod = process.getClass().getDeclaredMethod("pid");
            pidMethod.setAccessible(true);
            Long pid = (Long) pidMethod.invoke(process);

            AtomicReference<WinDef.RECT> tempRect = new AtomicReference<>();
            final User32 user32 = User32.INSTANCE;
            WinDef.HWND hwnd = null;

            do {
                hwnd = user32.FindWindowEx(null, hwnd, null, null);
                IntByReference intByReference = new IntByReference(0);
                user32.GetWindowThreadProcessId(hwnd, intByReference);
                if (pid.intValue() == intByReference.getValue()) {
                    WinDef.RECT rect = new WinDef.RECT();
                    User32.INSTANCE.GetWindowRect(hwnd, rect);
                    if (tempRect.get() == null
                            || ((tempRect.get().bottom - tempRect.get().top) < (rect.bottom - rect.top))) {
                        gameHwnd = hwnd;
                        tempRect.set(rect);
                        gameLocationTL = new Location(rect.left, rect.top, 0);
                    }
                }
            }
            while (hwnd != null);
        } catch (IOException | InterruptedException | NoSuchMethodException | IllegalAccessException | InvocationTargetException
                ignored) {
        }
    }

    void reset() {
        ArrayList<Location> reset_locations = match(getScreenshot(), resetTile, Color.red);
        System.out.println("Reset game...");
        if (reset_locations.size() == 0) return;
        try {
            Robot bot = new Robot();
            bot.mouseMove(new Float(((reset_locations.get(0).getX() + (TileWidth / 2f)) * ratioX) + gameLocationTL.getX()).intValue()
                    , new Float(((reset_locations.get(0).getY() + (TileHeight / 2f)) * ratioY) + gameLocationTL.getY()).intValue());
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
            moveMouse(x, y);
            Robot bot = new Robot();
            bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(150);
            bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        } catch (AWTException | InterruptedException ignored) {
        }
    }

    void flag(int x, int y) {
        try {
            moveMouse(x, y);
            Robot bot = new Robot();
            bot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
            Thread.sleep(150);
            bot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        } catch (AWTException | InterruptedException ignored) {
        }
    }

    private void moveMouse(int x, int y) throws AWTException {
        Robot bot = new Robot();
        Location tileLocation = board[x][y].getLocation();
        bot.mouseMove(new Float(gameLocationTL.getX() + ((tileLocation.getX() + (TileWidth / 2f)) * ratioX)).intValue()
                , new Float(gameLocationTL.getY() + ((tileLocation.getY() + (TileHeight / 2f)) * ratioY)).intValue());
    }

    void updateBoard() {
        Mat gameBoard = getScreenshot();

        Thread bombTileThread = new Thread(() -> {
            ArrayList<Location> bomb_locations = match(gameBoard, bombTile, null);
            bomb_count = bomb_locations.size();
            updateBoard(bomb_locations, -3);
        });
        Thread unknownTileThread = new Thread(() -> updateBoard(match(gameBoard, unknownTile, Color.blue), -1));
        Thread emptyTileThread = new Thread(() -> updateBoard(match(gameBoard, emptyTile, Color.GRAY), 0));
        Thread flagTileThread = new Thread(() -> updateBoard(match(gameBoard, flagTile, null), -2));
        Thread oneTileThread = new Thread(() -> updateBoard(match(gameBoard, oneTile, null), 1));
        Thread twoTileThread = new Thread(() -> updateBoard(match(gameBoard, twoTile, null), 2));
        Thread threeTileThread = new Thread(() -> updateBoard(match(gameBoard, threeTile, null), 3));
        Thread fourTileThread = new Thread(() -> updateBoard(match(gameBoard, fourTile, null), 4));
        Thread fiveTileThread = new Thread(() -> updateBoard(match(gameBoard, fiveTile, null), 5));
        Thread sixTileThread = new Thread(() -> updateBoard(match(gameBoard, sixTile, null), 6));
        Thread sevenTileThread = new Thread(() -> updateBoard(match(gameBoard, sevenTile, null), 7));
        Thread eightTileThread = new Thread(() -> updateBoard(match(gameBoard, eightTile, null), 8));
//        Thread wrongFlagTileThread = new Thread(() -> {
//            ArrayList<Location> bomb_locations = match(gameBoard, wrongFlagTile, null);
//            wrong_flag_count = bomb_locations.size();
//            updateBoard(bomb_locations, -3);
//        });

        bombTileThread.start();
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
//        wrongFlagTileThread.start();

        try {
            bombTileThread.join();
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
//            wrongFlagTileThread.join();
        } catch (InterruptedException ignored) {
            throw new RuntimeException("update board interrupted");
        }

//        if (getWrongFlagCount() != 0)
//            try {
//                int i = new Random().nextInt(1000);
//                ImageIO.write(toBufferedImage(gameBoardBackup), "png", new File(i + ".wrong.before.png"));
//                ImageIO.write(toBufferedImage(gameBoard), "png", new File(i + ".wrong.after.png"));
//            } catch (IOException ignored) {
//            }
//
//        gameBoardBackup = gameBoard;
        if (updateBoardListener != null)
            updateBoardListener.boardUpdated(board);

        for (Tile[] tiles : board) {
            for (Tile tile : tiles) {
                Location location = tile.getLocation();
                location.setThreshold(0);
                tile.setLocation(location);
            }
        }
    }

    int getBombCount() {
        return bomb_count;
    }

    private void updateBoard(ArrayList<Location> tileLocations, int state) {
        for (Location tileLocation : tileLocations) {
            TileLoop:
            for (Location approximateLocation : getApproximateLocations(tileLocation, 2))
                for (Tile[] yTiles : board)
                    for (Tile tile : yTiles) {
                        Location location = tile.getLocation();
                        if (approximateLocation.equals(location)) {
                            if (location.getThreshold() <= approximateLocation.getThreshold()) {
                                location.setThreshold(approximateLocation.getThreshold());
                                tile.setState(state);
                                tile.setLocation(location);
                                break TileLoop;
                            }
                        }
                    }
        }
    }

//    public int getWrongFlagCount() {
//        return wrong_flag_count;
//    }

    private List<Template> getTemplates(String name) {
        List<Template> result = new ArrayList<>();
        File dir = new File("resources\\" + name);
        File[] files = dir.listFiles((dir1, filename) -> filename.endsWith(".png"));
        if (files != null)
            for (File file : files) {
                String extension = file.getName().split("\\.")[1];
                result.add(new Template(Imgcodecs.imread(file.getAbsolutePath())
                        , extension.equals("png") ? DEFAULT_THRUSHOLD : Double.parseDouble("0." + extension)));
            }
        return result;
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

    private ArrayList<Location> getApproximateLocations(Location location, int accuracy) {
        int i = location.getX();
        int j = location.getY();

        ArrayList<Location> locations = new ArrayList<>();

        for (int w = -1 * accuracy; w < accuracy; w++)
            for (int h = -1 * accuracy; h < accuracy; h++)
                locations.add(new Location(i + w, j + h, location.getThreshold()));

        return locations;
    }

    private ArrayList<Location> match(Mat src, Template template, Color color) {
        Mat templateGray = new Mat();
        Imgproc.cvtColor(template.getTelmplate(), templateGray, Imgproc.COLOR_BGR2GRAY);
        Mat srcGray = new Mat();
        Imgproc.cvtColor(src, srcGray, Imgproc.COLOR_BGR2GRAY);
        Mat res = new Mat();
        Imgproc.matchTemplate(srcGray, templateGray, res, Imgproc.TM_CCOEFF_NORMED);

        ArrayList<Location> locations = new ArrayList<>();

        for (int i = 0; i < res.width(); i++)
            for (int j = 0; j < res.height(); j++) {
                if (res.get(j, i)[0] > template.getThreshold()) {
                    locations.add(new Location(i, j, res.get(j, i)[0]));
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

    private Mat getScreenshot() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }

        Mat gameBoard = null;

        WinDef.HWND hDesktop = User32.INSTANCE.GetDesktopWindow();
        WinDef.RECT desktopRect = new WinDef.RECT();
        WinDef.RECT rect = new WinDef.RECT();
        User32.INSTANCE.GetWindowRect(hDesktop, desktopRect);
        User32.INSTANCE.GetWindowRect(gameHwnd, rect);
        BufferedImage screenshot = JNAScreenShot.getScreenshot(rect.toRectangle());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(screenshot, "png", outputStream);
            ByteArrayInputStream stream = new ByteArrayInputStream(outputStream.toByteArray());
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];

            while ((nRead = stream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();
            byte[] temporaryImageInMemory = buffer.toByteArray();
            buffer.close();
            stream.close();
            gameBoard = Imgcodecs.imdecode(new MatOfByte(temporaryImageInMemory), Imgcodecs.IMREAD_COLOR);
        } catch (IOException ignored) {
        }
        ratioX = desktopRect.toRectangle().width / 1023.9999698823528; //desktopRect.toRectangle().width * 90.666664 / rect.toRectangle().width
        ratioY = desktopRect.toRectangle().height / 768.0000234375001; //desktopRect.toRectangle().height *( 182.04445 / rect.toRectangle().height)
        Imgproc.resize(gameBoard, gameBoard, new Size(rect.toRectangle().width / ratioX, rect.toRectangle().height / ratioY));
//        try {
//            ImageIO.write(toBufferedImage(gameBoard), "png", new File("test." + ks++ + ".png"));
//        } catch (IOException e) {
//        }
        return gameBoard;
    }

    interface UpdateBoardListener {
        void boardUpdated(Tile[][] board);
    }
}
