#本脚本由　by Han | 情非得已c，编写
#应用于玩机百宝箱上


abort() {
    echo "$@" 1>&2
    sleep 3
    exit 1
}



Installing_curl() {
    cp -rf $PeiZhi_File/curl/$Type/* $PREFIX
    cp -rf $PeiZhi_File/curl/etc $PREFIX
    find $PREFIX -exec chmod 700 {} \; -exec chown $APP_USER_ID:$APP_USER_ID {} \; >/dev/null
    [[ -f $PREFIX/lib/libssl.so ]] && ln -sf $PREFIX/lib/libssl.so $PREFIX/lib/libssl.so.3 >/dev/null
    [[ -f $PREFIX/lib/libcrypto.so ]] && ln -sf $PREFIX/lib/libcrypto.so $PREFIX/lib/libcrypto.so.3 >/dev/null
    $ELF1_Path/curl -V &>/dev/null && echo "$1" >$JCe || abort "！curl安装失败"
}

export ABI=`getprop ro.product.cpu.abi`
[[ -z "$ABI" ]] && export ABI=`getprop ro.product.cpu.abi2`


    case "$ABI" in
        arm64*) Type=arm64-v8a;;
        armeabi*) Type=armeabi;;
        x86_64*) Type=x86_64;;
        x86*) Type=x86;;
        *) echo "！ 未知的架构 ${ABI}，目前不会交叉别的平台cur，如果你会编译可以教我一下造福大家l"; exit 1;;
    esac

echo "- 当前架构：${ABI}"
echo "- 当前版本：${Version_Name}（$Version_code）"
    JCe=$Data_Dir/curl_Installed.log
    [[ -f $JCe ]] && JCe2=`cat $JCe`
        if [[ -z "$JCe2" || ! -f $PREFIX/ELF/curl ]]; then
            echo "- 开始安装curl"
            Installing_curl 1
        elif [[ "$JCe2" -lt "$2" ]]; then
            echo "- 开始更新curl"
            Installing_curl 2
        fi
            echo "- 测试curl是否可用"
            echo
            $ELF1_Path/curl -sL https://www.baidu.com
            Result=$?
            echo
            if [[ $Result -eq 0 ]]; then
                echo "- 初始化成功，请在玩机百宝箱1-7群里下载安装玩机百宝箱"
                echo "- 直接安装玩机百宝箱就可以了不要卸载当前版本否则数据会被清掉"
            elif [[ $Result -eq 6 ]]; then
                abort "！未连接到互联网"
            else
                abort "！错误代码：$Result"
            fi
