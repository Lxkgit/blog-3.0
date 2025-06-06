package com.blog.app.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.blog.app.R;
import com.blog.app.api.ApiClient;
import com.blog.app.api.ApiResponse;
import com.blog.app.api.ApiService;
import com.blog.app.entity.Article;
import com.blog.app.util.NetworkUtils;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.noties.markwon.Markwon;
import io.noties.markwon.image.glide.GlideImagesPlugin;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArticleDetailFragment extends Fragment {
    private static final String ARG_ARTICLE_ID = "article_id";

    private long articleId;
    private ProgressBar progressBar;
    private TextView errorText;
    private SwipeRefreshLayout swipeRefreshLayout;

    // 视图组件
    private ImageView articleImage;
    private TextView articleTitle;
    private TextView articleAuthor;
    private TextView articleDate;
    private TextView articleContent;
    private TextView articleType;
    private TextView likeCount;
    private TextView commentCount;
    private TextView browseCount;

    public static ArticleDetailFragment newInstance(long articleId) {
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ARTICLE_ID, articleId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            articleId = getArguments().getLong(ARG_ARTICLE_ID, 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article_detail, container, false);

        // 返回按钮
        ImageButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            // 返回上一页
            requireActivity().onBackPressed();
        });

        // 初始化视图
        progressBar = view.findViewById(R.id.progress_bar);
        errorText = view.findViewById(R.id.error_text);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        articleImage = view.findViewById(R.id.article_image);
        articleTitle = view.findViewById(R.id.article_title);
        articleAuthor = view.findViewById(R.id.article_author);
        articleDate = view.findViewById(R.id.article_date);
        articleContent = view.findViewById(R.id.article_content);
        articleType = view.findViewById(R.id.article_type);
        likeCount = view.findViewById(R.id.like_count);
        commentCount = view.findViewById(R.id.comment_count);
        browseCount = view.findViewById(R.id.browse_count);

        // 设置下拉刷新
        swipeRefreshLayout.setOnRefreshListener(this::loadArticleDetail);

        // 加载文章详情
        loadArticleDetail();

        return view;
    }

    private void loadArticleDetail() {
        progressBar.setVisibility(View.VISIBLE);
        errorText.setVisibility(View.GONE);

        // 构建详情API URL
        String url = "http://49.232.129.253/api/content/article/id?id=" + articleId;

        // 使用NetworkUtils加载数据
        NetworkUtils.fetchUrl(url, new NetworkUtils.NetworkCallback() {
            @Override
            public void onSuccess(String response) {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);

                    try {
                        JSONObject json = new JSONObject(response);

                        if (json.optString("code", "500").equals("200")) {
                            JSONObject articleData = json.optJSONObject("result");
                            if (articleData != null) {
                                updateUI(articleData);
                                return;
                            }
                        }

                        // 处理错误情况
                        String errorMsg = json.optString("message", "未知错误");
                        showError("API错误: " + errorMsg);
                    } catch (JSONException e) {
                        showError("解析错误: " + e.getMessage());
                        Log.e("DetailFragment", "解析错误", e);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    showError(errorMessage);
                });
            }
        });
    }

    private void updateUI(JSONObject articleData) {
        // 设置文章标题
        articleTitle.setText(articleData.optString("title", "无标题"));

        // 设置作者
        articleAuthor.setText("作者: " + articleData.optString("userVo.nickname", "未知"));

        // 设置日期
        articleDate.setText(articleData.optString("createTime", ""));

        // 设置内容 - 使用Markdown解析器（如需要）
        String content = articleData.optString("contentMd", "暂无内容");
        articleContent.setText(content);

        // 设置文章类型
        JSONArray types = articleData.optJSONArray("articleTypes");
        if (types != null && types.length() > 0) {
            JSONObject type = types.optJSONObject(0);
            articleType.setText("分类: " + type.optString("typeName", ""));
        } else {
            articleType.setText("分类: 无");
        }

        // 设置统计数据
        likeCount.setText(String.valueOf(articleData.optInt("likeCount", 0)));
        commentCount.setText(String.valueOf(articleData.optInt("commentCount", 0)));
        browseCount.setText(String.valueOf(articleData.optInt("browseCount", 0)));

        // 加载图片
        String imageUrl = articleData.optString("contentImg", "");
        if (!imageUrl.isEmpty()) {
            Glide.with(requireContext())
                    .load(imageUrl)
//                    .placeholder(R.drawable.placeholder_image)
//                    .error(R.drawable.error_image)
                    .into(articleImage);
        }

        // 设置Markdown内容
        String markdownContent = articleData.optString("contentMd", "暂无内容");

        // 创建Markwon实例
        Markwon markwon = Markwon.builder(requireContext())
                .usePlugin(GlideImagesPlugin.create(requireContext()))
                .build();

        // 渲染Markdown到TextView
        markwon.setMarkdown(articleContent, markdownContent);
    }

    private void showError(String message) {
        errorText.setVisibility(View.VISIBLE);
        errorText.setText(message);
    }
}
//public class ArticleDetailFragment extends Fragment {
//    private static final String ARG_ARTICLE_ID = "article_id";
//
//    private long articleId;
//    private ProgressBar progressBar;
//    private TextView errorText;
//    private SwipeRefreshLayout swipeRefreshLayout;
//
//    // 视图组件
//    private ImageView articleImage;
//    private TextView articleTitle;
//    private TextView articleAuthor;
//    private TextView articleDate;
//    private TextView articleContent;
//    private TextView articleType;
//    private TextView likeCount;
//    private TextView commentCount;
//    private TextView browseCount;
//
//    public static ArticleDetailFragment newInstance(long articleId) {
//        ArticleDetailFragment fragment = new ArticleDetailFragment();
//        Bundle args = new Bundle();
//        args.putLong(ARG_ARTICLE_ID, articleId);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            articleId = getArguments().getLong(ARG_ARTICLE_ID, 0);
//        }
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_article_detail, container, false);
//
//        // 初始化视图
//        progressBar = view.findViewById(R.id.progress_bar);
//        errorText = view.findViewById(R.id.error_text);
//        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
//        articleImage = view.findViewById(R.id.article_image);
//        articleTitle = view.findViewById(R.id.article_title);
//        articleAuthor = view.findViewById(R.id.article_author);
//        articleDate = view.findViewById(R.id.article_date);
//        articleContent = view.findViewById(R.id.article_content);
//        articleType = view.findViewById(R.id.article_type);
//        likeCount = view.findViewById(R.id.like_count);
//        commentCount = view.findViewById(R.id.comment_count);
//        browseCount = view.findViewById(R.id.browse_count);
//
//        // 设置下拉刷新
//        swipeRefreshLayout.setOnRefreshListener(this::loadArticleDetail);
//
//        // 加载文章详情
//        loadArticleDetail();
//
//        return view;
//    }
//
//    private void loadArticleDetail() {
//        progressBar.setVisibility(View.VISIBLE);
//        errorText.setVisibility(View.GONE);
//
//        ApiService apiService = ApiClient.getClient().create(ApiService.class);
//
//        Call<ApiResponse> call = apiService.getArticleDetailById(articleId);
//
//        call.enqueue(new Callback<ApiResponse>() {
//            @Override
//            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
//                progressBar.setVisibility(View.GONE);
//                swipeRefreshLayout.setRefreshing(false);
//
//                if (response.isSuccessful() && response.body() != null) {
//                    ApiResponse apiResponse = response.body();
//                    Gson gson = new Gson();
//                    Article article;
//                    try {
//                        article = parseJson(gson.toJson(apiResponse));
//                    } catch (JSONException e) {
//                        throw new RuntimeException(e);
//                    }
////                    // 检查API响应状态码
////                    if ("200".equals(apiResponse.getCode())) {
////                        // 更新RecyclerView数据
////                        adapter.updateData(newArticles);
////
////                        // 如果数据为空，显示提示信息
////                        if (newArticles.isEmpty()) {
////                            showMessage("暂无文章数据");
////                        }
////                    } else {
////                        // API返回错误
////                        showError("API错误: " + apiResponse.getMessage());
////                    }
//                } else {
//                    showError("网络请求失败");
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ApiResponse> call, Throwable t) {
//
//            }
//        });
//
//    }
//
//    private Article parseJson(String json) throws JSONException {
//
//        JSONObject response = new JSONObject(json);
//
//        Log.i("JSON", json);
//        // 检查状态码
//        String code = response.optString("code", "500");
//        if (!"200".equals(code)) {
//            String errorMsg = response.optString("message", "未知错误");
//            throw new RuntimeException("API错误: " + errorMsg);
//        }
//
//        // 获取result对象
//        JSONObject result = response.optJSONObject("result");
//        if (result == null) {
//            throw new RuntimeException("缺少result字段");
//        }
//
//
//        Article article = new Article();
//
//        // 解析基本字段
//        article.setId(result.optLong("id", 0));
//        article.setUserId(result.optLong("userId", 0));
//        article.setTitle(result.optString("title", "无标题"));
//        article.setContentMd(result.optString("contentMd", ""));
//        article.setContentImg(result.optString("contentImg", ""));
//        article.setContentMemo(result.optString("contentMemo", ""));
//        article.setArticleType(result.optString("articleType", ""));
//        article.setArticleLabel(result.optString("articleLabel", ""));
//        article.setArticleStatus(result.optInt("articleStatus", 0));
//        article.setBrowseCount(result.optInt("browseCount", 0));
//        article.setLikeCount(result.optInt("likeCount", 0));
//        article.setCreateTime(result.optString("createTime", ""));
//        article.setUpdateTime(result.optString("updateTime", ""));
//
//        // 解析文章类型数组
//        JSONArray typesArray = result.optJSONArray("articleTypes");
//        if (typesArray != null && typesArray.length() > 0) {
//            List<Article.ArticleType> types = new ArrayList<>();
//            for (int j = 0; j < typesArray.length(); j++) {
//                JSONObject typeObj = typesArray.optJSONObject(j);
//                if (typeObj != null) {
//                    Article.ArticleType type = new Article.ArticleType();
//                    type.setId(typeObj.optInt("id", 0));
//                    type.setParentId(typeObj.optInt("parentId", 0));
//                    type.setTypeName(typeObj.optString("typeName", ""));
//                    type.setNum(typeObj.optInt("num", 0));
//                    type.setNode(typeObj.optInt("node", 0));
//                    type.setCreateUser(typeObj.optInt("createUser", 0));
//                    type.setCreateTime(typeObj.optString("createTime", ""));
//                    type.setUpdateTime(typeObj.optString("updateTime", ""));
//                    types.add(type);
//                }
//            }
//            article.setArticleTypes(types);
//        }
//
//
//        return article;
//    }
//
//    private void updateUI(JSONObject articleData) {
//        // 设置文章标题
//        articleTitle.setText(articleData.optString("title", "无标题"));
//
//        // 设置作者
//        articleAuthor.setText("作者: " + articleData.optString("userVo.nickname", "未知"));
//
//        // 设置日期
//        articleDate.setText(articleData.optString("createTime", ""));
//
//        // 设置内容 - 使用Markdown解析器（如需要）
//        String content = articleData.optString("contentMd", "暂无内容");
//        articleContent.setText(content);
//
//        // 设置文章类型
//        JSONArray types = articleData.optJSONArray("articleTypes");
//        if (types != null && types.length() > 0) {
//            JSONObject type = types.optJSONObject(0);
//            articleType.setText("分类: " + type.optString("typeName", ""));
//        } else {
//            articleType.setText("分类: 无");
//        }
//
//        // 设置统计数据
//        likeCount.setText(String.valueOf(articleData.optInt("likeCount", 0)));
//        commentCount.setText(String.valueOf(articleData.optInt("commentCount", 0)));
//        browseCount.setText(String.valueOf(articleData.optInt("browseCount", 0)));
//
//        // 加载图片
//        String imageUrl = articleData.optString("contentImg", "");
//        if (!imageUrl.isEmpty()) {
//            Glide.with(requireContext())
//                    .load(imageUrl)
////                    .placeholder(R.drawable.placeholder_image)
////                    .error(R.drawable.error_image)
//                    .into(articleImage);
//        }
//    }
//
//    private void showError(String message) {
//        errorText.setVisibility(View.VISIBLE);
//        errorText.setText(message);
//    }
//}