package com.neurondigital.recipeapp;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by melvin on 25/09/2016.
 * This is a category item. The class contains the functionality to download a category object from the server via JSON
 */
public class SubCategory {

    //Category elements
    public int id;
    public String name;
    public int category;
    public String description;//never actually used. Added for future use.
    public String imageUrl;


    /**
     * Listens for when categories are downloaded
     */
    interface onSubCategoriesDownloadedListener {
        void onSubCategoriesDownloaded(List<SubCategory> categories);
    }


    /**
     * Listens for when the category name is downloaded
     */
    interface onNameFoundListener {
        void onNameFound(String name);
    }


    /**
     * Constructor
     *
     * @param id
     * @param name
     * @param description
     * @param imageUrl
     */
    public SubCategory(int id, String name, String description, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
    }


    /**
     * Constructor with just name as parameter
     *
     * @param name
     */
    public SubCategory(String name) {
        this.name = name;
    }


    /**
     * Constructor that generates a category from JSON
     *
     * @param JSONCategory
     */
    public SubCategory(JSONObject JSONCategory) {
        try {
            id = JSONCategory.getInt("id");
            name = JSONCategory.getString("name");
            description = JSONCategory.getString("description");
            category= JSONCategory.getInt("category");
            imageUrl = Configurations.SERVER_URL + "uploads/" + JSONCategory.getString("image");
            JSONArray JSONimageURL = new JSONArray(JSONCategory.getString("image"));
            if (JSONimageURL.length() > 0)
                imageUrl = Configurations.SERVER_URL + "uploads/" + JSONimageURL.getString(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void loadSubCategories(final String responseStr, final onSubCategoriesDownloadedListener subCategoriesDownloadedListener) {

//        // Instantiate the RequestQueue.
//        RequestQueue queue = Volley.newRequestQueue(context);
//        final String url = Configurations.SERVER_URL + "api/categories?search=" + search;
//        final Cache cache = new Cache(context);
//        System.out.println(url);
//        //generate request
//        StringRequest arrayreq = new StringRequest(url, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String responseStr) {
//                //save to cache
//                cache.store(url, responseStr);
//                //decode categories
//                decodeCategories(responseStr, subCategoriesDownloadedListener);
////                try {
////                    //parse categories
////                    List<Category> categories = new ArrayList<>();
////                    for (int i = 0; i < response.length(); i++) {
////                        JSONObject jsonObject = response.getJSONObject(i);
////                        categories.add(new Category(jsonObject));
////                    }
////                    //callback categories
////                    categoriesDownloadedListener.onCategoriesDownloaded(categories);
////                }
////                // Try and catch are included to handle any errors due to JSON
////                catch (JSONException e) {
////                    e.printStackTrace();
////                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            // Handles errors that occur due to Volley
//            public void onErrorResponse(VolleyError error) {
//                //No connection to server. Probably no internet
//                Log.e("Volley", "Error");
//                error.printStackTrace();
//                //Try to load from cache else view warning
//                String responseStr = cache.load(url);
//                if (responseStr != null) {
//                    System.out.println("loading cached data: " + responseStr);
//                    decodeCategories(responseStr, subCategoriesDownloadedListener);
//                }
//            }
//        });
//
//        // Add the request to the RequestQueue.
//        queue.add(arrayreq);
        decodeCategories(responseStr, subCategoriesDownloadedListener);

    }

    /**
     * Decode categories loaded from server
     *
     * @param responseStr
     * @param categoriesDownloadedListener
     */
    public static void decodeCategories(String responseStr, onSubCategoriesDownloadedListener categoriesDownloadedListener) {
        try {
            JSONArray response = new JSONArray(responseStr);
            //parse categories
            List<SubCategory> categories = new ArrayList<>();
            for (int i = 0; i < response.length(); i++) {
                JSONObject jsonObject = response.getJSONObject(i);
                categories.add(new SubCategory(jsonObject));
            }
            //callback categories
            categoriesDownloadedListener.onSubCategoriesDownloaded(categories);
        }
        // Try and catch are included to handle any errors due to JSON
        catch (JSONException e) {
            e.printStackTrace();
            System.out.println(responseStr);
        }
    }

    /**
     * Get a single Category name from server using the is.
     *
     * @param context
     * @param id                - id of category
     * @param nameFoundListener - callback to return category name
     */
//    public static void getCategoryName(Context context, final int id, final onNameFoundListener nameFoundListener) {
//
//        //laod all categories
//        SubCategory.loadSubCategories(context, "", new SubCategory.onSubCategoriesDownloadedListener() {
//            @Override
//            public void onSubCategoriesDownloaded(List<SubCategory> categories) {
//                for (int i = 0; i < categories.size(); i++) {
//                    //find category by id
//                    if (categories.get(i).id == id) {
//                        nameFoundListener.onNameFound(categories.get(i).name);
//                    }
//                }
//            }
//        });
//    }

}
