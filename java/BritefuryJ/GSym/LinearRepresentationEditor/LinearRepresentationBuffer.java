//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.LinearRepresentationEditor;

import BritefuryJ.DocPresent.Clipboard.LocalDataFlavor;
import BritefuryJ.Parser.ItemStream.ItemStream;

public class LinearRepresentationBuffer
{
	public static final LocalDataFlavor dataFlavor = new LocalDataFlavor( LinearRepresentationBuffer.class );
	
	protected ItemStream stream;
	
	public LinearRepresentationBuffer(ItemStream stream)
	{
		this.stream = stream;
	}
	
	
	protected boolean isTextual()
	{
		return stream.isTextual();
	}
	
	protected String getTextualValue()
	{
		return stream.textualValue();
	}
}
