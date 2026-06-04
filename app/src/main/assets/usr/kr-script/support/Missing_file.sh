#本脚本由　by Han | 情非得已c，编写
#应用于玩机百宝箱上


if [[ -n $(which magisk) ]]; then
    s=$(find /data/adb/magisk/ -type f | wc -l)
    if [[ $s -lt 10 ]]; then
        echo 1
        return 1
    else
        echo 0
        return 0
    fi
fi
