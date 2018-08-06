package com.example.ccs.maincrabproject;

import java.io.Serializable;

public class CrabModel implements Serializable{

    private String id;
    private String classifier;
    private float probval;
    private String fileurl;
    private Classification result;

    public CrabModel() {

    }

    public CrabModel(String id, String classifier, float probval, String fileurl, Classification res){
        this.id = id;
        this.classifier = classifier;
        this.probval = probval;
        this.fileurl = fileurl;
        this.result = res;
    }

    public String getId() { return id; }

    public String getClassifier() {
        return classifier;
    }

    public float getProbval() {
        return probval;
    }

    public String getFileurl() {
        return fileurl;
    }

    public Classification getResult() { return result; }

    public void setResult(Classification res) { this.result = res; }

    public void setId(String id) {
        this.id = id;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public void setProbval(float probval) {
        this.probval = probval;
    }

    public void setFileurl(String fileurl) {
        this.fileurl = fileurl;
    }

}
