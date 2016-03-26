//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
