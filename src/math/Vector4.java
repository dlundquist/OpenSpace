package math;

public class Vector4 extends Vector3 {
    private static final long serialVersionUID = 6357784296699011970L;
    float t;
    public Vector4(float x, float y, float z, float t){
        super(x,y,z);
        this.t = t;
    }
    
    public Vector4(float x, float y, float z){
        t = 1.0f;
    }
    
    public Vector4(Vector3 xyz,float t){
        super(xyz);
        this.t = t;
    }
    public Vector4() {
        super();
        t = 0.0f;
    }

    public float[] toFloatArray(){
        float[] toRet = {x,y,z,t};
        return toRet;
    }
}