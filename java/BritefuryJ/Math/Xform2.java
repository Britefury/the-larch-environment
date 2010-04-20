//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.Math;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class Xform2 implements Cloneable
{
	public static final Xform2 identity = new Xform2();
	
	
	public double scale;
	public Vector2 translation;
	

	public Xform2()
	{
		scale = 1.0;
		translation = new Vector2();
	}
	
	public Xform2(double scale)
	{
		this.scale = scale;
		translation = new Vector2();
	}
	
	public Xform2(Vector2 translation)
	{
		this.scale = 1.0;
		this.translation = translation.clone();
	}
	
	public Xform2(double scale, Vector2 translation)
	{
		this.scale = scale;
		this.translation = translation.clone();
	}
	
	
	
	public Xform2 clone()
	{
		try
		{
			return (Xform2)super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			return null;
		}
	}
	
	
	public boolean equals(Xform2 b)
	{
		if ( this == b )
		{
			return true;
		}
		
		return scale == b.scale  &&  translation.equals( b.translation );
	}

	
	
	public Xform2 inverse()
	{
		double invScale = 1.0 / scale;
		return new Xform2( invScale, translation.mul( -invScale ) );
	}
	
	
	public Xform2 concat(Xform2 b)
	{
		return new Xform2( scale*b.scale, translation.mul( b.scale ).add( b.translation ) );
	}
	
	
	public Vector2 transform(Vector2 v)
	{
		return v.mul( scale );
	}

	public Point2 transform(Point2 p)
	{
		return new Point2( p.x * scale + translation.x,  p.y * scale + translation.y );
	}

	public AABox2 transform(AABox2 b)
	{
		return new AABox2( transform( b.getLower() ), transform( b.getUpper() ) );
	}
	
	
	public double scale(double x)
	{
		return x * scale;
	}
	
	
	public AffineTransform toAffineTransform()
	{
		AffineTransform x = new AffineTransform();
		x.scale( scale, scale );
		x.translate( translation.x, translation.y );
		return x;
	}
	
	
	
	public void apply(Graphics2D graphics)
	{
		graphics.translate( translation.x, translation.y );
		graphics.scale( scale, scale );
	}
	
	
	public String toString()
	{
		return "Xform2( translation=" + translation.toString() + ", scale=" + String.valueOf( scale ) + " )";
	}
	
	
	public static Xform2 inverseOf(double scale, Vector2 translation)
	{
		double invScale = 1.0 / scale;
		return new Xform2( invScale, translation.mul( -invScale ) );
	}

	public static Xform2 inverseOf(double scale)
	{
		return new Xform2( 1.0 / scale );
	}

	public static Xform2 inverseOf(Vector2 translation)
	{
		return new Xform2( translation.negate() );
	}
}
