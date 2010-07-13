//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.SequentialEditor;

import BritefuryJ.DocPresent.Clipboard.LocalDataFlavor;
import BritefuryJ.DocPresent.StreamValue.StreamValue;

public class SequentialBuffer
{
	public static final LocalDataFlavor dataFlavor = new LocalDataFlavor( SequentialBuffer.class );
	
	protected StreamValue stream;
	
	public SequentialBuffer(StreamValue stream)
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
