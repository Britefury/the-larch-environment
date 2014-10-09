##-*************************
##-* This software can be used, redistributed and/or modified under
##-* the terms of the BSD 2-clause license as found in the file
##-* 'License.txt' in this distribution.
##-* This source code is (C)copyright Geoffrey French 1999-2014.
##-*************************

import hashlib, hmac, uuid, datetime, json

from .util import str_to_bytes, bytes_to_str, zmq_recv_multipart, zmq_send_multipart


_DELIM = str_to_bytes("<IDS|MSG>")
_KERNEL_PROTOCOL_VERSION = b'5.0'




class Session(object):
	def __init__(self, key, username=''):
		'''
		IPython session constructor

		:param key: message authentication key from connection file
		:param username: Username of user (or empty string)
		:return:
		'''
		self.__key = key.encode('utf8')

		self.auth = hmac.HMAC(self.__key, digestmod=hashlib.sha256)

		self.session = str(uuid.uuid4())
		self.username = username

		self.__none = self._pack({})


	def send(self, stream, msg_type, content=None, parent=None, metadata=None, ident=None, buffers=None):
		'''
		Build and sent a message on a JeroMQ stream

		:param stream: the JeroMQ stream over which the message is to be sent
		:param msg_type: the message type (see IPython docs for explanation of these)
		:param content: message content
		:param parent: message parent header
		:param metadata: message metadata
		:param ident: IDENT
		:param buffers: binary data buffers to append to message
		:return: a tuple of (message structure, message ID)
		'''
		msg, msg_id = self.build_msg(msg_type, content, parent, metadata)
		to_send = self.serialize(msg, ident)
		if buffers is not None:
			to_send.extend(buffers)
		zmq_send_multipart(stream, to_send)
		return msg, msg_id

	def recv(self, stream):
		'''
		Receive a message from a stream
		:param stream: the JeroMQ stream from which to read the message
		:return: a tuple: (idents, msg) where msg is the deserialized message
		'''
		msg_list = zmq_recv_multipart(stream)

		# Extract identities
		pos = msg_list.index(_DELIM)
		idents, msg_list = msg_list[:pos], msg_list[pos + 1:]
		return idents, self.deserialize(msg_list)


	def serialize(self, msg, ident=None):
		'''
		Serialize a message into a list of byte arrays

		:param msg: the message to serialize
		:param ident: the ident
		:return: the serialize message in the form of a list of byte arrays
		'''
		content = msg.get('content', {})
		if content is None:
			content = self.__none
		else:
			content = self._pack(content)

		payload = [self._pack(msg['header']),
			   self._pack(msg['parent_header']),
			   self._pack(msg['metadata']),
			   content]

		serialized = []

		if isinstance(ident, list):
			serialized.extend(ident)
		elif ident is not None:
			serialized.append(ident)
		serialized.append(_DELIM)

		signature = self.sign(payload)
		serialized.append(signature)
		serialized.extend(payload)

		return serialized


	def deserialize(self, msg_list):
		'''
		Deserialize a message, converting it from a list of byte arrays to a message structure (a dict)
		:param msg_list: serialized message in the form of a list of byte arrays
		:return: message structure
		'''
		min_len = 5
		if self.auth is not None:
			signature = msg_list[0]
			check = self.sign(msg_list[1:5])
			if signature != check:
				raise ValueError, 'Invalid signature'
		if len(msg_list) < min_len:
			raise ValueError, 'Message too short'
		header = self._unpack(msg_list[1])
		return {
			'header': header,
			'msg_id': header['msg_id'],
			'msg_type': header['msg_type'],
			'parent_header': self._unpack(msg_list[2]),
			'metadata': self._unpack(msg_list[3]),
			'content': self._unpack(msg_list[4]),
			'buffers': msg_list[5:]
		}


	def build_msg_header(self, msg_type):
		'''
		Build a header for a message of the given type
		:param msg_type: the message type
		:return: the message header
		'''
		msg_id = str(uuid.uuid4())
		return {
			'msg_id': msg_id,
			'msg_type': msg_type,
			'username': self.username,
			'session': self.session,
			'date': datetime.datetime.now().isoformat(),
			'version': _KERNEL_PROTOCOL_VERSION
		}

	def build_msg(self, msg_type, content=None, parent=None, metadata=None):
		'''
		Build a message of the given type, with content, parent and metadata
		:param msg_type: the message type
		:param content: message content
		:param parent: message parent header
		:param metadata: metadata
		:return: the message structure
		'''
		header = self.build_msg_header(msg_type)
		msg_id = header['msg_id']
		return {
			       'header': header,
			       'msg_id': msg_id,
			       'msg_type': msg_type,
			       'parent_header': {} if parent is None   else parent,
			       'content': {} if content is None   else content,
			       'metadata': {} if metadata is None   else metadata,
		       }, msg_id


	def sign(self, msg_payload_list):
		'''
		Sign a message payload

		:param msg_payload_list: the message payload (header, parent header, content, metadata)
		:return: signature hash hex digest
		'''
		if self.auth is None:
			return str_to_bytes('')
		else:
			h = self.auth.copy()
			for m in msg_payload_list:
				h.update(m)
			return str_to_bytes(h.hexdigest())


	def _pack(self, x):
		'''
		Pack message data into a byte array

		:param x: message data to pack
		:return: byte array
		'''
		return str_to_bytes(json.dumps(x))

	def _unpack(self, x):
		'''
		Unpack byte array into message data

		:param x: byte array to unpack
		:return: message component
		'''
		return json.loads(bytes_to_str(x))



