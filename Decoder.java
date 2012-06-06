import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
public class Decoder {	
	static byte qP; //less than 7	
	static int h ;
	static int w ;		
	static int frate;	
	static int nf;
	
	static OutputPanel original = null;
	static OutputPanel decoded = null;
	static VideoFile originalVideo = null;
	static VideoFile decodedVideo = null;	
	static ArrayList<ImageFile> dlist = null; 
	static ArrayList<ImageFile> elist = null;
	static ArrayList<ImageFile> olist = null;
	static int frameCounter;
	static boolean isStop; 	
	
	static private ImageFile previouFrame = null;	
	//temprory storage
	static short[][][] buferr = null;
	static byte[][][] mv = null;

	static ImageFile decodeImageFrame(boolean isI, RandomAccessFile fd) throws IOException{		
		ImageFile recoverf = null; 
		if(isI){
			recoverf = new ImageFile(h, w);			
			JPEGDecompressor.decompressFile(recoverf, fd, qP);
		}
		else{
			JPEGDecompressor.decompressErrorFile(buferr, fd, mv, qP);
			recoverf = MotionCompensation.getPredictedFrame(previouFrame, mv);			
			MotionCompensation.recoveredFrame(recoverf, recoverf, buferr);									
		}
		for(int y = 0; y < h; ++y){					
			System.arraycopy(recoverf.getData()[0][y], 0, previouFrame.getData()[0][y], 0, w);
			System.arraycopy(recoverf.getData()[1][y], 0, previouFrame.getData()[1][y], 0, w);
			System.arraycopy(recoverf.getData()[2][y], 0, previouFrame.getData()[2][y], 0, w);
		}
		return recoverf;
	}	
	static void decode(String bfilename, String enhanced){
		System.out.println("DECODING STARTS: " + DateUtils.now());
		RandomAccessFile filed = null;
		RandomAccessFile fde = null;
		try{
			filed = new RandomAccessFile(bfilename,"r");
			fde = new RandomAccessFile(enhanced,"r");
			byte[] temp = new byte[14];
			filed.readFully(temp);
				
			w = (temp[0] - '0')*1000 + 100*(temp[1] - '0') + 10*(temp[2] - '0') + (temp[3] - '0');
			h = (temp[4] - '0')*1000 + 100*(temp[5] - '0') + 10*(temp[6] - '0') + (temp[7] - '0');
			nf = (temp[8] - '0')*1000 + 100*(temp[9] - '0') + 10*(temp[10] - '0') + (temp[11] - '0');
			qP = (byte) (temp[12] - '0') ;
			
			buferr = new short[3][h][w];
			mv = new byte[h/16][w/16][2];	
			previouFrame = new ImageFile(h, w);									
				
			dlist = new ArrayList<ImageFile>(); 
			elist = new ArrayList<ImageFile>();			
			for(int i = 0; i < nf; ++i ){    	    		      		
				int fType = filed.readByte(); 
				ImageFile f = null;
				if(fType == 'I'){    			    			    	
					f = decodeImageFrame(true, filed);
					dlist.add(f);
				}
				else{    			
					f = decodeImageFrame(false, filed);
					dlist.add(f);    	
				}
				/////////////////////////////
				ImageFile fe = new ImageFile(h, w);
				JPEGDecompressor.decompressEnhanceFile(buferr, fde, qP);
				MotionCompensation.recoveredFrame(fe, f, buferr);
				JPEGDecompressor.convertYUV2RGB(fe);
				elist.add(fe);
				//////////////////////////////				
				JPEGDecompressor.convertYUV2RGB(f);
			}
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}	 
		catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			if(filed != null){
				try {
					filed.close();
				} catch (IOException e) {				
					e.printStackTrace();
				}
			}
			if(fde != null){
				try {
					fde.close();
				} catch (IOException e) {		
					e.printStackTrace();
				}
			}
		}	
		System.out.println("DECODING DONE: " + DateUtils.now());		
	}
	static void decodeEnhancedLayer(String enhanced) {
		System.out.println("ENHANCEMENT START: " + DateUtils.now());
		RandomAccessFile fde = null;		
		try{
			fde = new RandomAccessFile(enhanced,"r");			
			ArrayList<ImageFile> imgArray = decodedVideo.getFrames(); 		
			for(int i = 0; i < nf; ++i ){    	    		      		    			    			    	
				ImageFile f = imgArray.get(i);   	
				JPEGCompressor.convertRGB2YUV(f);
				JPEGDecompressor.decompressEnhanceFile(buferr, fde, qP);
				MotionCompensation.recoveredFrame(f, f, buferr);
				JPEGDecompressor.convertYUV2RGB(f);
			}			
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {		
			e.printStackTrace();
		} 
		finally{
			if(fde != null){
				try {
					fde.close();
				} catch (IOException e) {		
					e.printStackTrace();
				}
			}
		}
		System.out.println("ENHANCEMENT DONE: " + DateUtils.now());
	}
			
	public static void main(String[] args){			
		String filename = null;		
		String bfilename = null;
		String filenamee = null;
		if(args.length < 8){
			System.out.println("Use: java Encoder -i inputVideoFile -b baseFile -e enhancedFile -r frameRate");
			return;
		}
		for(int i=0; i < args.length; ++i){
			char[] arg = args[i].toCharArray();
			if (arg[0] == '-') {
				switch (arg[1]) {  
					case 'i': filename = args[++i];
	                  	break;	
					case 'b': bfilename = args[++i];
                  		break;	
					case 'e': filenamee = args[++i];
                  		break;	
					case 'r': 
						frate = Integer.parseInt(args[++i]);						
						break;
					default : System.out.println("Unaccepted Parameter " + arg[1]);
	                  return;
				}
			}
		}
		/*String base = filename.split("\\.")[0];
		String bfilename = base + ".base";
		final String efilename = base + ".enhanced";*/
		//final String efilename = filenamee;		
		Decoder.decode(bfilename, filenamee);		
		originalVideo = new VideoFile(h, w, nf);	
		originalVideo.readVidepFile_576v(filename);	
		olist = originalVideo.getFrames();
		decodedVideo = new VideoFile(h, w, nf);
		decodedVideo.setFrames(dlist);
		original = new OutputPanel(h, w);
		decoded = new OutputPanel(h, w);
	
		JFrame frame = new JFrame("Scalable Video Compression");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		frame.setSize(750, 500);
		final JPanel pane = new JPanel();
		pane.setSize(720, 325);
		pane.setLayout(null);
		
		frame.getContentPane().add(pane);		
		original.setSize(360, 325);
		original.setLocation(10, 10);
		pane.add(original);		
		decoded.setSize(360, 325);
		decoded.setLocation(380, 10);
		pane.add(decoded);
				
		final JLabel label = new JLabel("Frame " + (frameCounter + 1));
		label.setSize(100, 20);
		label.setLocation(520, 335);
		pane.add(label);
		
		JButton playb = new JButton("<"); 
		playb.setSize(60, 20);
		playb.setLocation(10, 335);
		pane.add(playb);
		playb.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				isStop = false;
				if(frameCounter > 0){
					Thread display = new Thread(){
						public void run(){
							frameCounter--;	
							for(; (frameCounter >= 0) && !isStop; --frameCounter ){    		    		
								original.setImgData(originalVideo.getFrames().get(frameCounter));								
								decoded.setImgData(decodedVideo.getFrames().get(frameCounter));			
								label.setText("Frame " + (frameCounter+1));
								pane.repaint();
								try{
									Thread.sleep(1000/frate);
								} catch (InterruptedException e) {				
									e.printStackTrace();
								}
							}
						}
					};
					display.start();
				}
			}
		});
		
		JButton playbs = new JButton("<<"); 
		playbs.setSize(60, 20);
		playbs.setLocation(80, 335);
		pane.add(playbs);
		playbs.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if(frameCounter > 0){
					frameCounter--;				
					original.setImgData(originalVideo.getFrames().get(frameCounter));
					decoded.setImgData(decodedVideo.getFrames().get(frameCounter));								
					label.setText("Frame " + (frameCounter+1));
					pane.repaint();
				}
			}
			
		});
		JButton stop = new JButton("[]"); 
		stop.setSize(60, 20);
		stop.setLocation(150, 335);
		pane.add(stop);
		stop.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				isStop = true; 				
			}
		});

		JButton playfs = new JButton(">>"); 
		playfs.setSize(60, 20);
		playfs.setLocation(220, 335);
		pane.add(playfs);
		playfs.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if(frameCounter < (nf-1)){
					frameCounter++;				
					original.setImgData(originalVideo.getFrames().get(frameCounter));					
					decoded.setImgData(decodedVideo.getFrames().get(frameCounter));								
					label.setText("Frame " + (frameCounter+1));
					pane.repaint();
				}
			}
		});

		JButton playf = new JButton(">"); 
		playf.setSize(60, 20);
		playf.setLocation(290, 335);
		pane.add(playf);
		playf.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				isStop = false;
				if(frameCounter < (nf-1)){
					Thread display = new Thread(){
						public void run(){
							frameCounter++;
							for(; (frameCounter < nf) && !isStop; ++frameCounter ){    		    		
								original.setImgData(originalVideo.getFrames().get(frameCounter));								
								decoded.setImgData(decodedVideo.getFrames().get(frameCounter));
								label.setText("Frame " + (frameCounter+1));
								pane.repaint();
								try{
									Thread.sleep(1000/frate);
								} catch (InterruptedException e) {				
									e.printStackTrace();
								}
							}
						}
					};
					display.start();
				}
			}
		});
		
		JButton enhanced = new JButton("ENHANCED"); 
		enhanced.setSize(100, 20);
		enhanced.setLocation(400, 335);
		pane.add(enhanced);
		enhanced.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){				
				//Decoder.decodeEnhancedLayer(efilename);	
				decodedVideo.setFrames(elist);
				//originalVideo.setFrames(dlist);
				frameCounter = 0;
				original.setImgData(originalVideo.getFrames().get(frameCounter));		
				decoded.setImgData(decodedVideo.getFrames().get(frameCounter));		
				pane.repaint();
			}
		});
		JButton normal = new JButton("BASE ONLY"); 
		normal.setSize(100, 20);
		normal.setLocation(600, 335);
		pane.add(normal);
		normal.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){				
				//Decoder.decodeEnhancedLayer(efilename);	
				decodedVideo.setFrames(dlist);
				//originalVideo.setFrames(olist);
				frameCounter = 0;
				original.setImgData(originalVideo.getFrames().get(frameCounter));		
				decoded.setImgData(decodedVideo.getFrames().get(frameCounter));		
				pane.repaint();
			}
		});
		frameCounter = 0;
		original.setImgData(originalVideo.getFrames().get(frameCounter));		
		decoded.setImgData(decodedVideo.getFrames().get(frameCounter));		
		pane.repaint();

		frame.setVisible(true);		
	}
}
