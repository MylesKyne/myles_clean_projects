import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.util.*;
import java.util.Random;

/**
 * This class provides a graphical user interface to a model of the solar system
 * @author Joe Finney
 */
public class SolarSystem extends JFrame 
{
	private int width = 300;
	private int height = 300;
    private boolean exiting = false;
    private Map<RenderingHints.Key, Object> renderingHints;

	private ArrayList<SolarObject> things = new ArrayList<SolarObject>();

	/**
	 * Create a view of the Solar System.
	 * Once an instance of the SolarSystem class is created,
	 * a window of the appropriate size is displayed, and
	 * objects can be displayed in the solar system
	 *
	 * @param width the width of the window in pixels.
	 * @param height the height of the window in pixels.
	 */
	public SolarSystem(int width, int height)
	{
		
		
		
		
		this.width = width;
		this.height = height;

		this.setTitle("The Solar System");
		this.setSize(width, height);
		this.setBackground(Color.BLACK);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);		
        
        renderingHints = new HashMap<>();
		renderingHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		renderingHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		renderingHints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		renderingHints.put(RenderingHints.KEY_FRACTIONALMETRICS,RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		renderingHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		renderingHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		renderingHints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		renderingHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
	}




	/**
	 * A method called by the operating system to draw onto the screen - <p><B>YOU DO NOT (AND SHOULD NOT) NEED TO CALL THIS METHOD.</b></p>
	 */
	public void paint (Graphics gr)
	{
		if (renderingHints == null)
			return;

		BufferedImage i = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = i.createGraphics();
        Graphics2D window = (Graphics2D) gr;
        g.setRenderingHints(renderingHints);
        window.setRenderingHints(renderingHints);
		
		synchronized (this)
		{
			if (!this.exiting)
			{
				g.clearRect(0,0,width,height);
				for(SolarObject t : things)
				{
					g.setColor(t.col);
					g.fillOval(t.x, t.y, t.diameter, t.diameter);

					//try{ Thread.sleep(0); } catch (Exception e) {} 
				}
			}
			
			window.drawImage(i, 0, 0, this);
		}
	}

	//
	// Shouldn't really handle colour this way, but the student's haven't been introduced
	// to constants properly yet, and Color.getColor() doesn't seem to work... hmmm....
	// 
	private Color getColourFromString(String col)
	{
		Color color;
		
		if (col.charAt(0) == '#')
		{
			color = new Color(
    	        Integer.valueOf( col.substring( 1, 3 ), 16 ),
        	    Integer.valueOf( col.substring( 3, 5 ), 16 ),
            	Integer.valueOf( col.substring( 5, 7 ), 16 ) );
		} 
		else 
		{
			try 
			{
				java.lang.reflect.Field field = Color.class.getField(col);
				color = (Color)field.get(null);
			} catch (Exception e) {
				color = Color.WHITE;
			}
		}
		return color;
	}
	
	/**
	 * Draws a round shape in the window at the given co-ordinates that represents an object in the solar system.
	 * The SolarSystem class uses <i>Polar Co-ordinates</i> to represent the location
	 * of objects in the solar system.
	 *
	 * @param distance the distance from the sun to the object.
	 * @param angle the angle (in degrees) that represents how far the planet is around its orbit of the sun.
	 * @param diameter the size of the object.
	 * @param col the colour of this object, as a string. Case insentive. <p>One of: BLACK, BLUE, CYAN, DARK_GRAY, GRAY, GREEN, LIGHT_GRAY, 
	 * MAGENTA, ORANGE, PINK, RED, WHITE, YELLOW. Alternatively, a 24 bit hexadecimal string representation of an RGB colour is also accepted, e.g. "#FF0000"</p>
	 */
	public void drawSolarObject(double distance, double angle, double diameter, String col)
	{
		Color colour = this.getColourFromString(col);
		double centreOfRotationX = ((double) width) / 2.0; 
		double centreOfRotationY = ((double) height) / 2.0; 

		double rads = Math.toRadians(angle);
		double x = (int) (centreOfRotationX + distance * Math.sin(rads)) - diameter / 2;
		double y = (int) (centreOfRotationY + distance * Math.cos(rads)) - diameter / 2;

		synchronized (this)
		{
			if (things.size() > 1000)
			{
				System.out.println("\n\n");
				System.out.println(" ********************************************************* ");
				System.out.println(" ***** Only 1000 Entities Supported per Solar System ***** ");
				System.out.println(" ********************************************************* ");
				System.out.println("\n\n");
				System.out.println("If you are't trying to add this many things");
				System.out.println("to your SolarSystem, then you have probably");
				System.out.println("forgotten to call the finishedDrawing() method");
				System.out.println("See the JavaDOC documentation for more information");
				System.out.println("\n-- Joe");
				System.out.println("\n\n");

				this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
			}
			else
			{
				SolarObject t = new SolarObject((int)x, (int)y, (int)diameter, colour);
				things.add(t);
			}
		}
	}

