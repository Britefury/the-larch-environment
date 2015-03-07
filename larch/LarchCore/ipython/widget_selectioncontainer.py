from BritefuryJ.Incremental import IncrementalValueMonitor
from BritefuryJ.Live import LiveValue, LiveFunction

from BritefuryJ.Controls import AccordionContainer, TabbedBox

from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.Primitive import Blank, Label, Row, Spacer, Column

from LarchCore.ipython.widget_container import ContainerView



class TabView (ContainerView):
	def _on_edit(self, selected_index):
		self._state_sync(selected_index=selected_index)

	def _present_children(self):
		child_view_ids = self.children
		children = [self._widget_manager.get_by_view_id(child_view_id)   for child_view_id in child_view_ids]

		tabs = []

		for i, child in enumerate(children):
			str_i = unicode(i)
			tabs.append([Label(self._titles.get(str_i, str_i)), child])

		def on_tab(control, tab_index):
			self._on_edit(tab_index)

		return TabbedBox(tabs, self.selected_index, on_tab)



class AccordionView (ContainerView):
	def _on_edit(self, selected_index):
		self._state_sync(selected_index=selected_index)

	def _present_children(self):
		child_view_ids = self.children
		children = [self._widget_manager.get_by_view_id(child_view_id)   for child_view_id in child_view_ids]

		tabs = []

		for i, child in enumerate(children):
			str_i = unicode(i)
			tabs.append([Label(self._titles.get(str_i, str_i)), child])

		def on_choice(control, section_index):
			self._on_edit(section_index)

		return AccordionContainer(tabs, self.selected_index, on_choice)
