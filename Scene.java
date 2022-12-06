import gmaths.*;

import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.awt.*;
import com.jogamp.opengl.util.glsl.*;
import com.jogamp.opengl.util.texture.*;


/**
 * The Scene class represents
 * the whole scene and stores global
 * data such as lights
 */

public class Scene {

	private Light[] worldLights;
	private PointLight[] lampLights;

	private Room room;
	private Garden garden;
	private Shader multiShader;
	private double startTime;


	public Scene(GL3 gl, Camera camera) {

		// Time
		startTime = getSeconds();

		// Create the lights for our scene
		Light roomLight = new Light(gl);
		Vec3 sunColour = new Vec3(0.99216f,  0.98039f,  0.84314f);
		Light sun = new Light(gl,sunColour,sunColour,sunColour);

		// Position & setup lights
		roomLight.setCamera(camera);
		roomLight.setPosition(0,Room.wallSize,0);
		sun.setCamera(camera);
		sun.setPosition(0,Garden.wallSize-(Garden.wallSize/Garden.nudegeDown),-(Room.wallSize/2));

		// Store our world lights
		worldLights = new Light[2];
		worldLights[0] = roomLight;
		worldLights[1] = sun;

		// Store the new shaders to handle multiple world lights
		multiShader = new Shader(gl, "shaders/tt_vs.glsl", "shaders/new_fs.glsl");

		// Create the room for the scene
		room = new Room(gl,camera, worldLights, multiShader);
		lampLights = room.getLamps();


		// Create the garden
		garden = new Garden(gl, camera);


	}

	private double getSeconds() {
		return System.currentTimeMillis()/1000.0;
	}



	public void render(GL3 gl) {
		// Render the world lights
		for (Light worldLight : worldLights) {
			worldLight.render(gl);
		}

		// Render the lamp lights
		for (PointLight lampLight : lampLights) {
			lampLight.render(gl);
		}
		room.render(gl, getSeconds());
		garden.render(gl, getSeconds());
	}


	public void dispose(GL3 gl) {
		room.dispose(gl);
		garden.dispose(gl);
	}

	public void toggleLight()
	{
		System.out.println("Toggle room light");
		worldLights[0].toggle();
	}

	public void toggleSun()
	{
		System.out.println("Toggle sun");
		worldLights[1].toggle();
	}
}
