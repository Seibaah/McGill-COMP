import java.awt.Color;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.ThreadLocalRandom;

public class fault {

    // width, heigh, threads, lines
    public static int w;
    public static int h;
    public static int t;
    public static int k;
    
    //pixel array
    public static AtomicInteger img[][];
    
    //threads and concurrent objects arrays
    public static Thread th[];
    public static fissure f[];
    
    //max and min data points in the pixel array
    public static int min, max;
    
    //timers
    public static long t0, tf;

    public static void main(String[] args) {
    	
        try {
        	//parsing arguments
            if (args.length==4) {
                w = Integer.parseInt(args[0]);
                h = Integer.parseInt(args[1]);
                t = Integer.parseInt(args[2]);
                k = Integer.parseInt(args[3]);
                
            }
            else {
            	System.out.println("4 arguments required: width, height, # of threads, # of lines");
            	System.out.println("Running default settings: 1000, 1000, 8, 2500");
            	w=1000; h=1000; t=8; k=2500;
            }
            
            //atomic array initialization
            img = new AtomicInteger[h][w];
            for (int i=0; i<h; i++) {
            	for (int j=0; j<w; j++) {
            		img[i][j] = new AtomicInteger(0);
            	}
            }
            
            //timer start
            t0 = System.currentTimeMillis();
            
            //thread creation and start
            f = new fissure[t];
            th = new Thread[t];
            for (int i=0; i<t; i++) {
            	f[i] = new fissure();
            	th[i] = new Thread(f[i]);
            	
            	th[i].start();
            }
            
            //main thread waits 
            for (int i=0; i<t; i++) {
            	th[i].join();
            }
                
            findExtremePoints();
            
            BufferedImage image = render();
            
            //end timer
            tf = System.currentTimeMillis();
            System.out.println("Time " + (tf-t0));
            
            // Write out the image
            File outputfile = new File("outputimage.png");
            ImageIO.write(image, "png", outputfile);
            
        } 
        catch (Exception e) {
            System.out.println("Exception " + e);
            e.printStackTrace();
        }
    }
    
    //find min and max values in the image data array
    public static void findExtremePoints() {
    	
    	min=Integer.MAX_VALUE;
        max=0;
        
        for (int i=0; i<h; i++) {
        	for (int j=0; j<w; j++) {
        		
        		if (img[i][j].get()<=min) {
        			min=img[i][j].get();
        		}
        		if (img[i][j].get()>=max) {
        			max=img[i][j].get();
        		}
        	}
        }
    }
    
    //Renders an image using the data array values to convert from HSB to RGB
    public static BufferedImage render() {
    	
    	// once we know what size we want we can create an empty image
        BufferedImage image = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
    	
    	for (int i=0; i<h; i++) {
        	for (int j=0; j<w; j++) {
        		try {
        			int v=img[i][j].get();
        			
        			while(v>=360) {
        				v-=360;
        			}
        			
        			Color col = Color.getHSBColor(v/360f, 1.f, 1.f);
            		int rgb = col.getRGB();
            		image.setRGB(j, i, rgb);
        		}
        		catch (Exception e) {
        			System.out.println("Exception" + e);            			
        			System.out.println("Min " + min + " Max " + max + " Val " + img[i][j].get());
        			e.printStackTrace();
        			break;
        		}
        		
        	}
        }
    	
    	return image;
    }
    
    //Tests and decreases the number of lines to be drawn if above 0
    public synchronized static boolean testAndDec() {
    	if (k>0) {
    		k--;
    		return true;
    	}
    	return false;
    }
}

class fissure implements Runnable {
	
	//initial and end points for the fault line
	int p0x, p0y;
	int p1x, p1y;
	
	//value by which to increase array cell
	int val;
	
	//if true, left side of the line is increased
	boolean left;
	
	
	@Override
	public void run() {
		
		//Thread will keep running if there is a line left to draw
		while (fault.testAndDec()) {
			
			setLineParameters();
			
			//Each array cell is visited and tested
			for (int p2y=0; p2y<fault.h; p2y++) {
				for (int p2x=0; p2x<fault.w; p2x++) {
					
					//formula
					int temp=(p1x-p0x)*(p2y-p0y);
					int temp2=(p2x-p0x)*(p1y-p0y);
					
					if (left==true) {
						if (temp-temp2>0 || temp-temp2==0) {
							fault.img[p2y][p2x].addAndGet(val);
						}
					}
					else {
						if (temp-temp2<0 || temp-temp2==0) {
							fault.img[p2y][p2x].addAndGet(val);
						}
					}
				}
			}
		}
	}
	
	//pseudo-random generation of the fault line points, the increase value and the side to increase
	public void setLineParameters() {
		p0x = ThreadLocalRandom.current().nextInt(fault.w);
		p0y = ThreadLocalRandom.current().nextInt(fault.h);
		p1x = ThreadLocalRandom.current().nextInt(fault.w);
		p1y = ThreadLocalRandom.current().nextInt(fault.h);
		
		val = ThreadLocalRandom.current().nextInt(11);
		
		left = ThreadLocalRandom.current().nextBoolean();
	}
	
}
