//****************************************************************************
// SketchBase.  
//****************************************************************************
// Comments : 
//   Subroutines to manage and draw points, lines an triangles
//
// History :
//   Aug 2014 Created by Jianming Zhang (jimmie33@gmail.com) based on code by
//   Stan Sclaroff (from CS480 '06 poly.c)

import java.awt.image.BufferedImage;
import java.util.*;

public class SketchBase 
{
	/** WIP **/
	public static float[][] depth_buffer;			// Stores the current closest z-value from a shape at point x,y
	public static ColorType[][] frame_buffer;		// Stores the color of the closest z-value shape at point x,y
	/*********/
	
	public SketchBase()
	{
		// deliberately left blank
	}
	
	public static float get_z_at(int x, int y)
	{
		return depth_buffer[x][y];
	}
	/*********/
	
	/**********************************************************************
	 * Draws a point.
	 * This is achieved by changing the color of the buffer at the location
	 * corresponding to the point. 
	 * 
	 * @param buff
	 *          Buffer object.
	 * @param p
	 *          Point to be drawn.
	 */
	/*
	public static void drawPoint(BufferedImage buff, Point2D p)
	{
		if(p.x>=0 && p.x<buff.getWidth() && p.y>=0 && p.y < buff.getHeight())
			buff.setRGB(p.x, buff.getHeight()-p.y-1, p.c.getRGB_int());	
	}
	*/
	
	public static void drawPoint3D(BufferedImage buff, DepthBuffer depth_buffer, Point3D p)
	{
		if(p.x>=0 && p.x<buff.getWidth() && p.y>=0 && p.y < buff.getHeight())
		{
			//System.out.println("z value at (" + p.x + ", " + p.y + ") is " + p.z);
			//System.out.println("z buffer at (" + p.x + ", " + p.y + ") is " + depth_buffer.get_z_value((int) p.x, (int) p.y));
			if (p.z > depth_buffer.get_z_value((int) p.x, (int) p.y))
			{
				buff.setRGB((int) p.x, buff.getHeight()-(int) p.y-1, p.c.getRGB_int());
				depth_buffer.update_buffer((int) p.x, (int) p.y, (int) p.z, p.c);
			}
			//buff.setRGB((int) p.x, buff.getHeight()-(int) p.y-1, p.c.getRGB_int());	
		}
	}
	
	/**********************************************************************
	 * Draws a line segment using Bresenham's algorithm, linearly 
	 * interpolating RGB color along line segment.
	 * This method only uses integer arithmetic.
	 * 
	 * @param buff
	 *          Buffer object.
	 * @param p1
	 *          First given endpoint of the line.
	 * @param p2
	 *          Second given endpoint of the line.
	 */
	/*
	public static void drawLine(BufferedImage buff, Point2D p1, Point2D p2)
	{
	    int x0=p1.x, y0=p1.y;
	    int xEnd=p2.x, yEnd=p2.y;
	    int dx = Math.abs(xEnd - x0),  dy = Math.abs(yEnd - y0);

	    if(dx==0 && dy==0)
	    {
	    	drawPoint(buff,p1);
	    	return;
	    }
	    
	    // if slope is greater than 1, then swap the role of x and y
	    boolean x_y_role_swapped = (dy > dx); 
	    if(x_y_role_swapped)
	    {
	    	x0=p1.y; 
	    	y0=p1.x;
	    	xEnd=p2.y; 
	    	yEnd=p2.x;
	    	dx = Math.abs(xEnd - x0);
	    	dy = Math.abs(yEnd - y0);
	    }
	    
	    // initialize the decision parameter and increments
	    int p = 2 * dy - dx;
	    int twoDy = 2 * dy,  twoDyMinusDx = 2 * (dy - dx);
	    int x=x0, y=y0;
	    
	    // set step increment to be positive or negative
	    int step_x = x0<xEnd ? 1 : -1;
	    int step_y = y0<yEnd ? 1 : -1;
	    
	    // deal with setup for color interpolation
	    // first get r,g,b integer values at the end points
	    int r0=p1.c.getR_int(), rEnd=p2.c.getR_int();
	    int g0=p1.c.getG_int(), gEnd=p2.c.getG_int();
	    int b0=p1.c.getB_int(), bEnd=p2.c.getB_int();
	    
	    // compute the change in r,g,b 
	    int dr=Math.abs(rEnd-r0), dg=Math.abs(gEnd-g0), db=Math.abs(bEnd-b0);
	    
	    // set step increment to be positive or negative 
	    int step_r = r0<rEnd ? 1 : -1;
	    int step_g = g0<gEnd ? 1 : -1;
	    int step_b = b0<bEnd ? 1 : -1;
	    
	    // compute whole step in each color that is taken each time through loop
	    int whole_step_r = step_r*(dr/dx);
	    int whole_step_g = step_g*(dg/dx);
	    int whole_step_b = step_b*(db/dx);
	    
	    // compute remainder, which will be corrected depending on decision parameter
	    dr=dr%dx;
	    dg=dg%dx; 
	    db=db%dx;
	    
	    // initialize decision parameters for red, green, and blue
	    int p_r = 2 * dr - dx;
	    int twoDr = 2 * dr,  twoDrMinusDx = 2 * (dr - dx);
	    int r=r0;
	    
	    int p_g = 2 * dg - dx;
	    int twoDg = 2 * dg,  twoDgMinusDx = 2 * (dg - dx);
	    int g=g0;
	    
	    int p_b = 2 * db - dx;
	    int twoDb = 2 * db,  twoDbMinusDx = 2 * (db - dx);
	    int b=b0;
	    
	    // draw start pixel
	    if(x_y_role_swapped)
	    {
	    	if(x>=0 && x<buff.getHeight() && y>=0 && y<buff.getWidth())
	    		buff.setRGB(y, buff.getHeight()-x-1, (r<<16) | (g<<8) | b);
	    }
	    else
	    {
	    	if(y>=0 && y<buff.getHeight() && x>=0 && x<buff.getWidth())
	    		buff.setRGB(x, buff.getHeight()-y-1, (r<<16) | (g<<8) | b);
	    }
	    
	    while (x != xEnd) 
	    {
	    	// increment x and y
	    	x+=step_x;
	    	if (p < 0)
	    		p += twoDy;
	    	else 
	    	{
	    		y+=step_y;
	    		p += twoDyMinusDx;
	    	}
		        
	    	// increment r by whole amount slope_r, and correct for accumulated error if needed
	    	r+=whole_step_r;
	    	if (p_r < 0)
	    		p_r += twoDr;
	    	else 
	    	{
	    		r+=step_r;
	    		p_r += twoDrMinusDx;
	    	}
		    
	    	// increment g by whole amount slope_b, and correct for accumulated error if needed  
	    	g+=whole_step_g;
	    	if (p_g < 0)
	    		p_g += twoDg;
	    	else 
	    	{
	    		g+=step_g;
	    		p_g += twoDgMinusDx;
	    	}
		    
	    	// increment b by whole amount slope_b, and correct for accumulated error if needed
	    	b+=whole_step_b;
	    	if (p_b < 0)
	    		p_b += twoDb;
	    	else 
	    	{
	    		b+=step_b;
	    		p_b += twoDbMinusDx;
	    	}
		    
	    	if(x_y_role_swapped)
	    	{
	    		if(x>=0 && x<buff.getHeight() && y>=0 && y<buff.getWidth())
	    			buff.setRGB(y, buff.getHeight()-x-1, (r<<16) | (g<<8) | b);
	    	}
	    	else
	    	{
	    		if(y>=0 && y<buff.getHeight() && x>=0 && x<buff.getWidth())
	    			buff.setRGB(x, buff.getHeight()-y-1, (r<<16) | (g<<8) | b);
	    	}
	    }
	}
	*/
	
