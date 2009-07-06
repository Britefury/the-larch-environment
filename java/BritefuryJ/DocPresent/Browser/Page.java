//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser;

import BritefuryJ.DocPresent.DPWidget;

public abstract class Page
{
	private Browser browser;
	
	
	public void contentsModified()
	{
		browser.onPageContentsModified( this );
	}
	
	
	public abstract DPWidget getContentsElement();


	protected void setBrowser(Browser browser)
	{
		this.browser = browser;
	}
}
