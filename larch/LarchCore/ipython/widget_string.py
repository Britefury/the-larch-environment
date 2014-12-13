from BritefuryJ.Incremental import IncrementalValueMonitor
from BritefuryJ.Live import LiveValue, LiveFunction

from BritefuryJ.Controls import TextEntry, TextArea

from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.Primitive import Blank, Label, Row, Spacer, Column

from LarchCore.ipython.widget import IPythonWidgetView



class TextView (IPythonWidgetView):
	class _Listener(TextEntry.TextEntryListener):
		def __init__(self, view, value_live):
			self._view = view
			self._value_live = value_live

		def onAccept(self, text_entry, text):
			self._view._on_edit(text)
			self._value_live.setLiteralValue(text)


	def _on_edit(self, new_value):
		sync_data = {'value': new_value}
		self._internal_update(sync_data)
		self.model.send_sync(sync_data)

	def __present__(self, fragment, inh):
		self._incr.onAccess()
		value = unicode(self.value)
		value_live = LiveValue(value)
		listener = self._Listener(self, value_live)
		return Row([Label(self.description), Spacer(10.0, 0.0),
			    TextEntry(value, listener)])



class TextareaView (IPythonWidgetView):
	class _Listener(TextArea.TextAreaListener):
		def __init__(self, view, value_live):
			self._view = view
			self._value_live = value_live

		def onAccept(self, text_area, text):
			self._view._on_edit(text)
			self._value_live.setLiteralValue(text)


	def _on_edit(self, new_value):
		sync_data = {'value': new_value}
		self._internal_update(sync_data)
		self.model.send_sync(sync_data)

	def __present__(self, fragment, inh):
		self._incr.onAccess()
		value = unicode(self.value)
		value_live = LiveValue(value)
		listener = self._Listener(self, value_live)
		return Column([Label(self.description), Spacer(0.0, 1.0),
			    TextArea(value, listener)])

