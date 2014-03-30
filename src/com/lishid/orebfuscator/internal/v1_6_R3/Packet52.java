package com.lishid.orebfuscator.internal.v1_6_R3;

import net.minecraft.server.v1_6_R3.Packet52MultiBlockChange;

import com.lishid.orebfuscator.internal.IPacket52;

public class Packet52 implements IPacket52 {

	private Packet52MultiBlockChange packet;

	@Override
	public void setPacket(Object packet) {
		if (packet instanceof Packet52MultiBlockChange) {
			this.packet = (Packet52MultiBlockChange) packet;
		}
	}

	@Override
	public int getChunkX() {
		return packet.a;
	}

	@Override
	public int getChunkZ() {
		return packet.b;
	}

	@Override
	public int getRecordsCount() {
		return packet.d;
	}

	@Override
	public byte[] getBuffer() {
		return packet.c;
	}

}
