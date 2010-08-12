//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser;

import java.awt.Color;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.GenericPerspective.Presentable;
import BritefuryJ.GSym.GenericPerspective.PresCom.GenericStyle;
import BritefuryJ.GSym.GenericPerspective.PresCom.ObjectBox;
import BritefuryJ.GSym.PresCom.LocationAsInnerFragment;
import BritefuryJ.GSym.View.GSymFragmentView;

public class Location implements Presentable
{
	//
	//
	// PRESENTABLE
	//
	//
	
	private static class LocationPresentable implements Presentable
	{
		private Location loc;
		
		
		protected LocationPresentable(Location loc)
		{
			this.loc = loc;
		}


		@Override
		public Pres present(GSymFragmentView fragment, SimpleAttributeTable inheritedState)
		{
			// TODO: Check for infinitely recursive nesting
			return new LocationAsInnerFragment( loc );
		}
	}
	
	
	
	
	private String locationString;
	
	
	public Location(String loc)
	{
		this.locationString = loc;
	}
	
	
	public String getLocationString()
	{
		return locationString;
	}
	
	
	
	public Location __add__(String x)
	{
		return new Location( locationString + x );
	}
	
	public Location __add__(Location x)
	{
		return new Location( locationString + x.locationString );
	}
	
	public Location join(String x)
	{
		return new Location( locationString + x );
	}
	
	public Location join(Location x)
	{
		return new Location( locationString + x.locationString );
	}
	
	
	public String toString()
	{
		return locationString;
	}
	
	public boolean equals(Object x)
	{
		if ( x == this )
		{
			return true;
		}
		
		if ( x instanceof String )
		{
			return locationString.equals( x );
		}
		else if ( x instanceof Location )
		{
			return locationString.equals( ((Location)x).locationString );
		}
		else
		{
			return false;
		}
	}
	
	public int hashCode()
	{
		return locationString.hashCode();
	}


	public Pres present(GSymFragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return locationStyle.applyTo( new ObjectBox( "Location", new StaticText( locationString ) ) );
	}
	
	
	public Presentable presentable()
	{
		return new LocationPresentable( this );
	}


	private static final StyleSheet locationStyle = StyleSheet.instance.withAttr( GenericStyle.objectBorderPaint, new Color( 0.4f, 0.65f, 0.4f ) )
			.withAttr( GenericStyle.objectTitlePaint, new Color( 0.4f, 0.65f, 0.4f ) ); 
}
