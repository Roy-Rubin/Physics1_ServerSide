// SERVER version 12131341

//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

class server {
    public static void main (String arg[]) {

        try { //the try block is because the ServerSocket CTOR might crash if theres another app in the same port [or something like that]

            // old stuff:
            //ServerSocket server = new ServerSocket(3000, 0, InetAddress.getByName(null)); //todo: change the port later .....
			//note: InetAddress.getByName(null) points to the loopback address (127.0.0.1)
            //System.out.println("Server started. Listening to port 3000");
			
			// updated:
			ServerSocket server = new ServerSocket(1936, 0, InetAddress.getByName("132.68.58.72"));  // "132.68.58.72" and not "http://132.68.58.72"
            System.out.println("Server started. Listening to port 1936");
			
            // wait for client [loop - until client enters !]
            System.out.println("now waiting for client ...");
            Socket client_socket = server.accept();

            // if we got here, it means a client has connected.
            System.out.println("Server connected");
			
            // use our own built function to get and save the picture.
            receivePicture(client_socket);
			// if we got here, we came back and the picture was saved.
            System.out.println("image recieved and saved. processing ....");

            //todo: add processing
            System.out.println("begin processing ....");


			//todo: add after processing phase
			
			System.out.println("processing completed. sending back JSON");

			//todo: send back the JSON
			

        } catch (Exception e) {
            // todo: print error

            System.out.println("could not... ?");
            System.out.println("now exiting.");
            return;
        }


    }


    private static void receivePicture(Socket socketX) {
        System.out.println("entering receivePicture ");

        try {
			//notify user, and get steam
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
			String path = "C:\\Users\\RRubin\\Desktop\\royServer2";  //todo: note this shit.
            File image_file_path = new File(path + File.separator + "image.jpeg");   ///change slash direction ?
			ImageIO.write(bufferedImage, "jpg", image_file_path);
			System.out.println("image created");
			
			//ending
            inStream.close();
            content.close();
            socketX.close();

        } catch (IOException e1) {

            //todo: print error.

            System.out.println("error");

        }

    }
}

		