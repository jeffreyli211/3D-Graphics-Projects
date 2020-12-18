import java.util.Random;

public class SpotLight extends Light{
public Point3D vl;
public float spot_theta;
private Random rnd=new Random();
	
	public SpotLight(ColorType _c, Point3D pl, Point3D _vl, float theta) 
	{
		color = new ColorType(_c);
		direction = new Point3D(0, 0, 0);
		vl = new Point3D(_vl);
		vl.normalize();
		spot_theta = theta;
		position = new Point3D(pl);
	}
	
	// apply this light source to the vertex / normal, given material
	// return resulting color value
	// v: viewing vector
	// n: face normal
	public ColorType applyLight(Material mat, Point3D v, Point3D n, Point3D ps, float a0, float a1, float a2, float al){
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
		
		// Calculate the distance between the surface point and the light source
		float dl = (float) (position.minus(ps)).magnitude();
		// Set the radial attenuation factor
		float f_radatten = 1 / (a0 + (a1 * dl) + (a2 * (float) Math.pow(dl, 2)));
		
		System.out.println("Distance from light source at point (" + ps.x + ", " + ps.y + ", " + ps.z + ") is " + dl);
		System.out.println("f_radatten for this point is " + f_radatten);
		
		// vobj = -L
		Point3D vobj = new Point3D();
		vobj.x = -1 * direction.x;
		vobj.y = -1 * direction.y;
		vobj.z = -1 * direction.z;
		
		float cos_alpha = vobj.dotProduct(vl);
		System.out.println(cos_alpha);
		float f_angatten;
		if (cos_alpha < (float) Math.cos(spot_theta))
		{
			f_angatten = 0.0f;
		}
		else
		{
			f_angatten = (float) Math.pow(cos_alpha, al);
		}
		
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
		
		res.r = /*I_amb.r + */ f_radatten * f_angatten * (I_dif.r + I_spec.r);
		res.g = /*I_amb.g + */ f_radatten * f_angatten * (I_dif.g + I_spec.g);
		res.b = /*I_amb.b + */ f_radatten * f_angatten * (I_dif.b + I_spec.b);
		
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
		/*
		float mag_L = (float) Math.sqrt((float) Math.pow(position.x - ps.x, 2) + (float) Math.pow(position.y - ps.y, 2) + (float) Math.pow(position.z - ps.z, 2));
		direction = new Point3D((position.x-ps.x) / mag_L, (position.y-ps.y) / mag_L, (position.z-ps.z) / mag_L);
		*/
		direction = position.minus(ps);
		direction.normalize();
	}
}
