public class MotionCompensation {			
	static ImageFile getPredictedFrame(ImageFile iFrame, byte[][][] mv){
		ImageFile predicted = new ImageFile(iFrame.getHeight(), iFrame.getWidth());		
		int blocky = iFrame.getHeight()/16;
		int blockx = iFrame.getWidth()/16;
		for(int y = 0; y < blocky; ++y){
			for(int x = 0; x < blockx; ++x){																	
				for(int j=0; j < 16; ++j){					
					System.arraycopy(iFrame.getData()[0][j + y*16 + + mv[y][x][0]], (x*16 + mv[y][x][1]), 
							predicted.getData()[0][j + y*16] , x*16, 16);
					System.arraycopy(iFrame.getData()[1][j + y*16 + + mv[y][x][0]], (x*16 + mv[y][x][1]), 
							predicted.getData()[1][j + y*16] , x*16, 16);
					System.arraycopy(iFrame.getData()[2][j + y*16 + + mv[y][x][0]], (x*16 + mv[y][x][1]), 
							predicted.getData()[2][j + y*16] , x*16, 16);
				}
			}
		}		
		return predicted;
	}
	static void motionEstimation(ImageFile iFrame, ImageFile pFrame, byte[][][] mv, byte k){		
		int blocky = iFrame.getHeight()/16;
		int blockx = iFrame.getWidth()/16;		
		for(int y = 0; y < blocky; ++y){
			for(int x = 0; x < blockx; ++x){																
				bruteForce(iFrame.getData()[0], pFrame.getData()[0], y*16, x*16, k, mv[y][x]);
			}			
		}		
	}
	static void bruteForce(byte[][] ibuf, byte[][] pbuf, int oy, int ox, byte k, byte[] bs) {
		long madVal = Long.MAX_VALUE;
		bs[0] = 0;
		bs[1] = 0;
		
		for(byte ky=(byte)(-k); ky <= k; ++ky){
			for(byte kx = (byte)(-k); kx <= k; ++kx){
				if(!inBoundary(ky + oy, kx + ox, ibuf.length, ibuf[0].length)){
					continue;
				}
				long sum = mad(ibuf, pbuf, oy, ox, ky, kx);
				if(madVal > sum){
					madVal = sum;
					bs[0] = ky;
					bs[1] = kx;
				}
			}
		}
	}
	static long mad(byte[][] ibuf, byte[][] pbuf, int oy, int ox, int ky, int kx){
		long sum = 0;				
		for(int j=0; j < 16; ++j){
			for(int i=0; i < 16; ++i){
				sum +=  Math.abs((pbuf[j + oy][i + ox]&0xff) - (ibuf[j + oy + ky][i + ox + kx]&0xff));
			}
		}
		return sum;
	}
	static boolean inBoundary(int i, int j, int h, int w) {
		if(i < 0 || j < 0){
			return false;
		}
		if((i + 16 > h)|| (j + 16 > w)){
			return false;
		}
		return true;		
	}
	static void getErrorFrame(ImageFile predicted, ImageFile present, short[][][] buf){		
		for(int y = 0; y < predicted.getHeight(); ++y){
			for(int x = 0; x < predicted.getWidth(); ++x){
				buf[0][y][x] = (short) ((present.getData()[0][y][x]&0xff) - (predicted.getData()[0][y][x]&0xff));
				buf[1][y][x] = (short) ((present.getData()[1][y][x]&0xff) - (predicted.getData()[1][y][x]&0xff));
				buf[2][y][x] = (short) ((present.getData()[2][y][x]&0xff) - (predicted.getData()[2][y][x]&0xff));
			}
		}		
	}
	static void recoveredFrame(ImageFile rec, ImageFile predicted, short[][][] err){	
		for(int y = 0; y < predicted.getHeight(); ++y){
			for(int x = 0; x < predicted.getWidth(); ++x){
				rec.getData()[0][y][x] = Const.truncatetorgberr(err[0][y][x] + (predicted.getData()[0][y][x]&0xff));
				rec.getData()[1][y][x] = Const.truncatetorgberr(err[1][y][x] + (predicted.getData()[1][y][x]&0xff));
				rec.getData()[2][y][x] = Const.truncatetorgberr(err[2][y][x] + (predicted.getData()[2][y][x]&0xff));
			}
		}		
	}
}

