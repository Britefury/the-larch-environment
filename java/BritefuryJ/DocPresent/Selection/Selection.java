//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Selection;

import java.util.ArrayList;

import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Marker.Marker;

public class Selection
{
	private Marker marker0, marker1;
	
	private Marker startMarker, endMarker;
	private ArrayList<DPWidget> startPathFromCommonRoot, endPathFromCommonRoot;
	private DPContainer commonRoot;
	
	
	public Selection(DPPresentationArea area, Marker marker0, Marker marker1)
	{
		this.marker0 = marker0;
		this.marker1 = marker1;
		
		area.registerSelection( this );
	}
	
	
	public void set(Marker marker0, Marker marker1)
	{
		this.marker0 = marker0;
		this.marker1 = marker1;
		modified();
	}
	
	
	
	public boolean isEmpty()
	{
		return marker0.equals( marker1 );
	}
	
	
	
	public Marker getMarker0()
	{
		return marker0;
	}
	
	public Marker getMarker1()
	{
		return marker1;
	}
	
	
	
	public void setMarker0(Marker marker0)
	{
		this.marker0 = marker0;
		modified();
	}
	
	public void setMarker1(Marker marker1)
	{
		this.marker1 = marker1;
		modified();
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
	
	public ArrayList<DPWidget> getStartPathFromCommonRoot()
	{
		refresh();
		return startPathFromCommonRoot;
	}
	
	public ArrayList<DPWidget> getEndPathFromCommonRoot()
	{
		refresh();
		return endPathFromCommonRoot;
	}
	
	public DPContainer getCommonRoot()
	{
		refresh();
		return commonRoot;
	}
	
	
	
	public void onStructureChanged()
	{
		modified();
	}



	
	private void modified()
	{
		startMarker = endMarker = null;
		startPathFromCommonRoot = endPathFromCommonRoot = null;
		commonRoot = null;
	}
	
	private void refresh()
	{
		if ( startMarker == null )
		{
			if ( !isEmpty() )
			{
				DPWidget w0 = marker0.getWidget();
				ArrayList<DPWidget> path0 = new ArrayList<DPWidget>();
				DPWidget w1 = marker1.getWidget();
				ArrayList<DPWidget> path1 = new ArrayList<DPWidget>();
				DPWidget.getPathsFromCommonSubtreeRoot( w0, path0, w1, path1 );
				
				boolean bInOrder = true;
				commonRoot = null;
				
				if ( path0.size() > 1  &&  path1.size() > 1 )
				{
					commonRoot = (DPContainer)path0.get( 0 );
					bInOrder = commonRoot.areChildrenInOrder( path0.get( 1 ), path1.get( 1 ) );
				}
				else if ( path0.size() == 1  &&  path1.size() == 1 )
				{
					if ( w0 != w1 )
					{
						throw new RuntimeException( "Paths have length 1, but widgets are different" );
					}
					bInOrder = marker0.getIndex()  <  marker1.getIndex();
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
		}
	}
}
