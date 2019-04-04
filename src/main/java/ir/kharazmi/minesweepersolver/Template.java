package ir.kharazmi.minesweepersolver;

import org.opencv.core.Mat;

class Template {
    private Mat telmplate;
    private double threshold;

    Template(Mat telmplate, double threshold) {
        this.telmplate = telmplate;
        this.threshold = threshold;
    }

    Mat getTelmplate() {
        return telmplate;
    }

    double getThreshold() {
        return threshold;
    }
}
