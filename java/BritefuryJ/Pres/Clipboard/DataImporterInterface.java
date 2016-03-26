//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Clipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import BritefuryJ.LSpace.Clipboard.DataTransfer;
import BritefuryJ.LSpace.Focus.Selection;
import BritefuryJ.LSpace.Focus.Target;

public abstract class DataImporterInterface <TargetType extends Target>
{
	abstract protected boolean canImportFlavor(DataFlavor flavor);
	abstract protected boolean canImport(TargetType target, Selection selection, DataTransfer dataTransfer, DataFlavor flavor) throws UnsupportedFlavorException, IOException;
	abstract protected Object importData(TargetType target, Selection selection, DataTransfer dataTransfer, DataFlavor flavor) throws UnsupportedFlavorException, IOException;
}
