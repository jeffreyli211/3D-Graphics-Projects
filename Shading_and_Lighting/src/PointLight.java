import java.util.Random;

public class PointLight extends Light{
private Random rnd=new Random();
	
	public PointLight(ColorType _c, Point3D pl) 
	{
		color = new ColorType(_c);
		direction = new Point3D(0, 0, 0);
		position = new Point3D(pl);
	}
	
	// apply this light source to the vertex / normal, given material
	// return resulting color value
	// v: viewing vector
	// n: face normal
	public ColorType applyLight(Material mat, Point3D v, Point3D n, Point3D ps){
		ColorType res = new ColorType();
		// ****************Implement Code here*******************//
		/*
		ColorType I_amb = new ColorType();
		I_amb.r = mat.ka.r * color.r;
		I_amb.g = mat.ka.g * color.g;
		I_amb.b = mat.ka.b * color.b;
		*/
		
		// Calculate the vector L for this surface point.
		find_Direction(ps);
		
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
	
	/**
	 * Helper function to calculate the light direction vector L from a surface point ps to light source pl.
	 * 
	 * @param ps
	 * 			The point on the surface of the object we are applying light to.
	 */
	public void find_Direction(Point3D ps)
	{
		direction = position.minus(ps);
		direction.normalize();
	}
}
