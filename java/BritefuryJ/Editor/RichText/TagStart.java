//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.RichText;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Blank;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Row;

abstract class TagStart extends Tag
{
	protected Pres presentTagContents()
	{
		return new Blank();
	}

	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return new Row( new Object[] { tagStyle.applyTo( new Label( "<" + getTagName() ) ), presentTagContents(), tagStyle.applyTo( new Label( ">" ) ) } );
	}
}
