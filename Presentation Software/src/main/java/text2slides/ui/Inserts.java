package text2slides.ui;

import text2slides.slides.SlidesProcessing;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class Inserts {
	// function to get list of images in resources/pic
	// added string extensions to allow for polymorphism so i can use this function
	// for other inserts: video/audio
	private static List<String> getFiles(String path, String[] extensions) {
		JOptionPane.showMessageDialog(null,
				"If you are using local files, ensure that they are saved in the correct directory (src/main/java/resources/(pic,audio,video)",
				"Warning", JOptionPane.WARNING_MESSAGE);

		List<String> filesExt = new ArrayList<>();
		File dir = new File(path);
		if (dir.exists() && dir.isDirectory()) {
			File[] files = dir.listFiles();
			// check if directory is empty
			if (files != null) {
				// for each file in files
				for (File file : files) {
					// check if file is file or directory
					if (file.isFile()) {
						String fileName = file.getName().toLowerCase();
						for (String extension : extensions) {
							// check if file is image/gif
							if (fileName.endsWith(extension)) {
								filesExt.add(fileName);
								break;
							}
						}

					}
				}
			}
		}
		return filesExt;
	}

	void insertImageFun(JFrame frame, JTextArea terminal) {
		String path = "src/main/resources/pic";
		String[] imageFiles = { ".png", ".jpg", ".gif" };
		List<String> images = getFiles(path, imageFiles);
		String[] options = images.toArray(new String[0]);

		String input = (String) JOptionPane.showInputDialog(null, "Choose an image from below", "Insert Image",
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		int zoom = JOptionPane.showConfirmDialog(null, "would you like zoom");

		System.out.println(input);
		final String[] text = { "" };
		// create substring to get image name without file type
		int chop = input.indexOf(".");
		String name = input.substring(0, chop);
		if (zoom == JOptionPane.YES_OPTION) {
			if (input.endsWith(".gif")) {
				JOptionPane.showMessageDialog(frame, "GIFs can't have zoom", "Error", JOptionPane.ERROR_MESSAGE);
			} else {
				// image with zoom
				text[0] += "[![" + name + "](/images?imageName=" + input + ")](/images?imageName=" + input + ")\n\n";
				text[0] += "";
				// text[0] += "!["+name+"](/images?imageName="+input+")\n";
				terminal.append(text[0]);
			}
		} else if (zoom == JOptionPane.NO_OPTION) {
			if (input.endsWith(".gif")) {
				text[0] += "![" + name + "](/images?imageName=" + input + " \"gif image format\")\n\n";
				terminal.append(text[0]);
			} else {
				text[0] += "![" + name + "](/images?imageName=" + input + ")\n\n";
				terminal.append(text[0]);
			}

		}
	}

	//
	void insertVideoFun(JTextArea terminal) {
		final String[] text = { "" };
		String[] type = { "Local video", "Video Link", "Embedded YouTube" };
		String input = (String) JOptionPane.showInputDialog(null, "Choose an video from below", "Insert Video",
				JOptionPane.QUESTION_MESSAGE, null, type, type[0]);
		if (input == type[0]) { // local video option
			// get availble local video files
			String path = "src/main/resources/video";
			String[] videoFiles = { ".webm", ".mp4" };
			List<String> video = getFiles(path, videoFiles);
			String[] options = video.toArray(new String[0]);
			String vInput = (String) JOptionPane.showInputDialog(null, "Choose a video from below", "Insert Video",
					JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			int chop = vInput.indexOf(".");
			String name = vInput.substring(0, chop);
			text[0] += "[" + name + "](/videos?videoName=windChimes.mp4 \"" + name + "\")\n\n";
			terminal.append(text[0]);
		} else if (input == type[1]) { // external video link option
			String link = JOptionPane.showInputDialog(null, "Input the link to your video:");
			String name = JOptionPane.showInputDialog(null, "What's the name of the video?");
			text[0] += "[" + name + "]" + "(" + link + ")\n\n";
			terminal.append(text[0]);
		} else if (input == type[2]) {// embedded yt
			String link = JOptionPane.showInputDialog(null, "Input the link to your video:");
			String name = JOptionPane.showInputDialog(null, "What's the name of the video?");
			String moddedUrl = link.replace("www.youtube.com", "www.youtube-nocookie.com/embed/");
			text[0] += "[" + name + "]" + "(" + moddedUrl + " \"" + name + "\")\n\n";
			terminal.append(text[0]);
		}
	}

	void insertAudioFun(JTextArea terminal) {
		final String[] text = { "" };
		String[] audioFiles = { ".mp3", ".ogg", ".wav" };
		String path = "src/main/resources/audio";
		List<String> audio = getFiles(path, audioFiles);
		String[] options = audio.toArray(new String[0]);
		String input = (String) JOptionPane.showInputDialog(null, "Choose an audio from below", "Insert Audio",
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		int chop = input.lastIndexOf(".");
		String fileType = input.substring(chop + 1);
		text[0] += "<audio controls>\n";
		text[0] += "  <source src=\"/audio?audioName=" + input + "\" type=\"audio/" + fileType + "\">\n";
		text[0] += "</audio>\n\n";
		terminal.append(text[0]);
	}

	void insertTweetFun(JTextArea terminal) {
		final String[] text = { "" };
		String input = JOptionPane.showInputDialog(null,
				"Input your tweet's ID, can be found in the url of the tweet after /status/: ");
		text[0] += "<Tweet id=\"" + input + "\" />\n\n";
		terminal.append(text[0]);
	}

	void insertInputFun(JTextArea terminal) {
		final String[] text = { "" };
		text[0] += "<Code filePath=\"./src/main/resources/code/Main.java\" />\n";
		terminal.append(text[0]);
	}

}
