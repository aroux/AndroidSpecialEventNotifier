package org.asen.service.twitter.json;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.google.gson.annotations.SerializedName;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class TweetEntry implements Serializable {

	private static final long serialVersionUID = 3895181432160127317L;

	private long id;

	private String text;

	@SerializedName("created_at")
	private Date date;
}
