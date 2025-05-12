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

/**
 * Main class for user interface.
 */

public class MainWindow extends Thread {
	private boolean fileSaved = false;
	private File savedFile;
	private Path projectPath;
	private Inserts i;

	// file save function
	// takes in frame and terminal to save what's in terminal
	private void saveFile(JFrame frame, JTextArea terminal) {
		// if file has been saved before, save new save to referenced file
		if (fileSaved || savedFile != null) {
			try (BufferedWriter w = new BufferedWriter(new FileWriter(savedFile))) {
				w.write(terminal.getText());
				JOptionPane.showMessageDialog(frame, "File saved");
			} catch (IOException ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(frame, "File save error", "ERROR", JOptionPane.ERROR_MESSAGE);
			}
		} else {
			// normal save action (save as)
			JFileChooser chooser = new JFileChooser();
			int dialog = chooser.showSaveDialog(frame);

			if (dialog == JFileChooser.APPROVE_OPTION) {
				java.io.File file = chooser.getSelectedFile();

				try (BufferedWriter w = new BufferedWriter(new FileWriter(file))) {
					w.write(terminal.getText());
					fileSaved = true;
					savedFile = file;
					JOptionPane.showMessageDialog(frame, "File saved");
				} catch (IOException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(frame, "File save error", "ERROR", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	// file load function, uses file reader to read line by line and add it to the
	// terminal textArea
	private void loadFile(JFrame frame, JTextArea terminal) {
		String text = "";
		JFileChooser chooser = new JFileChooser();
		int dialog = chooser.showOpenDialog(frame);

		if (dialog == JFileChooser.APPROVE_OPTION) {
			java.io.File filename = chooser.getSelectedFile();
			try (BufferedReader r = new BufferedReader(new FileReader(filename))) {
				String line = r.readLine();
				while (line != null) {
					text += line + "\n";
					terminal.setText(text);
					line = r.readLine();
				}
				fileSaved = false;
				savedFile = filename;
				JOptionPane.showMessageDialog(frame, "File loaded");
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(frame, "File load error", "ERROR", JOptionPane.ERROR_MESSAGE);
			} catch (IOException ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(frame, "File load error", "ERROR", JOptionPane.ERROR_MESSAGE);
			}

		}
	}

	private void openProjectFolder(JFrame frame, JTextArea terminal) {
		JFileChooser chooser = new JFileChooser();
		final String[] text = { "" };
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int dialog = chooser.showOpenDialog(frame);
		Path path = chooser.getSelectedFile().toPath();
		if (dialog == JFileChooser.APPROVE_OPTION) {
			projectPath = path;
			File dir = projectPath.toFile();
			String[] files = dir.list();
			JList<String> fileList = new JList<>(files);

			// check files aren't empty
			if (files != null) {

				// action listener
				fileList.addListSelectionListener(e -> {
					// check if the file is being acted upon
					if (!e.getValueIsAdjusting()) {
						String selectedFile = fileList.getSelectedValue();
						// check if the file is empty
						if (selectedFile != null) {
							// check if the selectedFile is empty
							String filePath = projectPath.resolve(selectedFile).toString();
							try (BufferedReader r = new BufferedReader(new FileReader(filePath))) {
								text[0] = ""; // reset the terminal (prevents files from appending on top of each other)
								String line = r.readLine();
								while (line != null) {
									text[0] += line + "\n";
									terminal.setText(text[0]);
									line = r.readLine();
								}
							} catch (FileNotFoundException ex) {
								ex.printStackTrace();
								JOptionPane.showMessageDialog(frame, "File load error", "ERROR",
										JOptionPane.ERROR_MESSAGE);
							} catch (IOException ex) {
								ex.printStackTrace();
								JOptionPane.showMessageDialog(frame, "File load error", "ERROR",
										JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				});
				JScrollPane scrollPane = new JScrollPane(fileList);
				JOptionPane.showMessageDialog(null, scrollPane, "Project Folder", JOptionPane.PLAIN_MESSAGE);
			}
		}
	}

	SlidesProcessing slidesProc;

	public MainWindow(SlidesProcessing sp) {
		i = new Inserts();
		slidesProc = sp;
	}

	public void run() {
		JFrame frame = new JFrame("Presentation");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1000, 650);
		frame.setLayout(new BorderLayout());

		// PANEL INTIALISATION
		JPanel toolPanel = new JPanel();
		JPanel slidePanel = new JPanel();

		// TOOL PANEL
		toolPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		toolPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		toolPanel.setSize(1000, 25);
		toolPanel.setLocation(0, 0);
		toolPanel.setMaximumSize(new Dimension(25, 25));
		// toolPanel.setBackground(Color.blue);

		ImageIcon plusIcon = new ImageIcon("src/main/java/text2slides/ui/icons/plus.png");
		JButton plus = new JButton(plusIcon);
		plus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("PLUS");
			}
		});

		ImageIcon folderIcon = new ImageIcon("src/main/java/text2slides/ui/icons/folder.png");
		JButton folder = new JButton(folderIcon);
		folder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("FOLDER");
			}
		});

		ImageIcon penIcon = new ImageIcon("src/main/java/text2slides/ui/icons/pen.png");
		JButton pen = new JButton(penIcon);
		pen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("PEN");
			}
		});

		ImageIcon binIcon = new ImageIcon("src/main/java/text2slides/ui/icons/binIcon.png");
		JButton bin = new JButton(binIcon);

