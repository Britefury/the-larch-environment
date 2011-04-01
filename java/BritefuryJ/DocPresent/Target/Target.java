//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Target;

import java.awt.Graphics2D;
import java.util.ArrayList;

public abstract class Target
{
	private ArrayList<TargetListener> listeners = new ArrayList<TargetListener>();;

	
	
	public abstract void draw(Graphics2D graphics);


	public void addTargetListener(TargetListener listener)
	{
		listeners.add( listener );
	}
	
	public void removeTargetListener(TargetListener listener)
	{
		listeners.remove( listener );
	}



	protected void notifyListenersOfChange()
	{
		for (TargetListener listener: listeners)
		{
			listener.targetChanged( this );
		}
	}
}
