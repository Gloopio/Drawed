package io.gloop.drawed.serializers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.gloop.drawed.model.Line;
import io.gloop.serializers.GloopSerializeToString;

/**
 * Created by Alex Untertrifaller on 17.02.17.
 */
public class LineSerializer extends GloopSerializeToString<List<Line>> {

    private static final Gson gson = new Gson();
    private static final Type listType = new TypeToken<ArrayList<Line>>(){}.getType();

    @Override
    public String serialize(List<Line> points, Map<String, Object> map) {
        return gson.toJson(points);
    }

    @Override
    public List<Line> deserialize(String s, Map<String, Object> map) {
        return gson.fromJson(s, listType);
    }
}
