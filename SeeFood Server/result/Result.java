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
        
    }// End Result()
    
    /**
     * Returns the value indicating whether food is present or not
     * @return Food present or not value 
     */
    public int getFood(){
        return this.food;
    }// End getFood()
    
    /**
     * Returns the AI confidence for this picture
     * @return AI confidence value
     */
    public int getConf(){
        return this.conf;
    }// End getConf()
    
    /**
     * Returns the image
     * @return A BufferedImage containing the analyzed image in this object
     * @throws IOException 
     */
    public BufferedImage getImage() throws IOException{
        // Create input stream and convert the bytes into a picture
        ByteArrayInputStream byteStream = new ByteArrayInputStream(this.image);
        BufferedImage img = ImageIO.read(byteStream);
        
        return img;
    }// End getImage()
    
}