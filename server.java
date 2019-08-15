// SERVER version 2 - inside local disk D:

/*
Instructions:
1. open cmd, change directory to server dir using:     D:   and then:    chdir D:\royServer2_in_d 
2. compile the server file using: javac -classpath "C:\Program Files\MATLAB\R2018a\extern\engines\java\jar\engine.jar" server.java
3. run the server using:   java -classpath .;"C:\Program Files\MATLAB\R2018a\extern\engines\java\jar\engine.jar" server

Notes:

Matlab versions:
apparently, the detector will only work if we are using the exact version of matlab and matlab toolboxes.
the correct versions are:
Matlab R2018b
computer vision system toolbox -version 8.2
deep learning toolbox -version 12

this works because: 
- i already know where to save the image
- the image is overrun each time
- all folders exist
- IP is correct
- only 1 client

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
    public static String jsonPath = null;  //note warning
	//
	public static String path_to_server_files_dir = "D:\\royServer2_in_d";  //note - the reason for \\ is to signal that this is a special char.
	public static String path_to_ObjectMapper_dir = "D:\\royServer2_in_d\\ObjectMapper";
	public static String image_file_path_string = null;    //note warning

    public static void main (String arg[]) {

		//print welcome message
		System.out.println("\n\nHi! welcome to the server. \nWarming up all matlab systems: ");		
		
		// warm up all systems
		InitializeEngine();
		InitializeDetector();
	
		System.out.println("\nMatlab systems initialized. initialize server: ");		

        try { //the try block is because the ServerSocket CTOR might crash if theres another app in the same port [or something like that]
		
			// updated:
			ServerSocket server = new ServerSocket(1936, 0, InetAddress.getByName("132.68.58.72"));  // "132.68.58.72" and not "http://132.68.58.72". note that this is this computer ipv4 address. the android client also loggs on to this address
            System.out.println("Server started. Listening to port 1936");
			
            // wait for client [loop - until client enters !]
            System.out.println("now waiting for client ...\n");
            Socket client_socket = server.accept();

            // if we got here, it means a client has connected.
            System.out.println("Server connected \n");
			
            // use our own built function to get and save the picture.
			System.out.println("waiting for image to be sent ....");
            receivePicture(client_socket);
			// if we got here, we came back and the picture was saved.
            System.out.println("image received and saved. processing ....");

            //todo: add processing
            System.out.println("begin processing ....");
			
			//setNewJsonPath(false, false, 98, image_file_path.getAbsolutePath() ); //this calls "runMatlab" which actually does all the work. //warning note: look at arg 4
			
			//todo: add after processing phase
			
			System.out.println("processing completed. JSON path set to: " + jsonPath);
			System.out.println("sending back JSON");

			//todo: send back the JSON
			
			//sendJsonToClient(client_socket, JSONOBJ jsonobj);
			
			// ending
			System.out.println("\nbegining ending phase:");
			System.out.println("closing matlab engine");
			matlab_engine.close(); 
			System.out.println("closing client socket");
			client_socket.close();  //also called "socketX" in other places.
			System.out.println("closing server socket");
			server.close();  
			//todo: perform next line only after getting confirmation from client ?
			System.out.println("\nServer finished. have a nice day :)");
			
        } catch (Exception e) {
			System.out.println("\n ----- error performing something in main after 'InitializeEngine' and 'InitializeDetector'. -----\n ");
			e.printStackTrace();
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
			String path = path_to_server_files_dir;  
            File image_file_path = new File(path + File.separator + "image.jpeg");   
			ImageIO.write(bufferedImage, "jpg", image_file_path);
			image_file_path_string = image_file_path.getAbsolutePath();
			System.out.println("image created. path is: " + image_file_path_string);
			
			//ending
            inStream.close();
            content.close();
            //socketX.close(); - do this in the main function ? //TODO: WARNING: check if this line i commented changes the behavior of the program. i copied it to the main func ending part

        } catch (Exception e) {
			System.out.println("\n ----- error performing something in receivePicture. -----\n ");
			e.printStackTrace();
            System.out.println("now returning.");

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

        } catch (Exception e) {

			System.out.println("\n ----- error performing something in sendJsonToClient. -----\n ");
			e.printStackTrace();
            System.out.println("now returning.");
        }

    } */
	
	////////////////////////////////////// ASSISTING FUNCTIONS ////////////////////////////////////////////
	////////////////////////////////////// MATLAB STUFF ////////////////////////////////////////////
	
	
	
	private static void InitializeEngine() {
		// this whole function is about 1 line that needs a try catch block around it
        try{
			
            System.out.println("Initializing Matlab Engine ...");  

            matlab_engine = MatlabEngine.startMatlab(); /*!!!*/

            System.out.println("Engine Initialization Completed \n");	
			
        } catch (InterruptedException e) {
            System.out.println("\n ----- error performing something in InitializeEngine. -----\n ");
			e.printStackTrace();
            System.out.println("now returning.");
        } catch (ExecutionException e) {
            System.out.println("\n ----- error performing something in InitializeEngine. -----\n ");
			e.printStackTrace();
            System.out.println("now returning.");
        } catch (Exception e) {
			System.out.println("\n ----- error performing something in InitializeEngine. -----\n ");
			e.printStackTrace();
            System.out.println("now returning.");
        }
    }
	
	
	private static void InitializeDetector() {
        System.out.println("Initializing Detector... (this will take some time) ");  
        // messages are printed from matlab (!?)
        try {
            // Change directory and evaluate function
			String matlabFunDir = path_to_ObjectMapper_dir;  
            matlab_engine.eval("cd '" + matlabFunDir + "'");
            matlab_engine.feval(0, "intializeDetector"); // "intializeDetector" is a ".m" matlab file which will be opened and run.
			
			System.out.println("Detector Initialization Completed (verify completion- no errors or warning above me) \n");	

        } catch (InterruptedException e) {
            System.out.println("\n ----- error performing something in InitializeDetector. -----\n ");
			e.printStackTrace();
            System.out.println("now returning.");
        } catch (ExecutionException e) {
            System.out.println("\n ----- error performing something in InitializeDetector. -----\n ");
			e.printStackTrace();
            System.out.println("now returning.");
        } catch (Exception e) {
            System.out.println("\n ----- error performing something in InitializeDetector. -----\n ");
			e.printStackTrace();
            System.out.println("now returning.");
        }
    }
	
	private static String RunMatlab(String imagePath, boolean showDetection, boolean showMapping, double th) {
        
		// messages are printed from matlab
		
        String temp_jsonPath = "";

        try {
            // Change directory and evaluate function
			String matlabFunDir = path_to_ObjectMapper_dir;  
            matlab_engine.eval("cd '" + matlabFunDir + "'");
			//
            Object result = matlab_engine.feval(1, "Detect_Map", imagePath, th, showDetection, showMapping);  // "Detect_Map" is a ".m" matlab file which will be opened and run.
			//
            temp_jsonPath = (String) result;

        } catch (InterruptedException e) {
            System.out.println("\n ----- error performing something in RunMatlab. -----\n ");
			e.printStackTrace();
            System.out.println("now returning.");
        } catch (ExecutionException e) {
            System.out.println("\n ----- error performing something in RunMatlab. -----\n ");
			e.printStackTrace();
            System.out.println("now returning.");
        }

        return temp_jsonPath;
    }
	
	
	public static void setNewJsonPath(boolean showDetection, boolean showMapping, int threshold, String image_path){
        jsonPath = RunMatlab(image_path, showDetection, showMapping, (double)threshold/100); 			//note: what is the threshold ?
    }


	
	
}  //end of class server


//old stuff for reference:
           // old stuff:
            //ServerSocket server = new ServerSocket(3000, 0, InetAddress.getByName(null)); //todo: change the port later .....
			//note: InetAddress.getByName(null) points to the loopback address (127.0.0.1)
            //System.out.println("Server started. Listening to port 3000");
		