package com.example.deviceid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bun.miitmdid.supplier.IdSupplier;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvDeviceId;

    // 是否允许了权限
    private boolean isPermissionAllowed;

    private Context mContext;
    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        if (XXPermissions.isHasPermission(this, Permission.READ_PHONE_STATE)) {
            isPermissionAllowed = true;
        } else {
            XXPermissions.with(this)
                    .permission(Permission.READ_PHONE_STATE)
                    .request(new OnPermission() {
                        @Override
                        public void hasPermission(List<String> granted, boolean isAll) {
                            isPermissionAllowed = true;
                        }

                        @Override
                        public void noPermission(List<String> denied, boolean quick) {
                            isPermissionAllowed = false;
                            Toast.makeText(MainActivity.this, "您禁止了该权限，无法获取设备相关标识", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        tvDeviceId = findViewById(R.id.tv_device_id);
        Button btnGetImei = findViewById(R.id.btn_get_imei);
        Button btnGetSerial = findViewById(R.id.btn_get_serial);
        Button btnGetMacAddress = findViewById(R.id.btn_get_mac_address);
        Button btnGetAndroidId = findViewById(R.id.btn_get_android_id);
        Button btnGetSupplierDeviceId = findViewById(R.id.btn_get_supplier_device_id);
        Button btnGetDeviceId = findViewById(R.id.btn_get_device_id);

        btnGetImei.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPermissionAllowed) {
                    try {
                        String imei = DeviceIdUtils.getIMEI(mContext);
                        tvDeviceId.setText("IMEI：" + imei);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                        Toast.makeText(mContext, "获取IMEI失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        btnGetSerial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPermissionAllowed) {
                    try {
                        String serial = DeviceIdUtils.getSerial();
                        tvDeviceId.setText("设备序列号：" + serial);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                        Toast.makeText(mContext, "获取设备序列号失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        btnGetMacAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String macAddress = DeviceIdUtils.getMacAddress();
                tvDeviceId.setText("Mac地址：" + macAddress);
            }
        });
        btnGetAndroidId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String androidId = DeviceIdUtils.getAndroidId(mContext);
                tvDeviceId.setText("ANDROID_ID：" + androidId);
            }
        });
        btnGetSupplierDeviceId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeviceIdUtils.getSupplierDeviceId(mContext, new DeviceIdUtils.OnSupplierDeviceIdListener() {
                    @Override
                    public void onSuccess(final IdSupplier idSupplier) {
                        // 这里的回调可能不是在主线程中的，如果要更新UI需要切回到主线程
                        // 个人测试结果：真机上支持获取补充设备标识，回调执行在工作线程；Android Studio创建的模拟器不支持获取补充设备标识，回调执行在主线程
                        // 因此这里保险起见还是切回主线程
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder sb = new StringBuilder();
                                sb.append("OAID：").append(idSupplier.getOAID()).append("\n");
                                sb.append("VAID：").append(idSupplier.getVAID()).append("\n");
                                sb.append("AAID：").append(idSupplier.getAAID()).append("\n");
                                tvDeviceId.setText(sb.toString());
                            }
                        });
                    }

                    @Override
                    public void onFailed(String message) {
                        Log.e(TAG, "获取补充设备标识失败，失败原因为：" + message);
                    }
                });
            }
        });
        btnGetDeviceId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeviceIdUtils.getDeviceId(mContext, new DeviceIdUtils.OnDeviceIdListener() {
                    @Override
                    public void onSuccess(final String deviceId) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvDeviceId.setText("通用设备标识：" + deviceId);
                            }
                        });
                    }
                });
            }
        });
    }
}
