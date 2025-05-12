package text2slides.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

/**
 * Helper class to work with files.
 */

public class FileHelper {
	public static String readFile(String path) {
		byte[] encoded = null;

		try {
			encoded = Files.readAllBytes(Paths.get(path));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new String(encoded, StandardCharsets.UTF_8);
	}

	public static void saveFile(String path, String data) {
		try {
			File file = new File(path);
			FileWriter writer = new FileWriter(file);
			writer.write(data);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}