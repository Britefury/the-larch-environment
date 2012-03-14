//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.Util.RichString.RichStringBuilder;

public interface ElementValueFunction
{
	public Object computeElementValue(LSElement element);
	public void addPrefixToRichString(RichStringBuilder builder, LSElement element);
	public void addSuffixToRichString(RichStringBuilder builder, LSElement element);
}
