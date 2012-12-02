//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Browser.TestPages;

import java.util.ArrayList;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Head;
import BritefuryJ.Pres.RichText.NormalText;
import BritefuryJ.Pres.RichText.Page;
import BritefuryJ.Pres.RichText.TitleBar;
import BritefuryJ.StyleSheet.StyleSheet;

public abstract class TestPage extends AbstractTestPage implements Presentable
{
	protected String getDescription()
	{
		return null;
	}
	
	protected abstract Pres createContents();
	
	
	
	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres linkHeader = TestsRootPage.createLinkHeader( TestsRootPage.LINKHEADER_SYSTEMPAGE );
		Pres title = new TitleBar( "Test page: " + getTitle() );
		
		Pres head = new Head( new Pres[] { linkHeader, title } );

		ArrayList<Object> bodyChildren = new ArrayList<Object>();
		String description = getDescription();
		if ( description != null )
		{
			bodyChildren.add( staticStyle.applyTo( new NormalText( description ) ) );
		}
		bodyChildren.add( createContents() );
		Body body = new Body( bodyChildren );
		
		return new Page( new Object[] { head, body } );
	}




	private static final StyleSheet staticStyle = StyleSheet.style( Primitive.editable.as( false ) );
}
