package com.lishid.orebfuscator.hook;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.minecraft.server.v1_6_R3.NetworkManager;
import net.minecraft.server.v1_6_R3.Packet;

import org.bukkit.entity.Player;

import com.lishid.orebfuscator.internal.Fields;
import com.lishid.orebfuscator.internal.Packet51;
import com.lishid.orebfuscator.internal.Packet52;
import com.lishid.orebfuscator.internal.Packet53;
import com.lishid.orebfuscator.internal.Packet56;
import com.lishid.orebfuscator.obfuscation.BlockUpdate;
import com.lishid.orebfuscator.obfuscation.Calculations;
import com.lishid.orebfuscator.utils.ReflectionHelper;

public class AsyncAddArrayList implements List<Packet> {

	private Player player;
	private List<Packet> originalQueue;

	private Object networkManagerLock;

	public AsyncAddArrayList(Player player, NetworkManager nm, List<Packet> list) {
		this.player = player;
		if (list instanceof AsyncAddArrayList) {
			this.originalQueue = ((AsyncAddArrayList) list).originalQueue;
		} else {
			this.originalQueue = list;
		}
		networkManagerLock = ReflectionHelper.getPrivateField(nm, Fields.NetworkManagerFields.getLockFieldName());
	}

	public void cleanup() {
		player = null;
	}

	private ExecutorService executor = Executors.newSingleThreadExecutor();
	private ExecutorService blockUpdateExecutor = Executors.newSingleThreadExecutor();

	@Override
	public boolean add(final Packet packet) {
		if (packet.n() == 53 || packet.n() == 52) {
			processBlockUpdate(packet);
		}
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
					}
				}
				synchronized (networkManagerLock) {
					originalQueue.add(packet);
				}
			}
		};
		executor.execute(
			processPacket
		);
		return true;
	}

	private void processBlockUpdate(final Packet packet) {
		blockUpdateExecutor.execute(
			new Runnable() {
				@Override
				public void run() {
					if (player != null) {
						if (packet.n() == 53) {
							Packet53 wrapper = new Packet53(packet);
							BlockUpdate.update(wrapper, player);
						} else if (packet.n() == 52) {
							Packet52 wrapper = new Packet52(packet);
							BlockUpdate.update(wrapper, player);
						}
					}
				}
			}
		);
	}

	@Override
	public int size() {
		return originalQueue.size();
	}

	@Override
	public boolean isEmpty() {
		return originalQueue.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return originalQueue.contains(o);
	}

	@Override
	public Iterator<Packet> iterator() {
		return originalQueue.iterator();
	}

	@Override
	public Object[] toArray() {
		return originalQueue.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return originalQueue.toArray(a);
	}

	@Override
	public boolean remove(Object o) {
		return originalQueue.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return originalQueue.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends Packet> c) {
		return originalQueue.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends Packet> c) {
		return originalQueue.addAll(originalQueue);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return originalQueue.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return originalQueue.removeAll(c);
	}

	@Override
	public void clear() {
		originalQueue.clear();
	}

	@Override
	public Packet get(int index) {
		return originalQueue.get(index);
	}

	@Override
	public Packet set(int index, Packet element) {
		return originalQueue.set(index, element);
	}

	@Override
	public void add(int index, Packet element) {
		originalQueue.add(index, element);
	}

	@Override
	public Packet remove(int index) {
		return originalQueue.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return originalQueue.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return originalQueue.lastIndexOf(o);
	}

	@Override
	public ListIterator<Packet> listIterator() {
		return originalQueue.listIterator();
	}

	@Override
	public ListIterator<Packet> listIterator(int index) {
		return originalQueue.listIterator(index);
	}

	@Override
	public List<Packet> subList(int fromIndex, int toIndex) {
		return originalQueue.subList(fromIndex, toIndex);
	}

}
