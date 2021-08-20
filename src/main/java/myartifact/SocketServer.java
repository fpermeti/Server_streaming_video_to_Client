
package myartifact;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

public class SocketServer {

	public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException {

		System.out.println("Server started...");

		File videosDir = new File("src\\main\\resources\\videos");

		File[] contentsOfDirectory = videosDir.listFiles();

		Hashtable<String, String> videos = MyHelpClass.populateHashtable(contentsOfDirectory);

		MyHelpClass.createMissingVideos(contentsOfDirectory, videos);

		// ----------------------------------------

		ServerSocket server = new ServerSocket(5000);

		Socket socket = server.accept();

		ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
		ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

		// ---------------------------------------

		String[] reply1 = { "", "" };
		String[] reply2 = { "", "" };

		Process p = null;

		String ready = "ready";

		while (true) {
//---------------------------------------------------------------------------------------------------------------------
//send ready to client
			oos.writeObject(ready);
			oos.flush();

//----------------------------------------------------------------------------------------------------------------------
//as soon as client sends Internet speed and video file extension
			// send back list with videos based on clients Internet speed and video file
			// extension
			while (true) {
				reply1 = (String[]) ois.readObject();

				if (reply1[1].equals("exit") || !reply1[1].equals(""))
					break;

			}
			if (reply1[1].equals("exit"))
				break;

			oos.writeObject(MyHelpClass.listOfVideosBasedOnSpeedAndFormat(reply1));
			oos.flush();
//-----------------------------------------------------------------------------------------------------------------------
			// read from client the selected video and transmission protocol
			while (true) {
				reply2 = (String[]) ois.readObject();

				if (reply2[0].equals("exit") || reply2[1].equals("exit") || !reply2[0].equals("")
						|| !reply2[1].equals(""))
					break;
			}
			if (reply2[0].equals("exit") || reply2[1].equals("exit"))
				break;

			System.out.println("Video selected: " + reply2[0]);

			System.out.println("Transmission protocol selected: " + reply2[1]);

// -----------------------------------------------------------------------------------------------------------------------
//server starts transmission
			MyHelpClass.serverTransmit(reply2, p);

// -----------------------------------------------------------------------------------------------------------------------

			break;
		}

		ois.close();
		oos.close();
		socket.close();
		server.close();

		System.out.println("\nServer terminated.");

	}
}