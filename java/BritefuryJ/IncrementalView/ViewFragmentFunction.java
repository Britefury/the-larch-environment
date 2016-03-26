//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.IncrementalView;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.Pres.Pres;

public interface ViewFragmentFunction
{
	public Pres createViewFragment(Object x, FragmentView ctx, SimpleAttributeTable inheritedState);
}
