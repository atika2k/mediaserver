package org.mobicents.media.core.ice;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mobicents.media.core.ice.harvest.HarvestManager;
import org.mobicents.media.core.ice.harvest.HarvestException;
import org.mobicents.media.core.ice.harvest.NoCandidatesGatheredException;

public abstract class IceAgent {

	private final Map<String, IceMediaStream> mediaStreams;
	private final HarvestManager harvestManager;

	protected final String ufrag;
	protected final String password;

	protected IceAgent() {
		this.mediaStreams = new LinkedHashMap<String, IceMediaStream>(5);
		this.harvestManager = new HarvestManager();

		SecureRandom random = new SecureRandom();
		this.ufrag = new BigInteger(24, random).toString(32);
		this.password = new BigInteger(128, random).toString(32);
	}

	/**
	 * Checks whether the Agent implements ICE Lite
	 * 
	 * @return true if the agent implements ICE Lite. False, in case of full
	 *         ICE.
	 */
	public abstract boolean isLite();

	/**
	 * Checks whether the Agent is controlling the ICE process.
	 * 
	 * @return
	 */
	public abstract boolean isControlling();

	/**
	 * Gets the local user fragment.
	 * 
	 * @return the local <code>ice-ufrag</code>
	 */
	public String getUfrag() {
		return ufrag;
	}

	/**
	 * Gets the password of the local user fragment
	 * 
	 * @return the local <code>ice-pwd</code>
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Creates an <tt>IceMediaStream</tt> and adds to it an RTP and an RTCP
	 * components.
	 * 
	 * @param streamName
	 *            the name of the stream to create
	 * @param agent
	 *            the <tt>Agent</tt> that should create the stream.
	 * 
	 * @return the newly created <tt>IceMediaStream</tt>.
	 * @throws IceException
	 *             if anything goes wrong.
	 * @throws IllegalArgumentException
	 *             When a stream with <code>streamName</code> already exists.
	 */
	public IceMediaStream addMediaStream(String streamName) throws IceException {
		return addMediaStream(streamName, true);
	}

	/**
	 * Creates and registers a new media stream with an RTP component.<br>
	 * An secondary component may be created if the stream supports RTCP.
	 * 
	 * @param streamName
	 *            the name of the media stream
	 * @param rtcp
	 *            Indicates whether the media server supports RTCP.
	 * @return The newly created media stream.
	 */
	public IceMediaStream addMediaStream(String streamName, boolean rtcp) {
		return this.mediaStreams.put(streamName, new IceMediaStream(streamName,
				rtcp));
	}

	/**
	 * Gets a media stream by name
	 * 
	 * @param streamName
	 *            The name of the media stream
	 * @return The media stream. Returns null, if no media stream exists with
	 *         such name.
	 */
	public IceMediaStream getMediaStream(String streamName) {
		IceMediaStream mediaStream;
		synchronized (mediaStreams) {
			mediaStream = this.mediaStreams.get(streamName);
		}
		return mediaStream;
	}

	public List<IceMediaStream> getMediaStreams() {
		List<IceMediaStream> copy;
		synchronized (mediaStreams) {
			copy = new ArrayList<IceMediaStream>(this.mediaStreams.values());
		}
		return copy;
	}

	/**
	 * Gathers all available candidates and sets the components of each media
	 * stream
	 * 
	 * @param preferredPort
	 *            The preferred port to bind candidates to
	 * @throws HarvestException
	 *             An error occurred while harvesting candidates
	 */
	public void gatherCandidates(int preferredPort) throws HarvestException,
			NoCandidatesGatheredException {
		for (IceMediaStream mediaStream : getMediaStreams()) {
			this.harvestManager.harvest(mediaStream, preferredPort);
		}
	}
}