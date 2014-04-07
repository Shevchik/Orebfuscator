/*
 * Copyright (C) 2011-2014 lishid.  All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lishid.orebfuscator.internal.v1_6_R3;

import java.util.zip.Deflater;

//Volatile
import net.minecraft.server.v1_6_R3.Packet51MapChunk;

import com.lishid.orebfuscator.internal.IPacket51;
import com.lishid.orebfuscator.utils.ReflectionHelper;

public class Packet51 implements IPacket51 {

	Packet51MapChunk packet;
	
	private byte[] inflatedBuffer;

	@Override
	public void setPacket(Object packet) {
		if (packet instanceof Packet51MapChunk) {
			this.packet = (Packet51MapChunk) packet;
			inflatedBuffer = (byte[]) ReflectionHelper.getPrivateField(packet, "field_73596_g");
		}
	}

	@Override
	public int getX() {
		return packet.a;
	}

	@Override
	public int getZ() {
		return packet.b;
	}

	@Override
	public int getChunkMask() {
		return packet.c;
	}

	@Override
	public int getExtraMask() {
		return packet.d;
	}

	@Override
	public byte[] getBuffer() {
		return inflatedBuffer;
	}

	@Override
	public void compress(Deflater deflater) {
		byte[] chunkBuffer = (byte[]) ReflectionHelper.getPrivateField(packet, "field_73595_f");

		deflater.reset();
		deflater.setInput(inflatedBuffer);
		deflater.finish();

		ReflectionHelper.setPrivateField(packet, "field_73602_h", deflater.deflate(chunkBuffer));
	}

}
