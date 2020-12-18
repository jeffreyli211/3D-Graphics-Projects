//****************************************************************************
//      Cylinder3D class
//****************************************************************************
// 
// Modified java class of Stan Sclaroff's Sphere3D.java
// to create the curved surface of a cylinder.
//

public class Cylinder3D
{
	private Point3D center;
	private float rx;
	private float ry;
	private float rz;
	private float z_end;	// Indicates how far from center-z the z_max and z_min would be. (z_max = center-z + z_end), (z_min = center-z - z_end)
	private int stacks,slices;
	public Mesh3D mesh;
	
	public Cylinder3D(float _x, float _y, float _z, float _rx, float _ry, float _rz, float _z_end, int _stacks, int _slices)
	{
		center = new Point3D(_x,_y,_z);
		rx = _rx;
		ry = _ry;
		rz = _rz;
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
	
	public void set_radii(float _rx, float _ry, float _rz)
	{
		rx = _rx;
		ry = _ry;
		rz = _rz;
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
		/*
		double pi = Math.PI;
		double phi;
		double theta;
		double phi_step = pi/(stacks-1);
		double theta_step = (2*pi)/(slices-1);
		int i;
		int j;
		
		for (i = 0, phi = -pi/2; i < stacks; i++, phi += phi_step)
		{
			double cos_phi = Math.cos(phi);
			double sin_phi = Math.sin(phi);
			
			for (j = 0, theta = -pi; j < slices; j++, theta += theta_step)
			{
				double cos_theta = Math.cos(theta);
				double sin_theta = Math.sin(theta);
				mesh.v[i][j].x = center.x + (rx * (float) cos_phi * (float) cos_theta);
				mesh.v[i][j].y = center.y + (ry * (float) cos_phi * (float) sin_theta);
				mesh.v[i][j].z = center.z + (rz * (float) sin_phi);
				
				mesh.n[i][j].x = (float) cos_phi * (float) cos_theta;
				mesh.n[i][j].y = (float) cos_phi * (float) sin_theta;
				mesh.n[i][j].z = (float) sin_phi;
			}
		}
		*/
		double pi = Math.PI;
		double z_min = center.z - z_end;
		double z_max = center.z + z_end;
		double z_pt;
		double theta_cyl;
		double z_pt_step = (2*z_end)/(stacks-1);
		double theta_cyl_step = (2*pi)/(slices-1);
		int i;
		int j;
		
		for (i = 0, z_pt = z_min; i < stacks; i++, z_pt += z_pt_step)
		{
			for (j = 0, theta_cyl = -pi; j < slices; j++, theta_cyl += theta_cyl_step)
			{
				double cos_theta_cyl = Math.cos(theta_cyl);
				double sin_theta_cyl = Math.sin(theta_cyl);
				mesh.v[i][j].x = center.x + (rx * (float) cos_theta_cyl);
				mesh.v[i][j].y = center.y + (ry * (float) sin_theta_cyl);
				mesh.v[i][j].z = (float) z_pt;
				
				mesh.n[i][j].x = (float) cos_theta_cyl;
				mesh.n[i][j].y = (float) sin_theta_cyl;
				mesh.n[i][j].z = (float) 0;
				
				mesh.n[i][j].normalize();
			}
		}
		
	}
}