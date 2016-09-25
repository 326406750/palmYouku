package uestc.palmyouku.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import uestc.palmyouku.model.Category;

import uestc.palmyouku.model.Keyword;
import uestc.palmyouku.model.Video;

/**
 * Created by Administrator on 2016/8/23.
 */
public class MyJsonParser {
    public static ArrayList<Category> parseCategories(JSONObject jsonObject){
        ArrayList<Category> array = new ArrayList<Category>();
        try{
            JSONArray categories = jsonObject.getJSONArray("categories");
            for(int i=0; i<categories.length(); i++){
                Category c = new Category();
                JSONObject json = (JSONObject)categories.get(i);
                c.setId(i);
                c.setName(json.getString("label"));

                array.add(c);
            }

        }catch (JSONException ex){

        }

        return array;

    }

    public static ArrayList<Video> parseVideos(JSONObject jsonObject){
        ArrayList<Video> array = new ArrayList<Video>();
        try{
            JSONArray videos = jsonObject.getJSONArray("videos");
            for(int i=0; i<videos.length(); i++)
            {
                JSONObject json = (JSONObject)videos.get(i);
                Video v = new Video();
                v.setId(json.getString("id"));
                v.setTitle(json.getString("title"));
                v.setLink(json.getString("link"));
                v.setDuration(json.getInt("duration"));
                v.setThumbnail(json.getString("thumbnail"));
                array.add(v);
            }


        }catch (JSONException ex){

        }
        return array;
    }

    public static ArrayList<Keyword> parseKeywords(JSONObject jsonObject){
        ArrayList<Keyword> array = new ArrayList<Keyword>();
        try{
            JSONArray keywords = jsonObject.getJSONArray("keywords");
            for(int i=0; i<keywords.length(); i++)
            {
                JSONObject json = (JSONObject)keywords.get(i);

                Keyword k = new Keyword();
                k.setKeyord(json.getString("keyword"));
                k.setSearchCount(json.getInt("search_count"));
                k.setLink(json.getString("link"));
                array.add(k);
            }


        }catch (JSONException ex){

        }
        return array;
    }
}
