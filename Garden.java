import gmaths.*;

import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.awt.*;
import com.jogamp.opengl.util.glsl.*;
import com.jogamp.opengl.util.texture.*;

class Garden {

    private Camera camera;
    private Light sun;
    private float wallSize = 25f;
    private float nudegeDown = 6;
    private float nudgeBack = (wallSize/2)-(Room.wallSize/2);
    private Vec2 cloudPos;

    private Texture skybox, cloudTexture;
    private Texture[] textures;
    private Model[] walls;

    private Shader dynamicShader;
    private SGNode roomRoot;


    private void loadTextures(GL3 gl) {
        textures = new Texture[5];
        textures[0] = TextureLibrary.loadTexture(gl, "textures/skybox/top.jpg");
        textures[1] = TextureLibrary.loadTexture(gl, "textures/skybox/bottom.jpg");
        textures[2] = TextureLibrary.loadTexture(gl, "textures/skybox/front.jpg");
        textures[3] = TextureLibrary.loadTexture(gl, "textures/skybox/left.jpg");
        textures[4] = TextureLibrary.loadTexture(gl, "textures/skybox/right.jpg");

        cloudTexture = TextureLibrary.loadTexture(gl, "textures/cloud.png");
        // TODO
        //skybox = Cubemap.loadFromStreams(gl, getClass().getClassLoader(),"park_", "jpg", true);
    }

    public Garden(GL3 gl, Camera c) {
        camera = c;

        // Load our textures
        loadTextures(gl);

        // Setup mesh & shaders
        Mesh mesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
        Shader shader = new Shader(gl, "shaders/tt_vs.glsl", "shaders/tt_fs.glsl");
        Material wallMaterial = new Material(new Vec3(0.76f, 0.62f, 0.51f), new Vec3(0.84f,  0.71f,  0.59f), new Vec3(0.3f, 0.3f, 0.3f), 1.0f);

        // Create a sun at the top of the garden, (this should have a yellow tinge)
        Vec3 sunColour = new Vec3(0.99216f,  0.98039f,  0.84314f);
        sun = new Light(gl, sunColour, sunColour, sunColour);
        sun.setCamera(camera);
        sun.setPosition(0,wallSize-(wallSize/nudegeDown),-(Room.wallSize/2));

        dynamicShader = new Shader(gl, "shaders/dynamic_background_vs.glsl", "shaders/dynamic_background_fs.glsl");

        // Build each model
        walls = new Model[5];
        buildWalls(gl, mesh, shader, wallMaterial);

        // ====================== Create the scene graph for our room =============================

        // Base matrix (both window's & floor need to be this size)
        Mat4 mStart = Mat4Transform.scale(wallSize,1f,wallSize);

        roomRoot = new NameNode("Room root");

        // Move our garden back a bit and down a bit to simulate window edge
        TransformNode roomMoveTransform = new TransformNode("move room transform", Mat4Transform.translate(0,-(Room.wallSize/nudegeDown),-nudgeBack));

        // Create the Roof node
        NameNode roof = new NameNode("Roof");
            Mat4 m = Mat4.multiply(Mat4Transform.rotateAroundX(180), mStart);
            m = Mat4.multiply(Mat4Transform.translate(0,wallSize,0), m);
                TransformNode roofTransform = new TransformNode("Roof transform", m);
                    ModelNode roofShape = new ModelNode("Roof shape", walls[0]);

        // Create the floor node
        NameNode floorNode = new NameNode("Floor");
            TransformNode floorTransform = new TransformNode("Floor transform", mStart);
                ModelNode floorShape = new ModelNode("floor shape", walls[1]);

        // Create the back wall node
        NameNode windowNode = new NameNode("Window");
            m = Mat4.multiply(Mat4Transform.rotateAroundX(90), mStart);
            m = Mat4.multiply(Mat4Transform.translate(0,wallSize*0.5f,-wallSize*0.5f), m);
                TransformNode windowTransform = new TransformNode("Window transform", m);
                    ModelNode windowShape = new ModelNode("Window shape", walls[2]);

        // Create the left wall node
        NameNode leftWall = new NameNode("Left wall");
            m = Mat4.multiply(Mat4Transform.rotateAroundY(90), mStart);
            m = Mat4.multiply(Mat4Transform.rotateAroundZ(-90), m);
            m = Mat4.multiply(Mat4Transform.translate(-wallSize*0.5f,wallSize*0.5f,0), m);
                TransformNode leftWallTransform = new TransformNode("Left wall transform", m);
                    ModelNode leftWallShape = new ModelNode("left wall shape", walls[3]);

        // Create the right wall node
        NameNode rightWall = new NameNode("Right wall");
            m = Mat4.multiply(Mat4Transform.rotateAroundY(90), mStart);
            m = Mat4.multiply(Mat4Transform.rotateAroundZ(-90), m);
            m = Mat4.multiply(Mat4Transform.translate(-wallSize*0.5f,wallSize*0.5f,0), m);
            m = Mat4.multiply(Mat4Transform.rotateAroundY(180), m);
            TransformNode rightWallTransform = new TransformNode("Right wall transform", m);
                    ModelNode rightWallShape = new ModelNode("Right wall shape", walls[4]);




        // Create Hierarchy
        roomRoot.addChild(roomMoveTransform);
            roomMoveTransform.addChild(floorNode);
                floorNode.addChild(floorTransform);
                    floorTransform.addChild(floorShape);
            roomMoveTransform.addChild(leftWall);
                leftWall.addChild(leftWallTransform);
                    leftWallTransform.addChild(leftWallShape);
            roomMoveTransform.addChild(windowNode);
                windowNode.addChild(windowTransform);
                windowTransform.addChild(windowShape);
            roomMoveTransform.addChild(rightWall);
                rightWall.addChild(rightWallTransform);
                rightWallTransform.addChild(rightWallShape);
            roomMoveTransform.addChild(roof);
                roof.addChild(roofTransform);
                    roofTransform.addChild(roofShape);

        roomRoot.update();

    }

    private void buildWalls(GL3 gl, Mesh mesh, Shader shader, Material material){
        // Loop through each texture
        for (int i = 0; i < textures.length; i++) {
            // We only want moving clouds on certain walls
            if (i > 1){
                walls[i] = new Model(gl,camera, sun, dynamicShader, material, new Mat4(),mesh,textures[i], cloudTexture);

            }else{
                walls[i] = new Model(gl,camera, sun, shader, material, new Mat4(),mesh,textures[i]);

            }

        }
    }

    public void toggleSun()
    {
        System.out.println("Gonna toggle sun");
        sun.toggle();
    }

    public void render(GL3 gl) {

        dynamicShader.use(gl);
        dynamicShader.setFloat(gl, "offset", cloudPos.x, cloudPos.y);
        sun.render(gl);
        roomRoot.draw(gl);
    }

    public void setClouds(Vec2 pos){
        cloudPos = pos;
    }


    public void dispose(GL3 gl) {
        for (int i = 0; i < walls.length; i++) {
            walls[i].dispose(gl);
        }
        sun.dispose(gl);

    }

    public Light getLight() {
        return sun;
    }
}
