
public class ImageFile {
	private int height;
	private int width;
	private byte[][][] data;
	
	public int getHeight() {
		return height;
	}
	public int getWidth() {
		return width;
	}
	public ImageFile(int height, int width) {
		super();
		this.height = height;
		this.width = width;
		data = new byte[3][height][width];
	}
	public byte[][][] getData() {
		return data;
	}
	public void setData(byte[][][] data) {
		this.data = data;
	}
}
