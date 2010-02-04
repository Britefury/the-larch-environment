//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;


public class VBoxStyleSheet extends AbstractBoxStyleSheet
{
	public static VBoxStyleSheet defaultStyleSheet = new VBoxStyleSheet();
	
	
	public VBoxStyleSheet()
	{
		this( 0.0 );
	}
	
	public VBoxStyleSheet(double spacing)
	{
		super( spacing );
	}
}