	public static void drawLine3D(BufferedImage buff, DepthBuffer depth_buffer, Point3D p1, Point3D p2)
	{
	    int x0=(int) p1.x, y0=(int) p1.y;
	    int xEnd=(int) p2.x, yEnd=(int) p2.y;
	    int dx = Math.abs(xEnd - x0),  dy = Math.abs(yEnd - y0);

	    if(dx==0 && dy==0)
	    {
	    	drawPoint3D(buff, depth_buffer, p1);
	    	return;
	    }
	    
	    // if slope is greater than 1, then swap the role of x and y
	    boolean x_y_role_swapped = (dy > dx); 
	    if(x_y_role_swapped)
	    {
	    	x0=(int) p1.y; 
	    	y0=(int) p1.x;
	    	xEnd=(int) p2.y; 
	    	yEnd=(int) p2.x;
	    	dx = Math.abs(xEnd - x0);
	    	dy = Math.abs(yEnd - y0);
	    }
	    
	    // initialize the decision parameter and increments
	    int p = 2 * dy - dx;
	    int twoDy = 2 * dy,  twoDyMinusDx = 2 * (dy - dx);
	    int x=x0, y=y0;
	    
	    // set step increment to be positive or negative
	    int step_x = x0<xEnd ? 1 : -1;
	    int step_y = y0<yEnd ? 1 : -1;
	    
	    // deal with setup for color interpolation
	    // first get r,g,b integer values at the end points
	    int r0=p1.c.getR_int(), rEnd=p2.c.getR_int();
	    int g0=p1.c.getG_int(), gEnd=p2.c.getG_int();
	    int b0=p1.c.getB_int(), bEnd=p2.c.getB_int();
	    
	    // compute the change in r,g,b 
	    int dr=Math.abs(rEnd-r0), dg=Math.abs(gEnd-g0), db=Math.abs(bEnd-b0);
	    
	    // set step increment to be positive or negative 
	    int step_r = r0<rEnd ? 1 : -1;
	    int step_g = g0<gEnd ? 1 : -1;
	    int step_b = b0<bEnd ? 1 : -1;
	    
	    // compute whole step in each color that is taken each time through loop
	    int whole_step_r = step_r*(dr/dx);
	    int whole_step_g = step_g*(dg/dx);
	    int whole_step_b = step_b*(db/dx);
	    
	    // compute remainder, which will be corrected depending on decision parameter
	    dr=dr%dx;
	    dg=dg%dx; 
	    db=db%dx;
	    
	    // initialize decision parameters for red, green, and blue
	    int p_r = 2 * dr - dx;
	    int twoDr = 2 * dr,  twoDrMinusDx = 2 * (dr - dx);
	    int r=r0;
	    
	    int p_g = 2 * dg - dx;
	    int twoDg = 2 * dg,  twoDgMinusDx = 2 * (dg - dx);
	    int g=g0;
	    
	    int p_b = 2 * db - dx;
	    int twoDb = 2 * db,  twoDbMinusDx = 2 * (db - dx);
	    int b=b0;
	    
	    // draw start pixel
	    if(x_y_role_swapped)
	    {
	    	if(x>=0 && x<buff.getHeight() && y>=0 && y<buff.getWidth())
	    	{
	    		//buff.setRGB(y, buff.getHeight()-x-1, (r<<16) | (g<<8) | b);
	    		ColorType curr_c = new ColorType();
	    		curr_c.setR_int(r);
	    		curr_c.setG_int(g);
	    		curr_c.setB_int(b);
	    		drawPoint3D(buff, depth_buffer, new Point3D(y, x, p1.z, curr_c));
	    	}
	    }
	    else
	    {
	    	if(y>=0 && y<buff.getHeight() && x>=0 && x<buff.getWidth())
	    	{
	    		//buff.setRGB(x, buff.getHeight()-y-1, (r<<16) | (g<<8) | b);
	    		/*
	    		if(x>=0 && x<buff.getWidth() && y>=0 && y < buff.getHeight())
	    		{
	    			if (p1.z > depth_buffer.get_z_value((int) x, (int) y))
	    			{
	    				buff.setRGB((int) x, buff.getHeight()-(int) y-1, p1.c.getRGB_int());
	    				depth_buffer.update_buffer((int) x, (int) y, p1.z);
	    			}
	    		}
	    		*/
	    		ColorType curr_c = new ColorType();
	    		curr_c.setR_int(r);
	    		curr_c.setG_int(g);
	    		curr_c.setB_int(b);
	    		drawPoint3D(buff, depth_buffer, new Point3D(x, y, p1.z, curr_c));
	    	}
	    }
	    
	    while (x != xEnd) 
	    {
	    	// increment x and y
	    	x+=step_x;
	    	if (p < 0)
	    		p += twoDy;
	    	else 
	    	{
	    		y+=step_y;
	    		p += twoDyMinusDx;
	    	}
		        
	    	// increment r by whole amount slope_r, and correct for accumulated error if needed
	    	r+=whole_step_r;
	    	if (p_r < 0)
	    		p_r += twoDr;
	    	else 
	    	{
	    		r+=step_r;
	    		p_r += twoDrMinusDx;
	    	}
		    
	    	// increment g by whole amount slope_b, and correct for accumulated error if needed  
	    	g+=whole_step_g;
	    	if (p_g < 0)
	    		p_g += twoDg;
	    	else 
	    	{
	    		g+=step_g;
	    		p_g += twoDgMinusDx;
	    	}
		    
	    	// increment b by whole amount slope_b, and correct for accumulated error if needed
	    	b+=whole_step_b;
	    	if (p_b < 0)
	    		p_b += twoDb;
	    	else 
	    	{
	    		b+=step_b;
	    		p_b += twoDbMinusDx;
	    	}
		    
	    	if(x_y_role_swapped)
	    	{
	    		if(x>=0 && x<buff.getHeight() && y>=0 && y<buff.getWidth())
	    		{
	    			//buff.setRGB(y, buff.getHeight()-x-1, (r<<16) | (g<<8) | b);
	    			
	    			float inter_z = p1.interpolate_z(p2, y);
	    			ColorType curr_c = new ColorType();
	    			curr_c.setR_int(r);
	    			curr_c.setG_int(g);
	    			curr_c.setB_int(b);
	    			Point3D curr_p = new Point3D (y, x, inter_z, curr_c);
	    			drawPoint3D(buff, depth_buffer, curr_p);
	    			
	    		}
	    	}
	    	else
	    	{
	    		if(y>=0 && y<buff.getHeight() && x>=0 && x<buff.getWidth())
	    		{
	    			//buff.setRGB(x, buff.getHeight()-y-1, (r<<16) | (g<<8) | b);
	    			
	    			float inter_z = p1.interpolate_z(p2, x);
	    			ColorType curr_c = new ColorType();
	    			curr_c.setR_int(r);
	    			curr_c.setG_int(g);
	    			curr_c.setB_int(b);
	    			Point3D curr_p = new Point3D (x, y, inter_z, curr_c);
	    			drawPoint3D(buff, depth_buffer, curr_p);
	    			
	    		}
	    	}
	    }
	}
	
/*
	public static void drawLinePhong(BufferedImage buff, DepthBuffer depth_buffer, Point3D side_a, Point3D side_b, Point3D[] p1, Point3D[] p2, Material mat,
			AmbientLight amb, ArrayList<InfiniteLight> infs, ArrayList<PointLight> pts, ArrayList<SpotLight> spots, Point3D v)
	{
	    int x0=(int) side_a.x, y0=(int) side_a.y;
	    int xEnd=(int) side_b.x, yEnd=(int) side_b.y;
	    int dx = Math.abs(xEnd - x0),  dy = Math.abs(yEnd - y0);

	    if(dx==0 && dy==0)
	    {
	    	ColorType pt_amb_c = amb.applyLight(mat);
	    	ColorType pt_inf_c = new ColorType();
	    	for (int i = 0; i < infs.size(); i++)
			{
				ColorType Idiff_Ispec = infs.get(i).applyLight(mat, v, p1[1]);
				pt_inf_c.r += Idiff_Ispec.r;
				pt_inf_c.g += Idiff_Ispec.g;
				pt_inf_c.b += Idiff_Ispec.b;
			}
	    	ColorType pt_pt_c = new ColorType();
	    	for (int i = 0; i < pts.size(); i++)
			{
				ColorType Idiff_Ispec = pts.get(i).applyLight(mat, v, p1[1], side_a);
				pt_pt_c.r += Idiff_Ispec.r;
				pt_pt_c.g += Idiff_Ispec.g;
				pt_pt_c.b += Idiff_Ispec.b;
			}
*/
	    	/*
	    	 * Missing: Spotlight influence
	    	 */
/*
	    	ColorType final_c = pt_amb_c.combine(pt_inf_c).combine(pt_pt_c);
	    	Point3D pt_draw = new Point3D(side_a, final_c);
	    	drawPoint3D(buff, depth_buffer, pt_draw);
	    	//drawPoint3D(buff, depth_buffer, p1[0]);
	    	return;
	    }
	    
	    // if slope is greater than 1, then swap the role of x and y
	    boolean x_y_role_swapped = (dy > dx); 
	    if(x_y_role_swapped)
	    {
	    	x0=(int) side_a.y; 
	    	y0=(int) side_a.x;
	    	xEnd=(int) side_b.y; 
	    	yEnd=(int) side_b.x;
	    	dx = Math.abs(xEnd - x0);
	    	dy = Math.abs(yEnd - y0);
	    }
	    
	    // initialize the decision parameter and increments
	    int p = 2 * dy - dx;
	    int twoDy = 2 * dy,  twoDyMinusDx = 2 * (dy - dx);
	    int x=x0, y=y0;
	    
	    // set step increment to be positive or negative
	    int step_x = x0<xEnd ? 1 : -1;
	    int step_y = y0<yEnd ? 1 : -1;
	    
	    // deal with setup for color interpolation
	    // first get r,g,b integer values at the end points
	    int r0=side_a.c.getR_int(), rEnd=side_b.c.getR_int();
	    int g0=side_a.c.getG_int(), gEnd=side_b.c.getG_int();
	    int b0=side_a.c.getB_int(), bEnd=side_b.c.getB_int();
*/
	    /*******************************HERE*******************************/
/*
	    // compute the change in r,g,b 
	    int dr=Math.abs(rEnd-r0), dg=Math.abs(gEnd-g0), db=Math.abs(bEnd-b0);
	    
	    // set step increment to be positive or negative 
	    int step_r = r0<rEnd ? 1 : -1;
	    int step_g = g0<gEnd ? 1 : -1;
	    int step_b = b0<bEnd ? 1 : -1;
	    
	    // compute whole step in each color that is taken each time through loop
	    int whole_step_r = step_r*(dr/dx);
	    int whole_step_g = step_g*(dg/dx);
	    int whole_step_b = step_b*(db/dx);
	    
	    // compute remainder, which will be corrected depending on decision parameter
	    dr=dr%dx;
	    dg=dg%dx; 
	    db=db%dx;
	    
	    // initialize decision parameters for red, green, and blue
	    int p_r = 2 * dr - dx;
	    int twoDr = 2 * dr,  twoDrMinusDx = 2 * (dr - dx);
	    int r=r0;
	    
	    int p_g = 2 * dg - dx;
	    int twoDg = 2 * dg,  twoDgMinusDx = 2 * (dg - dx);
	    int g=g0;
	    
	    int p_b = 2 * db - dx;
	    int twoDb = 2 * db,  twoDbMinusDx = 2 * (db - dx);
	    int b=b0;
	    
	    // draw start pixel
	    if(x_y_role_swapped)
	    {
	    	if(x>=0 && x<buff.getHeight() && y>=0 && y<buff.getWidth())
	    	{
	    		//buff.setRGB(y, buff.getHeight()-x-1, (r<<16) | (g<<8) | b);
	    		ColorType curr_c = new ColorType();
	    		curr_c.setR_int(r);
	    		curr_c.setG_int(g);
	    		curr_c.setB_int(b);
	    		drawPoint3D(buff, depth_buffer, new Point3D(y, x, p1.z, curr_c));
	    	}
	    }
	    else
	    {
	    	if(y>=0 && y<buff.getHeight() && x>=0 && x<buff.getWidth())
	    	{
	    		//buff.setRGB(x, buff.getHeight()-y-1, (r<<16) | (g<<8) | b);
	    		ColorType curr_c = new ColorType();
	    		curr_c.setR_int(r);
	    		curr_c.setG_int(g);
	    		curr_c.setB_int(b);
	    		drawPoint3D(buff, depth_buffer, new Point3D(x, y, p1.z, curr_c));
	    	}
	    }
	    
	    while (x != xEnd) 
	    {
	    	// increment x and y
	    	x+=step_x;
	    	if (p < 0)
	    		p += twoDy;
	    	else 
	    	{
	    		y+=step_y;
	    		p += twoDyMinusDx;
	    	}
		        
	    	// increment r by whole amount slope_r, and correct for accumulated error if needed
	    	r+=whole_step_r;
	    	if (p_r < 0)
	    		p_r += twoDr;
	    	else 
	    	{
	    		r+=step_r;
	    		p_r += twoDrMinusDx;
	    	}
		    
	    	// increment g by whole amount slope_b, and correct for accumulated error if needed  
	    	g+=whole_step_g;
	    	if (p_g < 0)
	    		p_g += twoDg;
	    	else 
	    	{
	    		g+=step_g;
	    		p_g += twoDgMinusDx;
	    	}
		    
	    	// increment b by whole amount slope_b, and correct for accumulated error if needed
	    	b+=whole_step_b;
	    	if (p_b < 0)
	    		p_b += twoDb;
	    	else 
	    	{
	    		b+=step_b;
	    		p_b += twoDbMinusDx;
	    	}
		    
	    	if(x_y_role_swapped)
	    	{
	    		if(x>=0 && x<buff.getHeight() && y>=0 && y<buff.getWidth())
	    		{
	    			//buff.setRGB(y, buff.getHeight()-x-1, (r<<16) | (g<<8) | b);
	    			
	    			float inter_z = p1.interpolate_z(p2, y);
	    			ColorType curr_c = new ColorType();
	    			curr_c.setR_int(r);
	    			curr_c.setG_int(g);
	    			curr_c.setB_int(b);
	    			Point3D curr_p = new Point3D (y, x, inter_z, curr_c);
	    			drawPoint3D(buff, depth_buffer, curr_p);
	    			
	    		}
	    	}
	    	else
	    	{
	    		if(y>=0 && y<buff.getHeight() && x>=0 && x<buff.getWidth())
	    		{
	    			//buff.setRGB(x, buff.getHeight()-y-1, (r<<16) | (g<<8) | b);
	    			
	    			float inter_z = p1.interpolate_z(p2, x);
	    			ColorType curr_c = new ColorType();
	    			curr_c.setR_int(r);
	    			curr_c.setG_int(g);
	    			curr_c.setB_int(b);
	    			Point3D curr_p = new Point3D (x, y, inter_z, curr_c);
	    			drawPoint3D(buff, depth_buffer, curr_p);
	    			
	    		}
	    	}
	    }
	}
	*/

