//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.DPContainerSequence;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;

public abstract class CollatedBranchElement extends CollatableSequenceBranchElement implements CollatedElementInterface
{
	private ElementCollator collator;
	
	
	//
	// Constructor
	//
	
	public CollatedBranchElement(ContainerStyleSheet styleSheet)
	{
		super( styleSheet );
		
		this.collator = null;
	}

	
	//
	// Widget
	//
	
	public DPContainerSequence getWidget()
	{
		return (DPContainerSequence)getContainer();
	}
	
	
	
	//
	// Collation methods
	//
	
	protected void setCollationMode(CollationMode m)
	{
		super.setCollationMode( m );
		
		if ( collationMode == CollationMode.ROOT )
		{
			collator = new ElementCollator( this );
		}
		else if ( collationMode == CollationMode.CONTENTSCOLLATED )
		{
			collator = null;
		}
		else
		{
			collator = null;
		}
	}
	
	
	protected void refreshContainerWidgetContents()
	{
		collator.refreshContainerWidgetContents();
	}
}
