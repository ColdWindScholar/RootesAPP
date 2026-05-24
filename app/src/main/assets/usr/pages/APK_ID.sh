#!/bin/bash

# 定义路径
temp_xml="/data/data/com.root.system/settings_ssaid.xml"
output_file="/data/system/users/0/settings_ssaid.xml"
input_file="/data/system/users/0/settings_ssaid.xml"

# 其他必要的变量
shell=". /data/data/com.root.system/files/usr/pages/APK_ID.sh \$package \$Content"

# 创建目录并备份
mkdir -p $GJZS/cpm
cp $input_file $GJZS/cpm

# 切换到备份目录
cd $GJZS/cpm

# 将 Android Binary XML 转换为人类可读的 XML
abx2xml settings_ssaid.xml "$temp_xml"

# 定义 XML 文件路径
xml_file="$temp_xml"

# 调试：提取包名和应用ID
yes=$(sed -n 's/.*package="\([^"]*\)".*defaultValue="\([^"]*\)".*/应用名=\1 \n应用ID=\2/p' "$xml_file")

# 输出 XML 模板
cat <<Han
<?xml version="1.0" encoding="utf-8"?>
<group title="温馨提示">

<group>
        <text>
            <title>应用ID列表</title>
            <desc>$yes</desc>
        </text>
        
    <action interruptible="false">
        <title>应用ID修改</title>
        <desc></desc>
        <set>
            mv $input_file $GJZS
            cd $GJZS
            abx2xml settings_ssaid.xml $temp_xml

            sed -i "/package=\"\$package_name\"/ {
                s/value=\"[^\"]*\"/value=\"\$new_value\"/
                s/defaultValue=\"[^\"]*\"/defaultValue=\"\$new_value\"/
            }" "$temp_xml"

            rm -rf "$output_file"

            cd /data/data/com.root.system/
            xml2abx settings_ssaid.xml $output_file

            rm "$temp_xml"

            echo "已将包名 \$package_name 的值更新为 \$new_value"
        </set>
        <param name="package_name" type="app"/>
        <param name="new_value" title="输入" required="true" />
    </action>
</group>

Han
