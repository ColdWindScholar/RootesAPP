#本脚本由　by Han | 情非得已c，编写
#应用于玩机百宝箱上

wifi=$(pm list packages -U | grep "$package_name" | awk -F'uid:' '{print $2}')
iptables -I OUTPUT -t filter -m owner -w --uid-owner "$wifi" -j REJECT

echo "iptables -I OUTPUT -t filter -m owner --uid-owner "$wifi" -j REJECT ">>/data/data/com.root.system/boot.sh
      chmod 777 /data/data/com.root.system/boot.sh >>/dev/null 2>&1
  echo "$package_name" >>$Data_Dir/wifi.log
    
    [[ -n "$package_name" ]] && echo "已隐藏应用的记录已写入到数据目录，清除「玩机百宝箱」全部数据会导致记录丢失哦⊙∀⊙！"
echo "如果发生错误，请杀死软件并关闭开机启动"