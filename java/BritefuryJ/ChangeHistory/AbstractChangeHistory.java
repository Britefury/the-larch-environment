//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ChangeHistory;

import BritefuryJ.ObjectPresentation.PresentationStateListenerList;
import org.python.core.PyObject;

import java.util.ArrayList;

public abstract class AbstractChangeHistory {
	private ArrayList<ChangeHistoryListener> listeners = new ArrayList<ChangeHistoryListener>();


	public void addChangeHistoryListener(ChangeHistoryListener listener)
	{
		listeners.add( listener );
	}

	public void removeChangeHistoryListener(ChangeHistoryListener listener)
	{
		listeners.remove( listener );
	}




	public abstract ChangeHistory concreteChangeHistory();

	public abstract boolean canUndo();
	public abstract boolean canRedo();

	public abstract int getNumUndoChanges();
	public abstract int getNumRedoChanges();


	protected void onModified()
	{
		for (ChangeHistoryListener listener: listeners)
		{
			listener.onChangeHistoryChanged( this );
		}
	}
}
