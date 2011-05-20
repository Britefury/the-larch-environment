//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.Math;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.Serializable;

import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyObject;
import org.python.core.PyTuple;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.Utils.HashUtils;

public class Vector2 implements Presentable, Serializable
{
	private static final long serialVersionUID = 1L;

	public double x, y;
	
	
	
	public Vector2()
	{
		x = y = 0.0;
	}
	
	public Vector2(double x, double y)
	{
		this.x = x;
		this.y = y;
	}
	
	
	public Vector2 copy()
	{
		return new Vector2( x, y );
	}
	
	
	public boolean equals(Vector2 b)
	{
		if ( this == b )
		{
			return true;
		}
		
		return x == b.x  &&  y == b.y;
	}
	
	public int hashCode()
	{
		return HashUtils.doubleHash( new Double( x ).hashCode(), new Double( y ).hashCode() );
	}
	
	
	
	public Vector2 add(Vector2 b)
	{
		return new Vector2( x+b.x, y+b.y );
	}
	
	public Vector2 __add__(Vector2 b)
	{
		return new Vector2( x+b.x, y+b.y );
	}
	
	public Vector2 sub(Vector2 b)
	{
		return new Vector2( x-b.x, y-b.y );
	}
	
	public Vector2 __sub__(Vector2 b)
	{
		return new Vector2( x-b.x, y-b.y );
	}
	
	public Vector2 mul(double s)
	{
		return new Vector2( x*s, y*s );
	}
	
	public Vector2 __mul__(double s)
	{
		return new Vector2( x*s, y*s );
	}
	
	public double dot(Vector2 v)
	{
		return x * v.x  +  y * v.y;
	}
	
	public Vector2 negate()
	{
		return new Vector2( -x, -y );
	}
	
	public Vector2 __neg__()
	{
		return new Vector2( -x, -y );
	}
	
	
	public double sqrLength()
	{
		return dot( this );
	}
	
	public double length()
	{
		return Math.sqrt( sqrLength() );
	}
	
	
	public Vector2 getNormalised()
	{
		double oneOverLength = 1.0 / length();
		return mul( oneOverLength );
	}
	
	
	public Vector2 transform(AffineTransform affine)
	{
		Point2D.Double origin = new Point2D.Double( 0.0, 0.0 );
		Point2D.Double v = new Point2D.Double( x, y );
		affine.transform( origin, origin );
		affine.transform( v, v );
		return new Vector2( v.x - origin.x, v.y - origin.y );
	}
	
	
	public PyObject __getstate__()
	{
		return new PyTuple( Py.newFloat( x ), Py.newFloat( y ) );
	}
	
	public void __setstate__(PyObject state)
	{
		if ( state instanceof PyTuple )
		{
			PyTuple tup = (PyTuple)state;
			if ( tup.size() == 2 )
			{
				x = tup.pyget( 0 ).asDouble();
				y = tup.pyget( 1 ).asDouble();
			}
			else
			{
				throw Py.TypeError( "State tuple must contain two items" );
			}
		}
		else
		{
			throw Py.TypeError( "State must be a tuple" );
		}
	}
	
	public PyObject __reduce__()
	{
		return new PyTuple( Py.java2py( getClass() ), new PyTuple(), __getstate__() );
	}
	
	
	public PyObject __copy__()
	{
		return Py.java2py( copy() );
	}
	
	public PyObject __deepcopy__(PyDictionary memo)
	{
		return Py.java2py( copy() );
	}
	
	
	public String toString()
	{
		return "Vector2(" + Double.toString( x ) + "," + Double.toString( y ) + ")";
	}

	
	
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return new Row( new Pres[] { delimStyle.applyTo( new Label( "[" ) ).alignVCentre(),
				vectorStyle.applyTo( new Column( new Pres[] { new Label( String.valueOf( x ) ), new Label( String.valueOf( x ) ) } ) ),
				delimStyle.applyTo( new Label( "]" ) ).alignVCentre() } );
	}
	
	private static StyleSheet vectorStyle = StyleSheet.instance.withAttr( Primitive.fontFace, "Serif" ).withAttr( Primitive.columnSpacing, 2.0 );
	private static StyleSheet delimStyle = StyleSheet.instance.withAttr( Primitive.fontScale, 2.1 );
}
