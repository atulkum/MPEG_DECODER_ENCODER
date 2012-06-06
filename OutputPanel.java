import java.awt.Graphics;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import javax.swing.JPanel;

public class OutputPanel extends JPanel{
	private int h;
	private int w;	
	byte[][] imgbuffer = null;
	private ComponentColorModel ccm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), 
			new int[] {8, 8, 8}, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
	private BandedSampleModel csm = null;
	
	public OutputPanel(int h, int w) {
		super();
		this.h = h;
		this.w = w;
		imgbuffer = new byte[3][h*w];
		csm = new BandedSampleModel(DataBuffer.TYPE_BYTE, 
				w, h, w, new int[] {0, 1, 2}, new int[] {0, 0, 0});
	}
	
	public void paintComponent(Graphics g){
    	super.paintComponent(g);       	
    	drawImage(g, imgbuffer, w, h);
    }
	
	void drawImage(Graphics g, byte[][] data, int w, int h) {
		DataBuffer buffer = new DataBufferByte(data, data[0].length);  		  								
		WritableRaster raster = Raster.createWritableRaster(csm, buffer, new Point(0, 0));
		BufferedImage img = new BufferedImage(ccm, raster, false, null);			 						
		g.drawImage(img, 0, 0, null);         
    }  	

	public void setImgData(ImageFile img) {	
		for(int m = 0; m < 3; ++m){
			for(int y = 0; y < h; ++y){					
				System.arraycopy(img.getData()[m][y], 0, imgbuffer[m], y*w,  w);
			}
		}
	}  
}

