import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class VideoFile {	
	private int width;
	private int height;
	private int noOfFrames;
	private ArrayList<ImageFile> frames = new ArrayList<ImageFile>();
	
	public VideoFile(int height, int width, int noOfFrames) {
		super();
		this.width = width;
		this.height = height;
		this.noOfFrames = noOfFrames;
	}
	public int getNoOfFrames() {
		return noOfFrames;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getWidth() {
		return width;
	}
	
	public void readVidepFile_576v(String filePath){
		RandomAccessFile videoFile = null;
		try {			
			videoFile = new RandomAccessFile(filePath,"r");			

			//byte[] buff = new byte[3*Main.h*Main.w];
			//videoFile.readFully(buff);
			
			for(int i = 0; i < noOfFrames; ++i){
				ImageFile img = new ImageFile(height, width);				
				frames.add(img);				
				int buffid = 0;
				byte[] buff = new byte[3*height*width];
				videoFile.readFully(buff);
				for(int m = 0; m < 3; ++m){
					for(int y = 0; y < height; ++y){					
						System.arraycopy(buff, buffid, img.getData()[m][y], 0, width);
						buffid += width;
					}
				}
			}			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		finally{
			try {
				if(videoFile != null)
					videoFile.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	static public void readNextFrame_576v(ImageFile img, RandomAccessFile videoFile, int h, int w){		
		try {		
			byte[] buff = new byte[3*h*w];
			videoFile.readFully(buff);		
			int buffid = 0;
			
			for(int m = 0; m < 3; ++m){
				for(int y = 0; y < h; ++y){					
					System.arraycopy(buff, buffid, img.getData()[m][y], 0, w);
					buffid += w;
				}
			}			
		}catch (IOException e) {
			e.printStackTrace();
		}			
	}

	public ArrayList<ImageFile> getFrames() {
		return frames;
	}
	public void setFrames(ArrayList<ImageFile> frames) {
		this.frames = frames;
	}
}
