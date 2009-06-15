//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.StyleSheets.WidgetStyleSheet;
import BritefuryJ.Math.Point2;

public class DPEmpty extends DPWidget
{
	public DPEmpty()
	{
		this( WidgetStyleSheet.defaultStyleSheet );
	}
	
	public DPEmpty(WidgetStyleSheet styleSheet)
	{
		super( styleSheet );
	}

	
	
	protected void updateRequisitionX()
	{
		layoutBox.clearRequisitionX();
	}

	protected void updateRequisitionY()
	{
		layoutBox.clearRequisitionY();
	}
	



	protected DPWidget getLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		if ( filter.testEmpty( this ) )
		{
			return this;
		}
		else
		{
			return null;
		}
	}
}