	/**********************************************************************
	 * Draws a filled triangle. 
	 * The triangle may be filled using flat fill or smooth fill. 
	 * This routine fills columns of pixels within the left-hand part, 
	 * and then the right-hand part of the triangle.
	 *   
	 *	                         *
	 *	                        /|\
	 *	                       / | \
	 *	                      /  |  \
	 *	                     *---|---*
	 *	            left-hand       right-hand
	 *	              part             part
	 *
	 * @param buff
	 *          Buffer object.
	 * @param p1
	 *          First given vertex of the triangle.
	 * @param p2
	 *          Second given vertex of the triangle.
	 * @param p3
	 *          Third given vertex of the triangle.
	 * @param do_smooth
	 *          Flag indicating whether flat fill or smooth fill should be used.                   
	 */
	/*
	public static void drawTriangle(BufferedImage buff, Point2D p1, Point2D p2, Point2D p3, boolean do_smooth)
	{
	    // sort the triangle vertices by ascending x value
	    Point2D p[] = sortTriangleVerts(p1,p2,p3);
	    
	    int x; 
	    float y_a, y_b;
	    float dy_a, dy_b;
	    float dr_a=0, dg_a=0, db_a=0, dr_b=0, dg_b=0, db_b=0;
	    
	    Point2D side_a = new Point2D(p[0]), side_b = new Point2D(p[0]);
	    
	    if(!do_smooth)
	    {
	    	side_a.c = new ColorType(p1.c);
	    	side_b.c = new ColorType(p1.c);
	    }
	    
	    y_b = p[0].y;
	    dy_b = ((float)(p[2].y - p[0].y))/(p[2].x - p[0].x);
	    
	    if(do_smooth)
	    {
	    	// calculate slopes in r, g, b for segment b
	    	dr_b = ((float)(p[2].c.r - p[0].c.r))/(p[2].x - p[0].x);
	    	dg_b = ((float)(p[2].c.g - p[0].c.g))/(p[2].x - p[0].x);
	    	db_b = ((float)(p[2].c.b - p[0].c.b))/(p[2].x - p[0].x);
	    }
	    
	    // if there is a left-hand part to the triangle then fill it
	    if(p[0].x != p[1].x)
	    {
	    	y_a = p[0].y;
	    	dy_a = ((float)(p[1].y - p[0].y))/(p[1].x - p[0].x);
		    
	    	if(do_smooth)
	    	{
	    		// calculate slopes in r, g, b for segment a
	    		dr_a = ((float)(p[1].c.r - p[0].c.r))/(p[1].x - p[0].x);
	    		dg_a = ((float)(p[1].c.g - p[0].c.g))/(p[1].x - p[0].x);
	    		db_a = ((float)(p[1].c.b - p[0].c.b))/(p[1].x - p[0].x);
	    	}
		    
		    // loop over the columns for left-hand part of triangle
		    // filling from side a to side b of the span
		    for(x = p[0].x; x < p[1].x; ++x)
		    {
		    	drawLine(buff, side_a, side_b);

		    	++side_a.x;
		    	++side_b.x;
		    	y_a += dy_a;
		    	y_b += dy_b;
		    	side_a.y = (int)y_a;
		    	side_b.y = (int)y_b;
		    	if(do_smooth)
		    	{
		    		side_a.c.r +=dr_a;
		    		side_b.c.r +=dr_b;
		    		side_a.c.g +=dg_a;
		    		side_b.c.g +=dg_b;
		    		side_a.c.b +=db_a;
		    		side_b.c.b +=db_b;
		    	}
		    }
	    }
	    
	    // there is no right-hand part of triangle
	    if(p[1].x == p[2].x)
	    	return;
	    
	    // set up to fill the right-hand part of triangle 
	    // replace segment a
	    side_a = new Point2D(p[1]);
	    if(!do_smooth)
	    	side_a.c =new ColorType(p1.c);
	    
	    y_a = p[1].y;
	    dy_a = ((float)(p[2].y - p[1].y))/(p[2].x - p[1].x);
	    if(do_smooth)
	    {
	    	// calculate slopes in r, g, b for replacement for segment a
	    	dr_a = ((float)(p[2].c.r - p[1].c.r))/(p[2].x - p[1].x);
	    	dg_a = ((float)(p[2].c.g - p[1].c.g))/(p[2].x - p[1].x);
	    	db_a = ((float)(p[2].c.b - p[1].c.b))/(p[2].x - p[1].x);
	    }

	    // loop over the columns for right-hand part of triangle
	    // filling from side a to side b of the span
	    for(x = p[1].x; x <= p[2].x; ++x)
	    {
	    	drawLine(buff, side_a, side_b);
		    
	    	++side_a.x;
	    	++side_b.x;
	    	y_a += dy_a;
	    	y_b += dy_b;
	    	side_a.y = (int)y_a;
	    	side_b.y = (int)y_b;
	    	if(do_smooth)
	    	{
	    		side_a.c.r +=dr_a;
	    		side_b.c.r +=dr_b;
	    		side_a.c.g +=dg_a;
	    		side_b.c.g +=dg_b;
	    		side_a.c.b +=db_a;
	    		side_b.c.b +=db_b;
	    	}
	    }
	}
	*/
	
