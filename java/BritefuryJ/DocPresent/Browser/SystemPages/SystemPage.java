//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.util.ArrayList;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Browser.Page;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.RichText.Head;
import BritefuryJ.DocPresent.Combinators.RichText.NormalText;
import BritefuryJ.DocPresent.Combinators.RichText.TitleBar;

public abstract class SystemPage extends Page
{
	public DPElement getContentsElement()
	{
		Pres linkHeader = SystemRootPage.createLinkHeader( SystemRootPage.LINKHEADER_ROOTPAGE | SystemRootPage.LINKHEADER_SYSTEMPAGE );
		Pres title = new TitleBar( "System page: " + getTitle() );
		
		Pres head = new Head( new Pres[] { linkHeader, title } );

		ArrayList<Object> pageChildren = new ArrayList<Object>();
		pageChildren.add( head );
		String description = getDescription();
		if ( description != null )
		{
			pageChildren.add( new NormalText( description ) );
		}
		pageChildren.add( createContents().alignHExpand() );
		
		return new BritefuryJ.DocPresent.Combinators.RichText.Page( pageChildren ).present();
	}


	protected String getDescription()
	{
		return null;
	}
	
	protected abstract Pres createContents();
}
