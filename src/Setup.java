import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import java.math.*;
public class Setup {
	
public static float sigma = 1;
public static float wetcost = 10000;
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Setup s = new Setup();
		
		float [][][] Data = readImg(new String("C:\\Users\\Philipp\\Desktop\\test.jpg"));
		float [][][] PaddedImage = {symmetricPad(Data[0],16),symmetricPad(Data[1],16),symmetricPad(Data[2],16),};	//TODO calc as int
		float [][] highpass = {{-0.0544158422f	,0.3128715909f,	-0.6756307363f,	0.5853546837f,	0.0158291053f,	-0.284015543f,	-0.0004724846f,	0.1287474266f,	0.017369301f,	-0.0440882539f,	-0.0139810279f,	0.008746094f, 0.004870353f,	-0.0003917404f,	-0.0006754494f,	-0.0001174768f}};
		float [][] lowpass = {{-0.0001174768f	,0.0006754494f,	-0.0003917404f,	-0.004870353f,	0.008746094f,	0.0139810279f,	-0.0440882539f,	-0.017369301f,	0.1287474266f,	0.0004724846f,	-0.284015543f,	-0.0158291053f,	0.5853546837f,	0.6756307363f,	0.3128715909f,	0.0544158422f}};
		float [][][] Filter = {arraymul(flip(highpass),lowpass),arraymul(flip(lowpass),highpass), arraymul(flip(highpass),highpass)};
		float [][][] RotatedFilters = rotateFilters(Filter);
		
		
		float[][][] Filtered = {
				 removePadding(circshift(convolution(sgmInvert(convolution(PaddedImage[0],Filter[0])),RotatedFilters[0])),16)
				,removePadding(circshift(convolution(sgmInvert(convolution(PaddedImage[0],Filter[1])),RotatedFilters[1])),16)
				,removePadding(circshift(convolution(sgmInvert(convolution(PaddedImage[0],Filter[2])),RotatedFilters[2])),16)
				,removePadding(circshift(convolution(sgmInvert(convolution(PaddedImage[1],Filter[0])),RotatedFilters[0])),16)
				,removePadding(circshift(convolution(sgmInvert(convolution(PaddedImage[1],Filter[1])),RotatedFilters[1])),16)
				,removePadding(circshift(convolution(sgmInvert(convolution(PaddedImage[1],Filter[2])),RotatedFilters[2])),16)
				,removePadding(circshift(convolution(sgmInvert(convolution(PaddedImage[2],Filter[0])),RotatedFilters[0])),16)
				,removePadding(circshift(convolution(sgmInvert(convolution(PaddedImage[2],Filter[1])),RotatedFilters[1])),16)
				,removePadding(circshift(convolution(sgmInvert(convolution(PaddedImage[2],Filter[2])),RotatedFilters[2])),16)};
		
