package com.uniplugin.wx_transfer;

import android.app.Activity;
import android.content.Context;

import com.tencent.mm.opensdk.constants.Build;
import com.tencent.mm.opensdk.modelbiz.WXOpenBusinessView;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;

public class WXTransferModule extends UniModule {

    @UniJSMethod(uiThread = true)
    public void requestMerchantTransfer(String appId, String mchId, String packageInfo, String openId, UniJSCallback callback) {
        if (mUniSDKInstance == null || !(mUniSDKInstance.getContext() instanceof Activity)) {
            if (callback != null) {
                callback.invoke("{\"errCode\":-1,\"errMsg\":\"context error\"}");
            }
            return;
        }

        Context context = mUniSDKInstance.getContext();
        
        try {
            IWXAPI api = WXAPIFactory.createWXAPI(context, appId, false);
            
            // 检查微信是否安装
            if (!api.isWXAppInstalled()) {
                if (callback != null) {
                    callback.invoke("{\"errCode\":-3,\"errMsg\":\"未安装微信\"}");
                }
                return;
            }
            
            // 检查微信版本
            int wxSdkVersion = api.getWXAppSupportAPI();
            if (wxSdkVersion < Build.OPEN_BUSINESS_VIEW_SDK_INT) {
                if (callback != null) {
                    callback.invoke("{\"errCode\":-4,\"errMsg\":\"微信版本过低，请升级微信\"}");
                }
                return;
            }
            
            // 构建请求
            WXOpenBusinessView.Req req = new WXOpenBusinessView.Req();
            req.businessType = "requestMerchantTransfer";
            
            // 构建 query 参数
            StringBuilder query = new StringBuilder();
            query.append("mchId=").append(URLEncoder.encode(mchId, "UTF-8"));
            query.append("&package=").append(URLEncoder.encode(packageInfo, "UTF-8"));
            
            if (appId != null && !appId.isEmpty()) {
                query.append("&appId=").append(URLEncoder.encode(appId, "UTF-8"));
            }
            if (openId != null && !openId.isEmpty()) {
                query.append("&openId=").append(URLEncoder.encode(openId, "UTF-8"));
            }
            
            req.query = query.toString();
            
            // 发送请求
            boolean ret = api.sendReq(req);
            
            if (ret) {
                if (callback != null) {
                    callback.invoke("{\"errCode\":0,\"errMsg\":\"ok\"}");
                }
            } else {
                if (callback != null) {
                    callback.invoke("{\"errCode\":-5,\"errMsg\":\"发送请求失败\"}");
                }
            }
            
        } catch (UnsupportedEncodingException e) {
            if (callback != null) {
                callback.invoke("{\"errCode\":-99,\"errMsg\":\"编码错误: " + e.getMessage() + "\"}");
            }
        } catch (Exception e) {
            if (callback != null) {
                callback.invoke("{\"errCode\":-99,\"errMsg\":\"调用失败: " + e.getMessage() + "\"}");
            }
        }
    }
}
