package com.example.deviceid;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.bun.miitmdid.core.ErrorCode;
import com.bun.miitmdid.core.IIdentifierListener;
import com.bun.miitmdid.core.MdidSdkHelper;
import com.bun.miitmdid.supplier.IdSupplier;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

/**
 * @description: 获取设备标识工具类
 * @author: zhukai
 * @date: 2019/12/16 16:17
 */
public class DeviceIdUtils {

    /**
     * 获取IMEI码
     *
     * @param context
     * @return
     */
    @SuppressLint("MissingPermission")
    public static String getIMEI(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return tm.getImei();
        } else {
            return tm.getDeviceId();
        }
    }

    /**
     * 获取设备序列号
     *
     * @return
     */
    @SuppressLint("MissingPermission")
    public static String getSerial() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Build.getSerial();
        } else {
            return Build.SERIAL;
        }
    }

    /**
     * 获取Mac地址
     */
    public static String getMacAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) {
                    continue;
                }
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }
                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }
                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取ANDROID_ID
     *
     * @param context
     * @return
     */
    public static String getAndroidId(Context context) {
        return Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * 获取补充设备标识
     *
     * @param context
     * @param listener
     */
    public static void getSupplierDeviceId(Context context, final OnSupplierDeviceIdListener listener) {
        int result = MdidSdkHelper.InitSdk(context, true, new IIdentifierListener() {
            @Override
            public void OnSupport(boolean isSupport, IdSupplier idSupplier) {
                if (isSupport) {
                    listener.onSuccess(idSupplier);
                }
                // 释放连接
                idSupplier.shutDown();
            }
        });
        switch (result) {
            case ErrorCode.INIT_ERROR_MANUFACTURER_NOSUPPORT:
                // 不支持的设备厂商
                listener.onFailed("不支持的设备厂商");
                break;
            case ErrorCode.INIT_ERROR_DEVICE_NOSUPPORT:
                listener.onFailed("不支持的设备");
                break;
            case ErrorCode.INIT_ERROR_LOAD_CONFIGFILE:
                listener.onFailed("加载配置文件出错");
                break;
            case ErrorCode.INIT_ERROR_RESULT_DELAY:
                // 获取接口是异步的，结果会在回调中返回，回调执行的回调可能在工作线程
                break;
            case ErrorCode.INIT_HELPER_CALL_ERROR:
                listener.onFailed("反射调用出错");
                break;
            default:
                break;
        }
    }

    /**
     * 获取设备标识
     *
     * @param context
     * @param listener
     */
    public static void getDeviceId(final Context context, final OnDeviceIdListener listener) {
        MdidSdkHelper.InitSdk(context, true, new IIdentifierListener() {
            @Override
            public void OnSupport(boolean isSupport, IdSupplier idSupplier) {
                String deviceId;
                if (isSupport) {
                    // 支持获取补充设备标识
                    deviceId = idSupplier.getOAID();
                } else {
                    // 不支持获取补充设备标识
                    // 可以自己决定设备标识获取方案，这里直接使用了ANDROID_ID
                    deviceId = getAndroidId(context);
                }
                // 将设备标识MD5加密后返回，以获取统一格式
                listener.onSuccess(MD5Utils.digest(deviceId));
                // 释放连接
                idSupplier.shutDown();
            }
        });
    }

    /**
     * 获取补充设备标识回调
     */
    public interface OnSupplierDeviceIdListener {
        /**
         * 获取补充设备标识成功
         *
         * @param idSupplier
         */
        void onSuccess(IdSupplier idSupplier);

        /**
         * 获取补充设备标识失败
         *
         * @param message 失败原因
         */
        void onFailed(String message);
    }

    /**
     * 获取设备标识回调
     */
    public interface OnDeviceIdListener {
        /**
         * 获取设备标识成功
         */
        void onSuccess(String deviceId);
    }
}
