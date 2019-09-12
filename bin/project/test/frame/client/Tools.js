/**
 * @author:zhangcheng
 * Modify the time: 2019-4-9
 */
(function () {
    if (typeof window.CustomEvent === "function") return false;

    function CustomEvent(event, params) {
        params = params || {
            bubbles: false,
            cancelable: false,
            detail: undefined
        };
        var evt = document.createEvent('CustomEvent');
        evt.initCustomEvent(event, params.bubbles, params.cancelable, params.detail);
        return evt;
    }

    CustomEvent.prototype = window.Event.prototype;
    window.CustomEvent = CustomEvent;
})();

(function () {
    function ajaxEventTrigger(event) {
        var ajaxEvent = new CustomEvent(event, {
            detail: this
        });
        window.dispatchEvent(ajaxEvent);
    }
    var oldXHR = window.XMLHttpRequest;

    var maskDialog = new FrameDialog();

    this.loading = function () {
        // maskDialog.mask();
    }

    this.loaded = function () {
        // maskDialog.unMask();
    }

    var xhrObj = this;

    function newXHR() {
        var realXHR = new oldXHR();
        realXHR.addEventListener('abort', function () {
            xhrObj.loading();
            ajaxEventTrigger.call(this, 'ajaxAbort');
        }, false);
        realXHR.addEventListener('error', function () {
            alert("请求失败！")
            xhrObj.loaded();
            ajaxEventTrigger.call(this, 'ajaxError');
        }, false);
        realXHR.addEventListener('load', function () {
            xhrObj.loading();
            ajaxEventTrigger.call(this, 'ajaxLoad');
        }, false);
        realXHR.addEventListener('loadstart', function () {
            ajaxEventTrigger.call(this, 'ajaxLoadStart');
        }, false);
        realXHR.addEventListener('progress', function () {
            ajaxEventTrigger.call(this, 'ajaxProgress');
        }, false);
        realXHR.addEventListener('timeout', function () {
            alert("请求时间超时！")
            xhrObj.loaded();
            ajaxEventTrigger.call(this, 'ajaxTimeout');
        }, false);
        realXHR.addEventListener('loadend', function () {
            xhrObj.loaded();
            ajaxEventTrigger.call(this, 'ajaxLoadEnd');
        }, false);
        realXHR.addEventListener('readystatechange', function () {
            ajaxEventTrigger.call(this, 'ajaxReadyStateChange');
        }, false);
        return realXHR;
    }
    window.XMLHttpRequest = newXHR;
})();

