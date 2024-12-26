package com.github.yuqingliu.economy.view;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class PageData<T> {
    private final Map<Integer, Map<List<Integer>, T>> pageData = new ConcurrentHashMap<>();
    int pageNumber = 1;

    public boolean hasPage(int pageNumber) {
        return pageData.containsKey(pageNumber);
    }

    public void nextPage(Runnable callback) {
        if(hasPage(pageNumber + 1)) {
            pageNumber++;
            callback.run();
        }
    }

    public void prevPage(Runnable callback) {
        if(pageNumber > 1) {
            pageNumber--;
            callback.run();
        }
    }

    public T get(int[] coords) {
        if(!pageData.containsKey(pageNumber)) {
            return null;
        }
        if(!pageData.get(pageNumber).containsKey(Arrays.asList(coords[0], coords[1]))) {
            return null;
        }
        return pageData.get(pageNumber).get(Arrays.asList(coords[0], coords[1]));
    }

    public Map<List<Integer>, T> getCurrentPageData() {
        if(!pageData.containsKey(pageNumber)) {
            return Collections.emptyMap();
        }
        return pageData.get(pageNumber);
    }

    public void put(int pageNumber, Map<List<Integer>, T> data) {
        pageData.put(pageNumber, data);
    }

    public void clear() {
        pageData.clear();
    }
}
