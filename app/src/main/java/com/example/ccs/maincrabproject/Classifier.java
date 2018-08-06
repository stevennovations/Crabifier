package com.example.ccs.maincrabproject;

import android.content.res.AssetManager;
import android.util.Log;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by CCS on 04/04/2018.
 */

public class Classifier {

    private TensorFlowInferenceInterface tfinference;
    private static final float THRESHOLD = 0.1f;
    private String inputName;
    private String outputName;
    private int inputSize;

    private List<String> labels;
    private float[] output;
    private String[] outputNames;
    private String out = "";

    static private List<String> readLabels(Classifier c, AssetManager am, String fileName) throws IOException {
        BufferedReader br = null;
        br = new BufferedReader(new InputStreamReader(am.open(fileName)));

        String line;
        List<String> labels = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            labels.add(line);
        }

        br.close();
        return labels;
    }

    static public Classifier create(AssetManager assetManager, String modelPath, String labelPath,
                                    int inputSize, String inputName, String outputName)
            throws IOException {

        Classifier c = new Classifier();

        c.inputName = inputName;
        c.outputName = outputName;

        // Read labels
        String labelFile = labelPath.split("file:///android_asset/")[0];
        Log.i("labelfile", labelFile);
        c.labels = readLabels(c, assetManager, labelFile);

        try{
            c.tfinference = new TensorFlowInferenceInterface(assetManager, modelPath);
        }catch (RuntimeException e){
            e.printStackTrace();
        }

        int numClasses = 3;

        c.inputSize = inputSize;

        // Pre-allocate buffer.
        c.outputNames = new String[]{ outputName };

        c.outputName = outputName;
        c.output = new float[numClasses];

        return c;
    }

    public Classification recognize(final float[] pixels) {

        tfinference.feed(inputName, pixels, 1, inputSize, inputSize, 3);
        tfinference.run(outputNames);

        tfinference.fetch(outputName, output);

        // Find the best classification
        Classification ans = new Classification();
        for (int i = 0; i < output.length; ++i) {
            System.out.println(output[i]);
            System.out.println(labels.get(i));
            if (output[i] > THRESHOLD && output[i] > ans.getConf()) {
                ans.update(output[i], labels.get(i));
                ans.putOthval(labels.get(i), output[i]);
            }
            else {
                ans.putOthval(labels.get(i), output[i]);
            }
        }

        return ans;
    }

    public String getOutput(){
        return out;
    }
}
