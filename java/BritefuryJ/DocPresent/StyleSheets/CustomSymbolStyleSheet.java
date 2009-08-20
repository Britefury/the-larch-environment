//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

import java.awt.Color;
import java.awt.Paint;

public class CustomSymbolStyleSheet extends ContentLeafStyleSheet
{
	public static CustomSymbolStyleSheet defaultStyleSheet = new CustomSymbolStyleSheet();
	
	
	private Paint symbolPaint;
	
	
	public CustomSymbolStyleSheet()
	{
		this( Color.black );
	}
	
	public CustomSymbolStyleSheet(Paint symbolPaint)
	{
		super();
		
		this.symbolPaint = symbolPaint;
	}
	
	
	public Paint getSymbolPaint()
	{
		return symbolPaint;
	}
}
