//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.StyleSheet.StyleValues;

public class PresentElement extends Pres
{
	private LSElement element;
	
	
	public PresentElement(LSElement element)
	{
		this.element = element;
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		return element;
	}
}
