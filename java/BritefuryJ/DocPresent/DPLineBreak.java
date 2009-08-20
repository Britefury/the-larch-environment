//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;

public class DPLineBreak extends DPBin implements LineBreakInterface
{
	private int lineBreakPriority;
	
	
	public DPLineBreak()
	{
		this( 0 );
	}
	
	public DPLineBreak(ContainerStyleSheet styleSheet)
	{
		this( styleSheet, 0 );
	}
	
	public DPLineBreak(int lineBreakPriority)
	{
		this( ContainerStyleSheet.defaultStyleSheet, lineBreakPriority );
	}
	
	public DPLineBreak(ContainerStyleSheet styleSheet, int lineBreakPriority)
	{
		super( styleSheet );
		this.lineBreakPriority = lineBreakPriority;
		layoutReqBox.setLineBreakCost( lineBreakPriority );
	}
	
	
	void setLineBreakPriority(int lineBreakPriority)
	{
		this.lineBreakPriority = lineBreakPriority;
		layoutReqBox.setLineBreakCost( lineBreakPriority );
		queueResize();
	}
	
	
	public int getLineBreakPriority()
	{
		return lineBreakPriority;
	}
	
	
	
	protected void updateRequisitionX()
	{
		super.updateRequisitionX();
		
		layoutReqBox.setLineBreakCost( lineBreakPriority );
	}

	protected void updateRequisitionY()
	{
		super.updateRequisitionY();
		
		layoutReqBox.setLineBreakCost( lineBreakPriority );
	}

	
	
	public LineBreakInterface getLineBreakInterface()
	{
		return this;
	}
}