		bin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("BIN");
			}
		});

		// toolPanel.add(plus);
		// toolPanel.add(folder);
		// toolPanel.add(pen);
		// toolPanel.add(bin);

		// SLIDE PANEL
		slidePanel.setBorder(BorderFactory.createLineBorder(Color.black));
		slidePanel.setSize(1000, 625);
		slidePanel.setLocation(0, 25);
		// slidePanel.setBackground(Color.green);

		// TERMINAL TEXT AREA
		JTextArea terminal = new JTextArea(35, 80);
		terminal.setLocation(50, 100);
		terminal.setBorder(BorderFactory.createLineBorder(Color.black));

		JScrollPane scroll = new JScrollPane(terminal);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		slidePanel.add(scroll);

		/************************************************************************************************************/

		// MENU INIALISATION
		JMenuBar menuBar = new JMenuBar();

		JMenu file = new JMenu("File");

		JMenuItem New = new JMenuItem("New");
		New.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!fileSaved && terminal.getText().trim().length() > 0) {
					int result = JOptionPane.showConfirmDialog(frame,
							"do you want to save this current file before creating a new one", "Save Confirmation",
							JOptionPane.YES_NO_OPTION);
					if (result == JOptionPane.YES_OPTION) {
						saveFile(frame, terminal);
					} else if (result == JOptionPane.NO_OPTION || result == JOptionPane.CLOSED_OPTION) {
						terminal.setText("");
						fileSaved = false;
						savedFile = null;
						return;
					}
				}
				terminal.setText("");
				fileSaved = false;
				savedFile = null;
			}
		});

		JMenuItem open = new JMenuItem("Open");
		open.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadFile(frame, terminal);
			}
		});

		JMenuItem save = new JMenuItem("Save");
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveFile(frame, terminal);
			}
		});

		JMenuItem export = new JMenuItem("Export");
		export.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Path route = Paths.get("src/main/resources/templates/index.html");
				JFileChooser chooser = new JFileChooser();
				chooser.setSelectedFile(route.toFile());
				int dialog = chooser.showSaveDialog(frame);

				if (dialog == JFileChooser.APPROVE_OPTION) {
					java.io.File file = chooser.getSelectedFile();

					try (BufferedWriter w = new BufferedWriter(new FileWriter(file))) {
						// copy the whole of index.html, wouldn't work if i didn't copy every byte
						String content = new String(Files.readAllBytes(route));
						// truncate_existing removes exception error of the file already existing
						Files.write(file.toPath(), content.getBytes(), StandardOpenOption.CREATE,
								StandardOpenOption.TRUNCATE_EXISTING);
						JOptionPane.showMessageDialog(frame, "File exported");
					} catch (IOException ex) {
						ex.printStackTrace();
						JOptionPane.showMessageDialog(frame, "File export error", "ERROR", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});

		JMenuItem openProject = new JMenuItem("Open Project");
		openProject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openProjectFolder(frame, terminal);
			}
		});

		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});

		file.add(New);
		file.add(open);
		file.add(save);
		file.add(export);
		file.add(openProject);
		file.add(exit);

		JMenu edit = new JMenu("Edit");

		JMenuItem insertImage = new JMenuItem("Insert Image");
		insertImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				i.insertImageFun(frame, terminal);
			}
		});

		JMenuItem insertInput = new JMenuItem("Insert Input");
		insertInput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				i.insertInputFun(terminal);
			}
		});

		JMenuItem insertAudio = new JMenuItem("Insert Audio");
		insertAudio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				i.insertAudioFun(terminal);
			}
		});

		JMenuItem insertVideo = new JMenuItem("Insert Video");
		insertVideo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				i.insertVideoFun(terminal);
			}
		});

		JMenuItem insertTweet = new JMenuItem("Insert Tweet");
		insertTweet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				i.insertTweetFun(terminal);
			}
		});

		edit.add(insertImage);
		edit.add(insertVideo);
		edit.add(insertAudio);
		edit.add(insertTweet);
		edit.add(insertInput);

		JMenu tools = new JMenu("Tools");

		JMenuItem executionSettings = new JMenuItem("Execution Settings");
		executionSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("EXECUTION SETTINGS");
			}
		});

		tools.add(executionSettings);

		JMenu help = new JMenu("Help");

		JMenuItem markdownGuideline = new JMenuItem("Markdown Guidelines");
		markdownGuideline.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("MARKDOWN GUIDLINES");
			}
		});

		JMenuItem aboutUs = new JMenuItem("About Us");
		aboutUs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("ABOUT US");
			}
		});

		help.add(markdownGuideline);
		help.add(aboutUs);

		menuBar.add(file);
		menuBar.add(edit);
		menuBar.add(tools);
		menuBar.add(help);

		terminal.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateSlides();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateSlides();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}

			private void updateSlides() {
				String inputText = terminal.getText();
				slidesProc.render(inputText);
			}
		});

		frame.setJMenuBar(menuBar);
		frame.add(slidePanel);
		frame.add(toolPanel, BorderLayout.PAGE_START);

		frame.setVisible(true);
		frame.setResizable(true);
	}

}

/*
 * TO DO: - write ation lcistener functionality - the inserts - execution
 * settings - markdown (tutorial) - about us
 * 
 */

/*
 * COMMIT NOTES - added open project folder - opens a side window with list of
 * files in a directory - when you select the file in the list it loads it's
 * contents into the terminal - uses parts of loadFile method
 * 
 */
