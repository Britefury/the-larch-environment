//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Input;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.TransferHandler;

import BritefuryJ.Math.Point2;

public class DndDropSwing extends DndDrop
{
	protected TransferHandler.TransferSupport support;
	
	
	public DndDropSwing(PointerInputElement targetElement, Point2 targetPosition, TransferHandler.TransferSupport support)
	{
		super( targetElement, targetPosition );

		this.support = support;
	}




	public int getSourceDropActions()
	{
		return support.getSourceDropActions();
	}
	
	public DataFlavor[] getDataFlavors()
	{
		return support.getDataFlavors();
	}
	
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		return support.isDataFlavorSupported( flavor );
	}
	
	
	
	public int getDropAction()
	{
		return support.getDropAction();
	}
	
	public Transferable getTransferable()
	{
		return support.getTransferable();
	}
	
	public int getUserDropAction()
	{
		return support.getUserDropAction();
	}
	
	public void setDropAction(int action)
	{
		support.setDropAction( action );
	}
}
