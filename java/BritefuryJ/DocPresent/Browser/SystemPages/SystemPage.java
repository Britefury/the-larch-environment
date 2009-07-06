//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Font;

import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Browser.Page;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public abstract class SystemPage extends Page
{
	protected void register(String systemLocation) 
	{
		SystemLocationResolver.getSystemResolver().registerPage( systemLocation, this );
	}



	public DPWidget getContentsElement()
	{
		VBoxStyleSheet pageBoxStyle = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.EXPAND, 40.0, false, 10.0 );
		DPVBox pageBox = new DPVBox( pageBoxStyle );
		
		VBoxStyleSheet titleBoxStyle = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.CENTRE, 0.0, false, 0.0 );
		DPVBox titleBox = new DPVBox( titleBoxStyle );
		
		TextStyleSheet titleStyle = new TextStyleSheet( new Font( "Serif", Font.BOLD, 32 ), Color.BLACK );
		DPText title = new DPText( titleStyle, "System page: " + getTitle() );
		titleBox.append( title );
		
		pageBox.append( titleBox );
		pageBox.append( createContents() );
		
		return pageBox;
	}



	protected abstract String getTitle();
	protected abstract DPWidget createContents();
}
