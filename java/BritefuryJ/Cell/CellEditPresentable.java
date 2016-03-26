//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Cell;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;

public interface CellEditPresentable
{
	public Pres editCellPresent(FragmentView fragment, SimpleAttributeTable inheritedState);
}
