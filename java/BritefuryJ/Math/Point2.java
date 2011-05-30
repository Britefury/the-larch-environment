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
import BritefuryJ.Util.HashUtils;


public class Point2 implements Presentable, Serializable
{
	private static final long serialVersionUID = 1L;

	public double x, y;
	
	
	
	public Point2()
	{
		x = y = 0.0;
	}
	
	public Point2(double x, double y)
	{
		this.x = x;
		this.y = y;
	}
	
	public Point2(Vector2 v)
	{
		this.x = v.x;
		this.y = v.y;
	}
	
	
	public Point2 copy()
	{
		return new Point2( x, y );
	}
	
	
	public boolean equals(Point2 b)
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

	
	
	public Point2 add(Vector2 b)
	{
		return new Point2( x+b.x, y+b.y );
	}
	
	public Point2 __add__(Vector2 b)
	{
		return new Point2( x+b.x, y+b.y );
	}
	
	public Vector2 sub(Point2 b)
	{
		return new Vector2( x-b.x, y-b.y );
	}
	
	public Vector2 __sub__(Point2 b)
	{
		return new Vector2( x-b.x, y-b.y );
	}
	
	public Point2 sub(Vector2 b)
	{
		return new Point2( x-b.x, y-b.y );
	}
	
	public Point2 __sub__(Vector2 b)
	{
		return new Point2( x-b.x, y-b.y );
	}
	
	public Point2 scale(double s)
	{
		return new Point2( x*s, y*s );
	}
	
	
	
	public Vector2 toVector2()
	{
		return new Vector2( x, y );
	}
	
	
	public Point2 transform(AffineTransform affine)
	{
		Point2D.Double p = new Point2D.Double( x, y );
		affine.transform( p, p );
		return new Point2( p.x, p.y );
	}
	
	
	public static Point2 average(Point2 a, Point2 b)
	{
		return new Point2( ( a.x + b.x ) * 0.5,  ( a.y + b.y ) * 0.5 );
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
		return "Point2(" + Double.toString( x ) + "," + Double.toString( y ) + ")";
	}

	
	
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return new Row( new Pres[] { delimStyle.applyTo( new Label( "(" ) ).alignVCentre(),
				vectorStyle.applyTo( new Column( new Pres[] { new Label( String.valueOf( x ) ), new Label( String.valueOf( x ) ) } ) ),
				delimStyle.applyTo( new Label( ")" ) ).alignVCentre() } );
	}
	
	private static StyleSheet vectorStyle = StyleSheet.instance.withAttr( Primitive.fontFace, "Serif" ).withAttr( Primitive.columnSpacing, 2.0 );
	private static StyleSheet delimStyle = StyleSheet.instance.withAttr( Primitive.fontScale, 2.1 );
}