	/**
	 * Draws a round shape in the window at the given co-ordinates. 
	 * The SolarSystem class uses <i>Polar Co-ordinates</i> to represent the location
	 * of objects in the solar system. This method operates in the same fashion as drawSolarObject, but
	 * provides additional co-ordinates to allow the programmer to use an arbitrary point about which
	 * the object orbits (e.g. a planet rather than the sun).
	 *
	 * @param distance the distance from this object to the point about which it is orbiting.
	 * @param angle the angle (in degrees) that represents how far the object is around its orbit.
	 * @param diameter the size of the object.
	 * @param col the colour of this object, as a string. Case insentive. <p>One of: BLACK, BLUE, CYAN, DARK_GRAY, GRAY, GREEN, LIGHT_GRAY, 
	 * MAGENTA, ORANGE, PINK, RED, WHITE, YELLOW</p>
	 * @param centreOfRotationDistance the distance part of the polar co-ordinate about which this object orbits.
	 * @param centreOfRotationAngle the angular part of the polar co-ordinate about which this object orbits.
	 */
	public void drawSolarObjectAbout(double distance, double angle, double diameter, String col, double centreOfRotationDistance, double centreOfRotationAngle)
	{
		Color colour = this.getColourFromString(col);
		double centrerads = Math.toRadians(centreOfRotationAngle);
		double centreOfRotationX = (((double) width) / 2.0) + centreOfRotationDistance * Math.sin(centrerads); 
		double centreOfRotationY = (((double) height) / 2.0) + centreOfRotationDistance * Math.cos(centrerads); 

		double rads = Math.toRadians(angle);
		double x = (int) (centreOfRotationX + distance * Math.sin(rads)) - diameter / 2;
		double y = (int) (centreOfRotationY + distance * Math.cos(rads)) - diameter / 2;

		synchronized (this)
		{
			if (things.size() > 1000)
			{
				System.out.println("\n\n");
				System.out.println(" ********************************************************* ");
				System.out.println(" ***** Only 1000 Entities Supported per Solar System ***** ");
				System.out.println(" ********************************************************* ");
				System.out.println("\n\n");
				System.out.println("If you are't trying to add this many things");
				System.out.println("to your SolarSystem, then you have probably");
				System.out.println("forgotten to call the finishedDrawing() method");
				System.out.println("See the JavaDOC documentation for more information");
				System.out.println("\n-- Joe");
				System.out.println("\n\n");

				this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
			}
			else
			{
				SolarObject t = new SolarObject((int)x, (int)y, (int)diameter, colour);
				things.add(t);
			}
		}
	}

	/**
     * Updates the window to show all objects that have recently been drawn using
     * drawSolarObject() or drawSolarObjectAbout().
     * 
     * The method also waits for 20 milliseconds (1/50th of one second) and then
     * clears the screen.
	 */
	public void finishedDrawing()
	{
		try
		{
			this.repaint();
			Thread.sleep(20);
			synchronized (this)
			{
				things.clear();
			}
		}
		catch (Exception e) { }
	}
	
