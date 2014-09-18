package org.tectonica.util;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentMultimap<K, V>
{
	private ConcurrentHashMap<K, Set<V>> index = new ConcurrentHashMap<>();

	// TODO: change to put()
	public int add(K primary, V foreign)
	{
		HashSet<V> initial = new HashSet<V>();
		Set<V> foreigns = index.putIfAbsent(primary, initial);
		if (foreigns == null)
			foreigns = initial;
		synchronized (foreigns)
		{
			foreigns.add(foreign);
		}
		return foreigns.size();
	}

	public int remove(K primary, V foreign)
	{
		Set<V> foreigns = index.get(primary);
		if (foreigns == null)
			return 0;
		synchronized (foreigns)
		{
			foreigns.remove(foreign);
		}
		// TODO: remove primary after the last foreign is gone
		return foreigns.size();
	}

	public void removeFromAll(V foreign)
	{
		for (K primary: index.keySet())
			remove(primary, foreign);
	}

	public void clear()
	{
		index.clear();
	}

	// TODO: change to get()
	public Set<V> valuesOf(K primary)
	{
		Set<V> foreigns = index.get(primary);
		if (foreigns == null)
			return null;
		synchronized (foreigns)
		{
			return new HashSet<V>(foreigns);
		}
	}
}
