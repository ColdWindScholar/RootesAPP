echo "数据无价！！！"
echo "正在提取..."
echo "过程中可能会手机过卡，是正常的，或者点右上角后台运行"
echo "原作者：酷安 @Rannki"
echo "二次修改者：酷安 @Quarters"
echo "三次修改者 玩机百宝箱 嘉明"
. $core
#文件位置初始化
dir_name="$GJZS/字库备份"
if [ ! -d "$dir_name" ]; then
    mkdir -p "$dir_name"
else
    rm -rf "$dir_name/*"
fi
if [ ! -d "$dir_name/images" ]; then
    mkdir "$dir_name/images"
fi
echo "" >> "$dir_name/fastboot.sh"
chmod +x "$dir_name/fastboot.sh"
# 过滤分区列表
exclude_partitions=(
"userdata"
"mmcblk0"
"sda"
"backup"
"sdb"
"sdc"
"sdd"
"sde"
"sdf"
"sdg"
"system"
"vendor"
"super"
)
fastboot_cmd=""
# 获取分区信息
all_partitions=$(ls /dev/block/by-name/)
for partition in ${all_partitions}
do
    rs="No" 
    for exclude_partition in ${exclude_partitions[@]}
    do
        if [[ "$exclude_partition" == "$partition" ]]; then
            rs="Yes"
            break
        fi
    done
    if [[ "Yes" == "$rs" ]]; then
        continue
    fi
    echo "> 备份【$partition】分区"
    dd if="/dev/block/by-name/$partition" of="$dir_name/images/$partition.img"
    echo ""
    
    fastboot_cmd="${fastboot_cmd}fastboot flash $partition ./images/$partition.img\n"
done
# 输出fastboot命令至文件
echo "$fastboot_cmd" > "$dir_name/fastboot.bat"
fastboot_cmd="# !/usr/bin/env bash\n# encoding: utf-8.0\n\n$fastboot_cmd"
echo "$fastboot_cmd" > "$dir_name/fastboot.sh"
# 信息提示
echo "-------------------------------------------------------"
echo "分区备份完毕，所有备份的分区镜像，在$dir_name目录下..."
echo "fastboot.bat/fastboot.sh为线刷脚本，手机进入fastboot模式后，可在PC端执行线刷"
echo "fastboot线刷，需要adb工具，请自行百度下载adb工具"
exit 0