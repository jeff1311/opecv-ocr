package com.ljf.opencvocr;

import java.awt.image.BufferedImage;
import java.util.Map;

public class Model {

    private Map<String,String> params;
    private BufferedImage img;

    public Model(Map<String,String> params,BufferedImage img){
        this.params = params;
        this.img = img;
    }

    public Map<String, String> getParams() {
        return params;
    }
    public void setParams(Map<String, String> params) {
        this.params = params;
    }
    public BufferedImage getImg() {
        return img;
    }
    public void setImg(BufferedImage img) {
        this.img = img;
    }

}