	private class SolarObject 
	{
		public int x;
		public int y;
		public int diameter;
		public Color col;


		/**
		 * Implements the Getter methods into the SolarObject classv
		 * 
		 */
		public int getX(){
			return x;
		}
		
		public int getY() {

			return y;
		}

		public SolarObject(int x, int y, int diameter, Color col)
		{
			this.x = x;
			this.y = y;
			this.diameter = diameter;
			this.col = col;
		}


	}
		
		
		public static void main(String[] args)
		{
			/**
			 * creates solarsystem frame window
			 */
		SolarSystem window = new SolarSystem(1000, 1000); 
		/**
		 * creates all the objects of the planets along with their corresponding moons 
		 * as well as the sun in the centre
		 */
		SolarObject sun = window.new Sun(0, 0, 100, Color.YELLOW);
		
		Planet Neptune = window.new Planet(0, 0, 30, Color.BLUE, 450, 0, sun, 0.02);
		Moon triton = window.new Moon(0, 0, 5, Color.PINK, 45, 0, Neptune, 0.5);

		Planet Uranus = window.new Planet(0, 0, 30, Color.BLUE, 370, 0, sun, 0.03);
		Moon titania = window.new Moon(0, 0, 5, Color.PINK, 35, 0, Uranus, 0.4);
		Moon oberon = window.new Moon(0, 0, 5, Color.RED, 30, 0, Uranus, 0.3);

		Planet Saturn = window.new Planet(0, 0, 50, Color.YELLOW, 310, 0, sun, 0.05);
		Moon titan = window.new Moon(0, 0, 5, Color.ORANGE, 38, 0, Saturn, 0.4);

		Planet Jupiter = window.new Planet(0, 0, 60, Color.ORANGE, 240, 0, sun, 0.1);
		Moon Io = window.new Moon(0, 0, 5, Color.GREEN, 40, 0, Jupiter, 0.4);
		Moon Europa = window.new Moon(0, 0, 5, Color.WHITE, 50, 0, Jupiter, 0.2);
		Moon Ganymede = window.new Moon(0, 0, 5, Color.ORANGE, 55, 0, Jupiter, 0.6);
        Moon Callisto = window.new Moon(0, 0, 5, Color.ORANGE, 60, 0, Jupiter, 0.7);




		Planet Mars = window.new Planet(0, 0, 10, Color.RED, 175, 0, sun, 0.4);
		Moon Phobos = window.new Moon(0, 0, 5, Color.RED, 13, 0, Mars, 1.0);
		Moon Deimos = window.new Moon(0, 0, 5, Color.RED, 20, 0, Mars, 0.7);

		Planet Venus = window.new Planet(0, 0, 20, Color.ORANGE, 95, 0, sun, 0.8);
		
		Planet Earth = window.new Planet(0, 0, 20, Color.BLUE, 140, 0, sun, 0.5);
		Moon earthMoon = window.new Moon(0, 0, 5, Color.LIGHT_GRAY, 20, 0, Earth, 0.7);
		
		Planet Mercury = window.new Planet(0, 0, 15, Color.GRAY, 70, 0, sun, 2.5);

		
	
		
		while (true) {
			window.drawSolarObject(sun.getX(), sun.getY(), sun.diameter, "YELLOW"); 
			/**
			 * draws sun in the solar system
			 */


			 /**
			  * update position makes the objects move, this is necessary for the model
			  * the velocity they move at is included
			  * draws the solar object onto the frame we created aswell, getting the attributes
			  * which we already defined before
			 
			  */





			Neptune.updatePosition(1);
			window.drawSolarObjectAbout(Neptune.distance, Neptune.angle, Neptune.diameter, "BLUE", 0, 0);
			triton.updatePosition(1);
			window.drawSolarObjectAbout(triton.distance, triton.angle, triton.diameter, "PINK", Neptune.distance, Neptune.angle);

			Uranus.updatePosition(1);
			window.drawSolarObjectAbout(Uranus.distance, Uranus.angle, Uranus.diameter, "BLUE", 0, 0);
			titania.updatePosition(1);
			window.drawSolarObjectAbout(titania.distance, titania.angle, titania.diameter, "PINK", Uranus.distance, Uranus.angle);
			oberon.updatePosition(1);
			window.drawSolarObjectAbout(oberon.distance, oberon.angle, oberon.diameter, "RED", Uranus.distance, Uranus.angle);


			Saturn.updatePosition(1);
			window.drawSolarObjectAbout(Saturn.distance, Saturn.angle, Saturn.diameter, "YELLOW", 0, 0);
			titan.updatePosition(1);
			window.drawSolarObjectAbout(titan.distance, titan.angle, titan.diameter, "ORANGE", Saturn.distance, Saturn.angle);

			Jupiter.updatePosition(1);
			window.drawSolarObjectAbout(Jupiter.distance, Jupiter.angle, Jupiter.diameter, "ORANGE", 0, 0);
			Io.updatePosition(1);
			window.drawSolarObjectAbout(Io.distance, Io.angle, Io.diameter, "GREEN", Jupiter.distance, Jupiter.angle);
			Europa.updatePosition(1);
			window.drawSolarObjectAbout(Europa.distance, Europa.angle, Europa.diameter, "WHITE", Jupiter.distance, Jupiter.angle);
			Ganymede.updatePosition(1);
			window.drawSolarObjectAbout(Ganymede.distance, Ganymede.angle, Ganymede.diameter, "ORANGE", Jupiter.distance, Jupiter.angle);
			Callisto.updatePosition(1);
			window.drawSolarObjectAbout(Callisto.distance, Callisto.angle, Callisto.diameter, "ORANGE", Jupiter.distance, Jupiter.angle);


			Mars.updatePosition(1);
			window.drawSolarObjectAbout(Mars.distance, Mars.angle, Mars.diameter, "RED", 0, 0);
			Phobos.updatePosition(1);
			window.drawSolarObjectAbout(Phobos.distance, Phobos.angle, Phobos.diameter, "RED", Mars.distance, Mars.angle);
			Deimos.updatePosition(1);
			window.drawSolarObjectAbout(Deimos.distance, Deimos.angle, Deimos.diameter, "RED", Mars.distance, Mars.angle);

			Venus.updatePosition(1);
			window.drawSolarObjectAbout(Venus.distance, Venus.angle, Venus.diameter, "ORANGE", 0, 0);
			
			Mercury.updatePosition(1);
			window.drawSolarObjectAbout(Mercury.distance, Mercury.angle, Mercury.diameter, "GRAY", 0, 0);
			

			Earth.updatePosition(1);
			window.drawSolarObjectAbout(Earth.distance, Earth.angle, Earth.diameter, "BLUE", 0, 0);
			earthMoon.updatePosition(1);
			window.drawSolarObjectAbout(earthMoon.distance, earthMoon.angle, earthMoon.diameter, "LIGHT_GRAY", Earth.distance, Earth.angle);


			window.finishedDrawing();
		

		}
		
		
	}



