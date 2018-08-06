package com.example.ccs.maincrabproject;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by CCS on 04/04/2018.
 */

public class Classification implements Serializable{

    private float conf;
    private String label;
    private Map<String, Float> othval;

    public Classification(float conf, String label) {
        update(conf, label);
    }

    public Classification() {
        this.conf = (float)-1.0;
        this.label = null;
        this.othval = new HashMap<String, Float>();
    }

    public void update(float conf, String label) {
        this.conf = conf;
        this.label = label;
    }

    public void setConf(float conf) {
        this.conf = conf;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public float getConf() {
        return conf;
    }

    public void putOthval(String key, float val) { othval.put(key, val); }
    public Map<String, Float> getOthval() { return othval; }
    public void setOthval(Map mp){ this.othval = mp; }
    public float getVal(String key) { return othval.get(key); }

}
