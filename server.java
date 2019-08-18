// SERVER version 2 - inside local disk D:

/*
Instructions:
1. open cmd, change directory to server dir using:     D:   and then:    chdir D:\royServer2_in_d 
2. compile the server file using: javac -classpath "D:\MATLAB\R2018b\extern\engines\java\jar\engine.jar" server.java
3. run the server using:   java -classpath .;"D:\MATLAB\R2018b\extern\engines\java\jar\engine.jar" server




new run lines with json tools


javac -classpath "D:\MATLAB\R2018b\extern\engines\java\jar\engine.jar";"D:\royServer2_in_d\json-simple-1.1.1.jar" server.java


java -classpath .;"D:\MATLAB\R2018b\extern\engines\java\jar\engine.jar";"D:\royServer2_in_d\json-simple-1.1.1.jar" server







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
- added "D:\MATLAB\R2018b\bin\win64" to PATH [environment variables] -> IMPORTANT: "If you have multiple versions of MATLAB installed on your system, 
																					the version you use to build your engine applications must be the first listed in your system Path environment variable.
																					Otherwise, MATLAB displays Can't start MATLAB engine."

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
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
//
// package org.jbox2d.testbed.framework.j2d;
//
import com.mathworks.engine.MatlabEngine;

//

import java.io.FileReader;
import java.util.Iterator;
 
//
//import org.json.*;
//import org.json.JSONObject;
//import org.json.JSONException;

//
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


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
			
			// general note: it isn't possible to close a socket and reopen it. thats why we'll use to client sockets.
			
            // wait for client [loop - until client enters !]
            System.out.println("now waiting for client on the first socket (intended socket to receive data)...\n");
            Socket client_socket_for_receiving_data = server.accept();

            // if we got here, it means a client has connected.
            System.out.println("client_socket_for_receiving_data connected \n");
			
            // use our own built function to get and save the picture.
			System.out.println("waiting for image to be sent ....");
            receivePicture(client_socket_for_receiving_data);
			// if we got here, we came back and the picture was saved.
            System.out.println("image received and saved. processing ....");

            //todo: add processing
            System.out.println("begin processing ....");
			
			setNewJsonPath(false, false, 98, image_file_path_string); //this calls "runMatlab" which actually does all the work. //warning note: look at arg 4
			
			//todo: add after processing phase
			
			System.out.println("processing completed. JSON path set to: " + jsonPath);
			System.out.println("\nsending back JSON");

			//todo: send back the JSON
			
			// wait for client [loop - until client enters !]
            System.out.println("now waiting for client on the second socket (intended socket to send data)...\n");
            Socket client_socket_for_sending_data = server.accept();
            // if we got here, it means a client has connected.
            System.out.println("client_socket_for_sending_data connected \n");
			//
			sendJsonToClient(client_socket_for_sending_data);
			
			// ending
			System.out.println("\nbegining ending phase:");
			System.out.println("closing matlab engine");
			matlab_engine.close(); 
			System.out.println("closing client socket");
			client_socket_for_receiving_data.close();  //also called "socketX" in other places.
			client_socket_for_sending_data.close();
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
			
			//
			System.out.println("finished sending data");
			
			// convert the buffered content to an image, and save it.
			byte [] data = content.toByteArray();
			ByteArrayInputStream byteArrayOutputStream = new ByteArrayInputStream(data);
			BufferedImage bufferedImage = ImageIO.read(byteArrayOutputStream);
			String path = path_to_server_files_dir;  
            File image_file_path = new File(path + File.separator + "image.jpeg");   
			ImageIO.write(bufferedImage, "jpg", image_file_path);
			image_file_path_string = image_file_path.getAbsolutePath();
			System.out.println("image created. path is: " + image_file_path_string);
			
			//ending
            inStream.close();
            content.close();
			socketX.close();

			//
			System.out.println("exiting receivePicture");

        } catch (Exception e) {
			System.out.println("\n ----- error performing something in receivePicture. -----\n ");
			e.printStackTrace();
            System.out.println("now returning.");

        }

    }
	
	private static JSONObject createJsonFromFile() {
		//note - this function works because the json path is a global variable
		System.out.println("entering createJsonFromFile ");
		 
		JSONObject jsonObject = null;
		 
        JSONParser parser = new JSONParser();
 
        try {
 
            //Object obj = parser.parse(new FileReader("/Users/<username>/Documents/file1.txt"));
			Object obj = parser.parse(new FileReader(jsonPath));
 
            jsonObject = (JSONObject) obj;
 
			// NOTE: temp commented - but needs to be deleted later.
 /* 
            JSONArray Triangles_list = (JSONArray) jsonObject.get("Triangles");
			
 			System.out.println("\n[temp check]Printing json file ----------");
            System.out.println("\nTriangles:");
            Iterator<String> iterator = Triangles_list.iterator();
            while (iterator.hasNext()) {
                System.out.println(iterator.next());
            }
			System.out.println("done\n"); */
 
        } catch (Exception e) {
            e.printStackTrace();
        } 
		
		

		
		return jsonObject;
	}
	
	// THIS IS FOR LATER !
 	private static void sendJsonToClient(Socket socketX) {
        System.out.println("entering sendJsonToClient ");

        try {
			// create json object, and convert it to a string to send
			JSONObject jsonObject = createJsonFromFile();
			
			
			//temp check. maybe it will be okay to delete later 
			//this next part is for us so we can see that the file was recieved correctly.
			String jsonConvertToString_unformatted = jsonObject.toString();
			String jsonConvertToString_formatted = null;
			
			try {
				byte[] encoded = Files.readAllBytes(Paths.get(jsonPath));
				jsonConvertToString_formatted = new String(encoded, StandardCharsets.US_ASCII); //note the encoding here (arg 2)
			
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			System.out.println("\nCreated JSONObject from file. Printing json file as string:");
			System.out.println("\nUnformatted:");
			System.out.println(jsonConvertToString_unformatted); 
			System.out.println("\nFormatted:");
			System.out.println(jsonConvertToString_formatted + "\n");

			
			//notify user, and get stream
            System.out.println("getting DataOutputStream");
			
			// try to send as a string
            //DataOutputStream outputStream = new DataOutputStream(socketX.getOutputStream());
			// "UTF8 is the only byte encoding for JSON. Any other encoding and it's not legally JSON."
			OutputStreamWriter outputStreamWrite = new OutputStreamWriter(socketX.getOutputStream(), StandardCharsets.UTF_8);
			
			outputStreamWrite.write(jsonObject.toString());
			outputStreamWrite.flush();
			outputStreamWrite.close();
            
			

        } catch (Exception e) {

			System.out.println("\n ----- error performing something in sendJsonToClient. -----\n ");
			e.printStackTrace();
            System.out.println("now returning.");
        }
		
    } 
	
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
	
	private static String RunMatlab(String imagePath, boolean showDetection, boolean showMapping, double threshold) {
        
		// messages are printed from matlab
		
        String temp_jsonPath = "";

        try {
            // Change directory and evaluate function
			String matlabFunDir = path_to_ObjectMapper_dir;  
            matlab_engine.eval("cd '" + matlabFunDir + "'");
			//
            Object result = matlab_engine.feval(1, "Detect_Map", imagePath, threshold, showDetection, showMapping);  // "Detect_Map" is a ".m" matlab file which will be opened and run.
			
			//		System.out.println("temp check: inside RunMatlab. json path is: " + result.toString());

			//
            temp_jsonPath = (String) result;
			
			//		System.out.println("temp check: inside RunMatlab. json path is: " + temp_jsonPath);


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
		