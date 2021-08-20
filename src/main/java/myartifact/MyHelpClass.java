
package myartifact;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

public class MyHelpClass {

	// ------------------------------------------------------------------------------------------------------------------------

	final static String ffplayTcp = "src\\main\\resources\\client_videos\\ffplay.exe tcp://127.0.0.1:1234?listen";

	final static String ffplayUdp = "src\\main\\resources\\client_videos\\ffplay.exe udp://127.0.0.1:1234";

	final static String ffplayRtp = "src\\main\\resources\\client_videos\\ffplay.exe rtp://127.0.0.1:1234";

	final static String route = "src\\main\\resources\\ffmpeg.exe -i src\\main\\resources\\videos\\";

	final static String ffmpegTcp = " -f mpegts tcp://127.0.0.1:1234";

	final static String ffmpegUdp = " -f mpegts udp://127.0.0.1:1234";

	final static String ffmpegRtp = " -f rtp_mpegts rtp://127.0.0.1:1234";

	// ------------------------------------------------------------------------------------------------------------------------

	// -------------------------------------------------------------------------------------------------------------------------

	// populate videos hashtable so that it contains only the highest resolution per
	// video

	public static Hashtable<String, String> populateHashtable(File[] contentsOfDirectory) {

		Hashtable<String, String> videos = new Hashtable<String, String>();

		String videoName = "", videoResolutionAndExtension = "", videoResolution = "", obj = "";

		for (File f : contentsOfDirectory) {

			obj = f.getName();

			videoName = obj.substring(0, obj.indexOf("-"));

			videoResolutionAndExtension = obj.substring(obj.indexOf("-") + 1);

			videoResolution = videoResolutionAndExtension.substring(0, videoResolutionAndExtension.indexOf(".") - 1);

			if (videos.isEmpty()) {// puts the first video that finds

				videos.put(videoName, videoResolutionAndExtension);

			} else {

				if (!videos.containsKey(videoName)) {

					videos.put(videoName, videoResolutionAndExtension);

				} else {

					if (Integer.parseInt(videoResolution) >= Integer
							.parseInt(videos.get(videoName).substring(0, videos.get(videoName).indexOf(".") - 1))) {

						videos.replace(videoName, videoResolutionAndExtension);

					}
				}

			}

		}

		return videos;
	}

	// ----------------------------------------------------------------------------------------------------------

	// ------------------------------------------------------------------------------------------------------

	// create the videos that are missing
	public static void createMissingVideos(File[] contentsOfDirectory, Hashtable<String, String> videos)
			throws IOException {

		FFmpeg ffmpeg = new FFmpeg("src\\main\\resources\\ffmpeg.exe");
		FFprobe ffprobe = new FFprobe("src\\main\\resources\\ffprobe.exe");

		String inDir = "src\\main\\resources\\videos\\";
		String outDir = "src\\main\\resources\\videos\\";

		List<String> fileList = new ArrayList<String>();

		// ---------------------------------------------------
		// populate fileList list with only the names of the videos inside "videos"
		// directory(excluding full file path)
		for (File obj : contentsOfDirectory) {

			fileList.add(obj.getName().toString());

		}

		// create a set containing the keys of videos hashtable
		Set<String> keys = videos.keySet();

		String[] extentionsArray = { "p.avi", "p.mp4", "p.mkv" };

		int[] heightArray = { 1920, 1280, 854, 640, 426 };

		int[] widthArray = { 1080, 720, 480, 360, 240 };

		for (String key : keys) {

			for (int i = 0; i < extentionsArray.length; i++) {

				for (int j = 0; j < widthArray.length; j++) {

					// if video file already exists
					if (fileList.contains(key + "-" + widthArray[j] + extentionsArray[i]) || widthArray[j] > Integer
							.parseInt(videos.get(key).substring(0, videos.get(key).indexOf(".") - 1))) {

						continue;

					} else // create the video that is missing
					{

						FFmpegBuilder builder = new FFmpegBuilder().setInput(inDir + key + "-" + videos.get(key))
								.addOutput(outDir + key + "-" + widthArray[j] + extentionsArray[i])
								.setVideoResolution(heightArray[j], widthArray[j]).done();

						FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

						executor.createJob(builder).run();

						System.out.println("input file: " + key + "-" + videos.get(key) + "  ---->  output file: " + key
								+ "-" + widthArray[j] + extentionsArray[i]);

					}
				}
			}
		}
		System.out.println();
	}
	// -------------------------------------------------------------------------------------------------------------------------

