package com.mashen.signaldetection;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mashen.signaldetection.model.ItemInfo;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DiaryActivity extends AppCompatActivity {

    private final int deleteMenuItemId = 100;

    private ListView lvItems;

    //列表的数据
    private List<ItemInfo> itemInfos;

    //列表数据文件名与索引相关联的map,Map<String filePath,ItemInfo currentItemInfo>
    private Map<String,ItemInfo> filePath_currentItemInfoMap;

    //已保存的历史截屏的文件夹路径
    private String dirNameToSaveImg = "/sdcard/SignalDetectionPrtSc";

    //自定义适配器
    private CustomAdapter customAdapter;

    private final String TAG = getContext().getClass().getName();

    //历史截屏记录页面的"next"箭头图标id
    private final int nextId = R.mipmap.next;

    //历史截屏记录页面的文件列表
    String[] imgArray;
    //构建List<String>，方便删除图片文件（免除删除String数组其中元素的麻烦）
    List<String> imgList;

    //记录当前文件是否勾选删除，Map<String fileName,Boolean isCheck>;
    Map<String,Boolean> currentFileIsCheckedMap;

    //记录已勾选待删除的图片
    List<String> selectedImgList;

    //截屏、查看截屏的Intent
    private Intent intent;

    private ImageView ivEmptyDirectory;

    //图片加载，引用第三方库
    private ImageLoader imageLoader;
    private ImageLoaderConfiguration configuration;
    private DisplayImageOptions options;

    private final int REQUEST_READ_EXTERNAL_STORAGE = 200;
    private final int REQUEST_WRITE_EXTERNAL_STORAGE = 201;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(R.layout.custom_actionbar_title);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setHomeAsUpIndicator(R.mipmap.back);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        lvItems = (ListView) findViewById(R.id.lvHistoricalPrtSc);
        ivEmptyDirectory = (ImageView) findViewById(R.id.ivEmptyDirectory);

        imgList = new ArrayList<>();

        selectedImgList = new ArrayList<>();
        filePath_currentItemInfoMap = new HashMap<>();

        configuration = ImageLoaderConfiguration.createDefault(getContext());
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(configuration);
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();

        if(Build.VERSION.SDK_INT >= 23){
            int checkReadExternalStoragePermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
            int checkWriteExternalStoragePermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (checkReadExternalStoragePermission != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_READ_EXTERNAL_STORAGE);
                return;
            }
            if (checkWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_WRITE_EXTERNAL_STORAGE);
                return;
            }
            itemInfos = getData();
        }else {
            itemInfos = getData();
        }
        if (itemInfos == null) return;

        customAdapter = new CustomAdapter(itemInfos);
        lvItems.setAdapter(customAdapter);
        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String imgFilePath = dirNameToSaveImg + "/" + imgList.get(position);
                openPrtSc(imgFilePath);
            }
        });
    }

    private List<ItemInfo> getData() {
        //初始打开app需要创建一个文件夹：SignalDetectionPrtSc,用于存放已保存的历史截屏图片
        List<ItemInfo> itemInformations = new ArrayList<>();

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dirToSaveImg = new File(dirNameToSaveImg);
            if (!dirToSaveImg.exists()) {
                dirToSaveImg.mkdir();
            }
            int length = dirToSaveImg.list().length;
            //文件夹不存在，则显示指定“文件为空”的图片
            if (length == 0) {
                lvItems.setVisibility(View.GONE);
                ivEmptyDirectory.setVisibility(View.VISIBLE);
                return null;
            }

            //将图片文件降序排序
            imgArray = dirToSaveImg.list();
            File [] filesArray = new File[imgArray.length];
            Map<Long,String> lastModifiedTimeToDescMap = new HashMap<>();
            for (int i = 0;i < imgArray.length;i++){
                filesArray[i] = new File(dirToSaveImg + "/" + imgArray[i]);
                lastModifiedTimeToDescMap.put(filesArray[i].lastModified(),imgArray[i]);
            }
            List<Long> lastModifiedTimeList = new ArrayList<>();
            Set<Long> lastModifiedTimeSet1= lastModifiedTimeToDescMap.keySet();
            Iterator iter = lastModifiedTimeSet1.iterator();
            while (iter.hasNext()){
                lastModifiedTimeList.add((Long)iter.next());
            }

            //lastModifiedTimeList降序
            Comparator desc = Collections.reverseOrder();
            Collections.sort(lastModifiedTimeList,desc);
            imgArray = new String[lastModifiedTimeList.size()];
            for (int i = 0;i < lastModifiedTimeList.size();i++){
                imgArray[i] = lastModifiedTimeToDescMap.get(lastModifiedTimeList.get(i));
            }

            for (int i = 0;i < imgArray.length;i++)
                imgList.add(imgArray[i]);

            //根据降序的图片文件名，将之关联其所涉及的图片、最后修改时间、checkbox状态、next图标
            List<Bitmap> bitmaps = new ArrayList<>();
            ItemInfo iteminfo;
            File currentFile;
            String path = dirToSaveImg + "/";

            for (int i = 0; i < imgList.size(); i++) {
                bitmaps.add(i, imageLoader.loadImageSync("file://" + path + imgList.get(i),options));
                currentFile = new File(path + imgList.get(i));
                iteminfo = new ItemInfo(false, bitmaps.get(i), imgList.get(i),currentFile.lastModified(), nextId);
                itemInformations.add(iteminfo);
            }

            if (itemInformations != null)
                return itemInformations;
        } else {
            Toast.makeText(getContext(), "未检测到SDCard！", Toast.LENGTH_SHORT).show();
            return null;
        }
        return null;
    }

    private final class ViewHolder {
        private CheckBox cbIsSelected;
        private ImageView ivImg;
        private TextView tvImgName;
        private TextView tvPrtScTime;
        private ImageView ivNext;
    }

    private class CustomAdapter extends BaseAdapter {
        private List<ItemInfo> listItemInfo;
        private HashMap<Integer, View> map = new HashMap<>();
        private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        public CustomAdapter(List<ItemInfo> list) {
            listItemInfo = new ArrayList<>();
            listItemInfo = list;
            //声明map保存CheckBox的选中状态
            currentFileIsCheckedMap = new HashMap<>();
        }

        @Override
        public int getCount() {
            return listItemInfo.size();
        }

        @Override
        public Object getItem(int position) {
            return null != listItemInfo ? listItemInfo.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        //通过声明View，表示每一行数据都是一个view
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view;
            ViewHolder holder = null;
            //待查看图片的路径
            final String currentImgFilePath = dirNameToSaveImg + "/" + imgList.get(position);

            if (map.get(position) == null) {
                LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = mInflater.inflate(R.layout.custom_listview_item, null);
                holder = new ViewHolder();
                holder.cbIsSelected = (CheckBox) view.findViewById(R.id.list_cbSelected);
                holder.ivImg = (ImageView) view.findViewById(R.id.list_ivImg);
                holder.tvImgName = (TextView) view.findViewById(R.id.list_tvImgName);
                holder.tvPrtScTime = (TextView) view.findViewById(R.id.list_tvPrtScTime);
                holder.ivNext = (ImageView) view.findViewById(R.id.list_ivNext);
                map.put(position, view);

                view.setTag(holder);
            } else {
                view = map.get(position);
                holder = (ViewHolder) view.getTag();
            }
            holder.cbIsSelected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    //保存当前CheckBox的选中状态
                    currentFileIsCheckedMap.put(currentImgFilePath, cb.isChecked());
                    if (cb.isChecked()) {
                        cb.setFocusable(true);
                        selectedImgList.add(currentImgFilePath);
                        //待删除文件名及其ItemInfo
                        filePath_currentItemInfoMap.put(currentImgFilePath,listItemInfo.get(position));
                    }
                    else {
                        cb.setFocusable(true);
                        selectedImgList.remove(currentImgFilePath);
                        filePath_currentItemInfoMap.remove(currentImgFilePath);
                    }
                }
            });
            holder.ivNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ImageView)v).setFocusable(true);
                    openPrtSc(currentImgFilePath);
                }
            });

            holder.cbIsSelected.setChecked(currentFileIsCheckedMap.get(currentImgFilePath) != null ? currentFileIsCheckedMap.get(currentImgFilePath) : false);
            holder.ivImg.setImageBitmap(listItemInfo.get(position).getImgBitmap());
            holder.tvImgName.setText(listItemInfo.get(position).getImgName());
            holder.tvPrtScTime.setText(sdf.format(
                    new Date(
                            listItemInfo.get(position).getPrtScTime()
                    )
            ).toString());
            holder.ivNext.setImageResource(listItemInfo.get(position).getImgNextId());
            return view;
        }
    }

    private void openPrtSc(String filePath) {
        intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(filePath)), "image/*");
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem deleteMenuItem = menu.add(0, deleteMenuItemId, 0, "删除");
        deleteMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        deleteMenuItem.setIcon(R.mipmap.trash);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case deleteMenuItemId:
                deleteImgFile();
                break;

            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteImgFile(){
        File fileToDelete;
        boolean isAllDeleted = false;
        try {
            for (int i = 0; i < selectedImgList.size(); i++) {
                fileToDelete = new File(selectedImgList.get(i));

                if (fileToDelete.delete()) {
                    itemInfos.remove(filePath_currentItemInfoMap.get(selectedImgList.get(i)));
                    imgList.remove(selectedImgList.get(i).substring(selectedImgList.get(i).lastIndexOf("/")+1));
                    //通知更新数据源
                    customAdapter.notifyDataSetChanged();
                    currentFileIsCheckedMap.remove(selectedImgList.get(i));
                    isAllDeleted = true;
                }

            }
            if (isAllDeleted) {
                filePath_currentItemInfoMap.clear();
                currentFileIsCheckedMap.clear();
                selectedImgList.clear();
                Toast.makeText(getContext(), "文件已删除！", Toast.LENGTH_SHORT).show();
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    itemInfos = getData();
                }
                else {
                    Toast.makeText(getContext(),"无法访问SD卡！",Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    itemInfos = getData();
                }
                else {
                    Toast.makeText(getContext(),"无法获取修改SD卡内容权限！",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private Context getContext() {
        return this;
    }
}
