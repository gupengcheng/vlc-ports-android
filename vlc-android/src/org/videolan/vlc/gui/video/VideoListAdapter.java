/*****************************************************************************
 * VideoListAdapter.java
 *****************************************************************************
 * Copyright © 2011-2012 VLC authors and VideoLAN
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/

package org.videolan.vlc.gui.video;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import org.videolan.vlc.BR;
import org.videolan.vlc.MediaGroup;
import org.videolan.vlc.MediaWrapper;
import org.videolan.vlc.R;
import org.videolan.vlc.VLCApplication;
import org.videolan.vlc.gui.AsyncImageLoader;
import org.videolan.vlc.util.BitmapCache;
import org.videolan.vlc.util.BitmapUtil;
import org.videolan.vlc.util.Strings;

import java.util.Comparator;
import java.util.Locale;
import java.util.concurrent.Callable;

public class VideoListAdapter extends ArrayAdapter<MediaWrapper>
                                 implements Comparator<MediaWrapper> {

    public final static int SORT_BY_TITLE = 0;
    public final static int SORT_BY_LENGTH = 1;
    public final static int SORT_BY_DATE = 2;
    private int mSortDirection = 1;
    private int mSortBy = SORT_BY_TITLE;
    private boolean mListMode = false;
    private VideoGridFragment mFragment;

    public static final BitmapDrawable DEFAULT_COVER = new BitmapDrawable(VLCApplication.getAppResources(), BitmapCache.getFromResource(VLCApplication.getAppResources(), R.drawable.ic_cone_o));

    public VideoListAdapter(VideoGridFragment fragment) {
        super(fragment.getActivity(), 0);
        mFragment = fragment;
    }

    public final static String TAG = "VLC/MediaLibraryAdapter";

    public synchronized void update(MediaWrapper item) {
        int position = getPosition(item);
        if (position != -1) {
            remove(item);
            insert(item, position);
        }
    }

    public void setTimes(ArrayMap<String, Long> times) {
        boolean notify = false;
        // update times
        for (int i = 0; i < getCount(); ++i) {
            MediaWrapper media = getItem(i);
            Long time = times.get(media.getLocation());
            if (time != null) {
                media.setTime(time);
                notify = true;
            }
        }
        if (notify)
            notifyDataSetChanged();
    }

    public int sortDirection(int sortby) {
        if (sortby == mSortBy)
            return  mSortDirection;
        else
            return -1;
    }

    public void sortBy(int sortby) {
        switch (sortby) {
            case SORT_BY_TITLE:
                if (mSortBy == SORT_BY_TITLE)
                    mSortDirection *= -1;
                else {
                    mSortBy = SORT_BY_TITLE;
                    mSortDirection = 1;
                }
                break;
            case SORT_BY_LENGTH:
                if (mSortBy == SORT_BY_LENGTH)
                    mSortDirection *= -1;
                else {
                    mSortBy = SORT_BY_LENGTH;
                    mSortDirection *= 1;
                }
                break;
            case SORT_BY_DATE:
                if (mSortBy == SORT_BY_DATE)
                    mSortDirection *= -1;
                else {
                    mSortBy = SORT_BY_DATE;
                    mSortDirection *= 1;
                }
                break;
            default:
                mSortBy = SORT_BY_TITLE;
                mSortDirection = 1;
                break;
        }
        sort();
    }

    public void sort() {
        if (!isEmpty())
            try {
                super.sort(this);
            } catch (ArrayIndexOutOfBoundsException e) {} //Exception happening on Android 2.x
    }

    @Override
    public int compare(MediaWrapper item1, MediaWrapper item2) {
        int compare = 0;
        switch (mSortBy) {
            case SORT_BY_TITLE:
                compare = item1.getTitle().toUpperCase(Locale.ENGLISH).compareTo(
                        item2.getTitle().toUpperCase(Locale.ENGLISH));
                break;
            case SORT_BY_LENGTH:
                compare = ((Long) item1.getLength()).compareTo(item2.getLength());
                break;
            case SORT_BY_DATE:
                compare = ((Long) item1.getLastModified()).compareTo(item2.getLastModified());
                break;
        }
        return mSortDirection * compare;
    }

    /**
     * Display the view of a file browser item.
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View v = convertView;

        if (v == null || (((ViewHolder)v.getTag(R.layout.video_grid)).listmode != mListMode)) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            holder = new ViewHolder();
            holder.binding = DataBindingUtil.inflate(inflater, mListMode ? R.layout.video_list_card : R.layout.video_grid_card, parent, false);
            v = holder.binding.getRoot();
            holder.listmode = mListMode;
            v.setTag(R.layout.video_grid, holder);
        } else {
            holder = (ViewHolder) v.getTag(R.layout.video_grid);
        }

        if (position >= getCount() || position < 0)
            return v;

        holder.position = position;

        MediaWrapper media = getItem(position);

        AsyncImageLoader.LoadVideoCover(new VideoCoverFetcher(media), holder.binding, mFragment.getActivity());

        holder.binding.setVariable(BR.scaleType, ImageView.ScaleType.CENTER);
        holder.binding.setVariable(BR.cover, DEFAULT_COVER);

        if (media instanceof MediaGroup)
            fillGroupView(holder, media);
        else
            fillVideoView(holder, media);

        holder.binding.setVariable(BR.media, media);
        holder.binding.setVariable(BR.handler, mClickHandler);
        holder.binding.executePendingBindings();
        return v;
    }

    public ClickHandler mClickHandler = new ClickHandler();
    public class ClickHandler {
        public void onMoreClick(View v){
            if (mFragment != null)
                    mFragment.onContextPopupMenu(v, ((ViewHolder) ((CardView)v.getParent().getParent()).getTag(R.layout.video_grid)).position);
        }
    }

    private void fillGroupView(ViewHolder holder, MediaWrapper media) {
        holder.binding.setVariable(BR.group, true);
        MediaGroup mediaGroup = (MediaGroup) media;
        int size = mediaGroup.size();
        String text = getContext().getResources().getQuantityString(R.plurals.videos_quantity, size, size);
        holder.binding.setVariable(BR.resolution, text);
        holder.binding.setVariable(BR.time, "");
        holder.binding.setVariable(BR.max, 0);
        holder.binding.setVariable(BR.progress, 0);
    }

    private void fillVideoView(ViewHolder holder, MediaWrapper media) {
        /* Time / Duration */
        if (media.getLength() > 0) {
            long lastTime = media.getTime();
            String text;
            if (lastTime > 0) {
                text = String.format("%s / %s",
                        Strings.millisToText(lastTime),
                        Strings.millisToText(media.getLength()));
                holder.binding.setVariable(BR.max, (int) (media.getLength() / 1000));
                holder.binding.setVariable(BR.progress, (int) (lastTime / 1000));
            } else {
                text = Strings.millisToText(media.getLength());
            }

            holder.binding.setVariable(BR.time, text);
        }
        if (media.getWidth() > 0 && media.getHeight() > 0)
            holder.binding.setVariable(BR.resolution, String.format("%dx%d", media.getWidth(), media.getHeight()));
        else
            holder.binding.setVariable(BR.resolution, "");
        holder.binding.setVariable(BR.group, false);
    }

    static class ViewHolder {
        boolean listmode;
        int position;
        ViewDataBinding binding;
    }

    public void setListMode(boolean value) {
        mListMode = value;
    }

    public boolean isListMode() {
        return mListMode;
    }

    public static class VideoCoverFetcher implements Callable<Bitmap> {

        MediaWrapper media;

        VideoCoverFetcher(MediaWrapper media){
            this.media = media;
        }

        @Override
        public Bitmap call() throws Exception {
            return BitmapUtil.getPictureFromCache(media);
        }
    }
}
