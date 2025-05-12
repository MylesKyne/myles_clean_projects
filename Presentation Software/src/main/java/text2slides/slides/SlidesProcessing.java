package text2slides.slides;

import text2slides.utils.FileHelper;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Class for slides processing. Should use commonmark lib for parsing and
 * rendering text desctiption of slides.
 */

@Service
public class SlidesProcessing {

	private String pageBody;
	private String updatePageScript;
	private String pageLogicScript;
	private String pageStyle;

	public SlidesProcessing() {
		try {

			pageBody = Files.readString(Path.of("./src/main/java/text2slides/slides/js/pageBody.html"));
			updatePageScript = applyTags(
					Files.readString(Path.of("./src/main/java/text2slides/slides/js/pageUpdateScript.js")));
			pageLogicScript = applyTags(
					Files.readString(Path.of("./src/main/java/text2slides/slides/js/pageLogicScript.js")));
			pageStyle = applyTags(Files.readString(Path.of("./src/main/java/text2slides/slides/js/pageStyle_1.css")));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String applyTags(String script) {
		script = script.replace("//<script>", "<script>");
		script = script.replace("//</script>", "</script>");

		script = script.replace("//<body>", "<body>");
		script = script.replace("//</body>", "</body>");

		script = script.replace("//<head>", "<head>");
		script = script.replace("//</head>", "</head>");

		script = script.replace("//<html>", "<html>");
		script = script.replace("//</html>", "</html>");

		script = script.replace("//<style>", "<style>");
		script = script.replace("//</style>", "</style>");

		return script;
	}

	private String addScript(String rawStr, int maxPageNum) {

		String pageLogic = this.pageLogicScript;
		pageLogic = pageLogic.replace("//MAXPAGENUM", String.valueOf(maxPageNum));
		return this.pageBody + rawStr + this.pageStyle + pageLogic + this.updatePageScript;
	}

	private int getPagesNum(String rawStr) // return total page numbers in text
	{
		String[] parts = rawStr.split("<hr />");
		return parts.length;
	}

	private String separatePages(String rawStr) // TODO try to rewrite using commonmark data structures
	{
		// separate html document to pages.
		String[] parts = rawStr.split("<hr />");
		if (parts.length == 0) {
			return rawStr;
		}
		// Store the result
		StringBuilder resultBuilder = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			if (i == 0) {
				parts[i] = "<div id=\"page1\" class=\"page active\">\n" + parts[i] + "</div>\n";
				resultBuilder.append(parts[i]);
			} else {
				int counter = i + 1;
				String layout = extractLayout(parts[i]);
				String imageUrl = extractImageUrl(parts[i]);
				String content = extractContent(parts[i]);
				content = embedYoutubeVideo(content);
				content = embedTweets(content);
				content = embedCode(content);
				parts[i] = String.format(
						"<div id=\"page%d\" class=\"page\" layout=\"%s\" background=\"%s\">\n%s</div>\n", counter,
						layout, imageUrl, content);
				resultBuilder.append(parts[i]);
			}
		}
		return resultBuilder.toString();
	}

	// Get the text layout
	private String extractLayout(String slideContent) {
		int startIndex = slideContent.indexOf("layout: ");
		if (startIndex != -1) {
			int endIndex = slideContent.indexOf("\n", startIndex);
			if (endIndex != -1) {
				return slideContent.substring(startIndex + "layout: ".length(), endIndex).trim();
			}
		}
		return "";
	}

	// Get the image url
	private String extractImageUrl(String slideContent) {
		int startIndex = slideContent.indexOf("background: ");
		if (startIndex != -1) {
			int endIndex = slideContent.indexOf("</h2>", startIndex);
			if (endIndex != -1) {
				return slideContent.substring(startIndex + "background: ".length(), endIndex).trim();
			}
		}
		return "";
	}

	// Get the content exludes the words "layout: " + input and "background: " +
	// input
	private String extractContent(String input) {
		String layout = extractLayout(input);
		String imageUrl = extractImageUrl(input);

		String layoutContent = "layout:\\s*" + layout;
		String backgroundContent = "background:\\s*" + imageUrl;

		String content = input.replaceAll(layoutContent, "").replaceAll(backgroundContent, "");

		return content.trim();
	}

	// Embed youtube videos
	private String embedYoutubeVideo(String content) {
		Pattern youtubePattern = Pattern.compile("\\<Youtube id = \"([^\"]+)\" \\/\\>"); // Define a regular expression
																							// pattern
		Matcher matcher = youtubePattern.matcher(content); // Match the pattern and the input

		while (matcher.find()) {
			String videoId = matcher.group(1); // Get id
			// Embed youtube video code
			String embeddedVideo = String.format(
					"<iframe src=\"https://www.youtube.com/embed/%s\" frameborder=\"0\" allowfullscreen></iframe>",
					videoId);
			// Replace the matched Youtube tag with the generated embedded video code in the
			// content
			content = content.replace(matcher.group(), embeddedVideo);
		}

		return content;
	}

	// Embed tweets
	private String embedTweets(String content) {
		Pattern tweetPattern = Pattern.compile("<Tweet id=\"([^\"]+)\" \\/>");
		Matcher matcher = tweetPattern.matcher(content);

		while (matcher.find()) {
			String tweetId = matcher.group(1);
			String embeddedTweet = String.format(
					"<blockquote class=\"twitter-tweet\"><p lang=\"en\" dir=\"ltr\"><a href=\"https://twitter.com/Twitter/status/%s\"></a></p></blockquote><script async src=\"https://platform.twitter.com/widgets.js\" charset=\"utf-8\"></script>",
					tweetId);
			content = content.replace(matcher.group(), embeddedTweet);
		}

		return content;
	}

	// Embed code compilation
	private String embedCode(String content) {
		Pattern codePattern = Pattern.compile("<Code filePath=\"([^\"]+)\" \\/>");
		Matcher matcher = codePattern.matcher(content);

		while (matcher.find()) {
			String filePath = matcher.group(1);
			String temp = matcher.group(0);

			String codeOnSlide = "Error! Wrong path to java file!";
			String codeForm = "";
			try {

				codeOnSlide = Files.readString(Path.of(filePath));
				codeForm = Files.readString(Path.of("./src/main/java/text2slides/slides/js/codeForm.html"));

				codeForm = codeForm.replace("{CODE}", codeOnSlide); // set code on slide

				int randomCodeFormID = (int) (Math.random() * (100000 - 2 + 1)) + 2;
				codeForm = codeForm.replace("{ID}", String.valueOf(randomCodeFormID));

			} catch (IOException e) {
				e.printStackTrace();
			}
			content = content.replace(matcher.group(), codeForm);
		}

		return content;
	}

	public void render(String slidesTextData) {
		Parser parser = Parser.builder().build();
		HtmlRenderer renderer = HtmlRenderer.builder().build();

		Node document = parser.parse(slidesTextData);
		String result = renderer.render(document);

		int pagesNum = getPagesNum(result);
		result = separatePages(result);
		FileHelper.saveFile("./src/main/resources/templates/index.html", addScript(result, pagesNum));
	}
}
