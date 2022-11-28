import gmaths.*;

import java.nio.*;
import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.awt.*;
import com.jogamp.opengl.util.glsl.*;

/**
 * Room class to represent the room
 * of the scene
 */

public class Room {

    private Model floor, wall;

    public Room(GL3 gl, Camera camera, Light light, int[] floorTexture, int[] wallTexture,int[] windowTexture) {


        Mesh mesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
        Shader shader = new Shader(gl, "vs_tt_05.txt", "fs_tt_05.txt");

        // The floor is going to be sand, this should be pretty matte
        Material floorMaterial = new Material(new Vec3(0.76f, 0.62f, 0.51f), new Vec3(0.84f,  0.71f,  0.59f), new Vec3(0.3f, 0.3f, 0.3f), 1.0f);

        // For now the wallMaterial can be the same as the floor
        Material wallMaterial = new Material(new Vec3(0.76f, 0.62f, 0.51f), new Vec3(0.84f,  0.71f,  0.59f), new Vec3(0.3f, 0.3f, 0.3f), 1.0f);

        // Create models for the floor & wall
        floor = new Model(gl, camera, light, shader, floorMaterial, Mat4Transform.scale(16,1f,16), mesh, floorTexture);
        wall = new Model(gl, camera, light, shader, wallMaterial, new Mat4(), mesh, wallTexture);



    }

    public void render(GL3 gl) {

        floor.render(gl);

        wall.setModelMatrix(transformWall(0));
        wall.render(gl);

        wall.setModelMatrix(transformWall(1));
        wall.render(gl);

        wall.setModelMatrix(transformWall(2));
        wall.render(gl);

        wall.setModelMatrix(transformWall(3));
        wall.render(gl);
    }

    private Mat4 transformWall(int side) {
        float size = 16f;
        Mat4 modelMatrix = new Mat4(1);
        modelMatrix = Mat4.multiply(Mat4Transform.scale(size,1f,size), modelMatrix);

        switch (side){
            case 1:
                modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundX(90), modelMatrix);
                modelMatrix = Mat4.multiply(Mat4Transform.translate(0,size*0.5f,-size*0.5f), modelMatrix);
                break;
            case 2:
                modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundY(90), modelMatrix);
                modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundZ(-90), modelMatrix);
                modelMatrix = Mat4.multiply(Mat4Transform.translate(-size*0.5f,size*0.5f,0), modelMatrix);
                break;
            case 3:

            default:
                break;

        }

        return modelMatrix;
    }

    public void dispose(GL3 gl) {
        floor.dispose(gl);
        wall.dispose(gl);
    }

}