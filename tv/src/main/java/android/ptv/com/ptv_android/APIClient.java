package android.ptv.com.ptv_android;

import android.ptv.com.ptv_android.model.CurrentlyPlayingResponse;
import android.ptv.com.ptv_android.model.Program;
import android.util.Log;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class APIClient {
    private ObjectMapper mapper;
    private String basePath = "URLGOESHERE";
    private OkHttpClient client;;

    public APIClient() {

        client = new OkHttpClient();
        this.mapper = new ObjectMapper();
    }


    public List<Program> getCurrentPlayingPrograms() {
        String url = this.basePath + "/whatson.php";
        return getPrograms(url);
    }

    private List<Program> getPrograms(String url) {
        String str = this.getDataFromUrl(url);

        try {
            CurrentlyPlayingResponse response = mapper.readValue(str, CurrentlyPlayingResponse.class);
            return response.getPrograms();
        } catch (IOException e) {
            Log.e("API", "JSON = " + str);
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public List<Program> getNextShows(int channelId) {
        String url = this.basePath + "/nextshows.php?channelId=" + channelId;
        return getPrograms(url);
    }


    private String getDataFromUrl(String urlString) {
        Log.d("API", "Loading: " + urlString);
        Request request = new Request.Builder()
                .url(urlString)
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            if (response != null) {
                response.close();
            }
            Log.e("API", "Error getting URL: " + urlString + " - Exception: " + e.getMessage());
        }
        return "";
    }
}
