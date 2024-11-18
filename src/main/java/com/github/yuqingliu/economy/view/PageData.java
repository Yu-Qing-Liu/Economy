package com.github.yuqingliu.economy.view;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PageData<T> {
    private final Map<Integer, Map<List<Integer>, T>> pageData = new ConcurrentHashMap<>();
    
    public T get(int pageNumber, int[] coords) {
        if(!pageData.containsKey(pageNumber)) {
            return null;
        }
        if(!pageData.get(pageNumber).containsKey(Arrays.asList(coords[1], coords[2]))) {
            return null;
        }
        return pageData.get(pageNumber).get(Arrays.asList(coords[1], coords[2]));
    }

    public Map<List<Integer>, T> get(int pageNumber) {
        if(!pageData.containsKey(pageNumber)) {
            return Collections.emptyMap();
        }
        return pageData.get(pageNumber);
    }

    public void put(int pageNumber, Map<List<Integer>, T> data) {
        pageData.put(pageNumber, data);
    }

    public boolean hasPage(int pageNumber) {
        return pageData.containsKey(pageNumber);
    }
}
