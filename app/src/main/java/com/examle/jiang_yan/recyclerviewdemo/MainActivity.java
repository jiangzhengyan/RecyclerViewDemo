package com.examle.jiang_yan.recyclerviewdemo;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "appCompatActivity";
    private List<String> list = new ArrayList<>();//原来数据
    private List<String> moreData = new ArrayList<>();//上拉数据
    private List<String> refreshData = new ArrayList<>();//下拉刷新数据
    private RecyclerView mRecyclerView;
    private TextView tv_empty;
    private SwipeRefreshLayout sfl;
    private Handler handler = new Handler();
    private MyAdapter myAdapter;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //设置ToolBar

//        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolBar);

        //FloatingActionButton
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //FloatingActionButton 的点击事件
                Toast.makeText(MainActivity.this, "你点击了 FloatingActionButton", Toast.LENGTH_SHORT).show();
            }
        });
        init();
    }

    private void init() {
        initData();
        initRefreshData();
        initMoreData();
        initView();
        initListener();
    }

    /**
     * 初始化数据,填充到list集合中的数据
     */
    private void initData() {
        for (int i = 0; i < 20; i++) {
            list.add("这是第" + i + "条数据");
        }
    }

    /**
     * 下拉刷新加载数据
     */
    private void initRefreshData() {
        for (int i = 0; i < 2; i++) {
            refreshData.add("刷新的第" + i + "条数据");
        }
    }

    /**
     * 上拉加载更多数据
     */
    private void initMoreData() {
        for (int i = 0; i < 4; i++) {
            moreData.add("这是上拉加载的新的第" + i + "条数据");
        }
    }

    /**
     * 初始化view控件
     */
    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.mRecyclerView);
        tv_empty = (TextView) findViewById(R.id.tv_empty);
        sfl = (SwipeRefreshLayout) findViewById(R.id.sfl);
        //设置刷新圈的颜色,三种
        sfl.setColorSchemeColors(Color.RED, Color.YELLOW, Color.BLACK,Color.BLUE,Color.GREEN);
        //给recyclerView设置显示方式  单排-双排-瀑布等
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        //创建Adapter对象
        myAdapter = new MyAdapter(this, mRecyclerView);
        //设置数据
        myAdapter.setData(list);
        mRecyclerView.setAdapter(myAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //设置显示状态
        if (list.isEmpty()) {
            mRecyclerView.setVisibility(View.GONE);
            tv_empty.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            tv_empty.setVisibility(View.GONE);
        }

    }

    /**
     * 刷新,上拉加载的监听
     */
    private void initListener() {
        //刷新的监听
        sfl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //把刷新出来的数据,全部添加到集合的开始位置
                        list.addAll(0, refreshData);
                        myAdapter.notifyDataSetChanged();
                        //1秒后停止转圈
                        sfl.setRefreshing(false);
                    }
                }, 2000);
            }
        });

        /**
         * 设置滑动监听,监听上拉加载更多数据
         *
         */
        if (mRecyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int itemCount = linearLayoutManager.getItemCount();//条目的总数
                    int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
                    Log.e(TAG, "lastVisibleItemPosition: " + lastVisibleItemPosition + "itemCount:" + itemCount);
                    if (lastVisibleItemPosition == itemCount - 1) {
                        isLoading = true;
                        Log.e(TAG, "onScrolled:  islooading " +itemCount+"=lastVisibleItemPosition"+lastVisibleItemPosition);
                        list.add(null);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (isLoading) {
                                    list.remove(list.size() - 1);//移除刷新的脚
                                    //加载更多数据
                                    list.addAll(list.size(), moreData);
                                    myAdapter.notifyDataSetChanged();
                                    isLoading = false;
                                }
                            }
                        }, 2000);
                    }


                }
            });

        }
    }


    /**
     * MyAdapter  数据适配器
     */
    class MyAdapter extends RecyclerView.Adapter {
        private List<String> mData;
        private static final int VIEW_ITEM = 0;
        private static final int VIEW_PROG = 1;
        private Context mContext;

        public MyAdapter(Context context, RecyclerView mRecyclerView) {
            this.mContext = context;
        }

        /**
         * 设置数据
         *
         * @param list
         */
        public void setData(List<String> list) {
            this.mData = list;
        }

        /**
         * 创建viewHolder
         *
         * @param parent
         * @param viewType
         * @return
         */
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder holder;
            if (viewType == VIEW_ITEM) {
                View inflate = View.inflate(mContext, R.layout.item_view, null);
                holder = new MyViewHolder(inflate);
            } else {
                View inflate = View.inflate(mContext, R.layout.item_footer, null);
                holder = new MyProgressViewHolder(inflate);
            }
            return holder;
        }

        /**
         * 绑定viewHolder
         *
         * @param holder
         * @param position
         */
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                if (((MyViewHolder) holder).tvName != null)
                    ((MyViewHolder) holder).tvName.setText(mData.get(position));
            } else if (holder instanceof MyProgressViewHolder) {
                Log.e(TAG, "MyProgressViewHolder " );
                if (((MyProgressViewHolder) holder).pb != null)
                    ((MyProgressViewHolder) holder).pb.setIndeterminate(true);
            }
        }

        /**
         * 获取列表条目的总数
         *
         * @return
         */
        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        /**
         * 设置不同的类型
         *
         * @param position
         * @return
         */
        @Override
        public int getItemViewType(int position) {
            Log.e("getItemViewType", "getI  "+mData.get(position) );
            return mData.get(position) != null ? VIEW_ITEM : VIEW_PROG;
        }

        /**
         * 进度圈的ViewHolder
         */
        class MyProgressViewHolder extends RecyclerView.ViewHolder {
            private ProgressBar pb;

            public MyProgressViewHolder(View itemView) {
                super(itemView);
                pb = (ProgressBar) itemView.findViewById(R.id.pb);
            }

        }

        /**
         * 条目的ViewHolder
         */
        class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView tvName;

            public MyViewHolder(View itemView) {
                super(itemView);
                tvName = (TextView) itemView.findViewById(R.id.tv_name);
            }
        }
    }
}
