//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Selection;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPRegion;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Marker.MarkerListener;

public class TextSelection extends Selection implements MarkerListener
{
	private Marker marker0, marker1;
	
	private Marker startMarker, endMarker;
	private ArrayList<DPElement> startPathFromCommonRoot, endPathFromCommonRoot;
	private DPContainer commonRootContainer;
	private DPElement commonRoot;
	
	private boolean  bRefreshRequired;
	
	
	public TextSelection(DPElement element, Marker m0, Marker m1)
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
	
	public ArrayList<DPElement> getStartPathFromCommonRoot()
	{
		refresh();
		return startPathFromCommonRoot;
	}
	
	public ArrayList<DPElement> getEndPathFromCommonRoot()
	{
		refresh();
		return endPathFromCommonRoot;
	}
	
	public DPContainer getCommonRootContainer()
	{
		refresh();
		return commonRootContainer;
	}
	
	public DPElement getCommonRoot()
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
	public DPRegion getRegion()
	{
		DPElement root = getCommonRoot();
		if ( root != null )
		{
			return root.getRegion();
		}
		else
		{
			return marker0.getElement().getRegion();
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
				DPElement w0 = marker0.getElement();
				ArrayList<DPElement> path0 = new ArrayList<DPElement>();
				DPElement w1 = marker1.getElement();
				ArrayList<DPElement> path1 = new ArrayList<DPElement>();
				DPElement.getPathsFromCommonSubtreeRoot( w0, path0, w1, path1 );
				
				boolean bInOrder = true;
				commonRootContainer = null;
				commonRoot = null;
				
				if ( path0.size() > 1  &&  path1.size() > 1 )
				{
					commonRootContainer = (DPContainer)path0.get( 0 );
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
			Marker startMarker = getStartMarker();
			Marker endMarker = getEndMarker();
			List<DPElement> startPath = getStartPathFromCommonRoot();
			List<DPElement> endPath = getEndPathFromCommonRoot();
	
			Color prevColour = graphics.getColor();
			graphics.setColor( Color.yellow );
			getCommonRoot().getRootElement().drawTextRange( graphics, startMarker, startPath, endMarker, endPath );
			graphics.setColor( prevColour );
		}
	}
}
