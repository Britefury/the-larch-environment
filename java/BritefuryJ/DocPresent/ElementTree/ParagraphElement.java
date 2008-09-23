//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import java.util.List;

import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.ParagraphStyleSheet;

public class ParagraphElement extends CollatedBranchElement
{
	private static class ParagraphCollationFilter implements CollatableBranchFilter
	{
		public boolean test(CollatableBranchElement branch)
		{
			return branch.isParagraph()  ||  branch.isProxy();
		}
	}
	
	
	public ParagraphElement()
	{
		this( ParagraphStyleSheet.defaultStyleSheet );
	}

	public ParagraphElement(ParagraphStyleSheet styleSheet)
	{
		super( styleSheet );
	}

	

	public DPParagraph getWidget()
	{
		return (DPParagraph)getContainer();
	}
	
	protected DPContainer createContainerWidget(ContainerStyleSheet styleSheet)
	{
		return new DPParagraph( (ParagraphStyleSheet)styleSheet );
	}

	
	
	protected CollatableBranchFilter createCollationFilter()
	{
		return new ParagraphCollationFilter();
	}
	

	protected void setCollatedContainerChildWidgets(List<DPWidget> childWidgets)
	{
		getWidget().setChildren( childWidgets );
	}
	

	
	
	//
	// Element type methods
	//
	
	protected boolean isParagraph()
	{
		return true;
	}
}
