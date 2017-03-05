package com.github.yinyee.locator.ocr;

import android.util.SparseArray;

import com.github.yinyee.locator.ui.camera.ocr.GraphicOverlay;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A very simple Processor which receives detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 */
public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {

    private GraphicOverlay<OcrGraphic> mGraphicOverlay;

    private static final Pattern pattern = Pattern.compile("\\s*INVOICE\\s*NO.\\s*(\\d+)\\s*");
    private Matcher matcher;
    private String invoiceNo;

    OcrDetectorProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay) {
        mGraphicOverlay = ocrGraphicOverlay;
    }

    /**
     * Called by the detector to deliver detection results.
     * If your application called for it, this could be a place to check for
     * equivalent detections by tracking TextBlocks that are similar in location and content from
     * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
     * multiple detections.
     */
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        mGraphicOverlay.clear();
        SparseArray<TextBlock> items = detections.getDetectedItems();
        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);
            OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, item);
            List<? extends Text> textComponents = item.getComponents();
            for(Text currentText : textComponents) {
                matcher = pattern.matcher(currentText.getValue());
                if(matcher.matches()){
                    invoiceNo = matcher.group(1);
                }
            }
            mGraphicOverlay.add(graphic);
        }
    }

    /**
     * Frees the resources associated with this detection processor.
     */
    @Override
    public void release() {
        mGraphicOverlay.clear();
    }
}