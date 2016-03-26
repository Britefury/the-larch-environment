//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Clipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

public abstract class DataExporterInterface <SelectionContentsType>
{
	abstract protected List<DataFlavor> getTransferDataFlavors(SelectionContentsType selectionContents);
	abstract protected Object getTransferData(SelectionContentsType selectionContents, DataFlavor flavor) throws UnsupportedFlavorException, IOException;
}
