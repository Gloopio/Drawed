package io.gloop.drawed.serializers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.gloop.drawed.model.Point;
import io.gloop.serializers.GloopSerializeToString;

/**
 * Created by Alex Untertrifaller on 17.02.17.
 */
public class PointsSerializer extends GloopSerializeToString<List<Point>> {

    private Gson gson = new Gson();

    @Override
    public String serialize(List<Point> points, Map<String, Object> map) {
        return gson.toJson(points);
    }

    @Override
    public List<Point> deserialize(String s, Map<String, Object> map) {
        Type listType = new TypeToken<ArrayList<Point>>(){}.getType();
        return gson.fromJson(s, listType);
    }
}
