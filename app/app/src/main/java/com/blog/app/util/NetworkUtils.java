package com.blog.app.util;

import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 网络请求工具类
 * 提供基本的HTTP GET请求功能
 */
public class NetworkUtils {
    private static final String TAG = "NetworkUtils";
    private static final int CONNECT_TIMEOUT = 15000; // 15秒连接超时
    private static final int READ_TIMEOUT = 15000;    // 15秒读取超时

    // 使用线程池管理网络请求
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    /**
     * 网络请求回调接口
     */
    public interface NetworkCallback {
        void onSuccess(String response);
        void onError(String errorMessage);
    }

    /**
     * 执行GET请求
     * @param urlString 请求URL
     * @param callback 结果回调
     */
    public static void fetchUrl(String urlString, NetworkCallback callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                Log.d(TAG, "开始请求: " + urlString);

                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(CONNECT_TIMEOUT);
                connection.setReadTimeout(READ_TIMEOUT);

                // 设置请求头
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                // 获取响应码
                int responseCode = connection.getResponseCode();
                Log.d(TAG, "响应码: " + responseCode);

                // 读取响应内容
                InputStream inputStream;
                if (responseCode >= 200 && responseCode < 300) {
                    inputStream = connection.getInputStream();
                } else {
                    inputStream = connection.getErrorStream();
                }

                String response = readStream(inputStream);
                Log.v(TAG, "原始响应数据:\n" + response);

                // 成功回调
                callback.onSuccess(response);
            } catch (Exception e) {
                Log.e(TAG, "网络请求失败", e);
                callback.onError("网络错误: " + e.getMessage());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    /**
     * 从InputStream读取数据
     */
    private static String readStream(InputStream inputStream) throws IOException {
        if (inputStream == null) return "";

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8)
        );

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        return response.toString();
    }

    /**
     * 停止所有网络请求
     */
    public static void shutdown() {
        executor.shutdownNow();
    }
}
