/*
 * This file is part of RuneSource.
 *
 * RuneSource is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RuneSource is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RuneSource.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.nio.ByteBuffer;

/**
 * An abstract parent class for two buffer type objects, one for reading data
 * and one for writing data. Provides static factory methods for initializing
 * these child buffers.
 * 
 * @author blakeman8192
 */
public abstract class StreamBuffer {

	/**
	 * An enum whose values represent the possible order in which bytes are
	 * written in a multiple-byte value. Also known as "endianness".
	 * 
	 * @author blakeman8192
	 */
	public static enum ByteOrder {
		LITTLE, BIG, MIDDLE, INVERSE_MIDDLE
	}

	/**
	 * An enum whose values represent the possible custom RuneScape value types.
	 * Type A is to add 128 to the value, type C is to invert the value, and
	 * type S is to subtract the value from 128. Of course, STANDARD is just the
	 * normal data value.
	 * 
	 * @author blakeman8192
	 */
	public static enum ValueType {
		STANDARD, A, C, S
	}

	/**
	 * An enum whose values represent the current type of access to a
	 * StreamBuffer. BYTE_ACCESS is for reading/writing bytes, BIT_ACCESS is for
	 * reading/writing bits.
	 * 
	 * @author blakeman8192
	 */
	public static enum AccessType {
		BYTE_ACCESS, BIT_ACCESS
	}

	/**
	 * Bit masks.
	 */
	public static final int[] BIT_MASK = { 0, 0x1, 0x3, 0x7, 0xf, 0x1f, 0x3f, 0x7f, 0xff, 0x1ff, 0x3ff, 0x7ff, 0xfff, 0x1fff, 0x3fff, 0x7fff, 0xffff, 0x1ffff, 0x3ffff, 0x7ffff, 0xfffff, 0x1fffff, 0x3fffff, 0x7fffff, 0xffffff, 0x1ffffff, 0x3ffffff, 0x7ffffff, 0xfffffff, 0x1fffffff, 0x3fffffff, 0x7fffffff, -1 };

	/** The current AccessType of the buffer. */
	private AccessType accessType = AccessType.BYTE_ACCESS;

	/** The current bit position. */
	private int bitPosition = 0;

	/**
	 * Creates a new InBuffer.
	 * 
	 * @param data
	 *            the data
	 * @return a new InBuffer
	 */
	public static final InBuffer newInBuffer(ByteBuffer data) {
		return new InBuffer(data);
	}

	/**
	 * Creates a new OutBuffer.
	 * 
	 * @param size
	 *            the size
	 * @return a new OutBuffer
	 */
	public static final OutBuffer newOutBuffer(int size) {
		return new OutBuffer(size);
	}

	/**
	 * Handles the internal switching of the access type.
	 * 
	 * @param type
	 *            the new access type
	 */
	abstract void switchAccessType(AccessType type);

	/**
	 * Sets the AccessType of this StreamBuffer.
	 * 
	 * @param accessType
	 *            the new AccessType
	 */
	public void setAccessType(AccessType accessType) {
		this.accessType = accessType;
		switchAccessType(accessType);
	}

	/**
	 * Gets the AccessType of this StreamBuffer.
	 * 
	 * @return the current AccessType
	 */
	public AccessType getAccessType() {
		return accessType;
	}

	/**
	 * Sets the bit position.
	 * 
	 * @param bitPosition
	 *            the new bit position
	 */
	public void setBitPosition(int bitPosition) {
		this.bitPosition = bitPosition;
	}

	/**
	 * Gets the current bit position.
	 * 
	 * @return the bit position
	 */
	public int getBitPosition() {
		return bitPosition;
	}

	/**
	 * A StreamBuffer used to read incoming data.
	 * 
	 * @author blakeman8192
	 */
	public static final class InBuffer extends StreamBuffer {

		/** The internal buffer. */
		private ByteBuffer buffer;

		/**
		 * Creates a new InBuffer.
		 * 
		 * @param buffer
		 *            the buffer
		 */
		private InBuffer(ByteBuffer buffer) {
			this.buffer = buffer;
		}

		@Override
		void switchAccessType(AccessType type) {
			if (type == AccessType.BIT_ACCESS) {
				throw new UnsupportedOperationException("Reading bits is not implemented!");
			}
		}

