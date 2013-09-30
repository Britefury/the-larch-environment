//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ChangeHistory;

public class ChangeHistoryProxy extends AbstractChangeHistory implements ChangeHistoryListener {
	private AbstractChangeHistory underlyingChangeHistory;


	public ChangeHistoryProxy(AbstractChangeHistory ch) {
		this.underlyingChangeHistory = null;

		setChangeHistory(ch);
	}


	public void setChangeHistory(AbstractChangeHistory ch) {
		if (this.underlyingChangeHistory != null) {
			this.underlyingChangeHistory.removeChangeHistoryListener(this);
		}
		this.underlyingChangeHistory = ch;
		if (this.underlyingChangeHistory != null) {
			this.underlyingChangeHistory.addChangeHistoryListener(this);
		}
		onModified();
	}


	public ChangeHistory concreteChangeHistory() {
		return underlyingChangeHistory != null  ?  underlyingChangeHistory.concreteChangeHistory()  :  null;
	}

	public boolean canUndo() {
		return underlyingChangeHistory != null && underlyingChangeHistory.canUndo();
	}

	public boolean canRedo() {
		return underlyingChangeHistory != null && underlyingChangeHistory.canRedo();
	}

	public int getNumUndoChanges() {
		return underlyingChangeHistory != null  ?  underlyingChangeHistory.getNumUndoChanges()  :  0;
	}

	public int getNumRedoChanges() {
		return underlyingChangeHistory != null  ?  underlyingChangeHistory.getNumRedoChanges()  :  0;
	}


	public void onChangeHistoryChanged(AbstractChangeHistory history) {
		onModified();
	}


	@Override
	public String toString() {
		return "ChangeHistoryProxy<" + System.identityHashCode(this) + ">(" + underlyingChangeHistory + ")";
	}
}