	public static void drawTriangle3D(BufferedImage buff, DepthBuffer depth_buffer, Point3D p1, Point3D p2, Point3D p3, boolean do_smooth)
	{
	    // sort the triangle vertices by ascending x value
	    Point3D p[] = sortTriangleVerts3D(p1,p2,p3);
	    
	    int x; 
	    float y_a, y_b;
	    float dy_a, dy_b;
	    float dr_a=0, dg_a=0, db_a=0, dr_b=0, dg_b=0, db_b=0;
	    
	    Point3D side_a = new Point3D(p[0], p[0].c), side_b = new Point3D(p[0], p[0].c);
	    
	    if(!do_smooth)
	    {
	    	side_a.c = new ColorType(p1.c);
	    	side_b.c = new ColorType(p1.c);
	    }
	    
	    y_b = p[0].y;
	    dy_b = ((float)(p[2].y - p[0].y))/(p[2].x - p[0].x);
	    
	    if(do_smooth)
	    {
	    	// calculate slopes in r, g, b for segment b
	    	dr_b = ((float)(p[2].c.r - p[0].c.r))/(p[2].x - p[0].x);
	    	dg_b = ((float)(p[2].c.g - p[0].c.g))/(p[2].x - p[0].x);
	    	db_b = ((float)(p[2].c.b - p[0].c.b))/(p[2].x - p[0].x);
	    }
	    
	    // if there is a left-hand part to the triangle then fill it
	    if(p[0].x != p[1].x)
	    {
	    	y_a = p[0].y;
	    	dy_a = ((float)(p[1].y - p[0].y))/(p[1].x - p[0].x);
		    
	    	if(do_smooth)
	    	{
	    		// calculate slopes in r, g, b for segment a
	    		dr_a = ((float)(p[1].c.r - p[0].c.r))/(p[1].x - p[0].x);
	    		dg_a = ((float)(p[1].c.g - p[0].c.g))/(p[1].x - p[0].x);
	    		db_a = ((float)(p[1].c.b - p[0].c.b))/(p[1].x - p[0].x);
	    	}
		    
		    // loop over the columns for left-hand part of triangle
		    // filling from side a to side b of the span
		    for(x = (int) p[0].x; x < (int) p[1].x; ++x)
		    {
		    	drawLine3D(buff, depth_buffer, side_a, side_b);

		    	++side_a.x;
		    	++side_b.x;
		    	y_a += dy_a;
		    	y_b += dy_b;
		    	side_a.y = (int)y_a;
		    	side_b.y = (int)y_b;
		    	if(do_smooth)
		    	{
		    		side_a.c.r +=dr_a;
		    		side_b.c.r +=dr_b;
		    		side_a.c.g +=dg_a;
		    		side_b.c.g +=dg_b;
		    		side_a.c.b +=db_a;
		    		side_b.c.b +=db_b;
		    	}
		    }
	    }
	    
	    // there is no right-hand part of triangle
	    if(p[1].x == p[2].x)
	    	return;
	    
	    // set up to fill the right-hand part of triangle 
	    // replace segment a
	    side_a = new Point3D(p[1], p[1].c);
	    if(!do_smooth)
	    	side_a.c =new ColorType(p1.c);
	    
	    y_a = p[1].y;
	    dy_a = ((float)(p[2].y - p[1].y))/(p[2].x - p[1].x);
	    if(do_smooth)
	    {
	    	// calculate slopes in r, g, b for replacement for segment a
	    	dr_a = ((float)(p[2].c.r - p[1].c.r))/(p[2].x - p[1].x);
	    	dg_a = ((float)(p[2].c.g - p[1].c.g))/(p[2].x - p[1].x);
	    	db_a = ((float)(p[2].c.b - p[1].c.b))/(p[2].x - p[1].x);
	    }

	    // loop over the columns for right-hand part of triangle
	    // filling from side a to side b of the span
	    for(x = (int) p[1].x; x <= p[2].x; ++x)
	    {
	    	drawLine3D(buff, depth_buffer, side_a, side_b);
		    
	    	++side_a.x;
	    	++side_b.x;
	    	y_a += dy_a;
	    	y_b += dy_b;
	    	side_a.y = (int)y_a;
	    	side_b.y = (int)y_b;
	    	if(do_smooth)
	    	{
	    		side_a.c.r +=dr_a;
	    		side_b.c.r +=dr_b;
	    		side_a.c.g +=dg_a;
	    		side_b.c.g +=dg_b;
	    		side_a.c.b +=db_a;
	    		side_b.c.b +=db_b;
	    	}
	    }
	}
	/*
	public static void drawTrianglePhong(BufferedImage buff, DepthBuffer depth_buffer, Point3D[] p1_n1, Point3D[] p2_n2, Point3D[] p3_n3,
			Material mat, AmbientLight amb, ArrayList<InfiniteLight> infs, ArrayList<PointLight> pts, ArrayList<SpotLight> spots, boolean do_smooth)
	{
	    // sort the triangle vertices by ascending x value
	    Point3D p[][] = sortTriangleVertsPhong(p1_n1,p2_n2,p3_n3);
	    
	    int x; 
	    float y_a, y_b;
	    float dy_a, dy_b;
	    float dr_a=0, dg_a=0, db_a=0, dr_b=0, dg_b=0, db_b=0;
	    
	    Point3D side_a = new Point3D(p[0][0], p[0][0].c), side_b = new Point3D(p[0][0], p[0][0].c);
	    Point3D side_a_n = new Point3D(p[0][1]), side_b_n = new Point3D(p[0][1]);
	    
	    if(!do_smooth)
	    {
	    	side_a.c = new ColorType(p1_n1[0].c);
	    	side_b.c = new ColorType(p1_n1[0].c);
	    }
	    
	    y_b = p[0][0].y;
	    dy_b = ((float)(p[2][0].y - p[0][0].y))/(p[2][0].x - p[0][0].x);
	    
	    if(do_smooth)
	    {
	    	// calculate slopes in r, g, b for segment b
	    	dr_b = ((float)(p[2][0].c.r - p[0][0].c.r))/(p[2][0].x - p[0][0].x);
	    	dg_b = ((float)(p[2][0].c.g - p[0][0].c.g))/(p[2][0].x - p[0][0].x);
	    	db_b = ((float)(p[2][0].c.b - p[0][0].c.b))/(p[2][0].x - p[0][0].x);
	    }
	    
	    // if there is a left-hand part to the triangle then fill it
	    if(p[0][0].x != p[1][0].x)
	    {
	    	y_a = p[0][0].y;
	    	dy_a = ((float)(p[1][0].y - p[0][0].y))/(p[1][0].x - p[0][0].x);
		    
	    	if(do_smooth)
	    	{
	    		// calculate slopes in r, g, b for segment a
	    		dr_a = ((float)(p[1][0].c.r - p[0][0].c.r))/(p[1][0].x - p[0][0].x);
	    		dg_a = ((float)(p[1][0].c.g - p[0][0].c.g))/(p[1][0].x - p[0][0].x);
	    		db_a = ((float)(p[1][0].c.b - p[0][0].c.b))/(p[1][0].x - p[0][0].x);
	    	}
		    
		    // loop over the columns for left-hand part of triangle
		    // filling from side a to side b of the span
		    for(x = (int) p[0][0].x; x < (int) p[1][0].x; ++x)
		    {
		    	drawLinePhong(buff, depth_buffer, side_a, side_b, p[0], p[1], mat, amb, infs, pts, spots);

		    	++side_a.x;
		    	++side_b.x;
		    	y_a += dy_a;
		    	y_b += dy_b;
		    	side_a.y = (int)y_a;
		    	side_b.y = (int)y_b;
		    	
		    	
		    	
		    	if(do_smooth)
		    	{
		    		side_a.c.r +=dr_a;
		    		side_b.c.r +=dr_b;
		    		side_a.c.g +=dg_a;
		    		side_b.c.g +=dg_b;
		    		side_a.c.b +=db_a;
		    		side_b.c.b +=db_b;
		    	}
		    }
	    }
	    
	    // there is no right-hand part of triangle
	    if(p[1].x == p[2].x)
	    	return;
	    
	    // set up to fill the right-hand part of triangle 
	    // replace segment a
	    side_a = new Point3D(p[1], p[1].c);
	    if(!do_smooth)
	    	side_a.c =new ColorType(p1.c);
	    
	    y_a = p[1].y;
	    dy_a = ((float)(p[2].y - p[1].y))/(p[2].x - p[1].x);
	    if(do_smooth)
	    {
	    	// calculate slopes in r, g, b for replacement for segment a
	    	dr_a = ((float)(p[2].c.r - p[1].c.r))/(p[2].x - p[1].x);
	    	dg_a = ((float)(p[2].c.g - p[1].c.g))/(p[2].x - p[1].x);
	    	db_a = ((float)(p[2].c.b - p[1].c.b))/(p[2].x - p[1].x);
	    }

	    // loop over the columns for right-hand part of triangle
	    // filling from side a to side b of the span
	    for(x = (int) p[1].x; x <= p[2].x; ++x)
	    {
	    	drawLine3D(buff, depth_buffer, side_a, side_b);
		    
	    	++side_a.x;
	    	++side_b.x;
	    	y_a += dy_a;
	    	y_b += dy_b;
	    	side_a.y = (int)y_a;
	    	side_b.y = (int)y_b;
	    	if(do_smooth)
	    	{
	    		side_a.c.r +=dr_a;
	    		side_b.c.r +=dr_b;
	    		side_a.c.g +=dg_a;
	    		side_b.c.g +=dg_b;
	    		side_a.c.b +=db_a;
	    		side_b.c.b +=db_b;
	    	}
	    }
	}
	*/

