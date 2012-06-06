import java.io.IOException;
import java.io.RandomAccessFile;

public class JPEGCompressor {	
	static void convertRGB2YUV(ImageFile img ){
		byte[][] r = img.getData()[0];
		byte[][] g = img.getData()[1];
		byte[][] b = img.getData()[2];
		
		for(int y = 0; y < img.getHeight(); ++y){
			for(int x = 0; x < img.getWidth(); ++x){
				double Y =  Const.RGB2YUV[0][0]*(r[y][x]&0xff) 
						+ Const.RGB2YUV[0][1]*(g[y][x]&0xff) 
						+ Const.RGB2YUV[0][2]*(b[y][x]&0xff);
				double U =  Const.RGB2YUV[1][0]*(r[y][x]&0xff) 
    					+ Const.RGB2YUV[1][1]*(g[y][x]&0xff) 
			            + Const.RGB2YUV[1][2]*(b[y][x]&0xff);
				double V =  Const.RGB2YUV[2][0]*(r[y][x]&0xff) 
			            + Const.RGB2YUV[2][1]*(g[y][x]&0xff) 
			            + Const.RGB2YUV[2][2]*(b[y][x]&0xff);						
				U = U + 127.5;
				V = V + 127.5;					
				r[y][x] = Const.truncatetorgb(Y);
				g[y][x] = Const.truncatetorgb(U);
				b[y][x] = Const.truncatetorgb(V);
			}
		}
	}
	static void doDCTFileBuffer(byte[][] src, short[][] dest, 
			int ox, int oy, boolean isY, RandomAccessFile fd, byte q) 
	throws IOException{
		double pc;		
		byte[] tempbuff = new byte[128];
		for(int v = 0; v < 8; v++){
			for(int u = 0; u < 8; u++){				
				pc = 0;								
				for(int y= 0; y < 8; y++){
					for(int x = 0; x < 8; x++){												
						double ang = Const.cosVal[((2*x + 1)*u)%32]*Const.cosVal[((2*y + 1)*v)%32];						
						pc += ((src[y + oy][x + ox]&0xff) - 128)*ang/4;					
					}					
				}				
				if((u == 0 && v != 0) || (u != 0 && v == 0)){
					pc*=Const.c;
				}				
				else if(u == 0 && v == 0){
					pc/= 2;					
				}
				if(isY){
					pc /= (Const.LQTable[v][u]*q);
				}
				else{
					pc /= (Const.UVQTable[v][u]*q);
				}
				dest[v + oy][u + ox] = (short)(pc);
				
				tempbuff[Const.Zigzag[v][u]*2] = tempbuff[Const.Zigzag[v][u]*2 + 1] = 0;								
				tempbuff[Const.Zigzag[v][u]*2] |= (dest[v + oy][u + ox]&0xff);
				tempbuff[Const.Zigzag[v][u]*2 + 1] |= ((dest[v + oy][u + ox]>>8)&0xff);						
			}
		}		
		fd.write(tempbuff);		
	}	
	static void compressFileBuffer(ImageFile img,short[][][] dest, RandomAccessFile fd, byte q) throws IOException{				
		int blocky = img.getHeight()/16;
		int blockx = img.getWidth()/16;				
		for(int y = 0; y < blocky; ++y){
			for(int x = 0; x < blockx; ++x){
				for(int yy = 0; yy < 2; ++yy){
					for(int xx = 0; xx < 2; ++xx){
						doDCTFileBuffer(img.getData()[0], dest[0], x*16 + xx*8, y*16 + yy*8, true, fd, q);
					}
				}
			}
		}
		for(int y = 0; y < blocky; ++y){
			for(int x = 0; x < blockx; ++x){
				for(int yy = 0; yy < 2; ++yy){
					for(int xx = 0; xx < 2; ++xx){
						doDCTFileBuffer(img.getData()[1], dest[1], x*16 + xx*8, y*16 + yy*8, false, fd, q);
						doDCTFileBuffer(img.getData()[2], dest[2], x*16 + xx*8, y*16 + yy*8, false, fd, q);
					}
				}
			}
		}		

	}
	static void doDCTErrorFileBuffer(short[][] src, short[][] dest, int ox, int oy, 
			boolean isY, RandomAccessFile fd, byte q) throws IOException{
		double pc;			
		byte[] tempbuff = new byte[128];
		for(int v = 0; v < 8; v++){
			for(int u = 0; u < 8; u++){				
				pc = 0;								
				for(int y= 0; y < 8; y++){
					for(int x = 0; x < 8; x++){												
						double ang = Const.cosVal[((2*x + 1)*u)%32]*Const.cosVal[((2*y + 1)*v)%32];						
						pc += src[y + oy][x + ox]*ang/4;					
					}					
				}				
				if((u == 0 && v != 0) || (u != 0 && v == 0)){
					pc*=Const.c;
				}				
				else if(u == 0 && v == 0){
					pc/= 2;					
				}
				if(isY){
					pc /= (Const.LQTable[v][u]*4*q);
				}
				else{
					pc /= (Const.UVQTable[v][u]*4*q);
				}
				dest[v + oy][u + ox] = (short)(pc);
				tempbuff[Const.Zigzag[v][u]*2] = tempbuff[Const.Zigzag[v][u]*2 + 1] = 0;								
				tempbuff[Const.Zigzag[v][u]*2] |= (dest[v + oy][u + ox]&0xff);
				tempbuff[Const.Zigzag[v][u]*2 + 1] |= ((dest[v + oy][u + ox]>>8)&0xff);				
			}
		}		
		fd.write(tempbuff);
	}	
	static void compressErrorFileBuffer(short[][][] src, short[][][] dest, RandomAccessFile fd, byte[][][] mv, byte q) 
		throws IOException{		
		int blocky = src[0].length/16;
		int blockx = src[0][0].length/16;				
		for(int y = 0; y < blocky; ++y){
			for(int x = 0; x < blockx; ++x){
				fd.writeByte(mv[y][x][0]);
				fd.writeByte(mv[y][x][1]);
				for(int yy = 0; yy < 2; ++yy){
					for(int xx = 0; xx < 2; ++xx){
						doDCTErrorFileBuffer(src[0], dest[0], x*16 + xx*8, y*16 + yy*8, true, fd, q);
					}
				}
			}
		}
		for(int y = 0; y < blocky; ++y){
			for(int x = 0; x < blockx; ++x){
				for(int yy = 0; yy < 2; ++yy){
					for(int xx = 0; xx < 2; ++xx){
						doDCTErrorFileBuffer(src[1], dest[1], x*16 + xx*8, y*16 + yy*8, false, fd, q);
						doDCTErrorFileBuffer(src[2], dest[2], x*16 + xx*8, y*16 + yy*8, false, fd, q);			
					}
				}
			}
		}																

	}
	static void doDCTEnhancedFile(short[][] src, int ox, int oy, boolean isY, RandomAccessFile fd, byte q) throws IOException{
		double pc;
		byte[] tempbuff = new byte[128];		
		for(int v = 0; v < 8; v++){
			for(int u = 0; u < 8; u++){				
				pc = 0;								
				for(int y= 0; y < 8; y++){
					for(int x = 0; x < 8; x++){												
						double ang = Const.cosVal[((2*x + 1)*u)%32]*Const.cosVal[((2*y + 1)*v)%32];						
						pc += src[y + oy][x + ox]*ang/4;					
					}					
				}				
				if((u == 0 && v != 0) || (u != 0 && v == 0)){
					pc*=Const.c;
				}				
				else if(u == 0 && v == 0){
					pc/= 2;					
				}
				if(isY){
					pc /= (Const.LQTable[v][u]);
				}
				else{
					pc /= (Const.UVQTable[v][u]);
				}
				short temp = (short)(pc) ;				
				tempbuff[Const.Zigzag[v][u]*2] = tempbuff[Const.Zigzag[v][u]*2 + 1] = 0;								
				tempbuff[Const.Zigzag[v][u]*2] |= (temp&0xff);
				tempbuff[Const.Zigzag[v][u]*2 + 1] |= ((temp>>8)&0xff);				
			}
		}		
		fd.write(tempbuff);
	}	
	static void compressEnhancedFile(short[][][] src, RandomAccessFile fd, byte q) 
		throws IOException{		
		int blocky = src[0].length/16;
		int blockx = src[0][0].length/16;				
		for(int y = 0; y < blocky; ++y){
			for(int x = 0; x < blockx; ++x){			
				for(int yy = 0; yy < 2; ++yy){
					for(int xx = 0; xx < 2; ++xx){
						doDCTEnhancedFile(src[0], x*16 + xx*8, y*16 + yy*8, true, fd, q);
					}
				}
			}
		}
		for(int y = 0; y < blocky; ++y){
			for(int x = 0; x < blockx; ++x){			
				for(int yy = 0; yy < 2; ++yy){
					for(int xx = 0; xx < 2; ++xx){
						doDCTEnhancedFile(src[1], x*16 + xx*8, y*16 + yy*8, false, fd, q);
						doDCTEnhancedFile(src[2], x*16 + xx*8, y*16 + yy*8, false, fd, q);			
					}
				}
			}
		}																

	}	
}
