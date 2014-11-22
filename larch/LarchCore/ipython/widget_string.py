from BritefuryJ.Incremental import IncrementalValueMonitor
from BritefuryJ.Live import LiveValue, LiveFunction

from BritefuryJ.Controls import TextEntry

from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.Primitive import Blank, Label, Row, Spacer

from LarchCore.ipython.widget import IPythonWidgetView



class TextView (IPythonWidgetView):
	class _Listener(TextEntry.TextEntryListener):
		def __init__(self, view, value_live):
			self._view = view
			self._value_live = value_live

		def onAccept(self, textEntry, text):
			self._view._on_edit(text)


	def _on_edit(self, new_value):
		sync_data = {'value': new_value}
		self._internal_update(sync_data)
		self.model.send_sync(sync_data)
		self._value_live.setLiteralValue(new_value)

	def __present__(self, fragment, inh):
		self._incr.onAccess()
		value = unicode(self.value)
		value_live = LiveValue(value)
		listener = self._Listener(self, value_live)
		return Row([Label(self.description), Spacer(10.0, 0.0),
			    TextEntry(value, listener)])


