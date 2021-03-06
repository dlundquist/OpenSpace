package graphics.core;

import static org.junit.Assert.*;
import graphics.core.Model;

import math.Vector3f;

import org.junit.Test;

public class ModelTest {
    public static final String TEST_MODEL = "cube_cube";
    private static final float EPSILON = 0.00001f;

    @Test
    public void testFindOrCreateByName() {
        Model cube = Model.findOrCreateByName(TEST_MODEL);
        assert(cube != null);
    }

    @Test
    public void testFindByName() {
        Model cube = Model.createByName(TEST_MODEL);
        cube = Model.findByName(TEST_MODEL);
        assert(cube != null);;
    }

    @Test
    public void testCreateByName() {
        Model cube = Model.createByName(TEST_MODEL);
        assert(cube != null);
    }

    @Test
    public void testGetFarthestPointInDirection() {
        Model cube = Model.findOrCreateByName(TEST_MODEL);
        assertEquals(4.0f,cube.getFarthestPointInDirection(Vector3f.UNIT_X).x,EPSILON);
        assertEquals(4.0f,cube.getFarthestPointInDirection(Vector3f.UNIT_Y).y,EPSILON);
        assertEquals(4.0f,cube.getFarthestPointInDirection(Vector3f.UNIT_Z).z,EPSILON);

        
        assertEquals(-4.0f,cube.getFarthestPointInDirection(Vector3f.UNIT_X.negate()).x,EPSILON);
        assertEquals(-4.0f,cube.getFarthestPointInDirection(Vector3f.UNIT_Y.negate()).y,EPSILON);
        assertEquals(-4.0f,cube.getFarthestPointInDirection(Vector3f.UNIT_Z.negate()).z,EPSILON);
    }

}
