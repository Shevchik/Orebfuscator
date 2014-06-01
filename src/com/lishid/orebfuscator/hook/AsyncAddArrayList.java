package com.lishid.orebfuscator.hook;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.minecraft.server.v1_6_R3.Packet;

import org.bukkit.entity.Player;

import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.internal.Packet51;
import com.lishid.orebfuscator.internal.Packet52;
import com.lishid.orebfuscator.internal.Packet53;
import com.lishid.orebfuscator.internal.Packet56;
import com.lishid.orebfuscator.obfuscation.BlockUpdate;
import com.lishid.orebfuscator.obfuscation.Calculations;

public class AsyncAddArrayList implements List<Packet> {

	private Player player;
	private List<Packet> list;

	private boolean async = OrebfuscatorConfig.Async;

	public AsyncAddArrayList(Player player, List<Packet> list) {
		this.player = player;
		if (list instanceof AsyncAddArrayList) {
			this.list = ((AsyncAddArrayList) list).list;
		} else {
			this.list = list;
		}
	}

	public void cleanup() {
		player = null;
	}

	private ExecutorService executor = Executors.newSingleThreadExecutor();

	@Override
	public boolean add(final Packet packet) {
		Runnable processPacket = new Runnable() {
			@Override
			public void run() {
				if (player != null) {
					if (packet.n() == 51) {
						Packet51 wrapper = new Packet51();
						wrapper.setPacket(packet);
						Calculations.Obfuscate(wrapper, player);
					} else if (packet.n() == 56) {
						Packet56 wrapper = new Packet56();
						wrapper.setPacket(packet);
						Calculations.Obfuscate(wrapper, player);
					} else if (packet.n() == 53) {
						Packet53 wrapper = new Packet53(packet);
						BlockUpdate.update(wrapper, player);
					} else if (packet.n() == 52) {
						Packet52 wrapper = new Packet52(packet);
						BlockUpdate.update(wrapper, player);
					}
				}
				list.add(packet);
			}
		};
		if (async) {
			executor.execute(
				processPacket
			);
		} else {
			processPacket.run();
		}
		return true;
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return list.contains(o);
	}

	@Override
	public Iterator<Packet> iterator() {
		return list.iterator();
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}

	@Override
	public boolean remove(Object o) {
		return list.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends Packet> c) {
		return list.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends Packet> c) {
		return list.addAll(list);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return list.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return list.removeAll(c);
	}

	@Override
	public void clear() {
		list.clear();
	}

	@Override
	public Packet get(int index) {
		return list.get(index);
	}

	@Override
	public Packet set(int index, Packet element) {
		return list.set(index, element);
	}

	@Override
	public void add(int index, Packet element) {
		list.add(index, element);
	}

	@Override
	public Packet remove(int index) {
		return list.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	@Override
	public ListIterator<Packet> listIterator() {
		return list.listIterator();
	}

	@Override
	public ListIterator<Packet> listIterator(int index) {
		return list.listIterator(index);
	}

	@Override
	public List<Packet> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

}
