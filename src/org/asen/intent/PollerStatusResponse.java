package org.asen.intent;

import java.io.Serializable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(suppressConstructorProperties=true)
public class PollerStatusResponse implements Serializable {

	private static final long serialVersionUID = -1075332552208206796L;

	public static final String ACTION_ID = "ACTION_POLLER_STATUS_RESPONSE";

	private final String url;

	private final boolean enabled;
}
