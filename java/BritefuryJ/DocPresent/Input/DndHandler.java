//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Input;

import java.awt.datatransfer.Transferable;

import javax.swing.TransferHandler;

public abstract class DndHandler
{
	public static int COPY = TransferHandler.COPY;
	public static int COPY_OR_MOVE = TransferHandler.COPY_OR_MOVE;
	public static int LINK = TransferHandler.LINK;
	public static int MOVE = TransferHandler.MOVE;
	public static int NONE = TransferHandler.NONE;

	
	
	public int getSourceRequestedAction(PointerInputElement sourceElement, PointerInterface pointer, int button)
	{
		return COPY;
	}
	
	public Transferable createTransferable(PointerInputElement sourceElement)
	{
		return null;
	}
	
	public void exportDone(PointerInputElement sourceElement, Transferable data, int action)
	{
	}

	
	
	
	public boolean canDrop(PointerInputElement destElement, DndDrop drop)
	{
		return false;
	}
	
	public boolean acceptDrop(PointerInputElement destElement, DndDrop drop)
	{
		return false;
	}
}
