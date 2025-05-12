package text2slides;

import text2slides.ui.MainWindow;
import text2slides.server.SpringServer;
import text2slides.slides.SlidesProcessing;
import text2slides.code_exec.CodeManager;

public class Text2slidesApplication {

	public static void main(String[] args) {

		SlidesProcessing sp = new SlidesProcessing();
		SpringServer ss = new SpringServer();
		CodeManager cm = new CodeManager();

		ss.addSlidesProcessing(sp);
		ss.addCodeManager(cm);

		MainWindow win = new MainWindow(sp);
		win.start();

		ss.startServer(args);
	}
}