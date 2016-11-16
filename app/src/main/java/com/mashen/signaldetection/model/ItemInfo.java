package com.mashen.signaldetection.model;

import android.graphics.Bitmap;


/**
 * Created by Administrator on 2016/10/16.
 */
public class ItemInfo {
    private boolean selected;
    private Bitmap imgBitmap;
//    private int imgId;
    private String imgName;
    private long prtScTime;
    private int imgNextId;

    public ItemInfo(boolean selected,Bitmap imgBitmap,String imgName,long prtScTime,int imgNextId){
        this.selected = selected;
        this.imgBitmap = imgBitmap;
        this.imgName = imgName;
        this.prtScTime = prtScTime;
        this.imgNextId = imgNextId;
    }

    public int getImgNextId() {
        return imgNextId;
    }

    public void setImgNextId(int imgNextId) {
        this.imgNextId = imgNextId;
    }

    public String getImgName() {
        return imgName;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Bitmap getImgBitmap() {
        return imgBitmap;
    }

    public void setImgBitmap(Bitmap imgBitmap) {
        this.imgBitmap = imgBitmap;
    }

    public long getPrtScTime() {
        return prtScTime;
    }

    public void setPrtScTime(long prtScTime) {
        this.prtScTime = prtScTime;
    }

}
