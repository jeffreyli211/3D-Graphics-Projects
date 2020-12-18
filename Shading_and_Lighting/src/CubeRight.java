//****************************************************************************
//      Cylinder3D class
//****************************************************************************
// 
// Modified java class of Stan Sclaroff's Sphere3D.java
// to create the curved surface of a cylinder.
//

public class CubeRight
{
	private Point3D center;
	private float size;
	private int stacks,slices;
	public Mesh3D mesh;
	
	public CubeRight(float _x, float _y, float _z, float _size, int _stacks, int _slices)
	{
		center = new Point3D(_x,_y,_z);
		size = _size;
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
		double row_layer;
		double col_layer;
		double stack_step = size/(stacks-1);
		double slice_step = size/(slices-1);
		int i;
		int j;
		
		for (i = 0, row_layer = center.y - (size/2); i < stacks; i++, row_layer += stack_step)
		{
			for (j = 0, col_layer = center.z + (size/2); j < slices; j++, col_layer -= slice_step)
			{
				mesh.v[i][j].x = (float) center.x + (size/2);
				mesh.v[i][j].y = (float) row_layer;
				mesh.v[i][j].z = (float) col_layer;
				
				mesh.n[i][j].x = (float) 1;
				mesh.n[i][j].y = (float) 0;
				mesh.n[i][j].z = (float) 0;
				
				mesh.n[i][j].normalize();
			}
		}
	}
}