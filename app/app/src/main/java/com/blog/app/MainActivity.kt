package com.blog.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.blog.app.fragment.ContentFragment;
import com.blog.app.fragment.HomeFragment;
import com.blog.app.fragment.UserFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    // 声明 Fragment 实例变量以便复用
    private Fragment homeFragment;
    private Fragment contentFragment;
    private Fragment userFragment;
    private Fragment currentFragment; // 当前显示的 Fragment

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化 Fragment 实例
        homeFragment = new HomeFragment();
        contentFragment = new ContentFragment();
        userFragment = new UserFragment();
        currentFragment = homeFragment; // 默认显示首页

        // 设置底部导航栏
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);

        // 默认显示首页
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, currentFragment)
                    .commit();
        }
    }

    private final NavigationBarView.OnItemSelectedListener navListener =
            new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    // 使用 if-else 替代 switch 解决 "Constant expression required" 错误
                    if (item.getItemId() == R.id.navigation_home) {
                        if (currentFragment != homeFragment) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, homeFragment)
                                    .commit();
                            currentFragment = homeFragment;
                        }
                        return true;
                    } else if (item.getItemId() == R.id.navigation_content) {
                        if (currentFragment != contentFragment) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, contentFragment)
                                    .commit();
                            currentFragment = contentFragment;
                        }
                        return true;
                    } else if (item.getItemId() == R.id.navigation_user) {
                        if (currentFragment != userFragment) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, userFragment)
                                    .commit();
                            currentFragment = userFragment;
                        }
                        return true;
                    }
                    return false;
                }
            };
}