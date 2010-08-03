//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.GenericPerspective.Presentable;
import BritefuryJ.GSym.GenericPerspective.PresCom.GenericStyle;
import BritefuryJ.GSym.GenericPerspective.PresCom.ObjectBox;
import BritefuryJ.GSym.PresCom.LocationAsInnerFragement;
import BritefuryJ.GSym.View.GSymFragmentView;

public class Location implements Presentable
{
	public static class TokenIterator
	{
		private Location loc;
		private TokenIterator parent;
		private int position;
		
		
		protected TokenIterator(Location loc, TokenIterator parent, int pos)
		{
			this.loc = loc;
			this.parent = parent;
			this.position = pos;
		}
		
		
		public Location getLocation()
		{
			return loc;
		}
		
		public String getPrefix()
		{
			return loc.locationString.substring( 0, position );
		}
		
		public String getSuffix()
		{
			return loc.locationString.substring( position, loc.locationString.length() );
		}
		
		public String lastToken()
		{
			int start = parent != null  ?  parent.position  :  0;
			return loc.locationString.substring( start, position );
		}
		
		public TokenIterator back()
		{
			return parent;
		}
		
		public TokenIterator consumeLiteral(String value)
		{
			if ( ( ( position + value.length() ) <= loc.locationString.length() )  &&  loc.locationString.subSequence( position, position + value.length()).equals( value ) )
			{
				return new TokenIterator( loc, this, position + value.length() );
			}
			else
			{
				return null;
			}
		}
		
		public TokenIterator consumeUpTo(String pattern)
		{
			String suffix = getSuffix();
			int index = suffix.indexOf( pattern );
			if ( index == -1 )
			{
				return new TokenIterator( loc, this, loc.locationString.length() );
			}
			else
			{
				return new TokenIterator( loc, this, position + index );
			}
		}

		public TokenIterator consumeRegex(Pattern pattern)
		{
			String input = loc.locationString;
			Matcher m = pattern.matcher( input.subSequence( position, input.length() ) );
			
			boolean bFound = m.find();
			if ( bFound  &&  m.start() == 0  &&  m.end() > 0 )
			{
				String match = m.group();
				return new TokenIterator( loc, this, position + match.length() );
			}
			else
			{
				return null;
			}
		}
		
		public TokenIterator consumeRemainder()
		{
			return new TokenIterator( loc, this, loc.locationString.length() );
		}
	}
	
	
	
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
			return new LocationAsInnerFragement( loc );
		}
	}
	
	
	
	
	private String locationString;
	
	
	public Location(String loc)
	{
		this.locationString = loc;
	}
	
	
	public TokenIterator iterator()
	{
		return new TokenIterator( this, null, 0 );
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