		/**
		 * Reads a value as a byte.
		 * 
		 * @param signed
		 *            the signedness
		 * @param type
		 *            the value type
		 * @return the value
		 */
		public int readByte(boolean signed, ValueType type) {
			int value = buffer.get();
			switch (type) {
			case A:
				value = value - 128;
				break;
			case C:
				value = -value;
				break;
			case S:
				value = 128 - value;
				break;
			}
			return signed ? value : value & 0xff;
		}

		/**
		 * Reads a standard signed byte.
		 * 
		 * @return the value
		 */
		public int readByte() {
			return readByte(true, ValueType.STANDARD);
		}

		/**
		 * Reads a standard byte.
		 * 
		 * @param signed
		 *            the signedness
		 * @return the value
		 */
		public int readByte(boolean signed) {
			return readByte(signed, ValueType.STANDARD);
		}

		/**
		 * Reads a signed byte.
		 * 
		 * @param type
		 *            the value type
		 * @return the value
		 */
		public int readByte(ValueType type) {
			return readByte(true, type);
		}

		/**
		 * Reads a short value.
		 * 
		 * @param signed
		 *            the signedness
		 * @param type
		 *            the value type
		 * @param order
		 *            the byte order
		 * @return the value
		 */
		public int readShort(boolean signed, ValueType type, ByteOrder order) {
			int value = 0;
			switch (order) {
			case BIG:
				value |= readByte(false) << 8;
				value |= readByte(false, type);
				break;
			case MIDDLE:
				throw new UnsupportedOperationException("Middle-endian short is impossible!");
			case INVERSE_MIDDLE:
				throw new UnsupportedOperationException("Inverse-middle-endian short is impossible!");
			case LITTLE:
				value |= readByte(false, type);
				value |= readByte(false) << 8;
				break;
			}
			return signed ? value : value & 0xffff;
		}

		/**
		 * Reads a standard signed big-endian short.
		 * 
		 * @return the value
		 */
		public int readShort() {
			return readShort(true, ValueType.STANDARD, ByteOrder.BIG);
		}

		/**
		 * Reads a standard big-endian short.
		 * 
		 * @param signed
		 *            the signedness
		 * @return the value
		 */
		public int readShort(boolean signed) {
			return readShort(signed, ValueType.STANDARD, ByteOrder.BIG);
		}

		/**
		 * Reads a signed big-endian short.
		 * 
		 * @param type
		 *            the value type
		 * @return the value
		 */
		public int readShort(ValueType type) {
			return readShort(true, type, ByteOrder.BIG);
		}

		/**
		 * Reads a big-endian short.
		 * 
		 * @param signed
		 *            the signedness
		 * @param type
		 *            the value type
		 * @return the value
		 */
		public int readShort(boolean signed, ValueType type) {
			return readShort(signed, type, ByteOrder.BIG);
		}

		/**
		 * Reads a signed standard short.
		 * 
		 * @param order
		 *            the byte order
		 * @return the value
		 */
		public int readShort(ByteOrder order) {
			return readShort(true, ValueType.STANDARD, order);
		}

		/**
		 * Reads a standard short.
		 * 
		 * @param signed
		 *            the signedness
		 * @param order
		 *            the byte order
		 * @return the value
		 */
		public int readShort(boolean signed, ByteOrder order) {
			return readShort(signed, ValueType.STANDARD, order);
		}

		/**
		 * Reads a signed short.
		 * 
		 * @param type
		 *            the value type
		 * @param order
		 *            the byte order
		 * @return the value
		 */
		public int readShort(ValueType type, ByteOrder order) {
			return readShort(true, type, order);
		}

		/**
		 * Reads an integer.
		 * 
		 * @param signed
		 *            the signedness
		 * @param type
		 *            the value type
		 * @param order
		 *            the byte order
		 * @return the value
		 */
		public long readInt(boolean signed, ValueType type, ByteOrder order) {
			long value = 0;
			switch (order) {
			case BIG:
				value |= readByte(false) << 24;
				value |= readByte(false) << 16;
				value |= readByte(false) << 8;
				value |= readByte(false, type);
				break;
			case MIDDLE:
				value |= readByte(false) << 8;
				value |= readByte(false, type);
				value |= readByte(false) << 24;
				value |= readByte(false) << 16;
				break;
			case INVERSE_MIDDLE:
				value |= readByte(false) << 16;
				value |= readByte(false) << 24;
				value |= readByte(false, type);
				value |= readByte(false) << 8;
				break;
			case LITTLE:
				value |= readByte(false, type);
				value |= readByte(false) << 8;
				value |= readByte(false) << 16;
				value |= readByte(false) << 24;
				break;
			}
			return signed ? value : value & 0xffffffffL;
		}

