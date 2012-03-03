//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace.Selection;

import java.awt.Graphics2D;
import java.util.ArrayList;

import BritefuryJ.LSpace.LSContainer;
import BritefuryJ.LSpace.LSContentLeafEditable;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSRegion;
import BritefuryJ.LSpace.ElementTreeVisitor;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.Marker.MarkerListener;

public class TextSelection extends Selection implements MarkerListener
{
	private static class DrawVisitor extends ElementTreeVisitor
	{
		private Graphics2D graphics;
		
		
		public DrawVisitor(Graphics2D graphics)
		{
			this.graphics = graphics;
		}
		
		
		@Override
		protected void preOrderVisitElement(LSElement e, boolean complete)
		{
		}

		@Override
		protected void inOrderCompletelyVisitElement(LSElement e)
		{
			if ( e instanceof LSContentLeafEditable )
			{
				( (LSContentLeafEditable)e ).drawTextSelection( graphics, 0, e.getTextRepresentationLength() );
			}
		}

		@Override
		protected void postOrderVisitElement(LSElement e, boolean complete)
		{
		}

		@Override
		protected void inOrderVisitPartialContentLeafEditable(LSContentLeafEditable e, int startIndex, int endIndex)
		{
			e.drawTextSelection( graphics, startIndex, endIndex );
		}

		@Override
		protected boolean shouldVisitChildrenOfElement(LSElement e, boolean completeVisit)
		{
			return true;
		}
	}
	
	
	
	private Marker marker0, marker1;
	
	private Marker startMarker, endMarker;
	private ArrayList<LSElement> startPathFromCommonRoot, endPathFromCommonRoot;
	private LSContainer commonRootContainer;
	private LSElement commonRoot;
	
	private boolean  bRefreshRequired;
	
	
	public TextSelection(LSElement element, Marker m0, Marker m1)
	{
		super( element );
		
		bRefreshRequired = true;
		
		marker0 = m0.copy();
		marker1 = m1.copy();
		
		marker0.addMarkerListener( this );
		marker1.addMarkerListener( this );
	}
	
	
	public void clear()
	{
		rootElement.setSelection( null );
	}
	
	
	
	public Marker getStartMarker()
	{
		refresh();
		return startMarker;
	}
	
	public Marker getEndMarker()
	{
		refresh();
		return endMarker;
	}
	
	public ArrayList<LSElement> getStartPathFromCommonRoot()
	{
		refresh();
		return startPathFromCommonRoot;
	}
	
	public ArrayList<LSElement> getEndPathFromCommonRoot()
	{
		refresh();
		return endPathFromCommonRoot;
	}
	
	public LSContainer getCommonRootContainer()
	{
		refresh();
		return commonRootContainer;
	}
	
	public LSElement getCommonRoot()
	{
		refresh();
		return commonRoot;
	}
	
	public boolean isValid()
	{
		refresh();
		return commonRoot != null;
	}
	
	
	@Override
	public LSRegion getRegion()
	{
		LSElement root = getCommonRoot();
		if ( root != null )
		{
			return root.getRegion();
		}
		else
		{
			return marker0.getElement().getRegion();
		}
	}
	
	
	@Override
	public boolean isEditable()
	{
		if ( isValid() )
		{
			LSRegion region = getRegion();
			return region != null  &&  region.isEditable();
		}
		else
		{
			return false;
		}
	}

	
	public void onPresentationTreeStructureChanged()
	{
		modified();
	}



	
	private void modified()
	{
		if ( !bRefreshRequired )
		{
			bRefreshRequired = true;
			startMarker = endMarker = null;
			startPathFromCommonRoot = endPathFromCommonRoot = null;
			commonRootContainer = null;
			commonRoot = null;
			
			notifyListenersOfChange();
		}
	}
	
	private void refresh()
	{
		if ( bRefreshRequired )
		{
			if ( !marker0.equals( marker1 )  &&  marker0.isValid()  &&  marker1.isValid())
			{
				LSElement w0 = marker0.getElement();
				ArrayList<LSElement> path0 = new ArrayList<LSElement>();
				LSElement w1 = marker1.getElement();
				ArrayList<LSElement> path1 = new ArrayList<LSElement>();
				LSElement.getPathsFromCommonAncestor( w0, path0, w1, path1 );
				
				boolean bInOrder = true;
				commonRootContainer = null;
				commonRoot = null;
				
				if ( path0.size() > 1  &&  path1.size() > 1 )
				{
					commonRootContainer = (LSContainer)path0.get( 0 );
					bInOrder = commonRootContainer.areChildrenInOrder( path0.get( 1 ), path1.get( 1 ) );
					commonRoot = commonRootContainer;
				}
				else if ( path0.size() == 1  &&  path1.size() == 1 )
				{
					if ( w0 != w1 )
					{
						throw new RuntimeException( "Paths have length 1, but elements are different" );
					}
					bInOrder = Marker.markerOrder( marker0, marker1 )  ==  1;
					commonRoot = w0;
				}
				else
				{
					throw new RuntimeException( "Paths should either both have length == 1, or both have length > 1" );
				}
				
				
				startMarker = bInOrder  ?  marker0  :  marker1;
				endMarker = bInOrder  ?  marker1  :  marker0;
				startPathFromCommonRoot = bInOrder  ?  path0  :  path1;
				endPathFromCommonRoot = bInOrder  ?  path1  :  path0;
			}
			
			bRefreshRequired = false;
		}
	}


	public void markerChanged(Marker m)
	{
		modified();
	}
	
	
	
	public void draw(Graphics2D graphics)
	{
		if ( isValid() )
		{
			DrawVisitor v = new DrawVisitor( graphics );
			v.visitTextSelection( this );
		}
	}
}
