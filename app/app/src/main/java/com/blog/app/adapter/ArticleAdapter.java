package com.blog.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blog.app.R;
import com.blog.app.entity.Article;
import com.bumptech.glide.Glide;
import java.util.List;

/**
 * 文章适配器 - 将数据绑定到RecyclerView
 */
public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {

    private List<Article> articles;

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Article article);
    }

    public ArticleAdapter(List<Article> articles, OnItemClickListener listener) {
        this.articles = articles;
        this.listener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载文章卡片布局
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_article, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Article article = articles.get(position);

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(article);
            }
        });

        // 设置文章标题
        holder.title.setText(article.getTitle());
        holder.author.setText("作者："+ article.getUserId());
        // 设置发布时间（简化处理）
        holder.time.setText(article.getUpdateTime()); // 实际项目中应格式化时间

        // 设置文章摘要
        String summary = article.getContentMemo();
        if (summary.length() > 100) {
            summary = summary.substring(0, 100) + "...";
        }
        holder.summary.setText(summary);

        // 使用Glide加载图片
        if (article.getContentImg() != null && !article.getContentImg().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(article.getContentImg())
//                    .placeholder(R.drawable.placeholder_image) // 加载中的占位图
//                    .error(R.drawable.error_image) // 加载失败的图片
                    .centerCrop()
                    .into(holder.image);
        } else {
            // 没有封面图片时使用默认图片
//            holder.image.setImageResource(R.drawable.placeholder_image);
        }

        // 设置点赞和评论数（示例数据）
        holder.likes.setText(article.getLikeCount()+"");
        holder.comments.setText(article.getBrowseCount()+"");
    }

    @Override
    public int getItemCount() {
        return articles != null ? articles.size() : 0;
    }

    /**
     * 更新数据
     */
    public void updateData(List<Article> newArticles) {
        this.articles = newArticles;
        notifyDataSetChanged(); // 通知RecyclerView刷新数据
    }

    /**
     * ViewHolder类 - 缓存视图组件
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, author, time, summary, likes, comments;
        ImageView bookmark;

        ViewHolder(View itemView) {
            super(itemView);
            // 初始化视图组件
            image = itemView.findViewById(R.id.article_image);
            title = itemView.findViewById(R.id.article_title);
            author = itemView.findViewById(R.id.article_author);
            time = itemView.findViewById(R.id.article_time);
            summary = itemView.findViewById(R.id.article_summary);
            likes = itemView.findViewById(R.id.article_likes);
            comments = itemView.findViewById(R.id.article_comments);
//            bookmark = itemView.findViewById(R.id.article_bookmark);
        }
    }
}