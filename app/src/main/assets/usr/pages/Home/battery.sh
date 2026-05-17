ba=$(. ./Battery_information.sh)

cat <<Han
<?xml version="1.0" encoding="utf-8"?>
<resource dir="file:///android_asset/Configuration_File" />
<resource dir="file:///android_asset/samples" />
<resource dir="file:///android_asset/usr/pages/display" />

<group>
<text>
<desc>$ba</desc>
</text>
</group>

<group title="温馨提示">
    <text>
        <desc>功能介绍：电池功能修改或者查看参数</desc>
    </text>
</group>
<group title="功能">

<page title="充电统计" activity="am start com.root.system/com.root.system.activities.ActivityCharge"/>

<page title="充电控制" activity="am start com.root.system/com.root.system.activities.ActivityChargeController"/>

<page title="耗电统计" activity="am start com.root.system/com.root.system.activities.ActivityPowerUtilization"/>
</group>

Han