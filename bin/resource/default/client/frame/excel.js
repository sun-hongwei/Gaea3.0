/*
FileReader共有4种读取方法：
1.readAsArrayBuffer(file)：将文件读取为ArrayBuffer。
2.readAsBinaryString(file)：将文件读取为二进制字符串
3.readAsDataURL(file)：将文件读取为Data URL
4.readAsText(file, [encoding])：将文件读取为文本，encoding缺省值为'UTF-8'
导入excel
@param readBinary = false; //是否将文件读取为二进制字符串
@param sheetIndex 导入那个sheet，如果为undefined则导入全部
*/
var ExcelTools = {
    importExcel: function (f, callback, sheetIndex, readBinary) { //导入
        var reader = new FileReader();
        reader.onload = function (e) {
            var data = e.target.result;
            if (readBinary) {
                wb = XLSX.read(btoa(ExcelTools.fixdata(data)), { //手动转化
                    type: 'base64'
                });
            } else {
                wb = XLSX.read(data, {
                    type: 'binary'
                });
            }

            if (Tools.isNull(sheetIndex))
                sheetIndex = 0;
            //wb.SheetNames[0]是获取Sheets中第一个Sheet的名字
            //wb.Sheets[Sheet名]获取第一个Sheet的数据
            var result = [];
            if (sheetIndex == -1) {
                var count = wb.SheetNames.length;
                for (var index = 0; index < count; index++) {
                    var element = wb.SheetNames[index];
                    result.push(XLSX.utils.sheet_to_json(wb.Sheets[wb.SheetNames[index]]));
                }
            } else {
                result.push(XLSX.utils.sheet_to_json(wb.Sheets[wb.SheetNames[sheetIndex]]));
            }

            callback(result);
        };

        if (!Tools.isNull(readBinary) && readBinary) {
            reader.readAsArrayBuffer(f);
        } else {
            reader.readAsBinaryString(f);
        }
    },

    /**
     * 导出Excel
     */
    exportExcel: function (json, type) {
        var tmpdata = json[0];
        json.unshift({});
        var keyMap = []; //获取keys
        //keyMap =Object.keys(json[0]);
        for (var k in tmpdata) {
            keyMap.push(k);
            json[0][k] = k;
        }
        var tmpdata = []; //用来保存转换好的json 
        json.map((v, i) => keyMap.map((k, j) => Object.assign({}, {
            v: v[k],
            position: (j > 25 ? ExcelTools.getCharCol(j) : String.fromCharCode(65 + j)) + (i + 1)
        }))).reduce((prev, next) => prev.concat(next)).forEach((v, i) => tmpdata[v.position] = {
            v: v.v
        });
        var outputPos = Object.keys(tmpdata); //设置区域,比如表格从A1到D10
        var tmpWB = {
            SheetNames: ['mySheet'], //保存的表标题
            Sheets: {
                'mySheet': Object.assign({},
                    tmpdata, //内容
                    {
                        '!ref': outputPos[0] + ':' + outputPos[outputPos.length - 1] //设置填充区域
                    })
            }
        };
        //导出的二进制对象
        var exportObj = new Blob([ExcelTools.s2ab(XLSX.write(tmpWB, {
                bookType: (type == undefined ? 'xlsx' : type),
                bookSST: false,
                type: 'binary'
            } //这里的数据是用来定义导出的格式类型
        ))], {
            type: ""
        }); //创建二进制对象写入转换好的字节流

        var href = URL.createObjectURL(exportObj); //创建对象超链接

        return href;
    },

    fixdata: function (data) { //文件流转BinaryString
        var o = "",
            l = 0,
            w = 10240;
        for (; l < data.byteLength / w; ++l) o += String.fromCharCode.apply(null, new Uint8Array(data.slice(l * w, l * w + w)));
        o += String.fromCharCode.apply(null, new Uint8Array(data.slice(l * w)));
        return o;
    },

    s2ab: function (s) { //字符串转字符流
        var buf = new ArrayBuffer(s.length);
        var view = new Uint8Array(buf);
        for (var i = 0; i != s.length; ++i) view[i] = s.charCodeAt(i) & 0xFF;
        return buf;
    },

    // 将指定的自然数转换为26进制表示。映射关系：[0-25] -> [A-Z]。
    getCharCol: function (n) {
        var temCol = '',
            s = '',
            m = 0
        while (n > 0) {
            m = n % 26 + 1
            s = String.fromCharCode(m + 64) + s
            n = (n - m) / 26
        }
        return s
    },
}