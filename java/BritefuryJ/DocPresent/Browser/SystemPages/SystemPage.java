//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Font;

import BritefuryJ.DocPresent.DPLink;
import BritefuryJ.DocPresent.DPStaticText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Browser.Page;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.ElementStyleSheet;

public abstract class SystemPage extends Page
{
	protected String systemLocation;
	
	
	protected void register(String systemLocation) 
	{
		this.systemLocation = systemLocation;
		SystemLocationResolver.getSystemResolver().registerPage( systemLocation, this );
	}
	
	protected String getSystemLocation()
	{
		return systemLocation;
	}
	
	protected String getLocation()
	{
		return SystemLocationResolver.systemLocationToLocation( systemLocation );
	}



	public DPWidget getContentsElement()
	{
		ElementStyleSheet pageBoxStyle = DPVBox.styleSheet( VTypesetting.NONE, HAlignment.EXPAND, 40.0, false, 0.0 );
		DPVBox pageBox = new DPVBox( pageBoxStyle );
		
		ElementStyleSheet headBoxStyle = DPVBox.styleSheet( VTypesetting.NONE, HAlignment.EXPAND, 0.0, false, 0.0 );
		DPVBox headBox = new DPVBox( headBoxStyle );
		
		ElementStyleSheet titleBoxStyle = DPVBox.styleSheet( VTypesetting.NONE, HAlignment.CENTRE, 0.0, false, 0.0 );
		DPVBox titleBox = new DPVBox( titleBoxStyle );
		
		ElementStyleSheet titleStyle = DPStaticText.styleSheet( new Font( "Serif", Font.BOLD, 32 ), Color.BLACK );
		DPStaticText title = new DPStaticText( titleStyle, "System page: " + getTitle() );
		titleBox.append( title );
		
		headBox.append( SystemRootPage.createLinkHeader( SystemRootPage.LINKHEADER_ROOTPAGE | SystemRootPage.LINKHEADER_SYSTEMPAGE ) );
		headBox.append( titleBox );

		pageBox.append( headBox );
		pageBox.append( createContents() );
		
		return pageBox;
	}


	protected DPLink createLink()
	{
		return new DPLink( getTitle(), getLocation() );
	}

	
	protected abstract String getTitle();
	protected abstract DPWidget createContents();
}
