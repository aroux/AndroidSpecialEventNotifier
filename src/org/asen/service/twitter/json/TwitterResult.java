package org.asen.service.twitter.json;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.google.gson.annotations.SerializedName;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class TwitterResult implements Serializable {

	private static final long serialVersionUID = 8945609557004554913L;

	@SerializedName("max_id")
	private long maxId;

	@SerializedName("results")
	private List<TweetEntry> tweets;
}
