/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package result;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import javax.imageio.ImageIO;

/**
 *
 * @author james
 */
public class Result implements Serializable{
    
    private int food = -1;
    private int conf = -1;
    private byte[] image = null;
    
    public Result(int food, int conf, byte[] image){
        
        this.food = food;
        this.conf = conf;
        this.image = image;
        
    }
    
    public int getFood(){
        return this.food;
    }
    
    public int getConf(){
        return this.conf;
    }
    
    public BufferedImage getImage() throws IOException{
        // Create input stream and convert the bytes into a picture
        ByteArrayInputStream byteStream = new ByteArrayInputStream(this.image);
        BufferedImage img = ImageIO.read(byteStream);
        
        return img;
    }
    
}

