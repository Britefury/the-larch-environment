//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
