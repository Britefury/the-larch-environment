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
	public final static int COPY = TransferHandler.COPY;
	public final static int COPY_OR_MOVE = TransferHandler.COPY_OR_MOVE;
	public final static int LINK = TransferHandler.LINK;
	public final static int MOVE = TransferHandler.MOVE;
	public final static int NONE = TransferHandler.NONE;
	
	public final static int ASPECT_NONE = 0;
	public final static int ASPECT_NORMAL = 0x1;
	public final static int ASPECT_DOC_NODE = 0x2;

	
	
	public abstract boolean isSource(PointerInputElement sourceElement);

	public int getSourceRequestedAction(PointerInputElement sourceElement, PointerInterface pointer, int button)
	{
		return COPY;
	}
	
	public int getSourceRequestedAspect(PointerInputElement sourceElement, PointerInterface pointer, int button)
	{
		return ASPECT_NORMAL;
	}
	
	public Transferable createTransferable(PointerInputElement sourceElement, int aspect)
	{
		return null;
	}
	
	public void exportDone(PointerInputElement sourceElement, Transferable data, int action)
	{
	}
	

	
	
	
	public abstract boolean isDest(PointerInputElement sourceElement);
	public abstract boolean isNonLocalDest(PointerInputElement sourceElement);

	public boolean canDrop(PointerInputElement destElement, DndDrop drop)
	{
		return false;
	}
	
	public boolean acceptDrop(PointerInputElement destElement, DndDrop drop)
	{
		return false;
	}
}
