package com.wyh.slide;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.xmwj.slidingmenu.R;

import java.util.ArrayList;
import java.util.List;

import static com.wyh.main.HomeActivity.ddd;

/**
 * created by yhao on 2017/9/8.
 * <p>
 * viewType:
 * item: 1-99
 * header:101-199
 * footer:201-299
 */


public class SlideAdapter extends RecyclerView.Adapter<ItemView> {


    private List mData;
    private List<SlideItem> mSlideItems;
    private IItemBind mIItemBind;
    private HeaderBind mHeaderBind;
    private FooterBind mFooterBind;
    private IItemType mIItemType;
    private int mItemViewWidth;
    private NormalItem mRefreshHeader;
    private List<NormalItem> mHeaders;
    private List<NormalItem> mFooters;
    private ItemView mBottomFooter;
    private static final int TYPE_REFRESH_HEADER = 0;
    private static final int TYPE_HEADER_ORIGIN = 101;
    private static final int TYPE_FOOTER_ORIGIN = 201;
    private boolean mLoading;
    private BottomListener mBottomListener;
    private RecyclerView mRecycleView;


    //侧滑相关
    private SlideLayout mOpenItem;
    private SlideLayout mScrollingItem;

    SlideLayout getScrollingItem() {
        return mScrollingItem;
    }

    void setScrollingItem(SlideLayout scrollingItem) {
        mScrollingItem = scrollingItem;
    }

    void holdOpenItem(SlideLayout openItem) {
        mOpenItem = openItem;
    }

    void closeOpenItem() {
        if (mOpenItem != null && mOpenItem.isOpen()) {
            mOpenItem.close();
            mOpenItem = null;
        }
    }

