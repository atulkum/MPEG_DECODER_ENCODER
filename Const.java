
public class Const {
	static{
		try{
			init();
		}
		catch(Exception e){
			e.printStackTrace();
		}		
	}
	public static final byte[][] LQTable = {
		{4, 4, 4, 8, 8, 16, 16, 32},
		{4, 4, 4, 8, 8, 16, 16, 32},
		{4, 4, 8, 8, 16, 16, 32, 32},
		{8, 8, 8, 16, 16, 32, 32, 32},
		{8, 8, 16, 16, 32, 32, 32, 32},
		{16, 16, 16, 32, 32, 32, 32, 32},
		{16, 16, 32, 32, 32, 32, 32, 32},
		{32, 32, 32, 32, 32, 32, 32, 32}
	};
	public static final byte[][] UVQTable = {
		{8, 8, 8, 16, 32, 32, 32, 32},
		{8, 8, 8, 16, 32, 32, 32, 32},
		{8, 8, 16, 32, 32, 32, 32, 32},
		{16, 16, 32, 32, 32, 32, 32, 32},
		{32, 32, 32, 32, 32, 32, 32, 32},
		{32, 32, 32, 32, 32, 32, 32, 32},
		{32, 32, 32, 32, 32, 32, 32, 32},
		{32, 32, 32, 32, 32, 32, 32, 32}
	};
		
	public static final byte[][] Zigzag = {
		{0,   1,   5,   6,  14,  15,  27,  28},
		{2,   4,   7,  13,  16,  26,  29,  42},
		{3,   8,  12,  17,  25,  30,  41,  43},
		{9,  11,  18,  24,  31,  40,  44,  53},
		{10, 19,  23,  32,  39,  45,  52,  54},
		{20, 22,  33,  38,  46,  51,  55,  60},
		{21, 34,  37,  47,  50,  56,  59,  61},
		{35, 36,  48,  49,  57,  58,  62,  63}	
	};		
	public static final double[][] RGB2YUV = {
		{0.2990, 0.5870, 0.1140},
		{0.5000, -0.4187, -0.0813},
		{-0.1687, -0.3313, 0.5000}
	};
	public static final double[][] YUV2RGB = {
		{1.0000, 1.4020, 0},
		{1.0000,-0.7141,-0.3441},
		{1.0000, 0, 1.7720}
	};
	public static double c;
	public static double[] cosVal;
	static void init(){
		c = Math.sqrt(2)/2;
		cosVal = new double[32];
		cosVal[0] = 1; cosVal[1] = Math.cos(Math.PI/16); cosVal[2] = Math.cos(Math.PI/8); 
		cosVal[3] = Math.cos((3*Math.PI)/16); cosVal[4] = c; cosVal[5] = Math.cos((5*Math.PI)/16); 
		cosVal[6] = Math.cos((3*Math.PI)/8); cosVal[7] = Math.cos((7*Math.PI)/16);cosVal[8] = 0; 
		cosVal[9] = -Math.sin(Math.PI/16); cosVal[10] = -Math.sin(Math.PI/8); 
		cosVal[11] = -Math.sin((3*Math.PI)/16);cosVal[12] = -c; cosVal[13] = -Math.sin((5*Math.PI)/16); 
		cosVal[14] = -Math.sin((3*Math.PI)/8); cosVal[15] = -Math.sin((7*Math.PI)/16);		
		for(int i = 0; i < 16; ++i){
			cosVal[i+16] = - cosVal[i];
		}
	}
	static byte truncatetorgb(double p){				
		int val = (int) p;		
		if( val > 255)val = 255;
		else if(val < 0)val = 0;		
		return int2byte(val);
	}
	static byte truncatetorgberr(int val){		
		if(val > 255)val = 255;
		else if(val < 0)val = 0;		
		return int2byte(val);
	}
	static byte int2byte(int val){
		byte ret=0;		
		val &= 0xff;
		if(val==128){
			ret = -128;
			return ret;
		}
		return (byte) val;
	}	
	
	public static void main(String[] args){
		/*byte[] orig = new byte[64];
		byte[] zigzag = { 
			0,   1,   5,   6,  14,  15,  27,  28,
			2,   4,   7,  13,  16,  26,  29,  42,
			3,   8,  12,  17,  25,  30,  41,  43,
			9,  11,  18,  24,  31,  40,  44,  53,
			10, 19,  23,  32,  39,  45,  52,  54,
			20, 22,  33,  38,  46,  51,  55,  60,
			21, 34,  37,  47,  50,  56,  59,  61,
			35, 36,  48,  49,  57,  58,  62,  63								  	
		};
		for(int i=0; i < 64; ++i){
			orig[zigzag[i]] = (byte) i;
		}	
		System.out.print("{");
		for(int i=0; i < 64; ++i){
			if(i%8 == 0)System.out.println();
			System.out.print(" " + orig[i] + ",");
		}		
		System.out.println("}");*/
		/*int u, v;
		int t = 0;
		for(int k= 0; k <= 14; ++k){		
			if((k%7)%2 == 0){
				if(k == 7 || k==14){
					u = 7; v = k-7;
				}
				else{
					v = k%7; u = k-v;
				}
			}
			else{
				u = k%7;v = k-u;
			}
			if(v > u){
				t = v - u;
				for(int i = 0; i <= t; ++i){
					double pc = 0;								
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
					dest[v + oy][u + ox] = (short)pc;

					v--;u++;				
				}			
			}
			else{
				t = u - v;
				for(int i = 0; i <= t; ++i){
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
					dest[v + oy][u + ox] = (short)pc;
					v++;u--;
				}
			}			
		}*/
		
	}
}
