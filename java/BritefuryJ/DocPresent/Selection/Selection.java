//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Selection;

import java.util.ArrayList;

import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPRegion;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Marker.MarkerListener;

public class Selection implements MarkerListener
{
	private Marker marker0, marker1;
	
	private Marker startMarker, endMarker;
	private ArrayList<DPElement> startPathFromCommonRoot, endPathFromCommonRoot;
	private DPContainer commonRoot;
	protected ArrayList<SelectionListener> listeners;
	
	private boolean  bRefreshRequired;
	
	
	public Selection()
	{
		bRefreshRequired = false;
		
		marker0 = new Marker();
		marker1 = new Marker();
		
		marker0.addMarkerListener( this );
		marker1.addMarkerListener( this );
	}
	
	
	
	public boolean isEmpty()
	{
		refresh();
		return startMarker == null;
	}
	
	
	public void setRegion(Marker m0, Marker m1)
	{
		marker0.moveTo( m0 );
		marker1.moveTo( m1 );
	}
	
	
	
	public void clear()
	{
		marker0.clear();
		marker1.clear();
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
	
	public DPContainer getCommonRoot()
	{
		refresh();
		return commonRoot;
	}
	
	
	public DPRegion getRegion()
	{
		if ( isEmpty() )
		{
			return null;
		}
		else
		{
			DPContainer root = getCommonRoot();
			if ( root != null )
			{
				return root.getRegion();
			}
			else
			{
				return marker0.getElement().getRegion();
			}
		}
	}
	
	
	
	public void addSelectionListener(SelectionListener listener)
	{
		if ( listeners == null )
		{
			listeners = new ArrayList<SelectionListener>();
		}
		listeners.add( listener );
	}
	
	public void removeSelectionListener(SelectionListener listener)
	{
		if ( listeners != null )
		{
			listeners.remove( listener );
			if ( listeners.isEmpty() )
			{
				listeners = null;
			}
		}
	}
	

	
	public void onStructureChanged()
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
			commonRoot = null;
			
			if ( listeners != null )
			{
				for (SelectionListener listener: listeners)
				{
					listener.selectionChanged( this );
				}
			}
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
						throw new RuntimeException( "Paths have length 1, but elements are different" );
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
			
			bRefreshRequired = false;
		}
	}


	public void markerChanged(Marker m)
	{
		modified();
	}
}
