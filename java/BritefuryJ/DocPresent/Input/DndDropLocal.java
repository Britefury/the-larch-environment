//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Input;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

public class DndDropLocal extends DndDrop
{
	protected PointerInputElement sourceElement;
	protected int sourceButton;
	
	protected boolean bInProgress;
	
	protected Transferable transferable;
	protected int sourceDropActions;
	protected int dropAction, userDropAction;
	
	

	protected DndDropLocal(PointerInputElement sourceElement, int sourceButton)
	{
		super();
		
		this.sourceElement = sourceElement;
		this.sourceButton = sourceButton;
		bInProgress = false;
	}
	
	
	
	protected void initialise(Transferable transferable, int sourceDropActions)
	{
		bInProgress = true;
		this.transferable = transferable;
		this.sourceDropActions = sourceDropActions;
	}

	
	
	public Transferable getTransferable()
	{
		return transferable;
	}
	
	
	
	public PointerInputElement getSourceElement()
	{
		return sourceElement;
	}
	

	
	public DataFlavor[] getDataFlavors()
	{
		return transferable.getTransferDataFlavors();
	}

	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		return transferable.isDataFlavorSupported( flavor );
	}

	
	
	public int getSourceDropActions()
	{
		return sourceDropActions;
	}
	
	public int getDropAction()
	{
		return dropAction;
	}
	
	public void setDropAction(int action)
	{
		dropAction = action;
	}

	public int getUserDropAction()
	{
		return userDropAction;
	}
}
