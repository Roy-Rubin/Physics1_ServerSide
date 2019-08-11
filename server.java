// SERVER version 1

/*
Instructions:
1. open cmd, change directory to server dir using "chdir C:\Users\RRubin\Desktop\royServer2"
2. compile the server file using "javac server.java" [changed to] javac -classpath engine.jar server.java
3. run the server using "java server"  [changed to] java -classpath .;engine.jar server

Notes:
this works because i already know where to save the image, the image is overrun each time, all folders exist, IP is correct. and only 1 client

*/

//
import javax.imageio.ImageIO;
import javax.swing.*;
//
import java.awt.image.BufferedImage;
import java.awt.BorderLayout;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
//
// package org.jbox2d.testbed.framework.j2d;
//
import com.mathworks.engine.MatlabEngine;


class server {
	
	// variable for the engine.
    // requires the addition of the .JAR file
	public static MatlabEngine matlab_engine;
    //
    public static String jsonPath;
	
    public static void main (String arg[]) {

		// warm up all systems
		InitializeEngine();
		InitializeDetector();
	
	
        try { //the try block is because the ServerSocket CTOR might crash if theres another app in the same port [or something like that]
		
			// updated:
			ServerSocket server = new ServerSocket(1936, 0, InetAddress.getByName("132.68.58.72"));  // "132.68.58.72" and not "http://132.68.58.72". note that this is this computer ipv4 address. the android client also loggs on to this address
            System.out.println("Server started. Listening to port 1936");
			
            // wait for client [loop - until client enters !]
            System.out.println("now waiting for client ...");
            Socket client_socket = server.accept();

            // if we got here, it means a client has connected.
            System.out.println("Server connected");
			
            // use our own built function to get and save the picture.
            receivePicture(client_socket);
			// if we got here, we came back and the picture was saved.
            System.out.println("image received and saved. processing ....");

            //todo: add processing
            System.out.println("begin processing ....");

			setNewJsonPath(false, false, 98, "C:\\Users\\RRubin\\Desktop\\royServer2\\image.jpeg"); //this calls "runMatlab" which actually does all the work.
			
			//todo: add after processing phase
			
			System.out.println("processing completed. sending back JSON");

			//todo: send back the JSON
			
			//sendJsonToClient(client_socket, JSONOBJ jsonobj);
			
			//todo: perform next line only after getting confirmation from client ?
			System.out.println("Server finished. have a nice day :)");

			// ending
			matlab_engine.close(); 
			
        } catch (Exception e) {
            // todo: print error

            System.out.println("could not log on... ");
            System.out.println("now exiting.");
            return;
        }


    }

	////////////////////////////////////// ASSISTING FUNCTIONS ////////////////////////////////////////////
	////////////////////////////////////// SOCKETWISE ////////////////////////////////////////////

    private static void receivePicture(Socket socketX) {
        System.out.println("entering receivePicture ");

        try {
			//notify user, and get stream
            System.out.println("getting DataInputStream");
            DataInputStream inStream = new DataInputStream(socketX.getInputStream());
			
            byte[] buffer = new byte[4096];
            ByteArrayOutputStream content = new ByteArrayOutputStream();
            int n;
			
            while ((n = inStream.read(buffer)) != -1) {
                content.write(buffer, 0, n);      
                content.flush();
            }
			
			// convert the buffered content to an image, and save it.
			byte [] data = content.toByteArray();
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			BufferedImage bufferedImage = ImageIO.read(bis);
			String path = "C:\\Users\\RRubin\\Desktop\\royServer2";  //note - the reason for \\ is to signal that this is a special char.
            File image_file_path = new File(path + File.separator + "image.jpeg");   
			ImageIO.write(bufferedImage, "jpg", image_file_path);
			System.out.println("image created");
			
			//ending
            inStream.close();
            content.close();
            socketX.close();

        } catch (Exception e) {

            //todo: print error.

            System.out.println("error in receivePicture");

        }

    }
	
	// THIS IS FOR LATER !
/* 	private static void sendJsonToClient(Socket socketX, JSONOBJ jsonobj) {
        System.out.println("entering sendJsonToClient ");

        try {
			//notify user, and get stream
            System.out.println("getting DataOutputStream");
            DataOutputStream outputStream = new DataOutputStream(socketX.getOutputStream());
			
            
			//ending
            outputStream.close();
            socketX.close();

        } catch (IOException e1) {

            //todo: print error.

            System.out.println("error in sendJsonToClient");

        }

    } */
	
	////////////////////////////////////// ASSISTING FUNCTIONS ////////////////////////////////////////////
	////////////////////////////////////// MATLAB STUFF ////////////////////////////////////////////
	
	
	
	private static void InitializeEngine() {
        try{
            System.out.println("Initializing Matlab Engine ...");  

            matlab_engine = MatlabEngine.startMatlab(); /*!!!*/

            System.out.println("Engine Initialization Completed");	
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // todo: print error
            System.out.println("now exiting.");
            return;
        }
    }
	
	
	private static void InitializeDetector() {
        System.out.println("Initializing Detector...(should take a long time) ");  
        // messages are printed from matlab (!?)
        try {
            // Change directory and evaluate function
			String matlabFunDir = "C:\\Users\\RRubin\\Desktop\\royServer2\\ObjectMapper";  
            matlab_engine.eval("cd '" + matlabFunDir + "'");
            matlab_engine.feval(0, "intializeDetector");
			
			System.out.println("Detector Initialization Completed (verify completion in matlab)");	

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // todo: print error
            System.out.println("now exiting.");
            return;
        }
    }
	
	private static String RunMatlab(String imagePath, boolean showDetection, boolean showMapping, double th) {
        
		// messages are printed from matlab
		
        String temp_jsonPath = "";

        try {
            // Change directory and evaluate function
			String matlabFunDir = "C:\\Users\\RRubin\\Desktop\\royServer2\\ObjectMapper";  
            matlab_engine.eval("cd '" + matlabFunDir + "'");
			//
            Object result = matlab_engine.feval(1, "Detect_Map", imagePath, th, showDetection, showMapping);
			//
            temp_jsonPath = (String) result;

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return temp_jsonPath;
    }
	
	
	public static void setNewJsonPath(boolean showDetection, boolean showMapping, int threshold, String image_path){
        //jsonPath = RunMatlab(getImagePath(), showDetection, showMapping, (double)threshold/100); //TODO: IMPORTANT - this is noted only because the function
    }


	
	
}  //end of class server


//old stuff for reference:
           // old stuff:
            //ServerSocket server = new ServerSocket(3000, 0, InetAddress.getByName(null)); //todo: change the port later .....
			//note: InetAddress.getByName(null) points to the loopback address (127.0.0.1)
            //System.out.println("Server started. Listening to port 3000");
		