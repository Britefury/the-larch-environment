//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.Table;

class TableSelectionContents
{
	protected Object contents[][];
	protected TableCellExportedValue exportContents[][];
	
	
	protected TableSelectionContents(Object contents[][], TableCellExportedValue exportContents[][])
	{
		this.contents = contents;
		this.exportContents = exportContents;
	}
}
