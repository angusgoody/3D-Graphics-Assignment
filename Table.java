import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.awt.*;
import com.jogamp.opengl.util.glsl.*;
import com.jogamp.opengl.util.texture.*;

import gmaths.Mat4;
import gmaths.Mat4Transform;
import gmaths.Vec3;

/**
 * Class represents the table
 * in the scene, the table contains
 * the egg
 * @author Angus Goody
 */

public class Table {

	private Model tableCube, legCube, eggSphere;
	private SGNode tableRoot;
	private Texture[] textures;
	private float eggJumpHeightFactor = 0.3f;
	private float eggJumpSpeedFactor = 2.2f;
	private float eggRotateSpeedFactor = 0.4f;
	private float tableHeight, eggHeight;
	private TransformNode eggJumpTransform;
	private Shader eggShader;

	private void loadTextures(GL3 gl) {
		textures = new Texture[4];

		textures[0] = TextureLibrary.loadTexture(gl, "textures/tabletop.jpg");
		textures[1] = TextureLibrary.loadTexture(gl, "textures/table_legs.jpg");
		textures[2] = TextureLibrary.loadTexture(gl, "textures/egg.jpg");
		textures[3] = TextureLibrary.loadTexture(gl, "textures/egg_map.jpg");

	}

	public Table(GL3 gl, Camera camera, Light[] worldLights, SpotLight[] spotLights, Shader multiShader) {

		loadTextures(gl);

		// Specify the size of our table
		float legHeight = 2f;
		float legWidth = 0.2f;
		float legDepth = 0.2f;

		float topHeight = 0.2f;
		float topWidth = 3f;
		float topDepth = 3f;

		// Top of base for egg to sit on
		tableHeight = legHeight+(topHeight*2);

		// How big the egg should be
		eggHeight = 3;

		// Define our table info
		Mesh cubeMesh = new Mesh(gl, Cube.vertices.clone(), Cube.indices.clone());
		Material tableMaterial = new Material();

		// Egg info
		Mesh sphereMesh = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
		Material eggMaterial = new Material(new Vec3(0.3f), new Vec3(0.7f), new Vec3(0.6f), 100);
		eggShader = new Shader(gl, "shaders/tt_vs.glsl", "shaders/egg_fs.glsl");


		// Create our models
		tableCube = new Model(gl, camera, worldLights, spotLights, multiShader, tableMaterial, new Mat4(1), cubeMesh, textures[0]);
		legCube = new Model(gl, camera, worldLights, spotLights, multiShader, tableMaterial, new Mat4(1), cubeMesh, textures[1]);
		eggSphere = new Model(gl, camera, worldLights, spotLights, eggShader, eggMaterial, new Mat4(1), sphereMesh, textures[2], textures[3]);

		// ================== Transformations ====================

		// Create root
		tableRoot = new NameNode("table");

		// Create the top of our table & move it to the top
		NameNode top = new NameNode("top");

			// Make the surface of the table and move it above the leg heights
			Mat4 m = Mat4.multiply(Mat4Transform.scale(topWidth,topHeight,topDepth), Mat4Transform.translate(0,0.5f,0));
			m = Mat4.multiply(Mat4Transform.translate(0,legHeight,0), m);
				TransformNode topTransform = new TransformNode("top transform", m);
				ModelNode topShape = new ModelNode("Cube(top of table)", tableCube);


		// Create the base for the egg (on top of the table)
		NameNode eggBase = new NameNode("egg base");

			// Make the base the right size and then move it into position
			m = Mat4.multiply(Mat4Transform.scale(topWidth/3,topHeight,topDepth/3), Mat4Transform.translate(0,0.5f,0));
			m = Mat4.multiply(Mat4Transform.translate(0,legHeight+topHeight,0), m);
				TransformNode baseTransform = new TransformNode("egg transform", m);
				ModelNode baseShape = new ModelNode("Cube(egg base)", legCube);

		// Array to store leg nodes
		NameNode[] legNodes = new NameNode[4];

		// Create 4 legs for the table
		int counter = 0;
		for (int x = 0; x < 2; x++) {
			for (int z = 0; z < 2; z++) {
				counter += 1;
				String legName = "leg " + counter;
				NameNode currentLeg = new NameNode(legName);

				// For every leg we need to, translate to surface & scale
				m = Mat4.multiply(Mat4Transform.scale(legWidth, legHeight, legDepth), Mat4Transform.translate(0, 0.5f, 0));

				// Translate in positive or negative direction for each leg
				int xFactor = (x == 0)
						? 1
						: -1;
				int zFactor = (z == 0)
						? 1
						: -1;

				// Move the leg into position
				m = Mat4.multiply(Mat4Transform.translate(xFactor*((topWidth/2)-(legWidth/2)), 0, zFactor*((topDepth/2)-(legDepth/2))), m);

				// Construct the transformation & model nodes
				TransformNode currentLegTransform = new TransformNode(legName+" transform", m);
				ModelNode currentLegShape = new ModelNode("Cube - "+legName, legCube);

				// Apply hierarchy
				currentLeg.addChild(currentLegTransform);
				currentLegTransform.addChild(currentLegShape);

				// Store in array
				legNodes[counter-1] = currentLeg;

			}
		}

		// Add egg to top of table
		NameNode eggNode = new NameNode("egg");

			// A transform node so we can spin and move our egg
			eggJumpTransform = new TransformNode("Egg jump", new Mat4(1));
			TransformNode eggMoveToTable = new TransformNode("Move egg onto table", Mat4Transform.translate(0,tableHeight,0));
				TransformNode setupEgg = new TransformNode("Setup egg", Mat4.multiply(Mat4Transform.scale(eggHeight/1.4f,eggHeight,eggHeight/1.4f), Mat4Transform.translate(0,0.5f,0)));
						ModelNode eggShape = new ModelNode("Sphere(Egg on top of table)", eggSphere);



		// Add nodes hierarchy
		tableRoot.addChild(top);
				top.addChild(topTransform);
					topTransform.addChild(topShape);
		// Add each leg from the array
		for (NameNode legNode : legNodes) {
			top.addChild(legNode);
		}

		// Add the base for the egg to sit on
		top.addChild(eggBase);
			eggBase.addChild(baseTransform);
			baseTransform.addChild(baseShape);

		// Add egg
		top.addChild(eggNode);
			eggNode.addChild(eggJumpTransform);
				eggJumpTransform.addChild(eggMoveToTable);
					eggMoveToTable.addChild(setupEgg);
		                setupEgg.addChild(eggShape);

		tableRoot.update();  // IMPORTANT - don't forget this

	}

	public SGNode getRoot(){
		return tableRoot;
	}


	public void makeEggJump(double elapsedTime) {

		// Calculate the translation & rotation of the egg
		float jumpHeight = (float) ((eggHeight* eggJumpHeightFactor)*Math.abs(Math.sin(elapsedTime*eggJumpSpeedFactor)));
		float rotateAngle = 360*(float)Math.sin(elapsedTime*eggRotateSpeedFactor);

		// Apply the transformation
		Mat4 transform = Mat4.multiply(Mat4Transform.rotateAroundY(rotateAngle), Mat4Transform.translate(0, jumpHeight,0));
		eggJumpTransform.setTransform(transform);
		eggJumpTransform.update();

	}

	public void dispose(GL3 gl) {
		tableCube.dispose(gl);
		legCube.dispose(gl);
		eggSphere.dispose(gl);
	}
}