
package myartifact;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;

public class SocketClient {

	public static void main(final String[] args)
			throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException {

		System.out.println("Client started...");
		System.out.println();

		// amount of time between each speed test reports set to 1s
		final int REPORT_INTERVAL = 1000;

		// speed test duration set to 5s
		final int SPEED_TEST_DURATION = 5000;

		// socket timeout used in ms
		final int SOCKET_TIMEOUT = 10000;

		// speed test server uri
		final String SPEED_TEST_SERVER_URI_DL = "http://ipv4.ikoula.testdebit.info/100M.iso";

		final List<String> speedList = new ArrayList<>();

		// instantiate speed test
		final SpeedTestSocket speedTestSocket = new SpeedTestSocket();

		// set timeout for download
		speedTestSocket.setSocketTimeout(SOCKET_TIMEOUT);

		// add a listener to wait for speed test completion and progress
		speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {

			// called when download is complete
			@Override
			public void onCompletion(final SpeedTestReport report) {

				int currentSpeed = report.getTransferRateBit().intValue() / 1000;
				speedList.add(String.valueOf(currentSpeed));

			}

			@Override
			public void onError(final SpeedTestError speedTestError, final String errorMessage) {

				System.out.println("Network error, exiting...");

				System.out.println("\nClient terminated.");

				System.exit(0);
			}

			@Override
			public void onProgress(final float percent, final SpeedTestReport downloadReport) {

				System.out.print("Wait speedtest...\r");
			}
		});

		speedTestSocket.startFixedDownload(SPEED_TEST_SERVER_URI_DL, SPEED_TEST_DURATION, REPORT_INTERVAL);

		while (true) {
			try {

				System.out.println("Current speed: " + speedList.get(0) + " Kbps\n");

				break;

			} catch (Exception e) {

			}
		}

		Process p = null;

		Socket socket = null;

		while (socket == null) {
			try {

				socket = new Socket("127.0.0.1", 5000);

				System.out.print("Waiting the server...\r");

			} catch (Exception e) {

				System.out.print("Waiting the server...\r");

			}
		}

		ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
		ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

		Scanner sc = new Scanner(System.in);

		String str1 = "", str2 = "";

		String[] response1 = { "", "" };
		String[] response2 = { "", "" };

		// for testing purposes
		// response1[0] = "5000";

		response1[0] = speedList.get(0);

		while (true) {
//-----------------------------------------------------------------------------------------------------------------------
			// as soon as server sends ready proceed
			while (true) {
				System.out.print("Waiting the server...\r");
				str1 = (String) ois.readObject();
				if (str1.equals("ready"))
					break;
			}

//--------------------------------------------------------------------------------------------------------------------------
//send to server Internet speed and video file extension
			while (true) {
				// client sends String array with Internet speed and video format
				System.out.print("- Select video format (avi, mp4, mkv) or type exit: ");

				response1[1] = sc.nextLine();

				if (response1[1].equals("exit") || response1[1].equals("mkv") || response1[1].equals("mp4")
						|| response1[1].equals("avi"))
					break;

				System.out.println("\n" + response1[1] + " video format is not available...\n");
			}

			if (response1[1].equals("exit")) {

				oos.writeObject(response1);
				oos.flush();
				break;

			}

			oos.writeObject(response1);
			oos.flush();

// ------------------------------------------------------------------------------------------------------------------
			// read from server the list with videos
			str2 = (String) ois.readObject();
			System.out.println("\nAvailable videos:\n" + str2);
// ------------------------------------------------------------------------------------------------------------------
//select video from list and transmission protocol
			// and send them back to server
			while (true) {
				System.out.print("- Select video or type exit: ");

				response2[0] = sc.nextLine();

				if (response2[0].equals("exit"))
					break;

				if (!str2.contains("\n" + response2[0] + "\n")) {
					System.out.println("\n" + response2[0] + " video is not available...\n");
					continue;
				} else
					break;

			}

			if (response2[0].equals("exit")) {

				oos.writeObject(response2);
				oos.flush();
				break;
			}

			System.out.println("\nYou selected " + response2[0] + " video\n");

			System.out.println();

			while (true) {

				System.out.print("- Select transmission protocol (tcp, udp, rtp/udp, default) or type exit: ");

				response2[1] = sc.nextLine();

				if (response2[1].equals("exit") || response2[1].equals("udp") || response2[1].equals("tcp")
						|| response2[1].equals("rtp/udp") || response2[1].equals("default"))
					break;

				System.out.println("\n" + response2[1] + " transmission protocol is not available...\n");
			}

			if (response2[1].equals("exit")) {

				oos.writeObject(response2);
				oos.flush();
				break;
			}

// ------------------------------------------------------------------------------------------------------------------
//client starts listening for incoming transmissions
			MyHelpClass.clientListen(response2, p);

			oos.writeObject(response2);
			oos.flush();

			break;

		}
		sc.close();
		ois.close();
		oos.close();
		socket.close();

		System.out.println("\nClient terminated.");
	}
}
