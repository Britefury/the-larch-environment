//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.DefaultPerspective;

import java.awt.Font;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Browser.Page;
import BritefuryJ.DocPresent.Clipboard.EditHandler;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.GSymBrowserContext;
import BritefuryJ.GSym.GSymLocationResolver;
import BritefuryJ.GSym.GSymPerspective;
import BritefuryJ.GSym.GSymSubject;
import BritefuryJ.GSym.ObjectView.GSymObjectViewLocationTable;
import BritefuryJ.GSym.ObjectView.Presentable;
import BritefuryJ.GSym.View.GSymFragmentViewContext;
import BritefuryJ.GSym.View.GSymViewContext;
import BritefuryJ.GSym.View.GSymViewFragmentFunction;

public class GSymDefaultPerspective implements GSymPerspective
{
	private static final PrimitiveStyleSheet asStringStyle = PrimitiveStyleSheet.instance.withFont( new Font( "Sans serif", Font.ITALIC, 14 ) );
	
	
	private class GSymObjectViewFragmentFunction implements GSymViewFragmentFunction
	{
		public DPElement createViewFragment(Object x, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
		{
			DPElement result;
			if ( x instanceof Presentable )
			{
				Presentable p = (Presentable)x;
				result = p.present( ctx, styleSheet, state );
			}
			else
			{
				result = asStringStyle.text( x.toString() );
			}
			result.setDebugName( x.getClass().getName() );
			return result;
		}
	}

	
	
	private static class DefaultPerspectiveLocationResolver implements GSymLocationResolver
	{
		private GSymDefaultPerspective perspective;
		
		
		public DefaultPerspectiveLocationResolver(GSymDefaultPerspective perspective)
		{
			this.perspective = perspective;
		}
		
		
		@Override
		public Page resolveLocationAsPage(Location location)
		{
			GSymSubject subject = perspective.resolveLocation( null, location.iterator() );
			if ( subject != null )
			{
				GSymViewContext viewContext = new GSymViewContext( subject, perspective.browserContext, null );
				return viewContext.getPage();
			}
			else
			{
				return null;
			}
		}

		@Override
		public GSymSubject resolveLocationAsSubject(Location location)
		{
			return perspective.resolveLocation( null, location.iterator() );
		}
	}
	
	
	private GSymObjectViewLocationTable locationTable = new GSymObjectViewLocationTable();
	private GSymObjectViewFragmentFunction fragmentViewFn = new GSymObjectViewFragmentFunction();
	private DefaultPerspectiveLocationResolver locationResolver = new DefaultPerspectiveLocationResolver( this );
	private GSymBrowserContext browserContext;
	
	
	public GSymDefaultPerspective(GSymBrowserContext browserContext)
	{
		this.browserContext = browserContext;
	}

	


	public GSymViewFragmentFunction getFragmentViewFunction()
	{
		return fragmentViewFn;
	}
	
	public StyleSheet getStyleSheet()
	{
		return PrimitiveStyleSheet.instance;
	}
	
	public AttributeTable getInitialState()
	{
		return AttributeTable.instance;
	}
	
	public Object createInitialState(GSymSubject subject)
	{
		return null;
	}

	public EditHandler getEditHandler()
	{
		return null;
	}


	public GSymSubject resolveLocation(GSymSubject enclosingSubject, Location.TokenIterator relativeLocation)
	{
		Object x = locationTable.getObjectAtLocation( relativeLocation );
		if ( x != null )
		{
			String title = x != null  ?  x.getClass().getName()  :  "<null>";
			return new GSymSubject( x, this, title, AttributeTable.instance );
		}
		else
		{
			return null;
		}
	}



	public Location getLocationForObject(Object x)
	{
		return locationTable.getLocationForObject( x );
	}
	
	public Object getObjectAtLocation(Location location)
	{
		return locationTable.getObjectAtLocation( location.iterator() );
	}
	
	
	public GSymLocationResolver getLocationResolver()
	{
		return locationResolver;
	}
}
