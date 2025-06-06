package com.blog.app.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.blog.app.R;
import com.blog.app.adapter.ArticleAdapter;
import com.blog.app.api.ApiClient;
import com.blog.app.api.ApiResponse;
import com.blog.app.api.ApiService;
import com.blog.app.entity.Article;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 内容页面Fragment - 显示文章列表
 */
public class ContentFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArticleAdapter adapter;
    private ProgressBar progressBar;
    private TextView errorText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Article> articles = new ArrayList<>();
    private AdapterView.OnItemClickListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_content, container, false);

        // 初始化视图组件
        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        errorText = view.findViewById(R.id.error_text);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);

        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        super.onViewCreated(view, savedInstanceState);

        // 初始化适配器并设置点击监听器
        adapter = new ArticleAdapter(new ArrayList<>(), article -> {
            // 创建详情Fragment并传递文章ID
            ArticleDetailFragment detailFragment = ArticleDetailFragment.newInstance(article.getId());

            // 执行导航
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack("article_detail") // 添加到返回栈
                    .commit();
        });

        recyclerView.setAdapter(adapter);

        // 设置下拉刷新监听器
        swipeRefreshLayout.setOnRefreshListener(this::loadArticles);

        // 首次加载数据
        loadArticles();

        return view;
    }

    /**
     * 从API加载文章数据
     */
    private void loadArticles() {
        // 显示加载状态
        progressBar.setVisibility(View.VISIBLE);
        errorText.setVisibility(View.GONE);

        // 创建API服务实例
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

//        showMessage("请求接口");
        // 构建API请求参数
        Call<ApiResponse> call = apiService.getArticles(
                1,   // pageNum
                5,   // pageSize
                0,   // type
                0,   // selectUser
                "1,2", // selectStatus
                "0,1"  // sortType
        );

        // 异步执行网络请求
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                // 隐藏加载状态
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    Gson gson = new Gson();
                    List<Article> newArticles = null;
                    try {
                        newArticles = parseJson(gson.toJson(apiResponse));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    // 检查API响应状态码
                    if ("200".equals(apiResponse.getCode())) {
                        // 更新RecyclerView数据
                        adapter.updateData(newArticles);

                        // 如果数据为空，显示提示信息
                        if (newArticles.isEmpty()) {
                            showMessage("暂无文章数据");
                        }
                    } else {
                        // API返回错误
                        showError("API错误: " + apiResponse.getMessage());
                    }
                } else {
                    // 网络请求失败
                    showError("请求失败: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // 隐藏加载状态
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                // 显示错误信息
                showError("网络错误: " + t.getMessage());
            }
        });
    }

    private List<Article> parseJson(String json) throws JSONException {
        List<Article> articles = new ArrayList<>();
        JSONObject response = new JSONObject(json);

        Log.i("JSON", json);
        // 检查状态码
        String code = response.optString("code", "500");
        if (!"200".equals(code)) {
            String errorMsg = response.optString("message", "未知错误");
            throw new RuntimeException("API错误: " + errorMsg);
        }

        // 获取result对象
        JSONObject result = response.optJSONObject("result");
        if (result == null) {
            throw new RuntimeException("缺少result字段");
        }

        // 获取文章列表
        JSONArray list = result.optJSONArray("list");
        if (list == null || list.length() == 0) {
            Log.w("Parser", "文章列表为空");
            return articles;
        }

        for (int i = 0; i < list.length(); i++) {
            JSONObject item = list.optJSONObject(i);
            if (item == null) continue;

            Article article = new Article();

            // 解析基本字段
            article.setId(item.optLong("id", 0));
            article.setUserId(item.optLong("userId", 0));
            article.setTitle(item.optString("title", "无标题"));
            article.setContentMd(item.optString("contentMd", ""));
            article.setContentImg(item.optString("contentImg", ""));
            article.setContentMemo(item.optString("contentMemo", ""));
            article.setArticleType(item.optString("articleType", ""));
            article.setArticleLabel(item.optString("articleLabel", ""));
            article.setArticleStatus(item.optInt("articleStatus", 0));
            article.setBrowseCount(item.optInt("browseCount", 0));
            article.setLikeCount(item.optInt("likeCount", 0));
            article.setCreateTime(item.optString("createTime", ""));
            article.setUpdateTime(item.optString("updateTime", ""));

            // 解析文章类型数组
            JSONArray typesArray = item.optJSONArray("articleTypes");
            if (typesArray != null && typesArray.length() > 0) {
                List<Article.ArticleType> types = new ArrayList<>();
                for (int j = 0; j < typesArray.length(); j++) {
                    JSONObject typeObj = typesArray.optJSONObject(j);
                    if (typeObj != null) {
                        Article.ArticleType type = new Article.ArticleType();
                        type.setId(typeObj.optInt("id", 0));
                        type.setParentId(typeObj.optInt("parentId", 0));
                        type.setTypeName(typeObj.optString("typeName", ""));
                        type.setNum(typeObj.optInt("num", 0));
                        type.setNode(typeObj.optInt("node", 0));
                        type.setCreateUser(typeObj.optInt("createUser", 0));
                        type.setCreateTime(typeObj.optString("createTime", ""));
                        type.setUpdateTime(typeObj.optString("updateTime", ""));
                        types.add(type);
                    }
                }
                article.setArticleTypes(types);
            }

            articles.add(article);
        }

        return articles;
    }

    /**
     * 显示错误信息
     */
    private void showError(String message) {
        errorText.setVisibility(View.VISIBLE);
        errorText.setText(message);
    }

    /**
     * 显示提示消息
     */
    private void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}