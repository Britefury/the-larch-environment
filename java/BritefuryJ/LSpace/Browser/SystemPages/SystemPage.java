//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.Browser.SystemPages;

import java.util.ArrayList;

import BritefuryJ.LSpace.Browser.BrowserPage;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Head;
import BritefuryJ.Pres.RichText.NormalText;
import BritefuryJ.Pres.RichText.Page;
import BritefuryJ.Pres.RichText.TitleBar;
import BritefuryJ.StyleSheet.StyleSheet;

public abstract class SystemPage extends BrowserPage
{
	public Pres getContentsPres()
	{
		Pres linkHeader = SystemRootPage.createLinkHeader( SystemRootPage.LINKHEADER_ROOTPAGE | SystemRootPage.LINKHEADER_SYSTEMPAGE );
		Pres title = new TitleBar( "System page: " + getTitle() );
		
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


	protected String getDescription()
	{
		return null;
	}
	
	protected abstract Pres createContents();


	private static final StyleSheet staticStyle = StyleSheet.style( Primitive.editable.as( false ) );
}