    @Override
    public ItemView onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_REFRESH_HEADER) {
            return ItemView.create(parent.getContext(), parent, mRefreshHeader);
        }
        if (isHeader(viewType)) {
            return ItemView.create(parent.getContext(), parent, mHeaders.get(viewType - TYPE_HEADER_ORIGIN));
        }
        if (isFooter(viewType)) {
            return ItemView.create(parent.getContext(), parent, mFooters.get(viewType - TYPE_FOOTER_ORIGIN));
        }
        return ItemView.create(parent.getContext(), parent, mSlideItems.get(viewType - 1));
    }

    @Override
    public void onViewAttachedToWindow(ItemView holder) {
        super.onViewAttachedToWindow(holder);
        ddd("onViewAttachedToWindow");
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        ddd("onAttachedToRecyclerView");
        mRecycleView.getLayoutManager().scrollVerticallyBy(100, null, null);

        //todo 怎么集成刷新头部 ？ md风格的吗？
    }

    @Override
    public void onBindViewHolder(final ItemView holder, int position) {
        ddd("onBindViewHolder");
        View contentView = holder.getContentView();
        LinearLayout.LayoutParams contentParams = (LinearLayout.LayoutParams) contentView.getLayoutParams();
        contentParams.width = mItemViewWidth;
        if (getItemViewType(position) == TYPE_REFRESH_HEADER) {
            if (mRefreshHeader.heightRatio > 0) {
                contentParams.height = (int) (ScreenSize.h(contentView.getContext()) *
                        mHeaders.get(position).heightRatio);
                contentView.setLayoutParams(contentParams);
            }
            return;
        }
        if (isHeader(getItemViewType(position))) {
            if (mHeaders.get(position - getRefreshHeaderNum()).heightRatio > 0) {
                contentParams.height = (int) (ScreenSize.h(contentView.getContext()) *
                        mHeaders.get(position - getRefreshHeaderNum()).heightRatio);
                contentView.setLayoutParams(contentParams);
            }
            if (mHeaderBind != null) {
                mHeaderBind.onBind(holder, position + 1 - getRefreshHeaderNum());
            }
            return;
        }
        if (isFooter(getItemViewType(position))) {
            if (mFooters.get(position - getRefreshHeaderNum() - getHeaderNum() - mData.size()).heightRatio > 0) {
                contentParams.height = (int) (ScreenSize.h(contentView.getContext()) *
                        mFooters.get(position - getRefreshHeaderNum() - getHeaderNum() - mData.size()).heightRatio);
                contentView.setLayoutParams(contentParams);
            }
            if (mFooterBind != null) {
                mFooterBind.onBind(holder, position + 1 - getRefreshHeaderNum());
            }
            if (position == getHeaderNum() + getRefreshHeaderNum() + mData.size() + getFooterNum() - 1) {
                mBottomFooter = holder;
            }
            return;
        }
        contentView.setLayoutParams(contentParams);
        initLeftRightMenu(holder, mItemViewWidth, position);
        if (mIItemBind != null) {
            mIItemBind.bind(holder, mData.get(position - getHeaderNum() - getRefreshHeaderNum()),
                    position - getHeaderNum() - getRefreshHeaderNum());
        }
    }

    private void initLeftRightMenu(ItemView holder, int itemWidth, int position) {
        final SlideItem item = mSlideItems.get(getItemViewType(position) - 1);
        View rightMenu = holder.getRightMenu();
        if (rightMenu != null) {
            LinearLayout.LayoutParams rightMenuParams = (LinearLayout.LayoutParams) rightMenu.getLayoutParams();
            rightMenuParams.width = (int) (itemWidth * item.rightMenuRatio);
            rightMenu.setLayoutParams(rightMenuParams);
            ((SlideLayout) holder.getView(R.id.yhaolpz_slideLayout)).setRightMenuWidth(rightMenuParams.width);
        }
        View leftMenu = holder.getLeftMenu();
        if (leftMenu != null) {
            LinearLayout.LayoutParams leftMenuParams = (LinearLayout.LayoutParams) leftMenu.getLayoutParams();
            leftMenuParams.width = (int) (itemWidth * item.leftMenuRatio);
            leftMenu.setLayoutParams(leftMenuParams);
            holder.getView(R.id.yhaolpz_slideLayout).scrollTo(leftMenuParams.width, 0);
            ((SlideLayout) holder.getView(R.id.yhaolpz_slideLayout)).setLeftMenuWidth(leftMenuParams.width);
        }
    }

    private boolean isHeader(int viewType) {
        return viewType >= TYPE_HEADER_ORIGIN && viewType < TYPE_FOOTER_ORIGIN;
    }

    private boolean isFooter(int viewType) {
        return viewType >= TYPE_FOOTER_ORIGIN;
    }

    private int getHeaderNum() {
        return mHeaders == null ? 0 : mHeaders.size();
    }

    private int getFooterNum() {
        return mFooters == null ? 0 : mFooters.size();
    }

    private int getRefreshHeaderNum() {
        return mRefreshHeader == null ? 0 : 1;
    }


    @Override
    public int getItemViewType(int position) {

        if (mRefreshHeader != null && position == 0) {
            return TYPE_REFRESH_HEADER;
        }
        if (getHeaderNum() > 0 && position < getHeaderNum() + getRefreshHeaderNum()) {
            return TYPE_HEADER_ORIGIN + position - getRefreshHeaderNum();
        }
        if (getFooterNum() > 0 && position >= getHeaderNum() + getRefreshHeaderNum() + mData.size()) {
            return TYPE_FOOTER_ORIGIN + position - getRefreshHeaderNum() - getHeaderNum() - mData.size();
        }
        return mIItemType == null || mSlideItems.size() == 1 ? 1 : mIItemType.type(
                mData.get(position - getHeaderNum() - getRefreshHeaderNum()),
                position - getHeaderNum() - getRefreshHeaderNum());
    }

    @Override
    public int getItemCount() {
        return mData.size() + getRefreshHeaderNum() + getHeaderNum() + getFooterNum();
    }


    private void onBottom() {
        if (mBottomListener != null) {
            if (!mLoading) {
                mLoading = true;
                mBottomListener.onBottom(mBottomFooter, SlideAdapter.this);
            }
        }
    }

    public void loadMore(List data) {
        int pos = mData.size() + getHeaderNum() + getRefreshHeaderNum();
        mData.addAll(data);
        this.notifyItemRangeInserted(pos, data.size());
        mLoading = false;
    }


    private SlideAdapter(final Builder build, final RecyclerView recyclerView) {
        this.mSlideItems = build.slideItems;
        this.mIItemBind = build.itemBind;
        this.mIItemType = build.itemType;
        this.mData = build.data;
        this.mHeaders = build.headers;
        this.mFooters = build.footers;
        this.mHeaderBind = build.headerBind;
        this.mFooterBind = build.footerBind;
        this.mBottomListener = build.bottomListener;
        this.mRefreshHeader = build.refreshHeader;
        this.mRecycleView = recyclerView;
        init();
    }

    private void init() {
        mRecycleView.setAdapter(this);
        mRecycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                setScrollingItem(null);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!recyclerView.canScrollVertically(1)) {
                        onBottom();
                    }
                }
            }
        });
        //TODO 如果recycleView 相对于屏幕有边距，则对recycleView设置margin或padding ，
        //若父布局是 viewPager 可能会报错
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mRecycleView.getLayoutParams();
        int recyclerViewMargin = layoutParams.leftMargin + layoutParams.rightMargin;
        int recyclerViewPadding = mRecycleView.getPaddingLeft() + mRecycleView.getPaddingRight();
        mItemViewWidth = ScreenSize.w(mRecycleView.getContext()) - recyclerViewMargin - recyclerViewPadding;
    }


    private static Builder mBuilder = null;

    public static class Builder {
        List data;
        List<SlideItem> slideItems;
        IItemBind itemBind;
        IItemType itemType;
        BottomListener bottomListener;
        List<NormalItem> headers;
        List<NormalItem> footers;
        NormalItem refreshHeader;
        HeaderBind headerBind;
        FooterBind footerBind;


        Builder load(List data) {
            this.data = data;
            return this;
        }

        public Builder item(@NonNull int itemLayoutId) {
            this.item(itemLayoutId, 0, 0, 0, 0);
            return this;
        }

        public Builder item(@NonNull int itemLayoutId, @NonNull int leftMenuLayoutId, @NonNull float leftMenuRatio, @NonNull int rightMenuLayoutId, @NonNull float rightMenuRatio) {
            if (slideItems == null) {
                slideItems = new ArrayList<>();
            }
            slideItems.add(new SlideItem(itemLayoutId, leftMenuLayoutId, leftMenuRatio, rightMenuLayoutId, rightMenuRatio));
            return this;
        }


        public Builder refreshHeader(@NonNull int layoutId, @NonNull float heightRatio) {
            refreshHeader = new NormalItem(layoutId, heightRatio);
            return this;
        }


        public Builder header(@NonNull int layoutId) {
            this.header(layoutId, 0);
            return this;
        }

        public Builder header(@NonNull int layoutId, @NonNull float heightRatio) {
            if (headers == null) {
                headers = new ArrayList<>();
            }
            headers.add(new NormalItem(layoutId, heightRatio));
            return this;
        }

        public Builder footer(@NonNull int layoutId) {
            this.footer(layoutId, 0);
            return this;
        }

        public Builder footer(@NonNull int layoutId, @NonNull float heightRatio) {
            if (footers == null) {
                footers = new ArrayList<>();
            }
            footers.add(new NormalItem(layoutId, heightRatio));
            return this;
        }

        public Builder bind(@NonNull IItemBind itemBind) {
            this.itemBind = itemBind;
            return this;
        }

        public Builder bind(@NonNull HeaderBind headerBind) {
            this.headerBind = headerBind;
            return this;
        }

        public Builder bind(@NonNull FooterBind footerBind) {
            this.footerBind = footerBind;
            return this;
        }

        public Builder type(@NonNull IItemType itemType) {
            this.itemType = itemType;
            return this;
        }

        public Builder listen(@NonNull BottomListener bottomListener) {
            this.bottomListener = bottomListener;
            return this;
        }

        public SlideAdapter into(@NonNull RecyclerView recyclerView) {
            SlideAdapter adapter = new SlideAdapter(mBuilder, recyclerView);
            mBuilder = null;
            return adapter;
        }
    }

    public static Builder load(List data) {
        return getBuilder().load(data);
    }


    private static Builder getBuilder() {
        if (mBuilder == null) {
            mBuilder = new Builder();
        }
        return mBuilder;
    }


}
