//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.Table;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.Cell.AbstractCell;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Bin;

public abstract class AbstractTableCell extends AbstractCell
{
	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres cellContents = presentCell( fragment, inheritedState );
		return new Bin( cellContents ).alignHExpand().alignVRefYExpand();
	}
}