var Tools = {
    timeout: 10000,
    reties: 10,
    globalhintid: "globalmsghint-010102",
    hintTime: undefined,
    requestCrypted: false,
    responseCrypted: false,

    tomcatUri: "",

    //获取屏幕DPI
    getScreenDpi: function () {
        var arrDPI = new Array();
        if (window.screen.deviceXDPI != undefined) {
            arrDPI[0] = window.screen.deviceXDPI;
            arrDPI[1] = window.screen.deviceYDPI;
        } else {
            var tmpNode = document.createElement("DIV");
            tmpNode.style.cssText = "width:1in;height:1in;position:absolute;left:0px;top:0px;z-index:99;visibility:hidden";
            document.body.appendChild(tmpNode);
            arrDPI[0] = parseInt(tmpNode.offsetWidth);
            arrDPI[1] = parseInt(tmpNode.offsetHeight);
            tmpNode.parentNode.removeChild(tmpNode);
        }
        return arrDPI;

    },

    getDpiScaleFromUIInfo : function(uiInfo){
        return Tools.getDpiScale(uiInfo.page.deviceDPI);
    },

    getDpiScale : function(deviceDpi){
        var screenDpis = Tools.getScreenDpi();
        var screenDpi = screenDpis[0] > screenDpis[1] ? screenDpis[0] : screenDpis[1];
 
        return 1.00 * screenDpi / deviceDpi;
    },

    /**
     * 导出json表示的数据集到excel
     * @param {jsonarray} rows 数据行列表 [{field:"值"}]
     * @param {jsonarray} cols 字段列表，格式：[{name:"字段显示名称", field:"字段名称"}]
     * @param {string} name 导出的文件名称，不包括扩展名
     * @param {string} sheetname 导出的excel的sheet名称
     * @param {jsonobject} booleanConvert 转换bool类型数据，必须为{"true":"真值名称", "false":"不为真值的名称"}，可以为空，如为空不转换
     * @param {*} convertString 转换字符串类型的数据到真实类型，true：转换，其他不转换
     * @param {*} datetimeFormat 转换/检查字符串类型的数据到时间日期的格式，仅convertString=true有效
     * @param {*} dateFormat 转换/检查字符串类型的数据到日期的格式，仅convertString=true有效
     * @param {*} timeFormat 转换/检查字符串类型的数据到时间的格式，仅convertString=true有效
     */
    exportToExcel: function (rows, cols, name, sheetname, booleanConvert, convertString = true,
        datetimeFormat = "yyyy-MM-dd HH:mm:ss", dateFormat = "yyyy-MM-dd", timeFormat = "HH:mm:ss") {
        var info = {};
        info.rows = rows;
        info.cols = cols;
        info.name = name;
        info.sheetName = sheetname;
        info.booleanConvert = booleanConvert;
        info.convertString = convertString;
        info.datetimeFormat = datetimeFormat;
        info.dateFormat = dateFormat;
        info.timeFormat = timeFormat;

        Tools.simpleTomcatSubmit("/jsonarray/service/excel_export", {
            info: JSON.stringify(info)
        }, function (data) {
            if (data.ret == 0) {
                var url = Tools.getTomcatUri() + data.data;
                Tools.openSaveFileSelector(info.name, url);
            } else {
                alert("导出报表失败！");
            }
        });
    },

    S4: function () {
        return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
    },

    /**
     * 创建guid并返回
     * @return {string} 返回生成的唯一guid字符串，格式{0000-0000-0000-0000}
     */
    guid: function () {
        return (this.S4() + this.S4() + "-" + this.S4() + "-" + this.S4() + "-" +
            this.S4() + "-" + this.S4() + this.S4() + this.S4());
    },

    /**
     * 移动页面上的元素
     * @param {Array} controls 要移动的控件元素列表，元素必须是通过frame创建的，一般通过getFrameControlByName函数返回
     * @param {int} hspace 要移动的横向距离
     * @param {int} vspace 要移动的纵向距离
     * @param {int} vspace x轴的参考坐标，一般为controls中所有元素构成的最大矩形的left值
     * @param {int} vspace y轴的参考坐标，一般为controls中所有元素构成的最大矩形的top值
     * @param {int} maxWidth x轴的最大宽度，一般为controls中所有元素构成的最大矩形的width值
     * @param {int} maxHeight y轴的最大高度，一般为controls中所有元素构成的最大矩形的height值
     */
    moveElements: function (controls, hspace, vspace, x, y, maxWidth, maxHeight) {
        controls.forEach(function (control) {
            var controlDiv = control.options["div"];
            var pos = $(controlDiv).offset();
            if (vspace > 0) {
                if (control.options["userdata"].data.height != "100%") {
                    pos.top = Tools.moveElementPos(pos.top, vspace, y);
                }

            }
            if (hspace > 0) {
                if (control.options["userdata"].data.width != "100%") {
                    pos.left = Tools.moveElementPos(pos.left, hspace, x);
                }
            }
            $(controlDiv).offset(pos);

        }, this);

    },

    /**
     * 计算移动页面元素的left或top值
     * @param {int} value 要移动的left或者top值，像素单位 
     * @param {int} move 移动的距离，像素单位
     * @param {*} refer 移动参照的位置值，与value要对应，如value为left值，则此值为x轴的参照值
     */
    moveElementPos: function (value, move, refer) {
        return move + value - refer;
    },

    /**
     * 获取spring的服务基地址，并设置全局地址属性
     * @return {string} 服务地址，格式如下：http://localhost:80/jspserver
     */
    getTomcatUri: function () {
        if (Tools.isNull(Tools.tomcatUri)) {
            Tools.tomcatUri = Tools.getRemoteTomcatUri();
        }
        return Tools.tomcatUri;
    },

    /**
     * 获取control在parent的相对位置
     * @param {element} control 子控件
     * @param {element} parent 符控件
     * @return {json} 位置信息，格式如下：{left:0, top:0}
     */
    getParentPosition: function (control, parent) {
        var pos = $(control).position()
        var curparent = $(control).parent()[0];
        if (Tools.isNull(curparent) || curparent == parent) {
            return pos;
        } else {
            var parentPos = Tools.getParentPosition(curparent, parent);
            return {
                left: parentPos.left,
                top: parentPos.top
            };
        }
    },

    /**
     * 在div上显示提示信息
     * @param {element} div 要显示提示信息的div
     * @param {string} text 要显示的提示文本
     * @param {int} timeout 提示的存在事件，毫秒，超过时间隐藏
     */
    showHintForDiv: function (div, text, timeout) {
        if (Tools.isNull(div))
            return;

        var pos = $(div).offset();
        pos.top += $(div).height() + 5;
        Tools.showHint(pos.left, pos.top, text, timeout);
    },

    /**
     * 在指定位置显示提示信息
     * @param {int} x body的绝对坐标x
     * @param {int} y body的绝对坐标y
     * @param {string} text 要显示的提示文本
     * @param {int} timeout 提示的存在事件，毫秒，超过时间隐藏
     */
    showHint: function (x, y, text, timeout) {
        if (Tools.isNull(timeout))
            timeout = 5000;

        Tools.hideHint();
        var hint = $("#" + Tools.globalhintid)[0];

        if (Tools.isNull(hint)) {
            divStr = "<div id='" + Tools.globalhintid + "' style='position:absolute;background-color: #000;color: #fff;" +
                "filter: alpha(opacity=80);opacity: 0.8;z-index: 9999;border-radius: 3px;font-size: 12px;display: none;'>";
            divStr += "</div>";
            var dw = new DocumentWriter();
            dw.write({}, divStr);
        }
        hint = $("#" + Tools.globalhintid)[0];
        var selecter = $(hint);
        selecter.show();
        selecter.text(text);
        selecter.offset({
            left: x,
            top: y
        });
        Tools.hintTime = window.setTimeout(function () {
            Tools.hideHint();
        }, timeout);
    },

    /**
     * 隐藏提示
     */
    hideHint: function () {
        if (!Tools.isNull(Tools.hintTime)) {
            window.clearTimeout(Tools.hintTime);
            Tools.hintTime = undefined;
        }
        var hint = $("#" + Tools.globalhintid);
        if (!Tools.isNull(hint))
            hint.hide();
    },

    /**
     * 根据funcName指定的函数名返回函数对象，如果此函数对象并没有在当前环境加载
     * @param {string} funcName 函数名称
     * @return {function} 函数对象，未找到返回undefined
     */
    getFunction: function (funcName) {
        try {
            var fn = eval(funcName);
            if (typeof (fn) == "function") {
                return fn;
            }
        } catch (e) {}
        return undefined;
    },

    /**
     * 根据函数名动态调用函数
     * @param {string} fnName 函数名称
     * @param {array} args 函数的参数列表
     * @return {object} fnName指定的函数返回的对象
     */
    dynamicCallFunctionByName: function (fnName, args) {
        var fn = Tools.getFunction(fnName);
        if (fn == undefined) {
            return undefined;
        }

        return Tools.dynamicCallFunction(fn, args);
    },

    /**
     * 根据字符串动态调用函数
     * @param {string} fnString 函数的字符串定义  
     * @param {array} args 调用参数
     */
    dynamicCallFunctionByString: function (fnString, args) {
        try {
            var fn = eval(fnString);
            if (typeof (fn) != "function") {
                return undefined;
            }
        } catch (e) {
            return undefined;
        }

        return Tools.dynamicCallFunction(this, args);
    },

    /**
     * 动态调用函数
     * @param {function} fn 函数对象
     * @param {array} args 调用参数
     * @return {any} 函数的返回值
     */
    dynamicCallFunction: function (fn, args) {
        return fn.apply(this, args);
    },

    /**
     * 是否未数组
     * @param {object} o 检查对象
     */
    isArray: function (o) {
        return Object.prototype.toString.call(o) == '[object Array]';
    },

    /**
     * 动态调用函数
     * @deprecated 向后兼容
     * @param {function} fn 函数对象
     * @param {array} args 调用参数
     * @return {any} 函数的返回值
     */
    callFunction: function (fn, args) {
        fn.apply(this, args);
    },

    /**
     * 获取文件路径的文件名称
     * @param {string} filepath 文件全路径
     */
    getFileName: function (filepath) {
        var pos = filepath.lastIndexOf("\\");
        return filepath.substring(pos + 1);
    },

    scrollbarWidth: 0,

    /**
     * 获取任意滚动条的宽度
     */
    getScrollbarWidth: function () {
        if (Tools.scrollbarWidth != 0)
            return Tools.scrollbarWidth;
        var oP = document.createElement('p'),
            styles = {
                width: '100px',
                height: '100px',
                overflowY: 'scroll',
            },
            i;

        for (i in styles) {
            oP.style[i] = styles[i];
        }
        document.body.appendChild(oP);
        Tools.scrollbarWidth = oP.offsetWidth - oP.clientWidth;
        $(oP).remove();

        return Tools.scrollbarWidth;
    },

    /**
     * 获取文件路径的扩展名，不包括[.]
     * @param {string} filename 文件全路径
     */
    getFileExt: function (filename) {
        var pos = filename.lastIndexOf(".");
        return filename.substring(pos + 1);
    },

    /**
     * 打印一组div，与printDiv的区别：如果打印的div是报表，则会处理chart图，而printDiv不处理chart图
     * @param {UIInfo} uiInfo 要打印的div所在的界面的描述文件
     * @param {array} divNames 要打印的div(也就是frame控件的最外层的div，一般通过getFrameControlByName)的name的列表
     * @param {element} headElements 自定义打印使用的兼容规则，样式等，默认为兼容ie最高级别或chrome，不符合w3c标准
     *          如果你的report或者div包括样式引用，非style="width:800px;"方式的，并且是html内定义，
     *          那么你需要通过这个属性定义style来引用样式，例如：
     *              <style type="text/css">
     *                  .content { font-size: 36px; color: #ff0000; }
     *              </style>
     *              然后你就可以在控件内引用，例如：<div class="content"/>
     * @param {string} mode 打印预览框的显示模式，可以为：popup弹出框，iframe内嵌方式，默认iframe
     * @param {bool} close 仅popup模式有效，为true表示打印完毕关闭窗口，其他不关闭
     * @param {string} extraCss 引用的外部css样式定义，多个用[,]分隔，例如：
     *          './css/style.css,./css/common.css'
     * @param {array} keepAttr 保持的属性列表，由于jquery的打印实际上是动态创建div，这里保持的属性就是会应用到
     *          创建的div的属性，默认["id","class","style"]
     */
    printFrameDiv: function (uiInfo, divNames, paging, scale, headElements, mode, close, extraCss, keepAttr) {
        var reports = [];
        var divs = [];
        for (var index = 0; index < divNames.length; index++) {
            var name = divNames[index];
            var div = getFrameControlByName(uiInfo, name);
            if (!Tools.isNull(div.report)) {
                var report = div.report;
                report.switchToImage();
                reports.push(report);
            }

            var control = getFrameControl(div);
            if (!Tools.isNull(control) && control instanceof DivControl) {
                control.switchToImage();
                divs.push(control);
            }
        }

        var printDivs = [];
        for (var index = 0; index < divNames.length; index++) {
            var element = divNames[index];
            printDivs.push(getFrameControlByName(uiInfo, element));
        }

        Tools.printDiv(printDivs, paging, scale, headElements, mode, close, extraCss, keepAttr);

        for (var index = 0; index < reports.length; index++) {
            var report = reports[index];
            report.switchImageToControl();
        }

        for (var index = 0; index < divs.length; index++) {
            var div = divs[index];
            div.switchToDiv();
        }
    },

    overflowElement: function (element, olds, overflow) {
        var old = {
            o: $(element).css("overflow"),
            x: $(element).css("overflowX"),
            y: $(element).css("overflowY"),
            e: element,
        };
        olds.push(old);
        $(element).css("overflow", overflow);
        $(element).css("overflowX", overflow);
        $(element).css("overflowY", overflow);
    },

    overflowElements: function (elements, olds, overflow) {
        if (Tools.isNull(elements) || elements.length == 0)
            return;

        for (var index = 0; index < elements.length; index++) {
            var element = elements[index];
            Tools.overflowElement(element, olds, overflow);
            var childrens = $(element).children();
            Tools.overflowElements(childrens, olds, overflow);
        }
    },

    /**
     * 打印一组div；会首先弹出打印预览框，然后手动打印
     * 
     * @param {UIInfo} uiInfo 要打印的div所在的界面的描述文件
     * @param {array} divs 要打印的div列表
     * @param {element} headElements 自定义打印使用的兼容规则，样式等，默认为兼容ie最高级别或chrome，不符合w3c标准
     *          如果你的report或者div包括样式引用，非style="width:800px;"方式的，并且是html内定义，
     *          那么你需要通过这个属性定义style来引用样式，例如：
     *              <style type="text/css">
     *                  .content { font-size: 36px; color: #ff0000; }
     *              </style>
     *              然后你就可以在控件内引用，例如：<div class="content"/>
     * @param {string} mode 打印预览框的显示模式，可以为：popup弹出框，iframe内嵌方式，默认iframe
     * @param {bool} close 仅popup模式有效，为true表示打印完毕关闭窗口，其他不关闭
     * @param {string} extraCss 引用的外部css样式定义，多个用[,]分隔，例如：
     *          './css/style.css,./css/common.css'
     * @param {array} keepAttr 保持的属性列表，由于jquery的打印实际上是动态创建div，这里保持的属性就是会应用到
     *          创建的div的属性，默认["id","class","style"]
     
    */
    printDiv: function (divs, paging, scale, headElements, mode, close, extraCss, keepAttr) {
        if (Tools.isNull(mode)) {
            mode = "iframe";
        }

        if (Tools.isNull(close))
            close = true;

        if (Tools.isNull(keepAttr)) {
            keepAttr = ["id", "class", "style"];

        }

        if (Tools.isNull(extraCss))
            extraCss = "";

        if (Tools.isNull(headElements)) {
            headElements = '<meta charset="utf-8" />,<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>' +
                "<style media=print>" +
                /* 应用这个样式的在打印时隐藏 */
                ".noPrint {	display: none; }" +

                /* 应用这个样式的，从那个标签结束开始另算一页，之后在遇到再起一页，以此类推 */
                ".page {	page-break-after: always;}" +
                "</style>";

        }

        var options = {
            mode: mode,
            popClose: close,
            extraCss: extraCss,
            retainAttr: keepAttr,
            extraHead: headElements,
            popWd: document.body.clientWidth,
            popHt: document.body.clientHeight,
            scale:scale,
        };

        var olds = [];
        for (var index = 0; index < divs.length; index++) {
            var div = divs[index];
            olds.push({
                left: $(div).css("left"),
                top: $(div).css("top"),
                position: $(div).css("position"),
            });


            if (!Tools.isNull(paging) && paging) {
                $(div).css({
                    "pageBreakAfter": "always",
                    "position": "relative"
                });
            }
            $(div).css("left", "0px");
            $(div).css("top", "0px");
        }

        // $(divs).print({
        //     //Use Global styles
        //     globalStyles : false,
        //     //Add link with attrbute media=print
        //     mediaPrint : false,
        //     //Custom stylesheet
        //     stylesheet : "",
        //     //Print in a hidden iframe
        //     iframe : true,
        //     //Don't print this
        //     noPrintSelector : "",
        //     //Add this at top
        //     prepend : "",
        //     //Add this on bottom
        //     append : "",
        // });

        $(divs).printArea(options);

        for (var index = 0; index < divs.length; index++) {
            var div = divs[index];
            $(div).css("left", olds[index].left);
            $(div).css("top", olds[index].top);
            $(div).css("position", olds[index].position);
            $(div).css("page-break-after", "");
        }

    },

    /**
     * 将value转换为px方式的尺寸
     * @param {int} size 参考尺寸，用于value为百分比（%）情况 
     * @param {string} value 要转换的值，可以为100px，100，100% 
     * @return {string} 以px为单位的尺寸
     */
    convetPX: function (size, value) {
        return Tools.convetTextPX(size, value, 0);
    },

    /**
     * 将value转换为px方式的尺寸，并返回用value+addValue
     * @param {int} size 参考尺寸，用于value为百分比（%）情况 
     * @param {string} value 要转换的值，可以为100px，100，100% 
     * @param {int} addValue 要累加的值，可以为复数
     * @return {string} 以px为单位的累加后的值
     */
    convetTextPX: function (size, value, addValue) {
        return Tools.convetPXToValue(size, value, addValue) + "px";
    },

    /**
     * 将value转换为rem方式的尺寸，并返回用value+addValue
     * @param {int} size 参考尺寸，用于value为百分比（%）情况 
     * @param {string} value 要转换的值，可以为100px，100，100% 
     * @param {int} addValue 要累加的值，可以为复数
     * @return {string} 以rem为单位的累加后的值
     */
    convetTextRem: function (size, value, addValue) {
        return (Tools.convetPXToValue(size, value, addValue) * 0.625 / 10) + "rem";
    },

    /**
     * 清除字符两端及内部的空格
     * @param {string} value 要操作的字符串
     */
    trim: function (value) {
        if (Tools.isNull(value))
            return "";
        try {
            return value.replace(/^\s+|\s+$/gm, '');
        } catch (e) {
            return value;
        }
    },

    /**
     * 获取表示尺寸的字符串的最后一个字符
     * @param {string} value 尺寸字符串
     * @return {string} 返回类型码，可为[0-9,'x','%']
     */
    getSizeType: function (value) {
        var bz = "";
        try {
            if (Tools.isNull(value) || value.length == 0)
                return "";
            value = Tools.trim(value);
            bz = value.substring(value.length - 1, value.length);
        } catch (e) {
            bz = "";
        }
        return bz;
    },

    /**
     * 是否为百分数
     * @param {string} value 尺寸字符
     */
    isPercentageValue: function (value) {
        return Tools.getSizeType(value) == "%";
    },

    /**
     * 将px格式的尺寸字符转换为px方式的数值表示
     * @param {int} size 参考尺寸，用于value为百分比（%）情况 
     * @param {string} value 要转换的值，可以为100px，100，100% 
     * @param {int} addValue 要累加的值，可以为复数
     * @param {bool} ignorePercentage 是否忽略百分数，true忽略，其他不忽略 
     * @return {int} 如果value表示的值为百分数，并且ignorePercentage=true，那么返回0，其他情况返回value对应的数值
     */
    convetPXToValue: function (size, value, addValue, ignorePercentage) {
        if (Tools.isNull(addValue))
            addValue = 0;

        if (Tools.isNull(ignorePercentage))
            ignorePercentage = false;

        if (Tools.isNull(value))
            return "0";

        value = Tools.trim(value);
        var bz = Tools.getSizeType(value);

        var r = 0;
        if (bz == "%") {
            if (ignorePercentage)
                return 0;
            r = Number(value.substring(0, value.length - 1));
            r = r / 100.00 * size;
        } else if (bz != "x") {
            r = Number(value) + addValue;
        } else {
            r = Number(value.substring(0, value.length - 2));
            r = r + addValue;
        }
        return r;
    },

    /**
     * 将颜色字符串转换为css格式字符串
     * @param {string} value 表示颜色的字符串，可为：0x000000（java）,#000000（css）
     * @return {string} value为空或长度小于2，返回"#000000"，如果为#开始直接返回，如果为[0x]开始则将[0x]替换为
     * [#]后返回
     */
    convetColor: function (value) {
        if (Tools.isNull(value))
            return "#000000";
        var bz = "";
        try {
            bz = value.substring(0, 2);
        } catch (e) {
            bz = "";
        }
        if (bz == "0x")
            return "#" + value.substring(4);
        else if (bz[0] == "#") {
            return value;
        }
        return "#000000";
    },

    /**
     * 将value转换为对应px形式字符
     * @param {string} value px格式的尺寸字符串
     * @return {string} 如果value包括px那么直接返回，否则在value结尾处添加px返回，如果value为空，返回[12px]
     */
    convetFontSize: function (value) {
        if (Tools.isNull(value))
            return "12px";
        var bz = "";
        try {
            bz = value.substring(value.length - 2, 2);
        } catch (e) {
            bz = "";
        }
        if (bz == "px")
            return value;
        else {
            return value + "px";
        }
    },

    /**
     * 获取一个对象的属性数量
     * @param {object} obj 任意对象
     * @return {int} obj所包换的属性个数
     */
    getMapLength: function (obj) {
        var count = 0;
        for (var i in obj) {
            if (obj.hasOwnProperty(i)) {
                count++;
            }
        }
        return count;
    },

    /**
     * 按照str2+split+str1的规则组合字符串
     * @param {string} split 分隔符 
     * @param {string} str1 尾部字符
     * @param {string} str2 首部字符
     * @return {string} 合并后的字符
     */
    insertString: function (split, str1, str2) {
        if (Tools.isNull(str1))
            str1 = "";
        if (Tools.isNull(str2))
            str2 = "";

        return str2 + split + str1;
    },

    /**
     * 按照str1 + str2的格式组合字符串
     * @param {string} str1 首字符 
     * @param {string} str2 尾字符
     */
    addString: function (str1, str2) {
        if (Tools.isNull(str1))
            str1 = "";
        if (Tools.isNull(str2))
            str2 = "";

        return str1 + str2;
    },

    /**
     * 检验输入值是否为一个数字，并且判断是否为正数；输入值可以为int，float
     * @param {number} num 要检验的数字
     * @return {int} 0 是数字并且为正数，1是数字并且为负数，-1不是数字
     */
    isNumber: function (num) {
        var reg = new RegExp("^-?[0-9]*.?[0-9]*$");
        if (reg.test(num)) {
            var absVal = Math.abs(num);
            return num == absVal ? 0 : 1;
        } else {
            return -1;
        }
    },

    /**
     * 获取substr在str中出现的次数，不区分大小写
     * @param {string} str 待检查的字符串
     * @param {string} substr 要获取重复次数的字符串
     * @return {int} substr出现在str的次数，未出现返回0
     */
    getRepeatCount: function (str, substr) {
        var count;
        var reg = "/" + substr + "/gi"; //查找时忽略大小写
        reg = eval(reg);
        if (str.match(reg) == null) {
            count = 0;
        } else {
            count = str.match(reg).length;
        }
        return count;

        //返回找到的次数
    },

    /**
     * 获取代码字典
     * @param {string} arg 代码字典名称
     * @deprecated 此版本不支持 
     */
    getDict: function (arg) {
        var a = encodeURI("./Services/CodeServices.php?code=" + arg);
        return a;
    },

    /**
     * 去除数组中的重复项目，数组项目
     * @param {array} arr 数组对象 
     */
    uniqueArray: function (arr) {
        var result = [],
            hash = {};
        for (var i = 0, elem;
            (elem = arr[i]) != null; i++) {
            if (!hash[elem]) {
                result.push(elem);
                hash[elem] = true;
            }
        }
        return result;
    },

    /**
     * 从Gaea框架获取验证码
     * @deprecated 此版本不支持
     */
    getYZMURL: function () {
        return "./Services/yzm.php?rnd=" + Math.random();
    },

    /**
     * 重新加载当前页面
     */
    reload: function () {
        window.parent.location.reload();
    },
    /**
     * 获取指定范围内的随机整数
     * @param {int} Min 范围最小值
     * @param {int} Max 范围最大值
     * @return {int} 生成的随机整数
     */
    getRandomNum: function (Min, Max) {
        var Range = Max - Min;
        var Rand = Math.random();
        return (Min + Math.round(Rand * Range));
    },

    /**
     * 获取随机的字符串，字符串由[0-9,A-Z]组成
     * @param {int} n 要获取的随机字符串的位数
     * @return {string} 生成的随机字符串 
     */
    generateMixed: function (n) {
        var chars = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'];
        var res = "";
        for (var i = 0; i < n; i++) {
            var id = Math.ceil(Math.random() * 35);
            res += chars[id];
        }
        return res;
    },

    /**
     * 按照对象的key以升序排序对象
     * @param {object} obj 要排序的对象 
     * @return {object} 已排序对象
     */
    sortObject: function (obj) {
        var arr = [];
        for (var index in obj) {
            arr.push(index);
        }

        var arr2 = arr.sort(function (a, b) {
            if (a > b) {
                return 1;
            } else if (a < b) {
                return -1
            } else {
                return 0;
            }
        });

        var result = {};
        for (var index = 0; index < arr2.length; index++) {
            var i = arr2[index];
            result[i] = obj[i];
        }

        return result;
    },

    /**
     * 按照升序排序数组
     * @param {array} arr 要排序的数组对象
     * @return {array} 已排序数组
     */
    sortArray: function (arr) {
        var arr2 = arr.sort(function (a, b) {
            if (a > b) {
                return 1;
            } else if (a < b) {
                return -1
            } else {
                return 0;
            }
        });

        return arr2;
    },

    /**
     * 验证邮箱地址是否合法
     * @param {string} email 邮箱地址字符串
     * @return {bool} true 合法，其他不合法 
     */
    isEmail: function (email) {
        invalidChars = " /;,:{}[]|*%$#!()`<>?";
        if (email == "") {
            return false;
        }
        for (i = 0; i < invalidChars.length; i++) {
            bar = invalidChars.charAt(i)
            if (email.indexOf(badChar, 0) > -1) {
                return false;
            }
        }
        atPos = email.indexOf("@", 1)
        if (atPos == -1) {
            return false;
        }
        if (email.indexOf("@", atPos + 1) != -1) {
            return false;
        }
        periodPos = email.indexOf(".", atPos)
        if (periodPos == -1) {
            return false; // and at least one "." after the "@"
        }
        if (atPos + 2 > periodPos) {
            return false; // and at least one character between "@" and "."
        }
        if (periodPos + 3 > email.length) {
            return false;
        }
        return true;
    },

    /**
     * 验证身份证是否合法
     * @param {string} socialNo 身份证号码字符 
     * @return {bool} true合法，其他不合法
     */
    checkCardId: function (socialNo) {
        if (socialNo == "") {
            alert("输入身份证号码不能为空!");
            return (false);
        }

        if (socialNo.length != 15 && socialNo.length != 18) {
            alert("输入身份证号码格式不正确!");
            return (false);
        }

        var area = {
            11: "北京",
            12: "天津",
            13: "河北",
            14: "山西",
            15: "内蒙古",
            21: "辽宁",
            22: "吉林",
            23: "黑龙江",
            31: "上海",
            32: "江苏",
            33: "浙江",
            34: "安徽",
            35: "福建",
            36: "江西",
            37: "山东",
            41: "河南",
            42: "湖北",
            43: "湖南",
            44: "广东",
            45: "广西",
            46: "海南",
            50: "重庆",
            51: "四川",
            52: "贵州",
            53: "云南",
            54: "西藏",
            61: "陕西",
            62: "甘肃",
            63: "青海",
            64: "宁夏",
            65: "新疆",
            71: "台湾",
            81: "香港",
            82: "澳门",
            91: "国外"
        };

        if (area[parseInt(socialNo.substr(0, 2))] == null) {
            alert("身份证号码不正确(地区非法)!");
            return (false);
        }

        if (socialNo.length == 15) {
            pattern = /^\d{15}$/;
            if (pattern.exec(socialNo) == null) {
                alert("15位身份证号码必须为数字！");
                return (false);
            }
            var birth = parseInt("19" + socialNo.substr(6, 2));
            var month = socialNo.substr(8, 2);
            var day = parseInt(socialNo.substr(10, 2));
            switch (month) {
                case '01':
                case '03':
                case '05':
                case '07':
                case '08':
                case '10':
                case '12':
                    if (day > 31) {
                        alert('输入身份证号码不格式正确!');
                        return false;
                    }
                    break;
                case '04':
                case '06':
                case '09':
                case '11':
                    if (day > 30) {
                        alert('输入身份证号码不格式正确!');
                        return false;
                    }
                    break;
                case '02':
                    if ((birth % 4 == 0 && birth % 100 != 0) || birth % 400 == 0) {
                        if (day > 29) {
                            alert('输入身份证号码不格式正确!');
                            return false;
                        }
                    } else {
                        if (day > 28) {
                            alert('输入身份证号码不格式正确!');
                            return false;
                        }
                    }
                    break;
                default:
                    alert('输入身份证号码不格式正确!');
                    return false;
            }
            var nowYear = new Date().getYear();
            if (nowYear - parseInt(birth) < 15 || nowYear - parseInt(birth) > 100) {
                alert('输入身份证号码不格式正确!');
                return false;
            }
            return (true);
        }

        var Wi = new Array(
            7, 9, 10, 5, 8, 4, 2, 1, 6,
            3, 7, 9, 10, 5, 8, 4, 2, 1
        );
        var lSum = 0;
        var nNum = 0;
        var nCheckSum = 0;

        for (i = 0; i < 17; ++i) {
            if (socialNo.charAt(i) < '0' || socialNo.charAt(i) > '9') {
                alert("输入身份证号码格式不正确!");
                return (false);
            } else {
                nNum = socialNo.charAt(i) - '0';
            }
            lSum += nNum * Wi[i];
        }


        if (socialNo.charAt(17) == 'X' || socialNo.charAt(17) == 'x') {
            lSum += 10 * Wi[17];
        } else if (socialNo.charAt(17) < '0' || socialNo.charAt(17) > '9') {
            alert("输入身份证号码格式不正确!");
            return (false);
        } else {
            lSum += (socialNo.charAt(17) - '0') * Wi[17];
        }

        if ((lSum % 11) == 1) {
            return true;
        } else {
            alert("输入身份证号码格式不正确!");
            return (false);
        }
    },

    isFunction: function (arg) {
        return typeof arg === "function";
    },

    /**
     * 检查对象是否为空
     * @param {object} arg 待检查对象
     * @return {bool} true为空，其他不为空 
     */
    isNull: function (arg) {
        if (arg != null && arg != undefined) {
            if (typeof (arg) == "string") {
                arg = arg.trim().replace(" ", "");
                return !(arg != "" && arg != '' && arg != "null" && arg != "undefined");
            } else if(typeof(arg) == "object"){
                return $.isEmptyObject(arg);
            }else
                return false;
        } else {
            return true;
        }
    },

    /**
     * 从配置文件获取ajax的超时时间和失败重试次数，并保存为全局量
     * @deprecated 此版本不支持 
     */
    getAjaxConfigTimeout: function () {
        var result = "";
        $.ajax({
            url: "./Services/AjaxConfig.php",
            async: false,
            dataType: "text",
            data: {
                flag: "timeout"
            },
            success: function (json) {
                result = json;
            }
        });
        // return result;
        Tools.timeout = result;

    },

    /**
     * 获取ajax的配置信息从服务端，并设置Tools的内置对象
     * @deprecated 此版本不支持
     */
    getAjaxConfigReties: function () {
        var result = "";
        $.ajax({
            url: "./Services/AjaxConfig.php",
            async: false,
            dataType: "text",
            data: {
                flag: "retries"
            },
            success: function (json) {
                result = json;
            }
        });
        //return result;
        Tools.reties = result;
    },

    /**
     * 是否为对象
     * @param {object} obj 
     */
    isObject: function (obj) {
        return typeof (obj) == "object";
    },

    /**
     * 是否为json对象
     * @param {object} obj 
     */
    isJson: function (obj) {
        var isjson = typeof (obj) == "object" && Object.prototype.toString.call(obj).toLowerCase() == "[object object]" && !obj.length;
        return isjson;
    },

    /**
     * 转换标准日期格式字符串为[yyyy/MM/dd hh:mm:ss]的格式
     * @param {string} string 日期的标准字符表示
     * @return {string} [yyyy/MM/dd hh:mm:ss]的日期格式字符串
     */
    stringToDateTime: function (string) {
        var regx = /(mon|monday|tue|tuesday|wed|wednesday|thu|thursday|fri|friday|sat|saturday|sun|sunday)[^\/)]+/gi;

        var r = null;
        var result = undefined;
        while (true) {
            r = regx.exec(string);
            if (r != null) {
                var dt = new Date(r[0].replace(/-/, "/"));
                if (dt != "Invalid Date")
                    string = string.replace(r[0] + ")", dt.format("yyyy/MM/dd hh:mm:ss"));
            } else
                break;
        }
        return string;
    },

    /**
     * 缺省ajax提交的错误处理函数
     * @param {object} jqXHR 发送ajax的XMLHttpClient对象 
     * @param {string} textStatus 错误信息
     * @param {object} errorThrown 错误对象
     */
    ajaxErrorProc: function (jqXHR, textStatus, errorThrown) {
        if (Tools.isNull(errorThrown)) {
            alert(textStatus);
        } else
            alert(errorThrown);
    },

    /**
     * 将指令及数据打包为Gaea框架的后端数据格式
     * @param {string} command 发送的指令
     * @param {object} postdata 发送的数据，单数据
     */
    createAjaxPackage: function (command, postdata) {
        var postdatas = [postdata];
        return Tools.createAjaxPackages(command, postdatas);
    },

    /**
     * 将指令及数据打包为Gaea框架的后端数据格式
     * @param {string} command 发送的指令
     * @param {array} postdatas 发送的数据列表
     * @return {object} 生成的包对象
     */
    createAjaxPackages: function (command, postdatas) {
        vsid = GlobalSessionObject.getSessionID();
        vsiinfo = GlobalSessionObject.getSessionInfo();
        vusername = GlobalSessionObject.getUserId();
        var value = "[";
        for (var i = 0; i < postdatas.length; i++) {
            var keystr = "";
            var postdata = postdatas[i];
            for (var key in postdata) {
                if (postdata.hasOwnProperty(key)) {
                    // or if (Object.prototype.hasOwnProperty.call(obj,prop)) for safety... 
                    var tmp = postdata[key];
                    if (Tools.isJson(tmp)) {
                        if (keystr == "")
                            keystr = key + ":" + mini.encode(tmp);
                        else
                            keystr += "," + key + ":" + mini.encode(tmp);
                    } else {
                        if (keystr == "")
                            keystr = key + ":\"" + tmp + "\"";
                        else
                            keystr += "," + key + ":\"" + tmp + "\"";
                    }
                }
            }

            if (i == 0)
                value += "{params:{" + keystr + "}}";
            else
                value += ",{params:{" + keystr + "}}";
        }
        value += "]";
        datastr = "{sid:\"" + vsid + "\",siinfo:\"" + vsiinfo + "\",username:\"" + vusername + "\", command:\"" + command +
            "\",data:{value:" + value + ",sign:\"\"}}";

        dataobj = mini.decode(datastr);
        return dataobj;
    },

    /**
     * 为dataobj生成签名信息
     * @param {object} dataobj 通过createAjaxPackages函数返回的对象
     */
    updateAjaxPackageSign: function (dataobj) {
        var data = dataobj["data"]["value"];
        value = mini.encode(data, "yyyy-MM-dd HH:mm:ss");
        value = Tools.stringToDateTime(value);
        //value = value.replace(,"$1".Format("yyyy-MM-dd HH:mm:ss"));
        //value = value.replace(/T14:01:01/g,'');
        //value = value.replace(/[\",']/g,"");
        value = ConvertFactory.Encode64(value);
        md5string = $.md5(value);
        dataobj["data"]["value"] = value;
        dataobj["data"]["sign"] = md5string;
        dataobj["Unique"] = GlobalSessionObject.getUnique();
    },

    /**使用ajax提交数据，与ajaxSubmit不同的是，这个提交会经由DispatchService服务分发到最终页面并执行。
     * @deprecated 此版本不支持
     * @param {string} dir:要执行页面所在的服务端路径名
     * @param {bool} pagename:要执行的服务端php页面名称，不包含".php"
     * @param {string} command:提交的指令，必须包含在pagename指定的页面中 
     * @param {json} postdata:提交的数据
     * @param {bool} async: 是否异步发送请求，true异步，否则同步
     * @param {callback} succ: 成功的回调函数，格式：function(data){}
     * @param {bool} fail:失败的回调函数，具体格式参见ajaxSubmit
     * @param {bool} memo:本次提交的说明 
     */
    ajaxSubmitDispatch: function (dir, pagename, command, postdata, memo, succ, fail) {
        var json = Tools.createAjaxPackage(command, postdata);
        json["Command_Running_Hint"] = memo;
        json["SERVICE_NAME"] = pagename;
        json["DIR"] = dir;
        Tools.updateAjaxPackageSign(json);
        Tools.ajaxSubmit({
            "url": "./Services/DispatchService.php",
            "async": false,
            data: json,
            success: succ,
            error: fail
        });
    },

    /**使用ajax提交数据到php框架端
     * @param {string} url:http格式的url，如：http://localhost:70/server/page/a.php
     * @param {string} command:提交的指令 
     * @param {json} postdata:提交的数据
     * @param {bool} async: 是否异步发送请求，true异步，否则同步
     * @param {callback} succ: 成功的回调函数，格式：function(data){}
     * @param {bool} memo:本次提交的说明 
     */
    ajaxSimpleFrameSubmit: function (url, async, command, postdata, succ, memo) {
        var json = Tools.createAjaxPackage(command, postdata);
        json["Command_Running_Hint"] = Tools.isNull(memo) ? "" : memo;
        Tools.updateAjaxPackageSign(json);
        Tools.ajaxSubmit({
            "url": url,
            "async": async,
            data: json,
            success: succ,
        });
    },

    /**
     * 获取指定文本的md5码
     * @param {string} strValue 要md5的文本
     * @return {string} md5码
     */
    signatureJson: function (strValue) {
        var data = ConvertFactory.Encode64(strValue + GlobalSessionObject.getSessionID());
        var md5Value = $.md5(data);
        return md5Value;
    },

    /**
     * 是否为文本
     * @param {object} str 
     * @return {bool} true是文本，其他不是
     */
    isString: function (str) {
        return (typeof str == 'string') && str.constructor == String;
    },

    /**
     * 检查数据是否包含【\\'】或者【\\"】，如果有则将其替换为【\'】或者【\"】，并返回json文本
     * @param {json} resultData 要替换的json对象
     * @return {string} 返回替换后的文本
     */
    tomcatResultConvert: function (resultData) {
        var text = JSON.stringify(resultData, function (k, v) {
            if (Tools.isString(v)) {
                v = v.replace(/\\(?=\"|\')/g, function (v) {
                    return v.substr(1);
                });
            }

            return v;
        });

        return text;
    },

    /**
     * 循环执行callback函数，知道此函数返回true
     * @param {callback} callback，格式：bool callback(); 
     */
    pollingTomcat: function (callback) {
        var t1 = window.setTimeout(function () {
            if (!callback()) {
                Tools.pollingTomcat(callback);
            }
        }, 2000);
        // var t2 = window.setTimeout("hello()", 3000); //使用字符串执行方法 
        // window.clearTimeout(t1); //去掉定时器 
    },

    /**使用ajax提交数据
     * @param {string} url:http://localhost:80/posturl.do格式的服务页面地址
     * @param {json} postdata:提交的数据
     * @param {bool} async: 是否异步发送请求，true异步，否则同步
     * @param {callback} succ: 成功的回调函数，格式：function(data){}
     * @param {bool} requestCrypted:请求是否加密发送，加解密过程由框架自动完成 
     * @param {bool} responseCrypted:返回数据是否要求服务端加密，加解密过程由框架自动完成
     */
    simpleTomcatSubmit: function (url, postdata, succ, requestCrypted, responseCrypted, async) {
        Tools.ajaxTomcatSubmit(url, null, postdata,
            Tools.isNull(async) ? true : async,
            false, succ, requestCrypted, responseCrypted);
    },

    /**
     * 获取tomcat的服务地址
     * @param {string} uri spring格式的uri，格式如：/server/page/c
     * @return {string} 返回完整地址，格式如下：http://localhost:80/server/page/c.do
     * @param {*} uri 
     */
    getAjaxTomcatUrl: function (uri) {
        return Tools.getTomcatUri() + uri + ".do";
    },

    /**
     * 获取tomcat的服务地址，检查地址是否由/结尾，如果是则删除再获取
     * @param {string} uri spring格式的uri，格式如：/server/page/c或/server/page/c/
     * @return {string} 返回完整地址，格式如下：http://localhost:80/server/page/c.do
     */
    smartGetAjaxTomcatUrl: function (uri) {
        if (uri.trim().indexOf("/") == 0) {
            if (uri.length > 3 && uri.slice(-3).toLowerCase() == ".do"){
                uri = uri.slice(0, -3);
            }
            
            return Tools.getAjaxTomcatUrl(uri);
        } else
            return uri;
    },

    /**使用ajax提交数据
     * @param {string} url:spring格式的url，如：/server/page/a.do
     * @param {string} command:保留 
     * @param {json} postdata:提交的数据
     * @param {bool} async: 是否异步发送请求，true异步，否则同步
     * @param {bool} needSignature:保留 
     * @param {callback} succ: 成功的回调函数，格式：function(data){}
     * @param {bool} requestCrypted:请求是否加密发送，加解密过程由框架自动完成 
     * @param {bool} responseCrypted:返回数据是否要求服务端加密，加解密过程由框架自动完成
     */
    ajaxTomcatSubmit: function (url, command, postdata, async, needSignature, succ,
        requestCrypted, responseCrypted) {
        url = Tools.smartGetAjaxTomcatUrl(url);
        Tools.ajaxTomcatDirectSubmit(url, command, postdata, async, needSignature, succ,
            requestCrypted, responseCrypted)
    },

    /**使用ajax提交数据
     * @param {string} url:http://localhost:80/posturl.do格式的服务页面地址
     * @param {string} command:保留 
     * @param {json} postdata:提交的数据
     * @param {bool} async: 是否异步发送请求，true异步，否则同步
     * @param {bool} needSignature:保留 
     * @param {callback} succ: 成功的回调函数，格式：function(data){}
     * @param {bool} requestCrypted:请求是否加密发送，加解密过程由框架自动完成 
     * @param {bool} responseCrypted:返回数据是否要求服务端加密，加解密过程由框架自动完成
     */
    ajaxTomcatDirectSubmit: function (url, command, postdata, async, needSignature, succ,
        requestCrypted, responseCrypted) {
        if (Tools.isNull(postdata)) {
            postdata = {};
        }

        if (!this.isJson(postdata)) {
            postdata = JSON.parse(postdata);
        }

        return Tools.ajaxSubmit({
            "url": url,
            "async": async,
            beforeSend: function (xhr) {
                xhr.withCredentials = true;
            },
            xhrFields: {
                withCredentials: true
            },
            crossDomain: true,
            data: postdata,
            requestCrypted: requestCrypted,
            responseCrypted: responseCrypted,
            dataType: "json",
            success: succ,
        });
    },

    /**使用ajax提交数据
     * @param {string} url:"服务页面地址" 
     * @param {json} data:提交的数据
     * @param {callback} success:function(data){}
     * @param {bool} async: 是否异步发送请求，true异步，否则同步
     */
    ajaxSimpleSubmit: function (url, async, postdata, succ) {
        return Tools.ajaxSubmit({
            "url": url,
            "async": async,
            data: postdata,
            success: succ
        });
    },

    /**
     * 执行ajax请求
     * @param {json} json，格式如下：
        options
        类型：Object
        可选。AJAX 请求设置。所有选项都是可选的。

        async
        类型：Boolean
        默认值: true。默认设置下，所有请求均为异步请求。如果需要发送同步请求，请将此选项设置为 false。
        注意，同步请求将锁住浏览器，用户其它操作必须等待请求完成才可以执行。

        beforeSend(XHR)
        类型：Function
        发送请求前可修改 XMLHttpRequest 对象的函数，如添加自定义 HTTP 头。

        XMLHttpRequest 对象是唯一的参数。
        这是一个 Ajax 事件。如果返回 false 可以取消本次 ajax 请求。

        cache
        类型：Boolean
        默认值: true，dataType 为 script 和 jsonp 时默认为 false。设置为 false 将不缓存此页面。
        jQuery 1.2 新功能。

        complete(XHR, TS)
        类型：Function
        请求完成后回调函数 (请求成功或失败之后均调用)。
        参数： XMLHttpRequest 对象和一个描述请求类型的字符串。
        这是一个 Ajax 事件。

        contentType
        类型：String
        默认值: "application/x-www-form-urlencoded"。发送信息至服务器时内容编码类型。
        默认值适合大多数情况。如果你明确地传递了一个 content-type 给 $.ajax() 那么它必定会发送给服务器（即使没有数据要发送）。

        context
        类型：Object
        这个对象用于设置 Ajax 相关回调函数的上下文。也就是说，让回调函数内 this 指向这个对象（如果不设定这个参数，那么 this 就指向调用本次 AJAX 请求时传递的 options 参数）。比如指定一个 DOM 元素作为 context 参数，这样就设置了 success 回调函数的上下文为这个 DOM 元素。
        就像这样：
        $.ajax({ url: "test.html", context: document.body, success: function(){
                $(this).addClass("done");
            }});

        data
        类型：String
        发送到服务器的数据。将自动转换为请求字符串格式。GET 请求中将附加在 URL 后。查看 processData 选项说明以禁止此自动转换。必须为 Key/Value 格式。如果为数组，jQuery 将自动为不同值对应同一个名称。如 {foo:["bar1", "bar2"]} 转换为 '&foo=bar1&foo=bar2'。

        dataFilter
        类型：Function
        给 Ajax 返回的原始数据的进行预处理的函数。提供 data 和 type 两个参数：data 是 Ajax 返回的原始数据，type 是调用 jQuery.ajax 时提供的 dataType 参数。函数返回的值将由 jQuery 进一步处理。

        dataType
        类型：String
        预期服务器返回的数据类型。如果不指定，jQuery 将自动根据 HTTP 包 MIME 信息来智能判断，比如 XML MIME 类型就被识别为 XML。在 1.4 中，JSON 就会生成一个 JavaScript 对象，而 script 则会执行这个脚本。随后服务器端返回的数据会根据这个值解析后，传递给回调函数。可用值:
        "xml": 返回 XML 文档，可用 jQuery 处理。
        "html": 返回纯文本 HTML 信息；包含的 script 标签会在插入 dom 时执行。
        "script": 返回纯文本 JavaScript 代码。不会自动缓存结果。除非设置了 "cache" 参数。注意：在远程请求时(不在同一个域下)，所有 POST 请求都将转为 GET 请求。（因为将使用 DOM 的 script标签来加载）
        "json": 返回 JSON 数据 。
        "jsonp": JSONP 格式。使用 JSONP 形式调用函数时，如 "myurl?callback=?" jQuery 将自动替换 ? 为正确的函数名，以执行回调函数。
        "text": 返回纯文本字符串

        error
        类型：Function
        默认值: 自动判断 (xml 或 html)。请求失败时调用此函数。
        有以下三个参数：XMLHttpRequest 对象、错误信息、（可选）捕获的异常对象。
        如果发生了错误，错误信息（第二个参数）除了得到 null 之外，还可能是 "timeout", "error", "notmodified" 和 "parsererror"。
        这是一个 Ajax 事件。

        global
        类型：Boolean
        是否触发全局 AJAX 事件。默认值: true。设置为 false 将不会触发全局 AJAX 事件，如 ajaxStart 或 ajaxStop 可用于控制不同的 Ajax 事件。

        ifModified
        类型：Boolean
        仅在服务器数据改变时获取新数据。默认值: false。使用 HTTP 包 Last-Modified 头信息判断。在 jQuery 1.4 中，它也会检查服务器指定的 'etag' 来确定数据没有被修改过。

        jsonp
        类型：String
        在一个 jsonp 请求中重写回调函数的名字。这个值用来替代在 "callback=?" 这种 GET 或 POST 请求中 URL 参数里的 "callback" 部分，比如 {jsonp:'onJsonPLoad'} 会导致将 "onJsonPLoad=?" 传给服务器。

        jsonpCallback
        类型：String
        为 jsonp 请求指定一个回调函数名。这个值将用来取代 jQuery 自动生成的随机函数名。这主要用来让 jQuery 生成度独特的函数名，这样管理请求更容易，也能方便地提供回调函数和错误处理。你也可以在想让浏览器缓存 GET 请求的时候，指定这个回调函数名。

        password
        类型：String
        用于响应 HTTP 访问认证请求的密码

        processData
        类型：Boolean
        默认值: true。默认情况下，通过data选项传递进来的数据，如果是一个对象(技术上讲只要不是字符串)，都会处理转化成一个查询字符串，以配合默认内容类型 "application/x-www-form-urlencoded"。如果要发送 DOM 树信息或其它不希望转换的信息，请设置为 false。
        scriptCharset
        类型：String
        只有当请求时 dataType 为 "jsonp" 或 "script"，并且 type 是 "GET" 才会用于强制修改 charset。通常只在本地和远程的内容编码不同时使用。

        success
        类型：Function
        请求成功后的回调函数。
        参数：由服务器返回，并根据 dataType 参数进行处理后的数据；描述状态的字符串。
        这是一个 Ajax 事件。

        traditional
        类型：Boolean
        如果你想要用传统的方式来序列化数据，那么就设置为 true。请参考工具分类下面的 jQuery.param 方法。

        timeout
        类型：Number
        设置请求超时时间（毫秒）。此设置将覆盖全局设置。

        type
        类型：String
        默认值: "GET"。请求方式 ("POST" 或 "GET")， 默认为 "GET"。注意：其它 HTTP 请求方法，如 PUT 和 DELETE 也可以使用，但仅部分浏览器支持。

        url
        类型：String
        默认值: 当前页地址。发送请求的地址。

        username
        类型：String
        用于响应 HTTP 访问认证请求的用户名。

        xhr
        类型：Function
        需要返回一个 XMLHttpRequest 对象。默认在 IE 下是 ActiveXObject 而其他情况下是 XMLHttpRequest 。用于重写或者提供一个增强的 XMLHttpRequest 对象。这个参数在 jQuery 1.3 以前不可用。     
    */
    ajaxSubmit: function (json) {
        var errorProc = Tools.isNull(json.error) ? json.success : json.error;
        var ajaxError = function (jqXHR, textStatus, errorThrown) {
            var result = {
                ret: -1
            };
            if (!Tools.isNull(textStatus))
                result.err = textStatus;
            else
                result.err = errorThrown;
            errorProc(result);
        };

        var isRequstCrypt = Tools.requestCrypted;
        if (!Tools.isNull(json.requestCrypted)) {
            isRequstCrypt = json.requestCrypted;
        }

        var isResponseCrypt = Tools.responseCrypted;
        if (!Tools.isNull(json.responseCrypted)) {
            isResponseCrypt = json.responseCrypted;
        }
        if (isRequstCrypt)
            json.data = {
                "_requestCrypted": true,
                data: GlobalEncrypt.encrypt(json.data, GlobalEncrypt.AES)
            };

        json.data._responseCrypted = isResponseCrypt;

        var success = Tools.isNull(json.success) ? function (data) {} : json.success;
        var senddata = {
            async: Tools.isNull(json.async) ? false : json.async,
            type: Tools.isNull(json.type) ? "post" : json.type,
            url: json.url,
            data: json.data,
            cache: Tools.isNull(json.cache) ? false : json.cache,
            dataType: json.dataType,
            contentType: Tools.isNull(json.contentType) ? "application/x-www-form-urlencoded;charset=UTF-8" : json.contentType,
            success: function (data) {
                if (!Tools.isNull(data.data) && !Tools.isNull(data.data._crypt) && data.data._crypt) {
                    var localText = data.data.data;
                    data = JSON.parse(GlobalEncrypt.decrypt(localText, GlobalEncrypt.AES));
                }
                json.success(data);
            },
            error: ajaxError
        };

        if (!Tools.isNull(json.beforeSend))
            senddata.beforeSend = json.beforeSend;
        if (!Tools.isNull(json.xhrFields))
            senddata.xhrFields = json.xhrFields;
        if (!Tools.isNull(json.crossDomain))
            senddata.crossDomain = json.crossDomain;
        return $.ajax(senddata);
    },

    /**
     * 获取服务端日期
     * @return 返回格式为[yyyy-MM-dd hh:mm:ss]的服务端的日期
     */
    getRemoteDate: function () {
        Date.prototype.format = function (format) {
            var o = {
                "M+": this.getMonth() + 1, //month
                "d+": this.getDate(), //day
                "h+": this.getHours(), //hour
                "m+": this.getMinutes(), //minute
                "s+": this.getSeconds(), //second
                "q+": Math.floor((this.getMonth() + 3) / 3), //quarter
                "S": this.getMilliseconds() //millisecond
            }
            if (/(y+)/.test(format)) format = format.replace(RegExp.$1,
                (this.getFullYear() + "").substr(4 - RegExp.$1.length));
            for (var k in o)
                if (new RegExp("(" + k + ")").test(format))
                    format = format.replace(RegExp.$1,
                        RegExp.$1.length == 1 ? o[k] :
                        ("00" + o[k]).substr(("" + o[k]).length));
            return format;
        }

        var param = {
            command: "getdate",
            Command_Running_Hint: "获取服务器日期"
        };
        var result = "";
        Tools.ajaxSimpleSubmit("./Services/ToolServices.php", false, param, function (data) {
            var info = mini.decode(data)
            if (info["ret"]) {
                result = info["value"];
                var d = new Date(result);
                result = d.format("yyyy-MM-dd hh:mm:ss");

            }
        });
        return result;
    },

    /**
     * 获取界面描述信息
     * @param {string} uiName Gaea的模块节点的name
     * @return {UIInfo} 界面描述信息
     */
    getUIInfo: function (uiName) {
        var param = {
            keys: {
                name: uiName
            },
            mark: "getUIContent"
        }

        var json = Tools.createAjaxPackage(param.mark, param.keys);
        json["Command_Running_Hint"] = "getUI" + uiName;
        Tools.updateAjaxPackageSign(json);
        var result = undefined;
        Tools.ajaxSimpleSubmit("./Services/ToolServices.php", false, json, function (data) {
            var info = mini.decode(data)
            if (info.ret)
                result = info["data"];
        });
        return result;
    },

    dataSources : {},
    /**
     * 获取数据源描述信息
     * @param {string} dataSourceId Gaea中的数据源定义id
     * @return {object} 数据源描述信息
     */
    getDataSource: function (dataSourceId) {
        if (!Tools.isNull(Tools.dataSources[dataSourceId])){
            return Tools.dataSources[dataSourceId];
        }
        var param = {
            keys: {
                name: dataSourceId
            },
            mark: "getDataSourceContent"
        }

        var json = Tools.createAjaxPackage(param.mark, param.keys);
        json["Command_Running_Hint"] = "getDataSourceContent " + dataSourceId;
        Tools.updateAjaxPackageSign(json);
        var result = undefined;
        Tools.ajaxSimpleSubmit("./Services/ToolServices.php", false, json, function (data) {
            var info = mini.decode(data)
            if (info.ret){
                result = info["data"];
                Tools.dataSources[dataSourceId] = result;
            }
        });
        return result;
    },

    /**
     * 获取服务端的数据服务器地址
     * @return {string} 数据服务地址，格式：http://localhost:8080/server/service.do
     */
    getRemoteTomcatUri: function () {
        var param = {
            keys: {},
            mark: "getTomcatUri"
        }

        var json = Tools.createAjaxPackage(param.mark, param.keys);
        json["Command_Running_Hint"] = "getRemoteTomcatUri" + name;
        Tools.updateAjaxPackageSign(json);
        var result = undefined;
        Tools.ajaxSimpleSubmit("./Services/ToolServices.php", false, json, function (data) {
            var info = mini.decode(data)
            if (info.ret)
                result = info["uri"];
        });
        return result;
    },


    /**
     * 获取报表模板的描述信息
     * @param {string} name Gaea定义的报表模板文件的名称，不包括[.rpt]扩展名
     * @return {object} 报表模板的描述信息
     */
    getReportInfo: function (name) {
        var param = {
            keys: {
                name: name
            },
            mark: "getReportInfo"
        }

        var json = Tools.createAjaxPackage(param.mark, param.keys);
        json["Command_Running_Hint"] = "getReportInfo" + name;
        Tools.updateAjaxPackageSign(json);
        var result = undefined;
        Tools.ajaxSimpleSubmit("./Services/ToolServices.php", false, json, function (data) {
            var info = mini.decode(data)
            if (info.ret)
                result = info["data"];
        });
        return result;
    },

    /**
     * 获取Gaea框架定义的入口界面描述信息
     * @return {UIInfo} 界面描述信息
     */
    getMainUIInfo: function () {
        var param = {
            keys: "",
            mark: "getUIMainContent"
        }

        var json = Tools.createAjaxPackage(param.mark, param.keys);
        json["Command_Running_Hint"] = "getMainUI";
        Tools.updateAjaxPackageSign(json);
        var result = undefined;
        Tools.ajaxSimpleSubmit("./Services/ToolServices.php", false, json, function (data) {
            var info = mini.decode(data)
            if (info.ret)
                result = info["data"];
        });
        return result;
    },

    cacheMainMenuInfo: undefined,
    cacheMainTreeInfo: undefined,
    cacheMainMetaInfo: undefined,

    /**
     * 获取Gaea定义的主菜单描述信息
     * @return {object} 主菜单描述信息
     */
    getMainMenuInfo: function () {
        if (!Tools.isNull(Tools.cacheMainMenuInfo))
            return Tools.cacheMainMenuInfo;

        var param = {
            keys: {
                servertype: "tomcat"
            },
            mark: "getMenuMainContent"
        }

        var json = Tools.createAjaxPackage(param.mark, param.keys);
        json["Command_Running_Hint"] = "getMainMenu";
        Tools.updateAjaxPackageSign(json);
        var result = undefined;
        Tools.ajaxSimpleSubmit("./Services/ToolServices.php", false, json, function (data) {
            var info = mini.decode(data)
            if (info.ret)
                result = info["data"];
        });
        Tools.cacheMainMenuInfo = result;
        return result;
    },

    /**
     * 登出系统
     * @deprecated 此版本不支持，请使用SessionStorageObject.clear()方法代替
     */
    logoff: function () {
        var param = {
            keys: {
                userid: GlobalSessionObject.GetUserId()
            },
            mark: "logoff"
        }

        var json = Tools.createAjaxPackage(param.mark, param.keys);
        json["Command_Running_Hint"] = "logoff";
        Tools.updateAjaxPackageSign(json);
        var result = undefined;
        Tools.ajaxSimpleSubmit("./Services/ToolServices.php", false, json, function (data) {
            var info = mini.decode(data)
            if (info.ret) {
                GlobalSessionObject.clear();
                globalClearOperationInfos();
            }
        });
        return result;
    },

    /**
     * 获取Gaea定义的主配置信息
     * @return {object} 著配置信息
     */
    getMetaFile: function () {
        if (!Tools.isNull(Tools.cacheMainMetaInfo))
            return Tools.cacheMainMetaInfo;

        var param = {
            keys: "",
            mark: "getMetaFile"
        }

        var json = Tools.createAjaxPackage(param.mark, param.keys);
        json["Command_Running_Hint"] = "getMetaFile";
        Tools.updateAjaxPackageSign(json);
        var result = undefined;
        Tools.ajaxSimpleSubmit("./Services/ToolServices.php", false, json, function (data) {
            var info = mini.decode(data)
            if (info.ret)
                result = info["data"];
        });
        Tools.cacheMainMetaInfo = result;
        return result;
    },

    /**
     * 获取Gaea定义的主导航树信息
     * @return {object} 主导航树信息
     */
    getMainTreeInfo: function () {
        if (!Tools.isNull(Tools.cacheMainTreeInfo))
            return Tools.cacheMainTreeInfo;

        var param = {
            keys: "",
            mark: "getTreeMainContent"
        }

        var json = Tools.createAjaxPackage(param.mark, param.keys);
        json["Command_Running_Hint"] = "getMainTree";
        Tools.updateAjaxPackageSign(json);
        var result = undefined;
        Tools.ajaxSimpleSubmit("./Services/ToolServices.php", false, json, function (data) {
            var info = mini.decode(data)
            if (info.ret)
                result = info["data"];
        });

        Tools.cacheMainTreeInfo = result;
        return result;
    },

    /**
     * 对url进行URI编码
     * @param {string} url
     * @return {string} 已编码url 
     */
    EncodeUrl: function (url) {
        if (Tools.isNull(url))
            return undefined;
        else {
            return encodeURI(url);
        }
    },

    /**
     * 深度拷贝json对象为新对象，如果传入对象包括函数，那么函数定义将丢失
     * @param {json} o 要拷贝的json对象
     * @return {json} 拷贝的新对象，与o无引用关系
     */
    deepCopyForJsonNoFunction: function (o) {
        return JSON.parse(JSON.stringify(o));
    },

    name: 'fileoptn_hide_select_file_x10',

    /**
     * 显示打开文件对话框
     * @description 用户选择文件后会调用callback函数并传入选择的文件
     * @param {callback} callback 回调函数，格式：function(files) 
     */
    openFileSelect: function (callback) {
        var name = Tools.name;
        var inputObj = $("#" + name)[0];
        if (!Tools.isNull(inputObj))
            $("#" + name).remove();
        $('body').append('<input type="file" style="display:none" mce_style="display:none" id="' + name + '">');
        inputObj = $("#" + name)[0];
        inputObj.onchange = function () {
            if (!Tools.isNull(inputObj.files) && inputObj.files.length > 0)
                callback(inputObj.files);
        }

        $("#" + name).click();
    },

    /**
     * 检查uiInfo指定的界面中所有设置检验规则的miniui组件
     * @param {UIInfo} uiInfo 要检查界面描述文件
     * @return {bool} true校验成功，其他失败
     */
    validateForm: function (uiInfo) {
        try {
            var form = new mini.Form("#" + uiInfo.workflow.id + "div");
            form.validate();
            return form.isValid();
        } catch (e) {
            return true;
        }
    },

    /**
     * 弹出用户另存为对话框，保存fileUrl指定的文件到本地
     * @param {string} filename 下载文件的默认名称，不包括扩展名
     * @param {string} fileUrl 下载文件的地址，格式：http://localhost/a/ab.png
     */
    openSaveFileSelector: function (filename, fileUrl) {
        var name = Tools.name;
        var downloadObj = $("#" + name)[0];
        if (!Tools.isNull(downloadObj))
            $("#" + name).remove();
        $('body').append('<a href="" style="display:none" download="' + filename + '" id="' + name + '"></a>');
        downloadObj = $("#" + name)[0];
        downloadObj.href = fileUrl;
        document.getElementById(name).click();
    },

    /**
     * 获取当前时间的字符表示
     * @return {string} 当前日期字符串，格式：[yyyy-MM-dd HH:mm:ss]
     */
    now: function (d) {
        if (d)
            var date = new Date(d);
        else
            var date = new Date();
        return date.getFullYear() + "-" + (date.getMonth() + 1) + "-" + date.getDate() + " " +
            date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
    },

    /**
     * 获取当前时间的字符表示，与now区别：返回时间带毫秒
     * @return {string} 当前日期字符串，格式：[yyyy-MM-dd HH:mm:ss:sss]
     */
    nowEx: function (d) {
        if (d)
            var date = new Date(d);
        else
            var date = new Date();
        return date.getFullYear() + "-" + (date.getMonth() + 1) + "-" + date.getDate() + " " +
        date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() + "." + date.getMilliseconds;
    },
    /**
     * 获取当前时间的字符表示，与now区别：返回时间年月日
     * @return {string} 当前日期字符串，格式：[yyyy-MM-dd]
     */
    nowYM: function (d) {
        if (d)
            var date = new Date(d);
        else
            var date = new Date();
        return date.getFullYear() + "-" + (date.getMonth() + 1) + "-" + date.getDate();
           
    },
    dateFormat: function (data, format) {
        switch (format) {
            case "yyyy-MM-dd HH:mm:ss":
                return this.now(data)
            case "yyyy-MM-dd HH:mm:ss:sss":
                return this.nowEx(data)
            case "yyyy-MM-dd":
                return this.nowYM(data)
        }
    }

}

// Date.prototype.format = function(fmt) { 
//     var o = { 
//        "M+" : this.getMonth()+1,                 //月份 
//        "d+" : this.getDate(),                    //日 
//        "h+" : this.getHours(),                   //小时 
//        "m+" : this.getMinutes(),                 //分 
//        "s+" : this.getSeconds(),                 //秒 
//        "q+" : Math.floor((this.getMonth()+3)/3), //季度 
//        "S"  : this.getMilliseconds()             //毫秒 
//    }; 
//    if(/(y+)/.test(fmt)) {
//            fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length)); 
//    }
//     for(var k in o) {
//        if(new RegExp("("+ k +")").test(fmt)){
//             fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));
//         }
//     }
//    return fmt; 
// }