import java.io.IOException;
import java.io.RandomAccessFile;


public class BitAllocator {
	byte[] lbits;
	byte[] ubits;
	byte[] vbits;
	int li;
	int ui;
	int vi;
	int lr;
	int ur;
	int vr;
	boolean isP;
	static int qf = 0;
	public static final byte[][] LBits = {
		{9, 10, 10, 9, 9, 8, 8, 7},
		{10, 10, 10, 9, 9, 8, 8, 7},
		{10, 10, 9, 9, 8, 8, 7, 7},
		{9, 9, 9, 8, 8, 7, 7, 7},
		{9, 9, 8, 8, 7, 7, 7, 7},
		{8, 8, 8, 7, 7, 7, 7, 7},
		{8, 8, 7, 7, 7, 7, 7, 7},
		{7, 7, 7, 7, 7, 7, 7, 7}
	};
	public static final int LBitsLen = 508;		
	public static final byte[][] UVBits = {
		{8, 9, 9, 8, 7, 7, 7, 7},
		{9, 9, 9, 8, 7, 7, 7, 7},
		{9, 9, 8, 7, 7, 7, 7, 7},
		{8, 8, 7, 7, 7, 7, 7, 7},
		{7, 7, 7, 7, 7, 7, 7, 7},
		{7, 7, 7, 7, 7, 7, 7, 7},
		{7, 7, 7, 7, 7, 7, 7, 7},
		{7, 7, 7, 7, 7, 7, 7, 7}
	};
	public static final int UVBitsLen = 468;  
	public BitAllocator(boolean isP) {
		super();
		int less = qf*64;		
		if(isP){
			less += 2*64;
		}		
		li = ui = vi = 0;
		lr =  ur = vr = 8;
		this.isP = isP;
		
		lbits = new byte[(int) Math.ceil(508 - less)];
		ubits = new byte[(int) Math.ceil(468 - less)];
		vbits = new byte[(int) Math.ceil(468 - less)];
	}
	public static void setQParam(byte q){
		switch(q){
			case 1: qf = 0; break;
			case 2:  
			case 3: qf = 1; break;
			case 4: 
			case 5: 
			case 6: 
			case 7: qf = 2; break;		
		}		
	}
	private void setBits(short cof, int reqBits, byte[] bits, int i, int r) {
		if(r >= reqBits){						
			bits[i] |= ((cof&(0x00ff >> (8 - reqBits))) << (8-r));
			r -= reqBits;
		}
		else{			
			bits[i] |= ((cof >> (reqBits - lr))&(0x00ff >> (8-r)));
			i++;			
			bits[i] |= ((cof << r) & (0x00ff << (8 - reqBits + r)));
			r = 8 - reqBits + r;
		}
		if(r == 0) i++;						
	}
	private short getVal(int reqBits, byte[] bits, int i, int r) {
		short ret = 0;
		if(r >= reqBits){
			ret = (short) ((bits[i]&((0xff >> (8 - r)) << (r - reqBits))) >> (r - reqBits));			
			r -= reqBits;
		}
		else{	
			ret |= bits[i]&(0xff >> (8 - r));
			i++;			
			
			r = 8 - reqBits + r;
		}
		if(r == 0) i++;						
		return ret;
	}
	public void setL(short coeff, int y, int x){
		int reqBits = LBits[y][x] - qf;
		if(isP) reqBits -= 2;
		setBits(coeff, reqBits, lbits, li, lr);
	}
	public void setU(short coeff, int y, int x){
		int reqBits = UVBits[y][x] - qf;
		if(isP) reqBits -= 2;		
		setBits(coeff, reqBits, ubits, ui, ur);		
	}
	public void setV(short coeff, int y, int x){
		int reqBits = UVBits[y][x] - qf;
		if(isP)	reqBits -= 2;
		setBits(coeff, reqBits, vbits, vi, vr);
	}
	public short getL(int y, int x){
		short ret = 0;
		int reqBits = LBits[y][x] - qf;
		if(isP) reqBits -= 2;
		ret = getVal(reqBits, lbits, li, lr);
		return ret;
	}	
	public short getU(int y, int x){
		int reqBits = UVBits[y][x] - qf;
		if(isP) reqBits -= 2;		
		return 0;		
	}
	public short getV(int y, int x){
		int reqBits = UVBits[y][x] - qf;
		if(isP)	reqBits -= 2;
		return 0;
	}
	public void write2File(RandomAccessFile fd) throws IOException{
		fd.write(lbits, 0, lbits.length);
		fd.write(ubits, 0, ubits.length);
		fd.write(vbits, 0, vbits.length);
	}
	public void readFile(RandomAccessFile fd) throws IOException{
		fd.read(lbits, 0, lbits.length);
		fd.read(ubits, 0, ubits.length);
		fd.read(vbits, 0, vbits.length);
	}
	public static void main(String[] args) {
		/*int tl = 0;
		int tuv = 0;
		for(int y=0; y < 8; ++y){
			for(int x=0; x < 8; ++x){
				tl += LBits[y][x];
				tuv += UVBits[y][x];
			}			
		}	
		System.out.println(tl + " " + tuv);*/
		int lrt = 7;
		short c = 7;
		int reqBitst = 3;
		System.out.println((c&(0x00ff >> (8 - reqBitst))));
		System.out.println(((c&(0x00ff >> (8 - reqBitst))) << (8-lrt)));
	}

}
