//****************************************************************************
//       3D vector class for example program
//****************************************************************************
// History :
//   Nov 6, 2014 Created by Stan Sclaroff
//

public class Point3D
{
	public float x, y, z;
	public ColorType c = new ColorType(0.0f, 0.0f, 0.0f);
	public static final float ROUNDOFF_THRESHOLD = 0.0001f;
	
	public Point3D(float _x, float _y, float _z)
	{
		x=_x;
		y=_y;
		z=_z;
	}
	
	public Point3D(float _x, float _y, float _z, ColorType _c)
	{
		x=_x;
		y=_y;
		z=_z;
		c=_c;
	}
	
	public Point3D(Point3D _v)
	{
		x=_v.x;
		y=_v.y;
		z=_v.z;
		c= new ColorType(_v.c.r, _v.c.g, _v.c.b);
	}
	
	public Point3D(Point3D _v, ColorType _c)
	{
		x=_v.x;
		y=_v.y;
		z=_v.z;
		c=new ColorType(_c.r, _c.g, _c.b);
	}
	
	public Point3D()
	{
		x=y=z=(float)0.0;
	}
	
	public void set(float _x, float _y, float _z)
	{
		x=_x;
		y=_y;
		z=_z;
	}
	
	// compute the cross-product this (x) v and return result in out
	public void crossProduct(Point3D v, Point3D out)
	{
		Point3D temp = new Point3D();
		temp.x = this.y*v.z-this.z*v.y;
		temp.y = this.z*v.x-this.x*v.z;
		temp.z = this.x*v.y-this.y*v.x;
		
		out.x = temp.x;
		out.y = temp.y;
		out.z = temp.z;
	}
	
	// compute the cross-product this (x) v and return result
	public Point3D crossProduct(Point3D v)
	{
		Point3D out = new Point3D();
		out.x = this.y*v.z-this.z*v.y;
		out.y = this.z*v.x-this.x*v.z;
		out.z = this.x*v.y-this.y*v.x;
		return(out);
	}
	
	// compute dot product of v and this vector
	public float dotProduct(Point3D v)
	{
		return(v.x*this.x+v.y*this.y+v.z*this.z);
	}
			
	// subtract vector v from this vector and return result in out
	public void minus(Point3D v, Point3D out)
	{
		out.x = this.x-v.x;
		out.y = this.y-v.y;
		out.z = this.z-v.z;
	}
		
	// subtract vector v from this vector and return result
	public Point3D minus(Point3D v)
	{
		Point3D out = new Point3D();
		out.x = this.x-v.x;
		out.y = this.y-v.y;
		out.z = this.z-v.z;
		return(out);
	}
	
	// scale this vector by s and return result
	public Point3D scale(float s)
	{
		Point3D out = new Point3D();
		out.x = this.x*s;
		out.y = this.y*s;
		out.z = this.z*s;
		return(out);
	}
	
	// add the vector v to this vector and return result 
	public Point3D plus(Point3D v)
	{
		Point3D out = new Point3D();
		out.x = this.x+v.x;
		out.y = this.y+v.y;
		out.z = this.z+v.z;
		return(out);
	}
	
	// compute the length / magnitude
	public double magnitude()
	{
		double mag = Math.sqrt(dotProduct(this));
		return(mag);
	}
	
	// produce unit vector
	public void normalize()
	{
		double mag = this.magnitude();
		if(mag>ROUNDOFF_THRESHOLD)
		{
			this.x /= mag;
			this.y /= mag;
			this.z /= mag;
		}
		// should probably throw an error exception here for zero magnitude 
	}
	
	public Point3D reflection(Point3D n)
	{
		return n.scale(2 * this.dotProduct(n)).minus(this);
	}
	
	public int interpolate_z(Point3D p2, int inter_x)
	{
		int z_res = (int) z + (inter_x - (int) x) * (int) ((p2.z - z) / (p2.x - x));
		return z_res;
	}
	
	public int interpolate_z_swapped(Point3D p2, int inter_y)
	{
		int z_res = (int) z + (inter_y - (int) y) * (int) ((p2.z - z) / (p2.y - y));
		return z_res;
	}
	
}