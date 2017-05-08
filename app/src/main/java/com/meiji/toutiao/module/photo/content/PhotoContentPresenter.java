package com.meiji.toutiao.module.photo.content;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.meiji.toutiao.InitApp;
import com.meiji.toutiao.bean.photo.PhotoArticleBean;
import com.meiji.toutiao.bean.photo.PhotoGalleryBean;

import java.util.List;

/**
 * Created by Meiji on 2017/2/16.
 */

class PhotoContentPresenter implements IPhotoContent.Presenter {

    private static final String TAG = "PhotoContentPresenter";
    private static final int SAVE_IMAGE_SUCCESS = 0;
    private static final int SAVE_IMAGE_FAIL = 1;
    private static final int HTTP_REQUEST_SUCCESS = 2;
    private static final int HTTP_REQUEST_FAIL = 3;
    private IPhotoContent.View view;
    private IPhotoContent.Model model;
    private PhotoGalleryBean bean;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if (message.what == HTTP_REQUEST_SUCCESS) {
                doSetImageBrowser();
            }
            if (message.what == SAVE_IMAGE_SUCCESS) {
                view.onSaveImageSuccess();
            }
            if (message.what == SAVE_IMAGE_FAIL || message.what == HTTP_REQUEST_FAIL) {
                view.onShowNetError();
            }
            return false;
        }
    });
    private int position;
    private String group_id;
    private String item_id;


    PhotoContentPresenter(IPhotoContent.View view) {
        this.view = view;
        this.model = new PhotoContentModel();
    }

    @Override
    public void doSetImageBrowser() {
        bean = model.getData();
        view.onSetImageBrowser(bean, 0);
    }

    @Override
    public void doRefresh() {

    }

    @Override
    public void doShowNetError() {

    }

    @Override
    public void doRequestData(PhotoArticleBean.DataBean dataBean) {
        group_id = dataBean.getGroup_id() + "";
        item_id = dataBean.getItem_id() + "";

        final String url = "http://www.toutiao.com/a" + dataBean.getGroup_id();
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean result = model.getRequestData(url);
                if (result) {
                    Message message = handler.obtainMessage(HTTP_REQUEST_SUCCESS);
                    message.sendToTarget();
                } else {
                    Message message = handler.obtainMessage(HTTP_REQUEST_FAIL);
                    message.sendToTarget();
                }
            }
        }).start();
    }

    @Override
    public int doGetImageCount() {
        return bean.getCount();
    }

    @Override
    public void doSetPosition(int position) {
        this.position = position;
    }

    @Override
    public void doSaveImage() {
        List<PhotoGalleryBean.SubImagesBean> sub_images = bean.getSub_images();
        final String url = sub_images.get(position).getUrl();
        Log.d(TAG, "doSaveImage: " + url);
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean result = model.SaveImage(url, InitApp.AppContext);
                if (result) {
                    Message message = handler.obtainMessage(SAVE_IMAGE_SUCCESS);
                    message.sendToTarget();
                } else {
                    Message message = handler.obtainMessage(SAVE_IMAGE_FAIL);
                    message.sendToTarget();
                }
            }
        }).start();
    }
}
