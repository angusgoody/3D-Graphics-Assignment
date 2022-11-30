import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.awt.*;
import com.jogamp.opengl.util.glsl.*;
import gmaths.Mat4;
import gmaths.Mat4Transform;
import gmaths.Vec3;

/**
 * Class represents the table
 * in the scene
 */

public class Table {

	private Model tableCube, legCube, eggSphere;
	private SGNode tableRoot;

	public Table(GL3 gl, Camera camera, Light light, int[] topTexture, int[] legTexture,
	             int[] eggTexture, int[] eggSpecular) {

		// Specify the size of our table
		float legHeight = 2f;
		float legWidth = 0.2f;
		float legDepth = 0.2f;

		float topHeight = 0.2f;
		float topWidth = 3f;
		float topDepth = 3f;

		float tableHeight = legHeight+(topHeight*2);

		// Define our table info
		Mesh cubeMesh = new Mesh(gl, Cube.vertices.clone(), Cube.indices.clone());
		Shader tableShader = new Shader(gl, "shaders/table_vs.glsl", "shaders/table_fs.glsl");
		Material tableMaterial = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);

		// Egg info
		Mesh sphereMesh = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
		Shader eggShader = new Shader(gl, "shaders/egg_vs.glsl", "shaders/egg_fs.glsl");
		Material eggMaterial = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);

		// Create our models
		tableCube = new Model(gl, camera, light, tableShader, tableMaterial, new Mat4(1), cubeMesh, topTexture);
		legCube = new Model(gl, camera, light, tableShader, tableMaterial, new Mat4(1), cubeMesh, legTexture);
		eggSphere = new Model(gl, camera, light, eggShader, eggMaterial, new Mat4(1), sphereMesh, eggTexture, eggSpecular);

		// ================== Transformations ====================

		// Create root
		tableRoot = new NameNode("table");

		// Create the top of our table & move it to the top
		NameNode top = new NameNode("top");

			/*
			 - Translate our cube to the surface
			 - Scale it into a table-top
			 - Translate it to above the table legs
			 */
			Mat4 m = Mat4.multiply(Mat4Transform.scale(topWidth,topHeight,topDepth), Mat4Transform.translate(0,0.5f,0));
			m = Mat4.multiply(Mat4Transform.translate(0,legHeight,0), m);
				TransformNode topTransform = new TransformNode("top transform", m);
				ModelNode topShape = new ModelNode("Cube(top of table)", tableCube);


		// Create the base for the egg (on top of the table)
		NameNode eggBase = new NameNode("egg base");
			/*
			 - Translate the cube to the surface
			 - Scale it to be right size (1/3 of the table-top surface)
			 - Move it on top of table (leg height + table width)
			 */
			m = Mat4.multiply(Mat4Transform.scale(topWidth/3,topHeight,topDepth/3), Mat4Transform.translate(0,0.5f,0));
			m = Mat4.multiply(Mat4Transform.translate(0,legHeight+topHeight,0), m);
				TransformNode baseTransform = new TransformNode("egg transform", m);
				ModelNode baseShape = new ModelNode("Cube(egg base)", tableCube);

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
			/*
			 - Translate the sphere to surface & scale
			 - Move the egg to the top of our table
			 */
			m = Mat4.multiply(Mat4Transform.scale(2,3,2), Mat4Transform.translate(0,0.5f,0));
			m = Mat4.multiply(Mat4Transform.translate(0,tableHeight,0), m);
				TransformNode eggTransform = new TransformNode("egg transform", m);
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
			eggNode.addChild(eggTransform);
				eggTransform.addChild(eggShape);

		tableRoot.update();  // IMPORTANT - don't forget this

	}

	public void render(GL3 gl) {
		tableRoot.draw(gl);
	}

	public void dispose(GL3 gl) {
		tableCube.dispose(gl);
		legCube.dispose(gl);
		eggSphere.dispose(gl);
	}
}