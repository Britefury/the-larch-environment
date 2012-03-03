//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Editor.Sequential;

import BritefuryJ.LSpace.Clipboard.LocalDataFlavor;

public class SequentialBuffer
{
	public static final LocalDataFlavor dataFlavor = new LocalDataFlavor( SequentialBuffer.class );
	
	protected Object sequential;
	protected SequentialClipboardHandler clipboardHandler;
	
	
	public SequentialBuffer(Object sequential, SequentialClipboardHandler clipboardHandler)
	{
		this.sequential = sequential;
		this.clipboardHandler = clipboardHandler;
	}
}