	class Sun extends SolarObject {
		public Sun(int x, int y, int diameter, Color color) {
			super(x, y, diameter, color);
			/**
			 * creates the sun class, which inherits from solarobject
			 */
		}
	}


	class Planet extends SolarObject {
		/**
		 * creates the planet class which inherits from solarobject
		 */
		public double distance;
		public double angle;
		public SolarObject center;
		private double velocity;
	
		public Planet(int x, int y, int diameter, Color color, double distance, double angle, SolarObject center, double velocity) {
			super(x, y, diameter, color);
			this.distance = distance;
			this.angle = angle;
			this.center = center;
			this.velocity = velocity;
		}

		public void updatePosition(double timeIncrement) {
			angle += velocity * timeIncrement;
			/**
			 * this is used to update the planets position so it is moving
			 */
			double rads = Math.toRadians(angle);
			x = (int) (center.getX() + distance * Math.sin(rads)) - diameter / 2;
			y = (int) (center.getY() + distance * Math.cos(rads)) - diameter / 2;
 


		}

	}

	class Moon extends SolarObject {
		public double distance;
		public double angle;
		public SolarObject center;
	    private double velocity;

		/**
		 * this creates the moon which inherits from solarobject
		 */
	
		public Moon(int x, int y, int diameter, Color color, double distance, double angle, SolarObject center, double velocity) {
			super(x, y, diameter, color);
			this.distance = distance;
			this.angle = angle;
			this.center = center;
			this.velocity = velocity;
		}
		
		
		public void updatePosition(double timeIncrement) {
			angle += velocity * timeIncrement;
			/**
			 * this also calculates the speed of the moon
			 */
			double rads = Math.toRadians(angle);
			x = (int) (center.getX() + distance * Math.sin(rads)) - diameter / 2;
			y = (int) (center.getY() + distance * Math.cos(rads)) - diameter / 2;
 


		}

	}
	

