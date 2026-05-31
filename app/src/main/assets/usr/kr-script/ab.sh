#!/bin/bash

export SLOT=""
export fenqu=""
check_ab_device=$(. $ShellScript/Block_Device_Name.sh | egrep -q 'boot_a|boot_b'
    return $?)
# 检查是否为 A/B 分区设备
if $check_ab_device; then
    # 从 /proc/cmdline 中获取分区槽位信息
    SLOT=$(cat /proc/cmdline | tr '[:space:]' '\n' | sed -rn 's/androidboot.slot_suffix=//p')
    
    if [[ -n "$SLOT" ]]; then
        echo "- 当前使用的分区系统：$SLOT。"
        case $SLOT in
            _a)
                slot=1
                fenqu="b"
            ;;
            _b)
                slot=0
                fenqu="a"
            ;;
            *)
                abort "！未知的分区槽位：$SLOT"
            ;;
        esac
    else
        # 如果未能从 /proc/cmdline 获取到分区槽位，使用 bootctl 获取
        qu=$($binariesPath/libbootctl.so get-current-slot 2>&1)
        echo -n "- 当前使用的分区系统："
        case $qu in
            0)
                SLOT="_a"
                echo "$SLOT"
                slot=1
                fenqu="b"
            ;;
            1)
                SLOT="_b"
                echo "$SLOT"
                slot=0
                fenqu="a"
            ;;
            *)
                abort "！未知错误：$qu"
            ;;
        esac
    fi
else
    echo "设备不支持 A/B 分区。"
fi
