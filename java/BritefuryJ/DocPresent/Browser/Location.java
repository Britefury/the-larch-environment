//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser;

import java.awt.Color;
import java.awt.Font;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.GSym.DefaultPerspective.DefaultPerspectiveStyleSheet;
import BritefuryJ.GSym.ObjectView.Presentable;
import BritefuryJ.GSym.View.GSymFragmentViewContext;

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
		public DPElement present(GSymFragmentViewContext ctx, DefaultPerspectiveStyleSheet styleSheet, AttributeTable state)
		{
			// TODO: Check for infinitely recursive nesting
			return ctx.presentLocationAsElement( loc );
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


	public DPElement present(GSymFragmentViewContext ctx, DefaultPerspectiveStyleSheet styleSheet, AttributeTable state)
	{
		DPElement label = labelStyle.staticText( "Location" );
		
		DPElement locLabel = PrimitiveStyleSheet.instance.staticText( locationString );
		
		return borderStyle.border( PrimitiveStyleSheet.instance.vbox( new DPElement[] { label, locLabel.padX( 5.0, 0.0 ) } ) );
	}
	
	
	public Presentable presentable()
	{
		return new LocationPresentable( this );
	}


	private static PrimitiveStyleSheet labelStyle = PrimitiveStyleSheet.instance.withFont( new Font( "Sans serif", Font.PLAIN, 10 ) ).withForeground( new Color( 0.4f, 0.65f, 0.4f ) ).withTextSmallCaps( true );
	private static PrimitiveStyleSheet borderStyle = PrimitiveStyleSheet.instance.withBorder( new SolidBorder( 1.0, 3.0, 5.0, 5.0, new Color( 0.4f, 0.65f, 0.4f ), null ) ); 
}