	// ------------------------------------------------------------------------------------------------------------------

	// return list of videos in analogy to the client download speed and format
	// preference
	// reply1[0] Internet speed
	// reply1[1] video format

	public static String listOfVideosBasedOnSpeedAndFormat(String[] reply1) {

		File myDirectory = new File("src\\main\\resources\\videos");

		File[] contentsOfDirectory = myDirectory.listFiles();

		String videosList = "\n", s2 = "", obj = "";

		int i1 = 0, i2 = 0;

		for (File f : contentsOfDirectory) {

			obj = f.getName();

			s2 = obj.substring(obj.indexOf("-") + 1, obj.indexOf(".") - 1);

			i1 = Integer.parseInt(reply1[0]);

			i2 = Integer.parseInt(s2);

			if (i1 >= 6000) {
				if (obj.contains("p." + reply1[1]) && 1080 >= i2)
					videosList += obj + "\n";

			} else if (6000 > i1 && i1 >= 4000) {
				if (obj.contains("p." + reply1[1]) && 720 >= i2)
					videosList += obj + "\n";

			} else if (4000 > i1 && i1 >= 2000) {
				if (obj.contains("p." + reply1[1]) && 480 >= i2)
					videosList += obj + "\n";

			} else if (2000 > i1 && i1 >= 1000) {
				if (obj.contains("p." + reply1[1]) && 360 >= i2)
					videosList += obj + "\n";

			} else { // (1000 > i1)

				if (obj.contains("p." + reply1[1]) && 240 >= i2)
					videosList += obj + "\n";
			}
		}

		return videosList;

	}

	// ------------------------------------------------------------------------------------------------------

	public static void clientListen(String[] response2, Process p) throws IOException {

		int tmp = Integer
				.parseInt(response2[0].substring(response2[0].indexOf("-") + 1, response2[0].indexOf(".") - 1));// resolution

		if (response2[1].equals("tcp")) {

			p = Runtime.getRuntime().exec(ffplayTcp);

		} else if (response2[1].equals("udp")) {

			p = Runtime.getRuntime().exec(ffplayUdp);

		} else if (response2[1].equals("rtp/udp")) {

			p = Runtime.getRuntime().exec(ffplayRtp);

		} else { // default

			if (tmp == 240) {

				p = Runtime.getRuntime().exec(ffplayTcp);

			} else if (tmp == 360 || tmp == 480) {

				p = Runtime.getRuntime().exec(ffplayUdp);

			} else { // 720, 1080

				p = Runtime.getRuntime().exec(ffplayRtp);

			}

		}

	}

	// ------------------------------------------------------------------------------------------------------

	// ------------------------------------------------------------------------------------------------------

	public static void serverTransmit(String[] reply2, Process p) throws IOException {

		int tmp = Integer.parseInt(reply2[0].substring(reply2[0].indexOf("-") + 1, reply2[0].indexOf(".") - 1));// resolution

		if (reply2[1].equals("tcp")) {

			p = Runtime.getRuntime().exec(route + reply2[0] + ffmpegTcp);

		} else if (reply2[1].equals("udp")) {

			p = Runtime.getRuntime().exec(route + reply2[0] + ffmpegUdp);

		} else if (reply2[1].equals("rtp/udp")) {

			p = Runtime.getRuntime().exec(route + reply2[0] + ffmpegRtp);

		} else { // default

			if (tmp == 240) {

				p = Runtime.getRuntime().exec(route + reply2[0] + ffmpegTcp);

			} else if (tmp == 360 || tmp == 480) {

				p = Runtime.getRuntime().exec(route + reply2[0] + ffmpegUdp);

			} else { // 720, 1080

				p = Runtime.getRuntime().exec(route + reply2[0] + ffmpegRtp);

			}

		}

	}

	// ------------------------------------------------------------------------------------------------------

}