	    class AsteroidBelt {
      /*
	   * This is the asteroidBelt class used to draw the asteoidBelt if needed
	   */
		
			private ArrayList<SolarObject> asteroids;
        private int numberOfAsteroids;
        private double innerRadius;
        private double outerRadius;


        public AsteroidBelt(int numberOfAsteroids, double innerRadius, double outerRadius) {
            this.numberOfAsteroids = numberOfAsteroids;
            this.innerRadius = innerRadius;
            this.outerRadius = outerRadius;
            asteroids = new ArrayList<>();


            // makes the asteroids randomly placed along the belt
            Random random = new Random();
            for (int i = 0; i < numberOfAsteroids; i++) {
                double distance = innerRadius + (outerRadius - innerRadius) * random.nextDouble();
                double angle = 360 * random.nextDouble();
            }
        }
	}



	    class RingSystem {
			/**
			 * This is the Ring used for Saturn but could be used anywhere in the program
			 */
        private ArrayList<SolarObject> ringParticles;
        private int numberOfParticles;
        private double innerRadius;
        private double outerRadius;


        public RingSystem(int numberOfParticles, double innerRadius, double outerRadius) {
            this.numberOfParticles = numberOfParticles;
            this.innerRadius = innerRadius;
            this.outerRadius = outerRadius;
            ringParticles = new ArrayList<>();


            // Randomly generates rocks around the ring
            Random random = new Random();
            for (int i = 0; i < numberOfParticles; i++) {
                double distance = innerRadius + (outerRadius - innerRadius) * random.nextDouble();
                double angle = 360 * random.nextDouble();

            }
        }
	}


	public class EllipticalOrbitObject extends SolarObject {
		private double semiMajorAxis;
		private double semiMinorAxis;
		private double eccentricity;
		private double orbitFocusDistance;
		private double currentAngle;
	
	
		public EllipticalOrbitObject(double semiMajorAxis, double semiMinorAxis, double eccentricity, Color color) {
			super(0, 0, 0, color); //this will set stuff in the updateposition class
			this.semiMajorAxis = semiMajorAxis;
			this.semiMinorAxis = semiMinorAxis;
			this.eccentricity = eccentricity;
			this.orbitFocusDistance = semiMajorAxis * eccentricity;
			this.currentAngle = 0; 
		}
	
	
		
		public void updatePosition(double timeIncrement) {
			// Increases angle
			currentAngle += timeIncrement;
			if (currentAngle >= 360) {
				currentAngle -= 360;
			}
			/**
			 * this math ensures the comet/object does not follow a centripetal pattern
			 */
	
			double rads = Math.toRadians(currentAngle);
			double distanceFromCenter = (semiMajorAxis * (1 - Math.pow(eccentricity, 2))) /
										(1 + eccentricity * Math.cos(rads));
			double adjustedDistance = distanceFromCenter - orbitFocusDistance;
	
			this.x = (int) (adjustedDistance * Math.cos(rads));
			this.y = (int) (adjustedDistance * Math.sin(rads));
		}
	
	
		
	}
	


}