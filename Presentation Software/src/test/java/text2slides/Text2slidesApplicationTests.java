package text2slides;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import text2slides.ui.MainWindow;
import text2slides.slides.SlidesProcessing;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.JTextArea;

import text2slides.code_exec.CodeManager;
import text2slides.ui.Inserts;
import text2slides.utils.FileHelper;
import text2slides.server.SpringServer;

/*
 * @SpringBootTest class Text2slidesApplicationTests {
 * 
 * @Test void contextLoads() { }
 * 
 * }
 */

public class Text2slidesApplicationTests {

	@Test
	void testUIInitialization() {
		SlidesProcessing sp = new SlidesProcessing(); // Assuming a default constructor or mockable setup
		MainWindow mainWindow = new MainWindow(sp);
		assertDoesNotThrow(mainWindow::run, "MainWindow run() method should not throw any exceptions");

	}

	@Test
	void testGetImage() {
		SpringServer springServer = new SpringServer();

		// Test for existing image
		ResponseEntity<byte[]> response = springServer.getImage("halfPizza.png");
		assertNotNull(response.getBody(), "Response body should not be null");

		// Test for non-existing image
		response = springServer.getImage("non_existing_image.jpg");
		assertNull(response.getBody(), "Response body should be null");
	}

	@Test
	void testGetVideo() {
		SpringServer springServer = new SpringServer();

		// Test for existing video
		ResponseEntity<byte[]> response = springServer.getVideo("windChimes.mp4");
		assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP status should be OK");
		assertNotNull(response.getBody(), "Response body should not be null");

		// Test for non-existing video
		response = springServer.getVideo("non_existing_video.mp4");
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(),
				"HTTP status should be Internal Server Error");
		assertNull(response.getBody(), "Response body should be null");
	}

	@Test
	void testGetAudio() {
		SpringServer springServer = new SpringServer();

		// Test for existing audio
		ResponseEntity<byte[]> response = springServer.getAudio("car-horn.mp3");
		assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP status should be OK");
		assertNotNull(response.getBody(), "Response body should not be null");

		// Test for non-existing audio
		response = springServer.getAudio("non_existing_audio.mp3");
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(),
				"HTTP status should be Internal Server Error");
		assertNull(response.getBody(), "Response body should be null");
	}

	@Test
	void testReadFile() {
		String expectedContent = "Test content";
		String filePath = "test.txt";

		// Write test content to a file
		try {
			Files.write(Paths.get(filePath), expectedContent.getBytes());
		} catch (Exception e) {
			fail("Failed to write test file: " + e.getMessage());
		}

		// Call the readFile method and verify the content
		String actualContent = FileHelper.readFile(filePath);
		assertEquals(expectedContent, actualContent, "File content should match expected content");

		// Clean up: delete the test file
		new File(filePath).delete();
	}

	@Test
	void testSaveFile() {
		String filePath = "test.txt";
		String expectedContent = "Test content";

		// Call the saveFile method to save content to a file
		FileHelper.saveFile(filePath, expectedContent);

		// Read the saved file and verify the content
		try {
			String actualContent = new String(Files.readAllBytes(Paths.get(filePath)));
			assertEquals(expectedContent, actualContent, "File content should match saved content");
		} catch (Exception e) {
			fail("Failed to read test file: " + e.getMessage());
		}

		// Clean up: delete the test file
		new File(filePath).delete();
	}

	@Test
    public void testExecCodeWithValidCode() {
        CodeManager codeManager = new CodeManager();
        String code = "public class Main { public static void main(String[] args) { System.out.println(\"Hello, World!\"); } }";
        String expectedOutput = "Hello, World!\nExit code: 0";

        String result = codeManager.execCode(code);

        assertEquals(expectedOutput, result);
    }



	public class SpringServerTests {

		// Initialize a SpringServer instance for testing
		SpringServer springServer = new SpringServer();
	
		@Test
		void testIndexPageRetrieval() {
			// Test retrieving the index page with a specified active page number
			String indexPage = springServer.indexPage("1");
			assertNotNull(indexPage, "Index page content should not be null");
			// Add more assertions to validate the content of the index page if needed
		}
	
		@Test
		void testCodeExecutionEndpoint() {
			// Test sending valid code and receiving the expected response
			String validCode = "System.out.println(\"Hello, World!\");";
			ResponseEntity<?> response = springServer.receiveData("{\"text\": \"" + validCode + "\"}");
			assertNotNull(response, "Response entity should not be null");
			assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP status should be OK");
			// Add more assertions to validate the response content if needed
		}
	
		@Test
		void testImageRetrievalEndpoint() {
			// Test retrieving an existing image
			ResponseEntity<byte[]> imageResponse = springServer.getImage("existing_image.png");
			assertNotNull(imageResponse, "Image response entity should not be null");
			assertEquals(HttpStatus.OK, imageResponse.getStatusCode(), "HTTP status should be OK");
			// Add more assertions to validate the image content and headers if needed
		}
	
		@Test
		void testVideoRetrievalEndpoint() {
			// Test retrieving an existing video
			ResponseEntity<byte[]> videoResponse = springServer.getVideo("existing_video.mp4");
			assertNotNull(videoResponse, "Video response entity should not be null");
			assertEquals(HttpStatus.OK, videoResponse.getStatusCode(), "HTTP status should be OK");
			// Add more assertions to validate the video content and headers if needed
		}
	
		@Test
		void testAudioRetrievalEndpoint() {
			// Test retrieving an existing audio file
			ResponseEntity<byte[]> audioResponse = springServer.getAudio("existing_audio.mp3");
			assertNotNull(audioResponse, "Audio response entity should not be null");
			assertEquals(HttpStatus.OK, audioResponse.getStatusCode(), "HTTP status should be OK");
			// Add more assertions to validate the audio content and headers if needed
		}
	}


}
