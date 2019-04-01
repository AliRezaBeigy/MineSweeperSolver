package ir.kharazmi.minesweepersolver;

import org.opencv.core.Mat;

class Template {
    private Mat telmplate;
    private float threshold;

    Template(Mat telmplate, float threshold) {
        this.telmplate = telmplate;
        this.threshold = threshold;
    }

    Mat getTelmplate() {
        return telmplate;
    }

    float getThreshold() {
        return threshold;
    }
}
