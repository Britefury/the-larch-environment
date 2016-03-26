//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Input;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.TransferHandler;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Math.Point2;
import BritefuryJ.Util.HashUtils;

public class DndDropSwing
{
	protected LSElement targetElement;
	protected Point2 targetPosition;
	protected TransferHandler.TransferSupport support;
	
	
	public DndDropSwing(LSElement targetElement, Point2 targetPosition, TransferHandler.TransferSupport support)
	{
		this.targetElement = targetElement;
		this.targetPosition = targetPosition;

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




	public LSElement getTargetElement()
	{
		return targetElement;
	}




	public Point2 getTargetPosition()
	{
		return targetPosition;
	}
	
	
	
	
	@Override
	public boolean equals(Object x)
	{
		if ( this == x )
		{
			return true;
		}
		
		if ( x instanceof DndDropSwing )
		{
			DndDropSwing dx = (DndDropSwing)x;
			return targetElement == dx.targetElement  &&  targetPosition.equals( dx.targetPosition )  &&  support.equals( dx.support );  
		}
		else
		{
			return false;
		}
	}
	
	
	@Override
	public int hashCode()
	{
		int a = targetElement.hashCode();
		int b = targetPosition.hashCode();
		int c = support.hashCode();
		return HashUtils.tripleHash( a, b, c );
	}
}