		/**
		 * Reads a signed standard big-endian integer.
		 * 
		 * @return the value
		 */
		public int readInt() {
			return (int) readInt(true, ValueType.STANDARD, ByteOrder.BIG);
		}

		/**
		 * Reads a standard big-endian integer.
		 * 
		 * @param signed
		 *            the signedness
		 * @return the value
		 */
		public long readInt(boolean signed) {
			return readInt(signed, ValueType.STANDARD, ByteOrder.BIG);
		}

		/**
		 * Reads a signed big-endian integer.
		 * 
		 * @param type
		 *            the value type
		 * @return the value
		 */
		public int readInt(ValueType type) {
			return (int) readInt(true, type, ByteOrder.BIG);
		}

		/**
		 * Reads a big-endian integer.
		 * 
		 * @param signed
		 *            the signedness
		 * @param type
		 *            the value type
		 * @return the value
		 */
		public long readInt(boolean signed, ValueType type) {
			return readInt(signed, type, ByteOrder.BIG);
		}

		/**
		 * Reads a signed standard integer.
		 * 
		 * @param order
		 *            the byte order
		 * @return the value
		 */
		public int readInt(ByteOrder order) {
			return (int) readInt(true, ValueType.STANDARD, order);
		}

		/**
		 * Reads a standard integer.
		 * 
		 * @param signed
		 *            the signedness
		 * @param order
		 *            the byte order
		 * @return the value
		 */
		public long readInt(boolean signed, ByteOrder order) {
			return readInt(signed, ValueType.STANDARD, order);
		}

		/**
		 * Reads a signed integer.
		 * 
		 * @param type
		 *            the value type
		 * @param order
		 *            the byte order
		 * @return the value
		 */
		public int readInt(ValueType type, ByteOrder order) {
			return (int) readInt(true, type, order);
		}

		/**
		 * Reads a signed long value.
		 * 
		 * @param type
		 *            the value type
		 * @param order
		 *            the byte order
		 * @return the value
		 */
		public long readLong(ValueType type, ByteOrder order) {
			long value = 0;
			switch (order) {
			case BIG:
				value |= (long) readByte(false) << 56L;
				value |= (long) readByte(false) << 48L;
				value |= (long) readByte(false) << 40L;
				value |= (long) readByte(false) << 32L;
				value |= (long) readByte(false) << 24L;
				value |= (long) readByte(false) << 16L;
				value |= (long) readByte(false) << 8L;
				value |= readByte(false, type);
				break;
			case MIDDLE:
				throw new UnsupportedOperationException("middle-endian long is not implemented!");
			case INVERSE_MIDDLE:
				throw new UnsupportedOperationException("inverse-middle-endian long is not implemented!");
			case LITTLE:
				value |= readByte(false, type);
				value |= (long) readByte(false) << 8L;
				value |= (long) readByte(false) << 16L;
				value |= (long) readByte(false) << 24L;
				value |= (long) readByte(false) << 32L;
				value |= (long) readByte(false) << 40L;
				value |= (long) readByte(false) << 48L;
				value |= (long) readByte(false) << 56L;
				break;
			}
			return value;
		}

		/**
		 * Reads a signed standard big-endian long.
		 * 
		 * @return the value
		 */
		public long readLong() {
			return readLong(ValueType.STANDARD, ByteOrder.BIG);
		}

		/**
		 * Reads a signed big-endian long
		 * 
		 * @param type
		 *            the value type
		 * @return the value
		 */
		public long readLong(ValueType type) {
			return readLong(type, ByteOrder.BIG);
		}

		/**
		 * Reads a signed standard long.
		 * 
		 * @param order
		 *            the byte order
		 * @return the value
		 */
		public long readLong(ByteOrder order) {
			return readLong(ValueType.STANDARD, order);
		}

		/**
		 * Reads a RuneScape string value.
		 * 
		 * @return the string
		 */
		public String readString() {
			byte temp;
			StringBuilder b = new StringBuilder();
			while ((temp = (byte) readByte()) != 10) {
				b.append((char) temp);
			}
			return b.toString();
		}

