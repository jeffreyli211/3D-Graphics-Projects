//****************************************************************************
//       Depth Buffer class to maintain a buffered image's depth buffer
//****************************************************************************
//
public class DepthBuffer
{
	public int cols,rows;
	public int[][] z_buffer;
	public ColorType[][] frame_buffer;
	
	public DepthBuffer(int _rows, int _cols)
	{
		cols=_cols;
		rows=_rows;
		z_buffer = new int[rows][cols];
		frame_buffer = new ColorType[rows][cols];
		for(int i=0;i<rows;i++)
		{
			for(int j=0;j<cols;j++)
			{
				z_buffer[i][j] = Integer.MIN_VALUE;
				frame_buffer[i][j] = new ColorType(0.0f, 0.0f, 0.0f);
			}
		}
	}
	
	public void update_buffer(int x, int y, int z, ColorType c)
	{
		z_buffer[x][y] = z;
		frame_buffer[x][y] = c;
	}
	
	public float get_z_value(int x, int y)
	{
		return z_buffer[x][y];
	}
	
	public ColorType get_color_value(int x, int y)
	{
		return frame_buffer[x][y];
	}
	
	public void clear_buffer()
	{
		for(int i=0;i<rows;++i)
		{
			for(int j=0;j<cols;++j)
			{
				z_buffer[i][j] = Integer.MIN_VALUE;
				frame_buffer[i][j] = new ColorType(0.0f, 0.0f, 0.0f);
			}
		}
	}
}