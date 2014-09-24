package com.kopjra.clidispatcher;

import java.util.Map;
import java.util.Set;

public interface AcceptsAttributes {
	public Object getAttribute(String key);
	public void setAttribute(String key, Object value);
	public Set<String> getAttributes();
	public void setAttributes(Map<String,Object> map);
}
