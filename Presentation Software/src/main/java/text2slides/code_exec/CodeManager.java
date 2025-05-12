package text2slides.code_exec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.springframework.stereotype.Service;


@Service
public class CodeManager 
{
	
    String className = "Main";
    String javaFileName = className + ".java";
    File sourceFile;
	
	public String execCode(String code)
	{
		createFiles(code);
		String programResult = "";
		
		try {
		    // call java Main in command line
		    ProcessBuilder builder = new ProcessBuilder("java", "Main");
		    builder.redirectErrorStream(true); // Redirect error stream
		    Process process = builder.start();

		    // Read output from the program
		    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		    String line;
		    while ((line = reader.readLine()) != null) {
		        programResult = programResult + line + "\n";
		    }

		    int exitCode = process.waitFor();		    
		    programResult = programResult + "Exit code: " + String.valueOf(exitCode);
		    
		} catch (IOException | InterruptedException e) {
		    e.printStackTrace();
		}
		
		deleteFiles();
		return programResult;
		
	}
	
	private void createFiles(String code)
	{
        try {
        	
        	// creating java file with code
            sourceFile = new File(javaFileName);
            FileWriter writer = new FileWriter(sourceFile);
            writer.write(code);
            writer.close();
            
            JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler(); // get sys compiler
            javaCompiler.run(null, null, null, sourceFile.getPath()); // compile code
            
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	private void deleteFiles()
	{
        try {
            
            sourceFile.delete(); // delete .java file
            new File(className + ".class").delete(); // delete .class file
            
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}