//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.Table;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.Cell.AbstractBlankCell;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Bin;

public abstract class AbstractBlankTableCell extends AbstractBlankCell
{
	public AbstractBlankTableCell(Pres blankPres)
	{
		super( blankPres );
	}

	
	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres cellContents = presentCell( fragment, inheritedState );
		//return new Bin( new Segment( cellContents.alignHPack().alignVRefY() ) ).alignHPack().alignVRefY();
		return new Bin( cellContents.alignHPack().alignVRefY() ).alignHPack().alignVRefY();
	}
}
