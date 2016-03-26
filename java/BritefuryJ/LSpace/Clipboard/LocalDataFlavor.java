//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Clipboard;

import java.awt.datatransfer.DataFlavor;

public class LocalDataFlavor extends DataFlavor
{
	public LocalDataFlavor(Class<?> representationClass)
	{
		super( representationClass, DataFlavor.javaJVMLocalObjectMimeType );
	}
}