		/**
		 * Reads the amuont of bytes into the array, starting at the current
		 * position.
		 * 
		 * @param amount
		 *            the amount to read
		 * @return a buffer filled with the data
		 */
		public byte[] readBytes(int amount) {
			return readBytes(amount, ValueType.STANDARD);
		}

		/**
		 * Reads the amount of bytes into a byte array, starting at the current
		 * position.
		 * 
		 * @param amount
		 *            the amount of bytes
		 * @param type
		 *            the value type of each byte
		 * @return a buffer filled with the data
		 */
		public byte[] readBytes(int amount, ValueType type) {
			byte[] data = new byte[amount];
			for (int i = 0; i < amount; i++) {
				data[i] = (byte) readByte(type);
			}
			return data;
		}

		/**
		 * Reads the amount of bytes from the buffer in reverse, starting at
		 * current position + amount and reading in reverse until the current
		 * position.
		 * 
		 * @param amount
		 *            the amount of bytes
		 * @param type
		 *            the value type of each byte
		 * @return a buffer filled with the data
		 */
		public byte[] readBytesReverse(int amount, ValueType type) {
			byte[] data = new byte[amount];
			int dataPosition = 0;
			for (int i = buffer.position() + amount - 1; i >= buffer.position(); i--) {
				int value = buffer.get(i);
				switch (type) {
				case A:
					value -= 128;
					break;
				case C:
					value = -value;
					break;
				case S:
					value = 128 - value;
					break;
				}
				data[dataPosition++] = (byte) value;
			}
			return data;
		}

		/**
		 * Gets the internal buffer.
		 * 
		 * @return the buffer
		 */
		public ByteBuffer getBuffer() {
			return buffer;
		}

	}

	/**
	 * A StreamBuffer used to write outgoing data.
	 * 
	 * @author blakeman8192
	 */
	public static final class OutBuffer extends StreamBuffer {

		/** The internal buffer. */
		private ByteBuffer buffer;

		/** The position of the packet length in the packet header. */
		private int lengthPosition = 0;

		/**
		 * Creates a new OutBuffer.
		 * 
		 * @param size
		 *            the size
		 */
		private OutBuffer(int size) {
			buffer = ByteBuffer.allocate(size);
		}

		@Override
		void switchAccessType(AccessType type) {
			switch (type) {
			case BIT_ACCESS:
				setBitPosition(buffer.position() * 8);
				break;
			case BYTE_ACCESS:
				buffer.position((getBitPosition() + 7) / 8);
				break;
			}
		}

		/**
		 * Writes a packet header.
		 * 
		 * @param cipher
		 *            the encryptor
		 * @param value
		 *            the value
		 */
		public void writeHeader(ISAACCipher cipher, int value) {
			writeByte(value + cipher.getNextValue());
		}

		/**
		 * Writes a packet header for a variable length packet. Note that the
		 * corresponding "finishVariablePacketHeader" must be called to finish
		 * the packet.
		 * 
		 * @param cipher
		 *            the ISAACCipher encryptor
		 * @param value
		 *            the value
		 */
		public void writeVariablePacketHeader(ISAACCipher cipher, int value) {
			writeHeader(cipher, value);
			lengthPosition = buffer.position();
			writeByte(0);
		}

		/**
		 * Writes a packet header for a variable length packet, where the length
		 * is written as a short instead of a byte. Note that the corresponding
		 * "finishVariableShortPacketHeader must be called to finish the packet.
		 * 
		 * @param cipher
		 *            the ISAACCipher encryptor
		 * @param value
		 *            the value
		 */
		public void writeVariableShortPacketHeader(ISAACCipher cipher, int value) {
			writeHeader(cipher, value);
			lengthPosition = buffer.position();
			writeShort(0);
		}

		/**
		 * Finishes a variable packet header by writing the actual packet length
		 * at the length byte's position. Call this when the construction of the
		 * actual variable length packet is complete.
		 */
		public void finishVariablePacketHeader() {
			buffer.put(lengthPosition, (byte) (buffer.position() - lengthPosition - 1));
		}

		/**
		 * Finishes a variable packet header by writing the actual packet length
		 * at the length short's position. Call this when the construction of
		 * the variable length packet is complete.
		 */
		public void finishVariableShortPacketHeader() {
			buffer.putShort(lengthPosition, (short) (buffer.position() - lengthPosition - 2));
		}

