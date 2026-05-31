#!/bin/bash

android=$(uname -r)
cd $GJZS

$binariesPath/libmagiskboot.so unpack "${img:="$img2"}"

$binariesPath/libksud.so boot-patch \
  -b "$img" \
  -k $GJZS/kernel \
  -o "$GJZS/KernelSU_$android.img" \
  --magiskboot $ELF3_Path/magiskboot \
  --kmi "$android" \
  -m "$img2"

echo "完成！输出路径：KernelSU_$android.img"
