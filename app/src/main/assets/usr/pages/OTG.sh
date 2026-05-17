if ! $Have_ROOT; then
    exit 1
fi
if [ "$ABI" != "arm64-v8a" ] && [ "$ABI" != "arm-v7a" ]; then
cat <<Han
<?xml version="1.0" encoding="utf-8"?>
<group>
    <text>
        <slices>
            <slice size="18" color="#FFFF0000">未找到支持您设备$ABI的adb和fastboot命令，而且我也不会移植</slice>
        </slices>
    </text>
</group>
Han
exit 1
fi


cat <<Han
<?xml version="1.0" encoding="utf-8"?>

<group>

    
    <text>
        <desc>功能介绍：这是一个手机给手机刷机的，也可以使用手机给手机传输文件，或者给手表安装或者传输文件</desc>
    </text>
    
         <page title="使用说明" config-sh="cat /data/user/$ANDROID_UID/$Package_name/files/usr/pages/Help/OTG.xml" />
</group>

<group title="连接与断开">
    <action>
        <title>网络ADB调试</title>
        <desc>请确保在同一WiFi局域网内，且对方设备已开启/支持网络ADB服务</desc>
        <params>
            <param name="port" label="输入端口" value="5555" required="true" value-sh="grep_prop port \$Data_Dir/Connect_Network_adb.log"/>
            <param name="fs" label="选择方式" options-sh="printf 'l|连接\nd|断开\nr|重新连接'" required="true"value-sh="grep_prop fs \$Data_Dir/Connect_Network_adb.log"/>
            <param name="ip" title="输入要连接的设备ip地址"  required="true" value-sh="grep_prop ip \$Data_Dir/Connect_Network_adb.log"/>
        </params>
        <set>. ./ADB/Connect_Network_adb.sh</set>
    </action>
    <action confirm="true" auto-off="true" interruptible="false">
        <title>断开所有adb服务，网络ADB连接</title>
        <set>{ adb disconnect; stop adbd; adb kill-server; rm -rf &#34;$Data_Dir/Connect_Network_adb2.log&#34;; } &#38;&#62;/dev/null</set>
    </action>
    <action confirm="true" auto-off="true" interruptible="false">
        <title>修复adb 5037端口被占用</title>
        <set>p=\`pgrep -f 'tcp:5037'\`; if [[ -n &#34;\$p&#34; ]]; then kill -1 &#34;\$p&#34;; else echo "- 5037端口未占用"; fi; sleep 3</set>
    </action>
    <text>
        <slices>
            <slice run="adb start-server; echo 已启用ADB服务" size="15" color="#FF0366D6" u="1">启用ADB服务</slice>
            <slice>　　</slice>
            <slice run="adb kill-server; echo 已关闭ADB服务" size="15" color="#FF0366D6" u="1">关闭ADB服务</slice>
            <slice>　　</slice>
            <slice run="adb -reset; echo 已重启ADB服务" size="15" color="#FF0366D6" u="1">重启ADB服务</slice>
        </slices>
    </text>
</group>

<group title="功能区">
    
    <pages>
        <page title="检查设备是否连接" summary="设备未连接本页所有功能均无法使用" config-sh="cat \$Pages/ADB/Check_device.xml">
            <menu type="refresh">刷新界面</menu>
            <menu type="finish">关闭页面</menu>
        </page>
        <page title="系统/网络ADB模式功能区" config-sh="cat \$Pages/ADB/System_Pattern.xml" />
        <page title="recovery/sideload模式功能区" config-sh="cat \$Pages/ADB/Recovery.xml" />
        <page title="FASTBOOT模式功能区" desc="数据无价，请备份数据/基带" config-sh="cat \$Pages/ADB/FASTBOOT.xml" />
    </pages>
    <action interruptible="false">
        <title>查看adb &#38; fastboot进程</title>
        <set>. ./ADB/View_adb_fastboot-PID.sh</set>
    </action>
</group>


<group>
        <text>
            <title>在寻找其他功能吗？</title>
            <desc/>
        </text>        
        
        
        <page title="OTG高级重启" config-sh="cat \$Pages/ADB/ADB_Advanced_Restart.xml" />
        
                  <action>
        <title>Shell终端</title>
        <desc>用于执行Shell终端</desc>
        <params>
            <param name="CMD" title="输入命令点击确认即可，多行命令用键盘上的回车换行" desc="可通过which 命令、去查找命令是否存在" required="true" value-sh="cat $ShellScript/Shell2.sh" />
        </params>
        <set>. ./Shell.sh</set>
    </action>
    <action>
        <title>Shell脚本</title>
        <desc>执行Shell脚本.sh</desc>
        <params>
            <param name="File" type="file" suffix="sh" editable="true" title="可输入.sh文件绝对路径，也可以使用文件选择器选择文件" desc="温馨提示：可用「MT管理器」长按目录或文件 --&gt;点属性 --&gt;点击目录即可复制目录绝对路径，长按目录或长按名称即可复制文件绝对路径" required="true"/>
        </params>
        <set>cd /; . &#34;\$File&#34;</set>
    </action>
    </group>
Han