	/**********************************************************************
	 * Helper function to bubble sort triangle vertices by ascending x value.
	 * 
	 * @param p1
	 *          First given vertex of the triangle.
	 * @param p2
	 *          Second given vertex of the triangle.
	 * @param p3
	 *          Third given vertex of the triangle.
	 * @return 
	 *          Array of 3 points, sorted by ascending x value.
	 */
	private static Point2D[] sortTriangleVerts(Point2D p1, Point2D p2, Point2D p3)
	{
	    Point2D pts[] = {p1, p2, p3};
	    Point2D tmp;
	    int j=0;
	    boolean swapped = true;
	         
	    while (swapped) 
	    {
	    	swapped = false;
	    	j++;
	    	for (int i = 0; i < 3 - j; i++) 
	    	{                                       
	    		if (pts[i].x > pts[i + 1].x) 
	    		{                          
	    			tmp = pts[i];
	    			pts[i] = pts[i + 1];
	    			pts[i + 1] = tmp;
	    			swapped = true;
	    		}
	    	}                
	    }
	    return(pts);
	}
	
	private static Point3D[] sortTriangleVerts3D(Point3D p1, Point3D p2, Point3D p3)
	{
	    Point3D pts[] = {p1, p2, p3};
	    Point3D tmp;
	    int j=0;
	    boolean swapped = true;
	         
	    while (swapped) 
	    {
	    	swapped = false;
	    	j++;
	    	for (int i = 0; i < 3 - j; i++) 
	    	{                                       
	    		if (pts[i].x > pts[i + 1].x) 
	    		{                          
	    			tmp = pts[i];
	    			pts[i] = pts[i + 1];
	    			pts[i + 1] = tmp;
	    			swapped = true;
	    		}
	    	}                
	    }
	    return(pts);
	}
	