		/**
		 * Writes the bytes from the argued buffer into this buffer. This method
		 * does not modify the argued buffer, and please do not flip() the
		 * buffer before hand.
		 * 
		 * @param from
		 */
		public void writeBytes(ByteBuffer from) {
			for (int i = 0; i < from.position(); i++) {
				writeByte(from.get(i));
			}
		}

		/**
		 * Writes the bytes from the argued byte array into this buffer, in
		 * reverse.
		 * 
		 * @param data
		 *            the data to write
		 */
		public void writeBytesReverse(byte[] data) {
			for (int i = data.length - 1; i >= 0; i--) {
				writeByte(data[i]);
			}
		}

		/**
		 * Writes the value as a variable amount of bits.
		 * 
		 * @param amount
		 *            the amount of bits
		 * @param value
		 *            the value
		 */
		public void writeBits(int amount, int value) {
			if (getAccessType() != AccessType.BIT_ACCESS) {
				throw new IllegalStateException("Illegal access type.");
			}
			if (amount < 0 || amount > 32) {
				throw new IllegalArgumentException("Number of bits must be between 1 and 32 inclusive.");
			}

			int bytePos = getBitPosition() >> 3;
			int bitOffset = 8 - (getBitPosition() & 7);
			setBitPosition(getBitPosition() + amount);

			// Re-size the buffer if need be.
			int requiredSpace = bytePos - buffer.position() + 1;
			requiredSpace += (amount + 7) / 8;
			if (buffer.remaining() < requiredSpace) {
				ByteBuffer old = buffer;
				buffer = ByteBuffer.allocate(old.capacity() + requiredSpace);
				old.flip();
				buffer.put(old);
			}

			for (; amount > bitOffset; bitOffset = 8) {
				byte tmp = buffer.get(bytePos);
				tmp &= ~BIT_MASK[bitOffset];
				tmp |= (value >> (amount - bitOffset)) & BIT_MASK[bitOffset];
				buffer.put(bytePos++, tmp);
				amount -= bitOffset;
			}
			if (amount == bitOffset) {
				byte tmp = buffer.get(bytePos);
				tmp &= ~BIT_MASK[bitOffset];
				tmp |= value & BIT_MASK[bitOffset];
				buffer.put(bytePos, tmp);
			} else {
				byte tmp = buffer.get(bytePos);
				tmp &= ~(BIT_MASK[amount] << (bitOffset - amount));
				tmp |= (value & BIT_MASK[amount]) << (bitOffset - amount);
				buffer.put(bytePos, tmp);
			}
		}

		/**
		 * Writes a boolean bit flag.
		 * 
		 * @param flag
		 *            the flag
		 */
		public void writeBit(boolean flag) {
			writeBits(1, flag ? 1 : 0);
		}

		/**
		 * Writes a value as a byte.
		 * 
		 * @param value
		 *            the value
		 * @param type
		 *            the value type
		 */
		public void writeByte(int value, ValueType type) {
			if (getAccessType() != AccessType.BYTE_ACCESS) {
				throw new IllegalStateException("Illegal access type.");
			}
			switch (type) {
			case A:
				value += 128;
				break;
			case C:
				value = -value;
				break;
			case S:
				value = 128 - value;
				break;
			}
			buffer.put((byte) value);
		}

		/**
		 * Writes a value as a normal byte.
		 * 
		 * @param value
		 *            the value
		 */
		public void writeByte(int value) {
			writeByte(value, ValueType.STANDARD);
		}

		/**
		 * Writes a value as a short.
		 * 
		 * @param value
		 *            the value
		 * @param type
		 *            the value type
		 * @param order
		 *            the byte order
		 */
		public void writeShort(int value, ValueType type, ByteOrder order) {
			switch (order) {
			case BIG:
				writeByte(value >> 8);
				writeByte(value, type);
				break;
			case MIDDLE:
				throw new IllegalArgumentException("Middle-endian short is impossible!");
			case INVERSE_MIDDLE:
				throw new IllegalArgumentException("Inverse-middle-endian short is impossible!");
			case LITTLE:
				writeByte(value, type);
				writeByte(value >> 8);
				break;
			}
		}

		/**
		 * Writes a value as a normal big-endian short.
		 * 
		 * @param value
		 *            the value.
		 */
		public void writeShort(int value) {
			writeShort(value, ValueType.STANDARD, ByteOrder.BIG);
		}

		/**
		 * Writes a value as a big-endian short.
		 * 
		 * @param value
		 *            the value
		 * @param type
		 *            the value type
		 */
		public void writeShort(int value, ValueType type) {
			writeShort(value, type, ByteOrder.BIG);
		}

