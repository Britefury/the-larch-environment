//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.ObjectView;

import java.awt.Font;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Browser.LocationResolver;
import BritefuryJ.DocPresent.Browser.Page;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.GSymBrowserContext;
import BritefuryJ.GSym.IncrementalContext.GSymIncrementalNodeContext;
import BritefuryJ.GSym.View.GSymFragmentViewContext;
import BritefuryJ.GSym.View.GSymViewContext;
import BritefuryJ.GSym.View.GSymViewFragmentFunction;

public class GSymObjectView
{
	private static final PrimitiveStyleSheet asStringStyle = PrimitiveStyleSheet.instance.withFont( new Font( "Sans serif", Font.ITALIC, 14 ) );
	
	
	private class GSymObjectViewFragmentFunction implements GSymViewFragmentFunction
	{
		public DPElement createViewFragment(Object x, GSymIncrementalNodeContext ctx, StyleSheet styleSheet, Object state)
		{
			GSymFragmentViewContext fragmentCtx = (GSymFragmentViewContext)ctx;
			if ( x instanceof Presentable )
			{
				Presentable p = (Presentable)x;
				return p.present( fragmentCtx, state );
			}
			else
			{
				return asStringStyle.text( x.toString() );
			}
		}
	}
	
	
	private class GSymObjectViewPage extends Page
	{
		private Object x;
		private DPElement element;
		
		public GSymObjectViewPage(Object x)
		{
			this.x = x;
			element = createObjectView( this, x );
		}
		
		public DPElement getContentsElement()
		{
			return element;
		}

		public String getTitle()
		{
			if ( x != null )
			{
				return x.getClass().getName();
			}
			else
			{
				return "<null>";
			}
		}
	}
	
	
	private class GSymObjectViewLocationResolver implements LocationResolver
	{
		public DPElement resolveLocationAsElement(Page page, String location)
		{
			Object x = locationTable.getObjectAtLocation( location );
			if ( x != null )
			{
				return createObjectView( page, x );
			}
			else
			{
				return null;
			}
		}

		public Page resolveLocationAsPage(String location)
		{
			Object x = locationTable.getObjectAtLocation( location );
			if ( x != null )
			{
				return new GSymObjectViewPage( x );
			}
			else
			{
				return null;
			}
		}
	}
	
	
	private GSymObjectViewFragmentFunction viewFragFn = new GSymObjectViewFragmentFunction();
	private GSymObjectViewLocationTable locationTable = new GSymObjectViewLocationTable();
	private GSymObjectViewLocationResolver locationResolver = new GSymObjectViewLocationResolver();
	private GSymBrowserContext browserContext;
	
	
	public GSymObjectView(GSymBrowserContext browserContext)
	{
		this.browserContext = browserContext;
	}
	
	
	public LocationResolver getLocationResolver()
	{
		return locationResolver;
	}
	
	
	public String getLocationForObject(Object x)
	{
		return locationTable.getLocationForObject( x );
	}
	
	public Object getObjectAtLocation(String location)
	{
		return locationTable.getObjectAtLocation( location );
	}
	
	private DPElement createObjectView(Page page, Object x)
	{
		GSymViewContext viewContext = new GSymViewContext( x, viewFragFn, PrimitiveStyleSheet.instance, null, browserContext, page, null );
		return viewContext.getRegion();
	}
}
