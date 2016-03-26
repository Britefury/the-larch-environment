//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
