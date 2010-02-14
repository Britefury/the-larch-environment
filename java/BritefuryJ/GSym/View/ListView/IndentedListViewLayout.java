//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View.ListView;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementContext;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.EmptyBorder;

abstract class IndentedListViewLayout extends ListViewLayout
{
	private Border indentationBorder;



	public IndentedListViewLayout(float indentation)
	{
		if ( indentation == 0.0 )
		{
			indentationBorder = null;
		}
		else
		{
			indentationBorder = new EmptyBorder( indentation, 0.0, 0.0, 0.0 );
		}
	}
	
	
	protected DPWidget indent(ElementContext ctx, DPWidget child)
	{
		if ( indentationBorder != null )
		{
			DPBorder indent = new DPBorder( indentationBorder );
			indent.setChild( child );
			return indent;
		}
		else
		{
			return child;
		}
	}
}
