//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ChangeHistory;

public class ChangeHistoryProxy extends AbstractChangeHistory implements ChangeHistoryListener {
	private AbstractChangeHistory ch;


	public ChangeHistoryProxy(AbstractChangeHistory ch) {
		this.ch = null;

		setChangeHistory(ch);
	}


	public void setChangeHistory(AbstractChangeHistory ch) {
		if (this.ch != null) {
			this.ch.removeChangeHistoryListener(this);
		}
		this.ch = ch;
		if (this.ch != null) {
			this.ch.addChangeHistoryListener(this);
		}
	}


	public ChangeHistory concreteChangeHistory() {
		return ch.concreteChangeHistory();
	}

	public boolean canUndo() {
		return ch.canUndo();
	}

	public boolean canRedo() {
		return ch.canRedo();
	}

	public int getNumUndoChanges() {
		return ch.getNumUndoChanges();
	}

	public int getNumRedoChanges() {
		return ch.getNumRedoChanges();
	}


	public void onChangeHistoryChanged(AbstractChangeHistory history) {
		onModified();
	}
}
