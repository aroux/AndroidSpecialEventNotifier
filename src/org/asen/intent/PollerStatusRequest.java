package org.asen.intent;

import java.io.Serializable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(suppressConstructorProperties=true)
public class PollerStatusRequest implements Serializable {

	private static final long serialVersionUID = -6516286182416338062L;

	public static final String ACTION_ID = "ACTION_POLLER_STATUS_REQUEST";

	private final String url;
}
