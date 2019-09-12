function UploadFile() {
    this.filesizeTitle = "";
    this.speed = 0;
    this.remainTime = 0;
    this.uploadItem = undefined;
    this.fileObj = undefined;
    this.existSize = 0;
    this.setsize = 500 * 1024;
    this.sendSize;
    this.lastSendSize;
    this.timeout = 20000;

    //上传进度
    this.clearPosition = function() {
        if (Tools.isNull(this.uploadItem))
            return;
        this.uploadItem.setPosition(0, "0%, 剩余时间：无", this.fileObj.name + ", 大小:0K, 完成0%, 速度:0k/s");
    }

    this.setPosition = function(count) {
        if (Tools.isNull(this.uploadItem))
            return;
        if (count < 100)
            this.uploadItem.setPosition(count, count + "%, 剩余时间：" + this.remainTime, "大小:" + this.filesizeTitle + ", 完成" + count + "%, 速度:" + this.speed +
                ",剩余时间：" + this.remainTime);
        else {
            this.uploadItem.setUploadEnd(this.filesizeTitle);
        }
    }

    this.getServerPos = function() {
        var size = -1;
        Tools.ajaxSimpleFrameSubmit("./Services/UploadCommand.php", false, "getSize", { filename: this.fileObj.name }, function(data) {
            var info = mini.decode(data)
            if (info.ret)
                size = info["size"];
        });
        return size;
    }

    this.removeServerFile = function() {
        var isok = false;
        Tools.ajaxSimpleFrameSubmit("./Services/UploadCommand.php", false, "removeFile", { filename: this.fileObj.name }, function(data) {
            var info = mini.decode(data)
            isok = info.ret;
        });
        return isok;
    }

    this.command;
    this.data;
    this.upload = function(uploadItem, fileObj, command, data) {
        this.uploadItem = uploadItem;
        this.command = command;
        this.data = data;
        this.fileObj = fileObj;
        if (this.fileObj.size >= 1024) {
            if (this.fileObj.size >= 1024 * 1024) {
                if (this.fileObj.size > 1024 * 1024 * 1024)
                    this.filesizeTitle = (this.fileObj.size / 1024 / 1024 / 1024).toFixed(2) + "G";
                else
                    this.filesizeTitle = (this.fileObj.size / 1024 / 1024).toFixed(2) + "M";
            } else
                this.filesizeTitle = (this.fileObj.size / 1024).toFixed(2) + "K";

        } else
            this.filesizeTitle = this.fileObj.size + "byte";

        var pos = this.getServerPos();
        this.existSize = 0;
        if (pos <= 0) {
            this.clearPosition();
        } else {
            this.existSize = pos;
            if (fileObj.size == pos) {
                this.uploadItem.setUploadEnd(this.filesizeTitle);
                return;
            }

            if (fileObj.size < pos) {
                this.removeServerFile();
                this.existSize = 0;
            }
        }
        this.start();
    }

    this.stop = false;
    this.needDelFile = false;
    this.remove = function() {
        this.stop = true;
        this.needDelFile = true;
        this.removeServerFile();
    }

    this.start = function() {
        if (this.stop) {
            return false;
        }

        this.lastSendSize = 0;

        //新建一个FormData对象
        var formData = new FormData(); //++++++++++
        //追加文件数据

        var startTime;
        var sendSize = 0;

        this.sendSize = Math.min(this.fileObj.size - this.existSize, this.setsize);
        var blobfile = this.fileObj.slice(this.existSize, this.existSize + this.sendSize);
        var datastr = Tools.isNull(this.data) ? "{}" : JSON.stringify(this.data);
        formData.append('file', blobfile);
        formData.append('userdata', datastr);
        formData.append('command', Tools.isNull(this.command) ? "" : this.command);
        formData.append('filesize', this.fileObj.size);
        formData.append('filename', this.fileObj.name); //++++++++++

        var xhr = new XMLHttpRequest();
        //post方式
        xhr.open('POST', './Services/Upload.php');
        xhr.options = { "uploader": this };
        xhr.upload.options = { "uploader": this };
        //第二步骤
        xhr.upload.onprogress = function(evt) {
            if (Tools.isNull(evt.srcElement.options))
                return;
            var control = evt.srcElement.options["uploader"];
            var nt = new Date().getTime(); //获取当前时间
            var pertime = (nt - startTime) / 1000; //计算出上次调用该方法时到现在的时间差，单位为s
            startTime = new Date().getTime(); //重新赋值时间，用于下次计算

            var perload = evt.loaded - control.lastSendSize; //计算该分段上传的文件大小，单位b       
            control.lastSendSize = evt.loaded; //重新赋值已上传文件大小，用以下次计算

            //上传速度计算
            var speed = perload / pertime; //单位b/s
            var bspeed = speed;
            var units = 'b/s'; //单位名称
            if (speed / 1024 > 1) {
                speed = speed / 1024;
                units = 'k/s';
            }

            if (speed / 1024 > 1) {
                speed = speed / 1024;
                units = 'M/s';
            }

            speed = speed.toFixed(1);
            //剩余时间
            var resttime = ((control.fileObj.size - control.existSize - evt.loaded) / bspeed).toFixed(1);
            if (resttime / 60 > 1) {
                if (resttime / 60 / 60 > 1) {
                    if (resttime / 60 / 60 / 24 > 1)
                        resttime = "大于1天";
                    else
                        resttime = Math.ceil(resttime / 60 / 60) + "小时";
                } else
                    resttime = Math.ceil(resttime / 60) + "分钟";
            } else {
                resttime = resttime + "秒";
            }
            control.speed = speed + units;
            control.remainTime = resttime;
            control.setPosition(Math.ceil((control.existSize + evt.loaded) / control.fileObj.size * 100));
        };
        //【上传进度调用方法实现】
        xhr.upload.onloadstart = function() { //上传开始执行方法
            startTime = new Date().getTime(); //设置上传开始时间
            sendSize = 0; //设置上传开始时，以上传的文件大小为0
        };
        xhr.onreadystatechange = function(evt) { //第四步
            var control = evt.srcElement.options["uploader"];
            var isok = true;　　　
            if (xhr.readyState == 4) {
                if (xhr.status == 200) {
                    var rText = xhr.responseText;
                    rText = rText.replace(/\r/g, "").replace(/\n/g, "");
                    var result = JSON.parse(rText);
                    isok = result.ret;
                    if (isok) {
                        control.existSize += control.sendSize;
                        if (control.existSize < control.fileObj.size) {
                            if (!control.stop)
                                control.start();
                        } else
                            control.uploadItem.setUploadEnd(control.filesizeTitle);　　　
                    }
                }　　　　　　

                if (control.needDelFile) {
                    control.removeServerFile();
                }

            }
            if (!isok)
                alert("上传文件：" + control.fileObj.name + "失败，请检查网络！");

        };
        xhr.timeout = this.timeout;
        xhr.ontimeout = function(event) {　　　　
            alert('请求超时，网络拥堵！低于25K/s');　　
        }

        //发送请求
        xhr.send(formData); //第三步骤
        //设置超时时间

        return true;
    }

}