//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Input;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import BritefuryJ.LSpace.LSElement;

public class DndDragSwing
{
	protected LSElement sourceElement;
	protected int sourceButton;
	
	protected boolean bInProgress;
	
	protected Transferable transferable;
	protected int sourceDropActions;
	protected int dropAction, userDropAction;
	
	

	protected DndDragSwing(LSElement sourceElement, int sourceButton)
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
	
	
	
	public LSElement getSourceElement()
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
