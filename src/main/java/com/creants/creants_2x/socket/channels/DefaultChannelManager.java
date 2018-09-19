package com.creants.creants_2x.socket.channels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.creants.creants_2x.core.util.QAntTracer;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

/**
 * @author LamHM
 *
 */
public final class DefaultChannelManager implements IChannelManager {
	private static final AttributeKey<Integer> SESSION_ID = AttributeKey.valueOf("dispatcher.sessionId");
	private static IChannelManager instance;
	private ConcurrentMap<Integer, Channel> channelsById;
	private final List<Channel> localChannels;
	private final ConcurrentMap<Integer, Channel> localChannelsById;
	private String serviceName;
	private int highestCCU;
	private final AtomicInteger idGenerator;


	public static IChannelManager getInstance() {
		if (instance == null) {
			instance = new DefaultChannelManager();
		}
		return instance;
	}


	private DefaultChannelManager() {
		this.serviceName = "DefaultSessionManager";
		this.highestCCU = 0;
		this.channelsById = new ConcurrentHashMap<Integer, Channel>();
		this.localChannels = new ArrayList<Channel>();
		this.localChannelsById = new ConcurrentHashMap<Integer, Channel>();
		idGenerator = new AtomicInteger(0);
	}


	public String getServiceName() {
		return serviceName;
	}


	@Override
	public void init(Object obj) {
	}


	@Override
	public void destroy(Object obj) {
	}


	@Override
	public void handleMessage(Object message) throws Exception {
	}


	@Override
	public String getName() {
		return serviceName;
	}


	@Override
	public void setName(String name) {
		this.serviceName = name;
	}


	@Override
	public void addChannel(Channel channel) {
		synchronized (localChannels) {
			localChannels.add(channel);
		}

		int id = idGenerator.getAndIncrement();
		channel.attr(SESSION_ID).set(id);

		localChannelsById.put(id, channel);
		if (localChannels.size() > highestCCU)
			highestCCU = localChannels.size();

		QAntTracer.info(this.getClass(), "Session created: " + channel + ", local address: "
				+ channel.localAddress().toString() + ", remote addr: " + channel.remoteAddress().toString());
	}


	@Override
	public void removeChannel(Channel channel) {
		if (channel == null)
			return;

		synchronized (localChannels) {
			localChannels.remove(channel);
		}

		localChannelsById.remove(channel.attr(SESSION_ID).get());
		QAntTracer.info(this.getClass(), "Channel removed: " + channel);
	}


	@Override
	public Channel removeChannel(int id) {
		Channel channel = localChannelsById.get(id);
		if (channel != null)
			removeChannel(channel);

		return channel;
	}


	@Override
	public boolean containsChannel(Channel channel) {
		return localChannelsById.containsValue(channel);
	}


	@Override
	public void shutDownLocalChannels() {

	}


	@Override
	public List<Channel> getAllChannels() {
		return getAllLocalChannels();
	}


	@Override
	public Channel getChannelById(int id) {
		return channelsById.get(id);
	}


	@Override
	public int getHighestCCU() {
		return highestCCU;
	}


	@Override
	public List<Channel> getAllLocalChannels() {
		List<Channel> allSessions = null;
		synchronized (localChannels) {
			allSessions = new ArrayList<Channel>(localChannels);
		}
		return allSessions;
	}


	@Override
	public Channel getLocalChannelById(int id) {
		return localChannelsById.get(id);
	}


	@Override
	public int getLocalChannelCount() {
		return localChannels.size();
	}


	@Override
	public void onSocketDisconnected(Channel channel) throws IOException {
		removeChannel(channel);
	}


	@Override
	public Channel reconnectSession(Channel channel, String name) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
