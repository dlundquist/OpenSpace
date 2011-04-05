package math;

// Differentiates between position and direction.
// Vectors should be used for directions, not positions.
public class Vector {
    public float x,y,z;

    public Vector(){
        x = 0;
        y = 0;
        z = 0;
    }

    Vector(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector times(float scalar){
        return new Vector( x * scalar, y * scalar, z * scalar);
    }

    public Vector plus(Vector other) {
        return new Vector(x + other.x, y + other.y, z + other.z);
    }
    
    public Vector plusEquals(Vector other){
        x += other.x;
        y += other.y;
        z += other.z;
        
        return this;
    }
}