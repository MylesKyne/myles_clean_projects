import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Coursework {




    public static Connection CreateConnection()//Connection creation function.. Pass by reference!
	{
		String jdbcUrl = "jdbc:mysql://localhost:3306/";
		Connection connection = null;
		try
		{ 
			connection = DriverManager.getConnection(jdbcUrl);
		}
		catch (SQLException e) 
		{
		    e.printStackTrace();
		}		
		return connection;
	}

	public void reader(String fileName)
	{
        try (BufferedReader br = new BufferedReader(new FileReader(fileName)))
        {
            String line;
            List<String[]> allRows = new ArrayList<>();

            // Read each line from the CSV file
            while ((line = br.readLine()) != null) {
                // Split the line into an array of values using a comma as the delimiter
                String[] row = line.split(",");
                allRows.add(row);
            }

            // Assuming the first row contains column headers
            String[] headers = allRows.get(0);

            // Create arrays for each column dynamically
            int numColumns = headers.length;
            String[][] dataArrays = new String[numColumns][];

            // Initialize arrays
            for (int i = 0; i < numColumns; i++) {
                dataArrays[i] = new String[allRows.size()];
            }

            // Populate arrays with data
            for (int i = 1; i < allRows.size(); i++) {
                String[] row = allRows.get(i);
                for (int j = 0; j < numColumns; j++) {
                    dataArrays[j][i - 1] = row[j];
                }
            }
            
            
            populateMathTables(dataArrays,numColumns,allRows.size());


            
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

    public static void populateMathTables(String [][] data, int c, int r) {
        Connection connection = CreateConnection();
        String databaseName = "Maths";



        try{
         

            Statement statement = connection.createStatement();
            PreparedStatement insertStudentStmt = connection.prepareStatement("INSERT IGNORE INTO Student (StudentID, SFirstName, SSurname, Gender, CourseID) VALUES (?, ?, ?, ?, ?)");
            PreparedStatement insertCourseStmt = connection.prepareStatement("INSERT IGNORE INTO Courses (CourseID, Title, Credits, YearGroup, TeacherID) VALUES (?, ?, ?, ?, ?)");
            PreparedStatement insertTeacherStmt = connection.prepareStatement("INSERT IGNORE INTO Teacher (TeacherID, TTitle, TFirstName, TSurname, Gender) VALUES (?, ?, ?, ?, ?)");
            String UseDatabase = "use " + databaseName;//Select the database..
		    statement.executeUpdate(UseDatabase);
            for (int i = 0; i < r - 1; i++) {
                
                
                insertTeacherStmt.setInt(1, Integer.parseInt(data[8][i]));
                insertTeacherStmt.setString(2, data[9][i]);
                insertTeacherStmt.setString(3, data[10][i]);
                insertTeacherStmt.setString(4, data[11][i]);
                insertTeacherStmt.setString(5, data[12][i]);
                insertTeacherStmt.executeUpdate();

                insertCourseStmt.setInt(1, Integer.parseInt(data[4][i]));
                insertCourseStmt.setString(2, data[5][i]);
                insertCourseStmt.setInt(3, Integer.parseInt(data[6][i]));
                insertCourseStmt.setInt(4, Integer.parseInt(data[7][i]));
                insertCourseStmt.setInt(5, Integer.parseInt(data[8][i]));
                insertCourseStmt.executeUpdate();

                insertStudentStmt.setInt(1, Integer.parseInt(data[0][i]));
                insertStudentStmt.setString(2, data[1][i]);
                insertStudentStmt.setString(3, data[2][i]);
                insertStudentStmt.setString(4, data[3][i]);
                insertStudentStmt.setInt(5, Integer.parseInt(data[4][i]));
                insertStudentStmt.executeUpdate();
        }
        

        System.out.println("Database for Student 38650452 has been populated..");
    
    connection.close();
        } catch (SQLException e)
    {
        e.printStackTrace();
    }
    finally 
		{
		    if (connection != null) 
		    {
		        try 
		        {
		        	connection.close();
		        } 
		        catch (SQLException e) { /* Ignored */}
		    }

        }

    }

    public static void myMathQueries() {
        String jdbcUrl = "jdbc:mysql://localhost:3306/"; // Update with your database URL
        String databaseName = "Maths";
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
            Statement statement = connection.createStatement()) {
            
                String UseDatabase = "use " + databaseName;
                statement.executeUpdate(UseDatabase);

                String queryA = "SELECT s.SFirstName, s.SSurname " +
                                "FROM Student s " +
                                "JOIN Courses c ON s.CourseID = c.CourseID " +
                                "JOIN Teacher t ON c.TeacherID = t.TeacherID " +
                                "WHERE s.Gender = 'M' " +
                                "AND c.Credits = 15 " +
                                "AND t.Gender = 'F' " +
                                "AND t.TTitle = 'Dr' " +
                                "GROUP BY s.SFirstName, s.SSurname " +
                                "HAVING COUNT(DISTINCT t.TeacherID) = 1";
                Statement qA = connection.createStatement();

                ResultSet qARes = qA.executeQuery(queryA);
                if(qARes.next()==false)
		        {
		    	System.out.println("Result for the first query is empty");
		        }
		        else//process the result set.
		        {
                    String LastName = qARes.getString(2);
		    	    String FirstName = qARes.getString(1);
		    	    System.out.println("Male Students Doing a course worth 15 credits taught by a female teacher with the title Dr:");
		    	    System.out.println(FirstName + " " + LastName);
		    	    while(qARes.next())
		    	    {
                        LastName = qARes.getString(2);
			    	    FirstName = qARes.getString(1);
			    	    System.out.println(FirstName + " " + LastName);
		    	    }
		        }



                //Query B
                String queryB = "SELECT s.SFirstName, s.SSurname " +
                                "FROM Student s " +
                                "INNER JOIN Courses c ON s.CourseID = c.CourseID " +
                                "INNER JOIN Teacher t ON c.TeacherID = t.TeacherID " +
                                "WHERE s.Gender = 'F' AND t.Gender = 'F' AND t.TTitle = 'Prof' " +
                                "GROUP BY s.SFirstName, s.SSurname " +
                                "HAVING COUNT(DISTINCT t.TeacherID) = 1";


                Statement qB = connection.createStatement();

                ResultSet qBRes = qB.executeQuery(queryB);
                if(qBRes.next()==false)
                {
                System.out.println("2nd Query empty");

                }
                else 
                {
                    String LastName = qBRes.getString(2);
		    	    String FirstName = qBRes.getString(1);
                    System.out.println(" ");
		    	    System.out.println("Female students taught by one female teacher with the title Prof:");
		    	    System.out.println(FirstName + " " + LastName);
		    	    while(qBRes.next())
		    	    {
                        LastName = qBRes.getString(2);
			    	    FirstName = qBRes.getString(1);
			    	    System.out.println(FirstName + " " + LastName);
		    	    }

                }

                //Query C
                String deleteQueryA = "DELETE FROM Teacher WHERE Gender = 'M'";
                int rowsAffected = statement.executeUpdate(deleteQueryA);
                System.out.println(" ");
                System.out.println("Rows affected: " + rowsAffected);
                
              

                connection.close();
            } catch (SQLException e) {
                System.out.println("Error deleting rows: " + e.getMessage());
            }

            try (Connection connection = DriverManager.getConnection(jdbcUrl);
            Statement statement = connection.createStatement()) {
            
                String UseDatabase = "use " + databaseName;
                statement.executeUpdate(UseDatabase);

                //Query D 
                String deleteQueryB = "DELETE FROM Courses WHERE Title = 'Calculus'";
                int rowsAffectedB = statement.executeUpdate(deleteQueryB);

                System.out.println("Rows affected: " + rowsAffectedB);

                connection.close();
            } catch (SQLException e) {
                System.out.println("Error deleting rows: " + e.getMessage());
            }
            }


    public static void createMathDatabaseAndTables() {
        String jdbcUrl = "jdbc:mysql://localhost:3306/"; // Update with your database URL
        String databaseName = "Maths";
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement()) {

            // Create the database if it doesn't exist
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + databaseName);
            System.out.println("Database for Student 38650452 has been created...");

            // Select the database
            statement.executeUpdate("USE " + databaseName);



            
        
            // Create Teacher table
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS Teacher ("
                    + "TeacherID INT NOT NULL, "
                    + "TTitle VARCHAR(20), "
                    + "TFirstName VARCHAR(255), "
                    + "TSurname VARCHAR(255), "
                    + "Gender VARCHAR(20), "
                    + "PRIMARY KEY (TeacherID))"
                    );

            // Create Courses table
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS Courses("
                    + "CourseID INT NOT NULL, "
                    + "Title VARCHAR(255), "
                    + "Credits INT, "
                    + "YearGroup INT, "
                    + "TeacherID INT NOT NULL, "
                    + "PRIMARY KEY(CourseID), "
                    + "FOREIGN KEY (TeacherID) REFERENCES Teacher(TeacherID) ON DELETE RESTRICT)"
                    );
         
            // Create Student table
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS Student ("
                    + "StudentID INT NOT NULL, "
                    + "SFirstName VARCHAR(255), "
                    + "SSurname VARCHAR(255), "
                    + "Gender VARCHAR(20), "
                    + "CourseID INT NOT NULL, "
                    + "PRIMARY KEY (StudentID), "
                    + "FOREIGN KEY (CourseID) REFERENCES Courses(CourseID) ON DELETE RESTRICT)"
                    );
            

            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        // Replace "your_file.csv" with the path to your CSV file
        createMathDatabaseAndTables();
        String CSVFile = "38650452.csv";
        
        Coursework Read = new Coursework();
        Read.reader(CSVFile);




        myMathQueries();
    }


}
