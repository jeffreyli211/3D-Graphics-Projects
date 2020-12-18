//****************************************************************************
//      CylinderCap class
//****************************************************************************
// 
// Modified java class of Stan Sclaroff's Sphere3D.java
// to create the triangle mesh of a cylinder's end caps.
//

public class CylinderFront
{
	private Point3D center;
	private float rx;
	private float ry;
	private float z_end;
	private int stacks,slices;
	public Mesh3D mesh;
	
	public CylinderFront(float _x, float _y, float _z, float _rx, float _ry, float _z_end, int _stacks, int _slices)
	{
		center = new Point3D(_x,_y,_z);
		rx = _rx;
		ry = _ry;
		z_end = _z_end;
		stacks = _stacks;
		slices = _slices;
		initMesh();
	}
	
	public void set_center(float _x, float _y, float _z)
	{
		center.x=_x;
		center.y=_y;
		center.z=_z;
		fillMesh();  // update the triangle mesh
	}
	
	public void set_radii(float _rx, float _ry)
	{
		rx = _rx;
		ry = _ry;
		fillMesh(); // update the triangle mesh
	}
	
	public void set_stacks(int _stacks)
	{
		stacks = _stacks;
		initMesh(); // resized the mesh, must re-initialize
	}
	
	public void set_slices(int _slices)
	{
		slices = _slices;
		initMesh(); // resized the mesh, must re-initialize
	}
	
	public int get_n()
	{
		return slices;
	}
	
	public int get_m()
	{
		return stacks;
	}

	private void initMesh()
	{
		mesh = new Mesh3D(stacks,slices);
		fillMesh();  // set the mesh vertices and normals
	}
		
	// fill the triangle mesh vertices and normals
	// using the current parameters for the sphere
	private void fillMesh()
	{
		// ****************Implement Code here*******************//
		double pi = Math.PI;
		double theta;
		double phi;
		double phi_step = pi/2;
		double theta_step = (2*pi)/(slices-1);
		int i;
		int j;
		
		for(i = 0, phi = 0; i < 2; i++, phi += phi_step)
		{
			double cos_phi = Math.cos(phi);
			double sin_phi = Math.sin(phi);
			
			for(j = 0, theta = -pi; j < slices; j++, theta += theta_step)
			{
				double cos_theta = Math.cos(theta);
				double sin_theta = Math.sin(theta);
				mesh.v[i][j].x = center.x + (rx * (float) cos_phi * (float) cos_theta);
				mesh.v[i][j].y = center.y + (ry * (float) cos_phi * (float) sin_theta);
				mesh.v[i][j].z = center.z + z_end;
				
				mesh.n[i][j].x = (float) 0;
				mesh.n[i][j].y = (float) 0;
				mesh.n[i][j].z = (float) 1;
				
				mesh.n[i][j].normalize();
			}
		}
	}
}