		int[][][] direction = new int[3][Data[0].length][Data[0][0].length];
		float[][][]  costs = computeCosts(Filtered,Data,direction);
		System.out.println("a");
		/*
		for(int i = 0; i < costs.length;i++) {
			for(int j = 0;j<costs[0].length;j++) {
		System.out.println(Arrays.toString(costs[i][j]));
			}
			System.out.println("#############################################################################################################################################################");
		}
		System.out.println(Arrays.toString(costs[0][0]));
	    //int[][] test = symmetricPad(A,2); //TODO Faltung implementieren -> 
		//System.out.println(Filter.toString());
	    /*
	    float[][] a = 
	    		 {{0.7566f,    0.1156f,    0.9012f,    0.0167f,    0.3691f},
	    		    {0.9955f,    0.0514f,    0.5406f,    0.8009f,    0.6618f},
	    		    {0.9624f,    0.3043f,    0.4320f,    0.1425f,    0.1696f},
	    		    {0.5351f,    0.5802f,    0.5427f,    0.4785f,    0.2788f},
	    		    {0.9639f,    0.5310f,    0.7124f,    0.2568f,    0.1982f}};

	    float[][] b = {{ 0.1951f,    0.4040f,    0.8445f,    0.7849f},
	    	    {0.3268f,    0.1792f,    0.6153f,    0.4650f},
	    	    {0.8803f,    0.9689f,    0.3766f,    0.8140f},
	    	    {0.4711f,    0.4075f,    0.8772f,    0.8984f}};

	    float[][] c = convolution(a,b);
	    */
	   // float [][]  t = convolution(Filter[0],a);
	    
		
 	}
	
	private static float[][][]  computeCosts(float[][][] filtered,float[][][] rawImg, int[][][] dir) {
		float[][][] result = new float[filtered.length/3][filtered[0].length][filtered[0][0].length];
		for(int i= 0 ; i<filtered.length/3;i++) {
			for(int j= 0 ; j<filtered[0].length;j++) {
				for(int k=0;k<filtered[0][0].length;k++) {
					
					
					result[i][j][k] = filtered[3*i][j][k]+ filtered[3*i+1][j][k]+filtered[3*i+2][j][k];
					
					switch((int)rawImg[i][j][k]) {
					case 255: dir[i][j][k] = -1;
					case 0: dir[i][j][k] = 1;
					default: dir[i][j][k] =0;
					}
					
				}
			}
		}
	return result;
}
	
	private static float[][][]  computeCosts(float[][][] filtered,float[][][] rawImg) {
			float[][][] result = new float[filtered.length/3*2][filtered[0].length][filtered[0][0].length];
			for(int i= 0 ; i<filtered.length/3;i++) {
				for(int j= 0 ; j<filtered[0].length;j++) {
					for(int k=0;k<filtered[0][0].length;k++) {
						if(rawImg[i][j][k]!= 255) {
						result[2*i][j][k] = filtered[3*i][j][k]+ filtered[3*i+1][j][k]+filtered[3*i+2][j][k];             //p1
						}else {
							result[2*i][j][k]=wetcost;	
						}
						
						if(rawImg[i][j][k]!= 0) {
							result[2*i+1][j][k] = filtered[3*i][j][k]+ filtered[3*i+1][j][k]+filtered[3*i+2][j][k];             //m1
							}else {
								result[2*i+1][j][k]=wetcost;	
							}
					}
				}
			}
		return result;
	}

	public static float[][] removePadding (float[][] mat, int n){
		float[][] result = new float[mat.length-2*n][mat[0].length-2*n];
		for(int i= 0; i< result.length;i++) {
			for(int j=0; j< result[0].length;j++) {
				result[i][j] = mat[i+n][j+n];
			}
		}
		return result;
	}
	public static float[][] circshift(float [][] mat){
		float[][] xshift = new float[mat.length][mat[0].length];
		for(int i =0 ; i < mat.length ; i++) {
			for(int j=0 ; j< mat[0].length;j++) {
				if(i==0) {
				xshift[i][j] = mat[mat.length-1][j];
						}else {
				xshift[i][j] = mat[i-1][j];			
						}
			}
		
		}
		float[][] yshift =  new float[mat.length][mat[0].length];
		for(int i =0 ; i < mat.length ; i++) {
			for(int j=0 ; j< mat[0].length;j++) {
				if(j == 0) {
				yshift[i][j] = xshift[i][mat[0].length-1];
						}else {
				yshift[i][j] = xshift[i][j-1];			
						}
			}
		
		}
		return yshift;
	}
	
	public static float[][] sgmInvert(float[][] dat){
		float [][] result = new float[dat.length][dat[0].length];
		for(int i=0;i<dat.length;i++) {
			for(int j=0;j<dat[0].length;j++) {
				result[i][j] = 1/(Math.abs(dat[i][j])+sigma);
			}
		}
		return result;
		
	}
	public static float[][][] rotateFilters(float[][][] filt){
		float [][][] result = new float[filt.length][filt[0].length][filt[0][0].length];
		for(int k = 0 ; k<filt.length;k++) {
			for(int i=0;i<filt[0].length;i++) {
				for(int j=0;j<filt[0][0].length;j++) {
					result[k][i][j] = Math.abs(filt[k][filt[0].length-i-1][filt[0][0].length-1-j]);
				}
			}
		}
		return result;
	}
	public static float[][] convolution(float[][]data, float [][] filter){ 
		
		float[][] result = new float[data.length][data[0].length];
		
		
		for(int i=0 ; i<result.length+filter.length/2 ; i++) {
			for(int j=0; j<result[0].length+filter.length/2; j++) {
			
				if(i-(int)Math.ceil((double)filter.length/2)>=0 && j-(int)Math.ceil((double)filter[0].length/2)>=0){
				float sum= 0;
				
				for(int k=0; k<filter.length; k++) {
					for(int l=0; l<filter[0].length;l++) {
						if(i-k< data.length &&i-k>=0 && j-l<data[0].length && j-l>=0) {
						sum += data[i-k][j-l] * filter[k][l];
						}
					}
					
				}
				result[i-(int)Math.ceil((double)filter.length/2)][j-(int)Math.ceil((double)filter[0].length/2)]= sum;
				}
			}
		}
		
		return result;
	}
	
	
	
	public static float[][] symmetricPad(float[][]mat , int padsize){
		float [][] result  = new float[mat.length+2*padsize][mat[0].length+2*padsize];
		for(int i=0; i<result.length;i++) {
			for(int j=0;j<result[0].length;j++) {
				int k,l;
				if(i<padsize) {
				k = padsize-1-i;}else if(i<mat.length+padsize){
				k = i-padsize;	
				}else {
					//k=mat.length-1 - (i-(mat.length+padsize));
					k=2*mat.length+padsize-1 - i;
				}
				if(j<padsize) {
					l = padsize-1-j;}else if(j<mat[0].length+padsize){
					l = j-padsize;	
					}else {
						//l=mat[0].length-1- (j-(mat[0].length+padsize));
						l=2*mat[0].length+padsize-1-j;
					}
				result[i][j] = mat[k][l];
			}
		}
		return result;
	}
	public static float[][] flip(float[][] mat){
		float[][] result = new float[mat[0].length][mat.length];
		for(int i = 0 ; i < mat[0].length; i++) {
			for(int j= 0 ;j < mat.length; j++) {
				result[i][j] = mat[j][i];
			}
		}
		
		
		return result;
	}
	public static float[][] arraymul(float [][] A , float [][]B){
		float[][] result = null;
		if(A.length == B[0].length) {
			result = new float[A.length][B[0].length];	
		}
		else {
			System.out.println("Input matricies not compatible");
			return result;
		}
		
		for(int row= 0 ; row<A.length;row++) {
			for(int col = 0; col < B[0].length ; col++) {
				float sum= 0;
				for(int i = 0; i <B.length; i++) {
					sum += A[row][i] * B[i][col];
				}
				result[row][col]=sum;
			}
		}
		return result;
	}

	public static float[][][] readImg(String filepath) throws IOException {
		BufferedImage image = ImageIO.read(new File(filepath));
	      final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
	      final int width = image.getWidth();
	      final int height = image.getHeight();
	      final boolean hasAlphaChannel = image.getAlphaRaster() != null;
	      float[][][] result = null;

	     
	      if (hasAlphaChannel) {
	    	 result = new float[4][height][width];
	         final int pixelLength = 4;
	         for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
	          
	        	result[0][row][col] =  ((int) pixels[pixel + 1]+256)%256; // blue
	        	result[1][row][col] = ((int) pixels[pixel + 2]+256)%256; // green
	        	result[2][row][col] = ((int) pixels[pixel + 3]+256)%256; // red
	        	result[3][row][col] = ((int) pixels[pixel]+256)%256; // alpha
	            col++;
	            if (col == width) {
	               col = 0;
	               row++;
	            }
	         }
	      } else {
	         final int pixelLength = 3;
	         result = new float[3][height][width];
	         for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
	          
	        	
	            result[0][row][col] = (((int) pixels[pixel])+256)%256; // blue;
	            result[1][row][col] = (((int) pixels[pixel + 1])+256)%256; // green
	            result[2][row][col] = (((int) pixels[pixel + 2])+256)%256; // red
	            col++;
	            if (col == width) {
	               col = 0;
	               row++;
	            }
	         }
	      }

	      return result;
	}
}