	private static Point3D[][] sortTriangleVertsPhong(Point3D[] p1n1, Point3D[] p2n2, Point3D[] p3n3)
	{
	    Point3D[][] pts = {p1n1, p2n2, p3n3};
	    Point3D[] tmp;
	    int j=0;
	    boolean swapped = true;
	         
	    while (swapped) 
	    {
	    	swapped = false;
	    	j++;
	    	for (int i = 0; i < 3 - j; i++) 
	    	{                                       
	    		if (pts[i][0].x > pts[i + 1][0].x) 
	    		{                          
	    			tmp = pts[i];
	    			pts[i] = pts[i + 1];
	    			pts[i + 1] = tmp;
	    			swapped = true;
	    		}
	    	}                
	    }
	    return(pts);
	}
	
	/**********************************************************************
	 * Helper function to find the matching 3D of the 2D point (after it has been sorted)
	 * to obtain the corresponding z-value.
	 * 
	 * @param p
	 * 			The 2D point to be matched.
	 * 
	 * @param p1
	 *          First 3D point to match to p.
	 * @param p2
	 *          Second 3D point to match to p.
	 * @param p3
	 *          Third 3D point to match to p.
	 * @return 
	 *          The z-value from the matching 3D point.
	 */
	private static Point3D match2D_3D(Point2D p, Point3D p1, Point3D p2, Point3D p3)
	{
		if (p.x == (int) p1.x && p.y == (int) p1.y)
		{
			return p1;
		}
		else if (p.x == (int) p2.x && p.y == (int) p2.y)
		{
			return p2;
		}
		else
		{
			return p3;
		}
	}
}