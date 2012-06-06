import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
public class Encoder {				
	static byte k;//16 to 20	
	static int h;
	static int w;
	static byte qP; //less than 7			
	static int nf;	
	
	static private ImageFile referenceFrame = null;
	//temp storage
	static short[][][] buf = null;	
	static short[][][] buferr = null;
	static byte[][][] mv = null;

	static  void encodeImageFrame(ImageFile frame, boolean isI, RandomAccessFile fd) throws IOException{		
		ImageFile predictedf = null;			
		if(isI){									
			JPEGCompressor.compressFileBuffer(frame, buf, fd, qP);
			//update previous frame memory			
			JPEGDecompressor.decompressBuffer(referenceFrame, buf, qP);			
		}
		else{
			//motion compensate with the previous frame			
			MotionCompensation.motionEstimation(referenceFrame, frame, mv, k);
			predictedf = MotionCompensation.getPredictedFrame(referenceFrame, mv);
			MotionCompensation.getErrorFrame(predictedf, frame, buferr);
			JPEGCompressor.compressErrorFileBuffer(buferr, buf, fd, mv, qP);
			//update previous frame memory			
			JPEGDecompressor.decompressErrorBuffer(buf, buferr, qP);			
			MotionCompensation.recoveredFrame(referenceFrame, predictedf, buferr);
		}		 	
	}
	static void encodeEnhancedLayer(ImageFile present, ImageFile decoded, RandomAccessFile fd) throws IOException {
		MotionCompensation.getErrorFrame(decoded, present, buferr);
		JPEGCompressor.compressEnhancedFile(buferr, fd, qP);		
	}
	static void encode(String vfilename){
		String base = vfilename.split("\\.")[0];
		String bfilename = base + ".base";
		String efilename = base + ".enhanced";
		System.out.println("ENCODING STARTS: " + DateUtils.now());
		
		RandomAccessFile fd = null;
		RandomAccessFile fdb = null;
		RandomAccessFile fde = null;
		try{
			fd = new RandomAccessFile(vfilename,"rw");
			fdb = new RandomAccessFile(bfilename,"rw");
			fde = new RandomAccessFile(efilename,"rw");		
			int t = w;
			byte[] temp = new byte[14];
			temp[3] = (byte)(t%10 + '0'); t /= 10;
			temp[2] = (byte)(t%10 + '0'); t /= 10;
			temp[1] = (byte)(t%10 + '0'); t /= 10;
			temp[0] = (byte)(t%10 + '0');						
			t = h;
			temp[7] = (byte)(t%10 + '0'); t /= 10;
			temp[6] = (byte)(t%10 + '0'); t /= 10;
			temp[5] = (byte)(t%10 + '0'); t /= 10;
			temp[4] = (byte)(t%10 + '0');			
			t = nf;
			temp[11] = (byte)(t%10 + '0'); t /= 10;
			temp[10] = (byte)(t%10 + '0'); t /= 10;
			temp[9] = (byte)(t%10 + '0'); t /= 10;
			temp[8] = (byte)(t%10 + '0');
			
			temp[12] = (byte)(qP + '0');
			temp[13] = '\n';
			
			fdb.write(temp);
			ImageFile f = new ImageFile(h, w);			
			for(int i = 0; i < nf; ++i ){    	
				VideoFile.readNextFrame_576v(f, fd, h, w);
				JPEGCompressor.convertRGB2YUV(f);
				//0 th 29 th etc frames are i frame else p frame
				if(i  % 30 == 0){    	
					fdb.writeByte('I');
					encodeImageFrame(f, true, fdb);    			
				}
				else{    			    			
					fdb.writeByte('P');
					encodeImageFrame(f, false, fdb);    	
				}  
				encodeEnhancedLayer(f, referenceFrame, fde);
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally{
			try {
				if(fd != null)
					fd.close();
				if(fdb != null)
					fdb.close();				
				if(fde != null)
					fde.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("ENCODING DONE: " + DateUtils.now());
	}
	public static void main(String[] args){		
		String filename = null;
		if(args.length < 12){
			System.out.println("Use: java Encoder -i inputVideoFile -h height -w width -n numberOfFrame -q qParameter -k kValue");
			return;
		}
		for(int i=0; i < args.length; ++i){
			char[] arg = args[i].toCharArray();
			if (arg[0] == '-') {
				switch (arg[1]) {  
					case 'i': filename = args[++i];
	                  	break;
					case 'h': 
						h = Integer.parseInt(args[++i]);						
						break;			
					case 'w': 
						w = Integer.parseInt(args[++i]);						
						break;
					case 'n': 
						nf = Integer.parseInt(args[++i]);						
						break;		
					case 'q': 
						qP = Byte.parseByte(args[++i]);											
						break;		
					case 'k': 
						k = Byte.parseByte(args[++i]);						
						break;		
				
					default : System.out.println("Unaccepted Parameter " + arg[1]);
	                  return;
				}
			}
		}
		
		buf = new short[3][h][w];	
		buferr = new short[3][h][w];
		mv = new byte[(h/16)][(w/16)][2];
		referenceFrame = new ImageFile(h, w);		

		Encoder.encode(filename);	
	}
}
