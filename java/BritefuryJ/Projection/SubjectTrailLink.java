//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Projection;

import BritefuryJ.Controls.Hyperlink;

public class SubjectTrailLink
{
	private String linkText;
	private Subject subject;
	
	
	public SubjectTrailLink(String linkText, Subject subject)
	{
		this.linkText = linkText;
		this.subject = subject;
	}
	
	
	public Hyperlink hyperlink()
	{
		return new Hyperlink( linkText, subject );
	}
}
