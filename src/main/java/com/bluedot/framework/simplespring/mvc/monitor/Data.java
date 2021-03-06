package com.bluedot.framework.simplespring.mvc.monitor;

import com.bluedot.electrochemistry.pojo.domain.User;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author JDsen99
 * @description
 * @createDate 2021/9/6-18:33
 */
public class Data implements Comparable<Data>, Map {
    /**
     * 优先级 默认为5
     */
    private Integer priority = 5;

    private Map<String, Object> data;

    private HttpServletRequest request;

    public Data(HttpServletRequest request) {
        this(request, 5);
    }

    public Data(HttpServletRequest request, Integer priority) {
        this.request = request;
        data = new HashMap<>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        Set<Entry<String, String[]>> entries = parameterMap.entrySet();
        for (Entry<String, String[]> entry : entries) {
            this.data.put(entry.getKey(), entry.getValue()[0]);
        }
        //重新将boundary 取出 并放入
        data.put("boundary",request.getParameter("boundary"));
        if (priority != null) {
            this.priority = priority;
        }
    }

    public Class getService() {
        return (Class) data.get("service");
    }

    public String getServiceMethod() {
        return (String) data.get("serviceMethod");
    }

    /**
     * 获取session中的 user
     *
     * @return
     */
    public User getRequestUser() {
        return (User) request.getSession().getAttribute("user");
    }

    public HttpServletRequest getRequest() {
        return request;
    }


    public Integer getPriority() {
        return priority;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public int compareTo(Data o) {
        return this.getPriority() - o.priority;
    }

    @Override
    public int size() {
        return this.data.size();
    }

    public int newSize() {
        return this.data.size();
    }

    /**
     * 当requestData 与 newData 同时为空时 则为空
     *
     * @return
     */
    @Override
    public boolean isEmpty() {
        return this.data.isEmpty();
    }

    /**
     * TODO
     */
    @Override
    public boolean containsKey(Object key) {
        return this.data.containsKey(key);
    }

    /**
     * TODO
     */
    @Override
    public boolean containsValue(Object value) {
        return this.data.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return data.get(key);
    }

    /**
     * TODO
     */
    @Override
    public Object put(Object key, Object value) {
        return data.put((String) key, value);
    }

    @Override
    public Object remove(Object key) {
        return data.remove(key);
    }

    @Override
    public void putAll(Map m) {
        data.putAll(m);
    }

    @Override
    public void clear() {
        data.clear();
    }

    @Override
    public Set keySet() {
        return this.data.keySet();
    }

    @Override
    public Collection values() {
        return data.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return this.data.entrySet();
    }

}
