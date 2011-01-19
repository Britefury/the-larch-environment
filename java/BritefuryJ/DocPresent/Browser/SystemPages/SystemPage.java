//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.util.ArrayList;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Browser.BrowserPage;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.RichText.Head;
import BritefuryJ.Pres.RichText.NormalText;
import BritefuryJ.Pres.RichText.Page;
import BritefuryJ.Pres.RichText.TitleBar;
import BritefuryJ.StyleSheet.StyleSheet;

public abstract class SystemPage extends BrowserPage
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
			pageChildren.add( staticStyle.applyTo( new NormalText( description ) ) );
		}
		pageChildren.add( createContents().alignHExpand() );
		
		return new Page( pageChildren ).present();
	}


	protected String getDescription()
	{
		return null;
	}
	
	protected abstract Pres createContents();
	
	
	private static final StyleSheet staticStyle = StyleSheet.instance.withAttr( Primitive.editable, false );
}
