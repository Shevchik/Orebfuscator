package com.lishid.orebfuscator.internal.v1_6_R3;

import net.minecraft.server.v1_6_R3.Packet52MultiBlockChange;

public class Packet52 {

	private Packet52MultiBlockChange packet;

	public void setPacket(Object packet) {
		if (packet instanceof Packet52MultiBlockChange) {
			this.packet = (Packet52MultiBlockChange) packet;
		}
	}

	public int getChunkX() {
		return packet.a;
	}

	public int getChunkZ() {
		return packet.b;
	}

	public int getRecordsCount() {
		return packet.d;
	}

	public byte[] getBuffer() {
		return packet.c;
	}

}
