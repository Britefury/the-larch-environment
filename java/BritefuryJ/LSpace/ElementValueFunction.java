//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.Util.RichString.RichStringBuilder;

public interface ElementValueFunction
{
	public Object computeElementValue(LSElement element);
	public void addPrefixToRichString(RichStringBuilder builder, LSElement element);
	public void addSuffixToRichString(RichStringBuilder builder, LSElement element);
}
