package com.blog.app.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * API服务接口
 */
public interface ApiService {
    @GET("content/article/list")
    Call<ApiResponse> getArticles(
            @Query("pageNum") int pageNum,
            @Query("pageSize") int pageSize,
            @Query("type") int type,
            @Query("selectUser") int selectUser,
            @Query("selectStatus") String selectStatus,
            @Query("sortType") String sortType
    );
}
