package com.example.raksheet.majorproject.clienttoserver;

public interface APIResponseListner<T> {
	public void onApiCallSucceded(T response);
	public void onApiCallFailed();
}
