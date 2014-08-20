package lav.testredmadrobot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by LAV on 19.08.2014.
 */
public class ImageAdapter extends BaseAdapter{
    private Context mContext;
    private ArrayList<String> mImageURLs;
    private ArrayList<Boolean> mImageChose;

    public ImageAdapter(Context c){
        mContext = c;
        mImageURLs = new ArrayList<String>();
        mImageChose = new ArrayList<Boolean>();
    }

    public void add(String url){
        mImageURLs.add(url);
        mImageChose.add(false);
    }

    public boolean getItemChosed(int position){
        return mImageChose.get(position);
    }

    public void setItemChosed(int position, boolean chose) {
        mImageChose.set(position, chose);
    }

    public int getCountChoiseItem() {
        int count = 0;
        for(int i = 0; i < mImageChose.size(); i++){
            if (mImageChose.get(i)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public int getCount() {
        return mImageURLs.size();
    }

    @Override
    public Object getItem(int position) {
        return mImageURLs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View gridItem = null;
        if (convertView == null) {
            gridItem = inflater.inflate(R.layout.grid_item, null);
            gridItem.findViewById(R.id.imSelect).setVisibility(View.INVISIBLE);
        }
        else {
            gridItem = convertView;
        }

        gridItem.findViewById(R.id.imSelect).setVisibility(mImageChose.get(position) ? View.VISIBLE : View.INVISIBLE);
        Picasso.with(mContext).load(mImageURLs.get(position)).placeholder(R.drawable.empty_photo).into((SquareImage)gridItem.findViewById(R.id.imPhoto));

        return gridItem;
    }
}