		/**
		 * Writes a value as a standard short.
		 * 
		 * @param value
		 *            the value
		 * @param order
		 *            the byte order
		 */
		public void writeShort(int value, ByteOrder order) {
			writeShort(value, ValueType.STANDARD, order);
		}

		/**
		 * Writes a value as an int.
		 * 
		 * @param value
		 *            the value
		 * @param type
		 *            the value type
		 * @param order
		 *            the byte order
		 */
		public void writeInt(int value, ValueType type, ByteOrder order) {
			switch (order) {
			case BIG:
				writeByte(value >> 24);
				writeByte(value >> 16);
				writeByte(value >> 8);
				writeByte(value, type);
				break;
			case MIDDLE:
				writeByte(value >> 8);
				writeByte(value, type);
				writeByte(value >> 24);
				writeByte(value >> 16);
				break;
			case INVERSE_MIDDLE:
				writeByte(value >> 16);
				writeByte(value >> 24);
				writeByte(value, type);
				writeByte(value >> 8);
				break;
			case LITTLE:
				writeByte(value, type);
				writeByte(value >> 8);
				writeByte(value >> 16);
				writeByte(value >> 24);
				break;
			}
		}

		/**
		 * Writes a value as a standard big-endian int.
		 * 
		 * @param value
		 *            the value
		 */
		public void writeInt(int value) {
			writeInt(value, ValueType.STANDARD, ByteOrder.BIG);
		}

		/**
		 * Writes a value as a big-endian int.
		 * 
		 * @param value
		 *            the value
		 * @param type
		 *            the value type
		 */
		public void writeInt(int value, ValueType type) {
			writeInt(value, type, ByteOrder.BIG);
		}

		/**
		 * Writes a value as a standard int.
		 * 
		 * @param value
		 *            the value
		 * @param order
		 *            the byte order
		 */
		public void writeInt(int value, ByteOrder order) {
			writeInt(value, ValueType.STANDARD, order);
		}

		/**
		 * Writes a value as a long.
		 * 
		 * @param value
		 *            the value
		 * @param type
		 *            the value type
		 * @param order
		 *            the byte order
		 */
		public void writeLong(long value, ValueType type, ByteOrder order) {
			switch (order) {
			case BIG:
				writeByte((int) (value >> 56));
				writeByte((int) (value >> 48));
				writeByte((int) (value >> 40));
				writeByte((int) (value >> 32));
				writeByte((int) (value >> 24));
				writeByte((int) (value >> 16));
				writeByte((int) (value >> 8));
				writeByte((int) value, type);
				break;
			case MIDDLE:
				throw new UnsupportedOperationException("Middle-endian long is not implemented!");
			case INVERSE_MIDDLE:
				throw new UnsupportedOperationException("Inverse-middle-endian long is not implemented!");
			case LITTLE:
				writeByte((int) value, type);
				writeByte((int) (value >> 8));
				writeByte((int) (value >> 16));
				writeByte((int) (value >> 24));
				writeByte((int) (value >> 32));
				writeByte((int) (value >> 40));
				writeByte((int) (value >> 48));
				writeByte((int) (value >> 56));
				break;
			}
		}

		/**
		 * Writes a value as a standard big-endian long.
		 * 
		 * @param value
		 *            the value
		 */
		public void writeLong(long value) {
			writeLong(value, ValueType.STANDARD, ByteOrder.BIG);
		}

		/**
		 * Writes a value as a big-endian long.
		 * 
		 * @param value
		 *            the value
		 * @param type
		 *            the value type
		 */
		public void writeLong(long value, ValueType type) {
			writeLong(value, type, ByteOrder.BIG);
		}

		/**
		 * Writes a value as a standard long.
		 * 
		 * @param value
		 *            the value
		 * @param order
		 *            the byte order
		 */
		public void writeLong(long value, ByteOrder order) {
			writeLong(value, ValueType.STANDARD, order);
		}

		/**
		 * Writes a RuneScape string value.
		 * 
		 * @param string
		 *            the string
		 */
		public void writeString(String string) {
			for (byte value : string.getBytes()) {
				writeByte(value);
			}
			writeByte(10);
		}

		/**
		 * Gets the internal buffer.
		 * 
		 * @return the buffer
		 */
		public ByteBuffer getBuffer() {
			return buffer;
		}

	}

}
