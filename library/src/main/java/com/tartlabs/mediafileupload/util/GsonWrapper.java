package com.tartlabs.mediafileupload.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class GsonWrapper {
    public static Gson newInstance() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.registerTypeAdapter(Boolean.class, new GsonTypeIntToBoolAdapter());
        gsonBuilder.registerTypeAdapter(boolean.class, new GsonTypeIntToBoolAdapter());
        gsonBuilder.registerTypeHierarchyAdapter(Calendar.class, new GsonCalenderConverter());
        gsonBuilder.registerTypeHierarchyAdapter(Date.class, new GsonDateConverter());
        gsonBuilder.setLenient();
        return gsonBuilder.create();
    }

    private static class GsonTypeIntToBoolAdapter extends TypeAdapter<Boolean> {
        @Override
        public void write(JsonWriter out, Boolean value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value);
            }
        }

        @Override
        public Boolean read(JsonReader in) throws IOException {
            JsonToken peek = in.peek();
            switch (peek) {
                case BOOLEAN:
                    return in.nextBoolean();
                case NULL:
                    in.nextNull();
                    return null;
                case NUMBER:
                    return in.nextInt() != 0;
                case STRING:
                    return Boolean.parseBoolean(in.nextString());
                default:
                    throw new IllegalStateException("Expected BOOLEAN or NUMBER but was " + peek);
            }
        }
    }

    private static class GsonDateConverter implements JsonDeserializer<Date>, JsonSerializer<Date> {

        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            String dateString = json.getAsString();
            return TimeUtils.parseTimestamp(dateString);
        }

        @Override
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            SimpleDateFormat format = TimeUtils.DEFAULT_DATE_FORMAT;
            return new JsonPrimitive(format.format(src));
        }
    }

    private static class GsonCalenderConverter implements JsonDeserializer<Calendar>, JsonSerializer<Calendar> {

        @Override
        public Calendar deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String dateString = json.getAsString();
            Calendar cal = Calendar.getInstance();
            cal.setTime(TimeUtils.parseTimestamp(dateString));
            return cal;
        }

        @Override
        public JsonElement serialize(Calendar src, Type typeOfSrc, JsonSerializationContext context) {
            SimpleDateFormat format = TimeUtils.DEFAULT_DATE_FORMAT;
            return new JsonPrimitive(format.format(src.getTime()));
        }
    }
}
