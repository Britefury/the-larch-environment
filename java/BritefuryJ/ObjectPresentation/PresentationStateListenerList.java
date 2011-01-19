//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ObjectPresentation;

import java.util.WeakHashMap;

public class PresentationStateListenerList
{
	private WeakHashMap<PresentationStateListener, Object> listeners = new WeakHashMap<PresentationStateListener, Object>();
	
	
	private PresentationStateListenerList()
	{
	}
	
	private void add(PresentationStateListener listener)
	{
		listeners.put( listener, null );
	}
	
	private void onPresentationStateChanged(Object x)
	{
		for (PresentationStateListener listener: listeners.keySet())
		{
			listener.onPresentationStateChanged( x );
		}
	}
	
	
	public static PresentationStateListenerList addListener(PresentationStateListenerList listeners, PresentationStateListener listener)
	{
		if ( listeners == null )
		{
			listeners = new PresentationStateListenerList();
		}
		listeners.add( listener );
		return listeners;
	}
	
	public static PresentationStateListenerList onPresentationStateChanged(PresentationStateListenerList listeners, Object x)
	{
		if ( listeners != null )
		{
			listeners.onPresentationStateChanged( x );
			
			if ( listeners.listeners.isEmpty() )
			{
				listeners = null;
			}
		}
		
		return listeners;
	}
}
