##-*************************
##-* This software can be used, redistributed and/or modified under
##-* the terms of the BSD 2-clause license as found in the file
##-* 'License.txt' in this distribution.
##-* This source code is (C)copyright Geoffrey French 1999-2014.
##-*************************

import os


if os.name == 'java':
	from org.python.core.util import StringUtil

	from org.zeromq import ZMQ

	str_to_bytes = StringUtil.toBytes
	bytes_to_str = StringUtil.fromBytes

	ZMQ_new_context = ZMQ.context

	class ZMQReadPoller (object):
		def __init__(self, n_sockets=4):
			self.__poller = ZMQ.Poller(n_sockets)
			self.__indices_and_sockets = []


		def register(self, socket):
			index = self.__poller.register(socket, ZMQ.Poller.POLLIN)
			self.__indices_and_sockets.append((index, socket))

		def poll(self, timeout, event_callback):
			n_events_processed = 0
			n_events = self.__poller.poll(timeout)
			while n_events > 0:
				for index, socket in self.__indices_and_sockets:
					if self.__poller.pollin(index):
						n_events_processed += 1
						event_callback(socket)


				n_events = self.__poller.poll(0)
			return n_events_processed


	def zmq_subscribe_socket(socket, topic):
		socket.subscribe(str_to_bytes(topic))

	def zmq_recv_multipart(stream):
		msg_list = [stream.recv()]
		while stream.hasReceiveMore():
			msg_list.append(stream.recv())
		return msg_list

	def zmq_send_multipart(stream, msg_parts):
		for part in msg_parts[:-1]:
			stream.sendMore(part)
		stream.send(msg_parts[-1])

else:
	import zmq as ZMQ

	def _identity(x):
		return x

	str_to_bytes = _identity
	bytes_to_str = _identity

	ZMQ_new_context = ZMQ.Context

	class ZMQReadPoller (object):
		def __init__(self):
			self.__poller = ZMQ.Poller()


		def register(self, socket):
			self.__poller.register(socket, ZMQ.POLLIN)

		def poll(self, timeout, event_callback):
			events = self.__poller.poll(timeout)
			for socket, event_flags in events:
				if event_flags & ZMQ.POLLIN == 0:
					raise ValueError, 'Event is not read'
				event_callback(socket)

			return len(events)


	def zmq_subscribe_socket(socket, topic):
		socket.set(ZMQ.SUBSCRIBE, topic)

	def zmq_recv_multipart(stream):
		return stream.recv_multipart()

	def zmq_send_multipart(stream, msg_parts):
		stream.send_multipart(msg_parts)


class MessageRouter(object):
	'''
	    Message router

	    Takes an incoming message and invokes a corresponding handler method on the attached object
	    '''

	def __init__(self, instance, socket_name):
		'''
		Message router constructor

		:param instance: the object on which handler methods can be found
		:param socket_name: the name of the socket that the router receives messages from
		'''
		self.__handler_method_cache = {}
		self.__instance = instance
		self.__socket_name = socket_name


	def handle(self, idents, msg):
		'''
		Handle a message

		Will look for a handler method on the attached instance (given as an argument to the constructor).
		Will look for a method called _handle_msg_<socket_name>_<msg_type>.
		Handler methods should be of the form:
		def _handle_msg_iopub_status(self, idents, msg)

		The message router for the 'iopub' socket will router messages whose msg_type is 'status' to
		the above method.

		:param idents: the ZeroMQ idents
		:param msg: the message to route
		'''

		msg_type = msg['msg_type']
		try:
			bound_method = self.__handler_method_cache[msg_type]
		except KeyError:
			method_name = '_handle_msg_{0}_{1}'.format(self.__socket_name, msg_type)
			try:
				bound_method = getattr(self.__instance, method_name)
			except AttributeError:
				bound_method = None
			self.__handler_method_cache[msg_type] = bound_method

		if bound_method is not None:
			return bound_method(idents, msg)
		else:
			print 'WARNING: socket {0} did not handle message of type {1} with ident {2}'.format(
				self.__socket_name, msg_type, idents)



