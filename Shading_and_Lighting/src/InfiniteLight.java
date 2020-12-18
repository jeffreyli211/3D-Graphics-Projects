import java.util.Random;

public class InfiniteLight extends Light{
private Random rnd=new Random();
	
	public InfiniteLight(ColorType _c, Point3D _direction) 
	{
		color = new ColorType(_c);
		direction = new Point3D(_direction);
		direction.normalize();
		position = new Point3D(0, 0, 0); // Not used in this class
	}
	
	// apply this light source to the vertex / normal, given material
	// return resulting color value
	// v: viewing vector
	// n: face normal
	public ColorType applyLight(Material mat, Point3D v, Point3D n){
		ColorType res = new ColorType();
		// ****************Implement Code here*******************//
		/*
		ColorType I_amb = new ColorType();
		I_amb.r = mat.ka.r * color.r;
		I_amb.g = mat.ka.g * color.g;
		I_amb.b = mat.ka.b * color.b;
		*/
		
		ColorType I_dif = new ColorType();
		I_dif.r = mat.kd.r * color.r * n.dotProduct(direction);
		I_dif.g = mat.kd.g * color.g * n.dotProduct(direction);
		I_dif.b = mat.kd.b * color.b * n.dotProduct(direction);
		
		ColorType I_spec = new ColorType();
		Point3D R = direction.reflection(n);
		float temp = (float) Math.pow(v.dotProduct(R), mat.ns);
		I_spec.r = mat.ks.r * color.r * temp;
		I_spec.g = mat.ks.g * color.g * temp;
		I_spec.b = mat.ks.b * color.b * temp;
		
		res.r = /*I_amb.r + */I_dif.r + I_spec.r;
		res.g = /*I_amb.g + */I_dif.g + I_spec.g;
		res.b = /*I_amb.b + */I_dif.b + I_spec.b;
		
		return res;

	}
}
