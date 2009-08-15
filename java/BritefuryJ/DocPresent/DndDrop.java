//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import BritefuryJ.Math.Point2;

public abstract class DndDrop
{
	protected DPWidget targetElement;
	protected Point2 targetPosition;
	
	
	
	protected DndDrop()
	{
	}
	
	protected DndDrop(DPWidget targetElement, Point2 targetPosition)
	{
		this.targetElement = targetElement;
		this.targetPosition = targetPosition;
	}
	
	
	
	public abstract Transferable getTransferable();

	public abstract DataFlavor[] getDataFlavors();
	public abstract boolean isDataFlavorSupported(DataFlavor flavor);
	
	public abstract int getSourceDropActions();
	public abstract int getDropAction();
	public abstract void setDropAction(int action);
	public abstract int getUserDropAction();
	



	public DPWidget getTargetElement()
	{
		return targetElement;
	}
	
	public Point2 getTargetPosition()
	{
		return targetPosition;
	}
	
	
	protected void setTarget(DPWidget element, Point2 pos)
	{
		targetElement = element;
		targetPosition = pos;
	}
}
