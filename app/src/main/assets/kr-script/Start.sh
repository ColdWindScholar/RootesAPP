
if [ -e "$GJZS/cache" ]; then
    rm -rf $GJZS/cache
fi

    mkdir -p /data/data/$Package_name/files/usr/busybox
    busybox --install -s /data/data/$Package_name/files/usr/busybox
    chmod 777 /data/data/$Package_name/boot.sh
    mkdir -p $GJZS 
   
    if [ -f "$GJZS/.start" ]; then
    mv /data/data/$Package_name/files/kr-script/cache $GJZS/
fi


if [ -e "/data/data/com.root.system/.kr" ]; then
    rm -rf $TMPDIR/*
    rm -rf $PeiZhi_File/*
    rm -rf $HOME/kr-script/cache/*
    echo "初始化完成"
else
    
echo "初始化完成"
fi
