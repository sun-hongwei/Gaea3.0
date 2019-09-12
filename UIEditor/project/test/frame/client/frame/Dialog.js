var GlobalDialogDefine = {
    titleHeight: 30,
};

function FrameDialog() {
    // 显示窗口
    this.deckId = undefined;//遮罩id
    this.boxId = undefined;//显示box
    this.id = undefined;
    this.ondestory = undefined;
    this.userparam = undefined;
    this.oldOverflowX = undefined;
    this.oldOverflowY = undefined;
    this.closeButtonId = undefined;
    this.zIndex = 1;

    this.computeAutoHeight = function (divHeight, barWidth, size) {
        var result;

        var height = divHeight + barWidth;
        if (height > size.height - GlobalDialogDefine.titleHeight) {
            result = size.height - GlobalDialogDefine.titleHeight;
        } else {
            result = height;
        }

        return result;
    }

    this.computeAutoWidth = function (divWidth, barWidth, size) {
        var result;

        if (divWidth + barWidth > size.width) {
            result = size.width;
        } else {
            result = divWidth + barWidth;
        }

        return result;
    }

    this.computeDialogAutoSize = function (div, sizeInfo) {
        var divWidth = $(div).width();
        var divHeight = $(div).height();

        var barWidth = Tools.getScrollbarWidth();
        var size = getFrameContentWindowSize();

        var result = {
            width: divWidth > size.width ? size.width : divWidth,
            height: divHeight > size.height - GlobalDialogDefine.titleHeight ? size.height - GlobalDialogDefine.titleHeight : divHeight
        };
        if (sizeInfo.hasHScrollBar && sizeInfo.hasVScrollBar) {
            result.width = this.computeAutoWidth(divWidth, barWidth, size);
            result.height = this.computeAutoHeight(divHeight, barWidth, size);
        } else if (sizeInfo.hasHScrollBar) {
            result.height = this.computeAutoHeight(divHeight, barWidth, size);
        } else if (sizeInfo.hasVScrollBar) {
            result.width = this.computeAutoWidth(divWidth, barWidth, size);
        }

        return result;

    }

    this.showFrameDialog = function (uiName, title, onload, ondestory, userparam, count, width, height) {
        var uiInfo = globalGetUIInfo(uiName);
        var cf = new ControlFactory();
        var width = uiInfo.workflow.dialogAutoSize ? 500 : uiInfo.page.size.width;
        var height = uiInfo.workflow.dialogAutoSize ? 400 : uiInfo.page.size.height;
        if (Tools.isNull(title))
            title = uiInfo.workflow.title;

        this.showDialog(title, width, height,
            function (div, param, title) {
                var dialog = param;
                if (!Tools.isNull(uiName)) {
                    var documentWriter = new DocumentWriter();

                    var name = uiInfo.workflow.id + "_dialog_layout";
                    dialog.closeButtonId = Tools.guid();

                    var height = $(div).height() - GlobalDialogDefine.titleHeight;
                    documentWriter.writeForElement(div, "<div id = '" + name + "' style='height:" + height + "px' class='ContentDiv'></div>");

                    var bodyEl = document.getElementById(name);
                    var width = bodyEl.offsetWidth;
                    var height = bodyEl.offsetHeight;

                    uiInfo.tabParent = bodyEl;
                    cf.write(documentWriter, uiInfo, width, height, jumpUIForName,
                        function (contentdiv, sizeInfo) {
                            if (uiInfo.workflow.dialogAutoSize) {
                                var newSize = dialog.computeDialogAutoSize(contentdiv, sizeInfo);

                                var parentDivSelector = $("#" + name);
                                parentDivSelector.width(newSize.width);
                                parentDivSelector.height(newSize.height);
                                $(div).width(newSize.width);
                                $(div).height(newSize.height + GlobalDialogDefine.titleHeight);
                            }

                            documentWriter.writeForElement(div, "<div id='" + name + "_dialog" + "' class='TitleDiv'>" +
                                "<span style='line-height:GlobalDialogDefine.titleHeightpx'>" + title +
                                "    </span><img src='frameimage/close.png' id='" + dialog.closeButtonId +
                                "' style='height:24;cursor:hand;vertical-align:middle'/>" +
                                "</div>");

                            $("#" + dialog.closeButtonId).click(function () {
                                dialog.close();
                            });

                            setupFrameDialogMover($("#" + name + "_dialog")[0]);

                            // 调整位置至居中
                            dialog.adjustLocation(bodyEl);
                        });
                    if (!Tools.isNull(onload)) {
                        onload(bodyEl, uiInfo, userparam);
                    }
                }
            }, ondestory, this, count);
    };

    this.showDialog = function (title, dialogWidth, dialogHeight, onload, ondestory, param, count) {
        if (count > 0)
            count++;

        this.ondestory = ondestory;
        this.userparam = param;

        this.oldOverflowX = $(document.body).css("overflow-x");
        this.oldOverflowY = $(document.body).css("overflow-y");
        this.deckId = Tools.guid();
        this.id = Tools.guid();
        var objDeck = document.createElement("div");
        objDeck.id = this.deckId;
        document.body.appendChild(objDeck);

        objDeck.style.filter = "alpha(opacity=50)";
        objDeck.style.opacity = 50 / 100;
        objDeck.style.MozOpacity = 50 / 100;
        $(objDeck).css("top", document.body.scrollTop);
        $(objDeck).css("height", "100%");
        $(objDeck).css("width", "100%");
        objDeck.className = "showDeck";
        var zindex = Number($(objDeck).css("z-index"));
        this.zIndex = zindex + count;
        $(objDeck).css("z-index", zindex + count);
        // 显示遮盖的层end

        $(document.body).css({
            "overflow-x": "hidden",
            "overflow-y": "hidden"
        });

        // 改变样式
        dialog = document.createElement("div");
        dialog.id = this.id;
        document.body.appendChild(dialog);

        dialog.className = "showDlg";
        var zindex = Number($(dialog).css("z-index"));
        $(dialog).css("z-index", zindex + count);
        $(dialog).width(dialogWidth);
        $(dialog).height(dialogHeight);
        $(dialog).css("overflow", "hidden");

        if (!Tools.isNull(onload))
            onload(document.getElementById(this.id), param, title);

    };

    this.mask = function (title, cls) {
        this.oldOverflowX = $(document.body).css("overflow-x");
        this.oldOverflowY = $(document.body).css("overflow-y");
        this.deckId = Tools.guid();
        var objDeck = document.createElement("div");
        objDeck.id = this.deckId;
        document.body.appendChild(objDeck);
        console.log("show", GlobalDialog.dialogs);
        objDeck.style.filter = "alpha(opacity=50)";
        objDeck.style.opacity = 50 / 100;
        objDeck.style.MozOpacity = 50 / 100;
        $(objDeck).css("top", document.body.scrollTop);
        $(objDeck).css("height", "100%");
        $(objDeck).css("width", "100%");
        objDeck.className = "showDeck";
        if (GlobalDialog.dialogs.length > 0)
            var zindex = GlobalDialog.dialogs[GlobalDialog.dialogs.length - 1].zIndex + 10;
        else
            var zindex = Number($(objDeck).css("z-index"));
        $(objDeck).css("z-index", zindex);
        // 显示遮盖的层end
        if (title) {
            var showBox = document.createElement("div");
            var boxId = Tools.guid();
            this.boxId = boxId;
            showBox.id = boxId;
            showBox.innerHTML = `<div class="loaderDiv"><div class="loader loader-1">
            <div class="loader-outter"></div>
            <div class="loader-inner"></div>
            </div></div>${title}`;
            showBox.className = 'mask-loading ' + cls;
            // $(showBox).css("height", "10%");
            // $(showBox).css("width", "10%");
            $(showBox).css("z-index", zindex + 1);
            document.body.appendChild(showBox);
        }
        $(document.body).css({
            "overflow-x": "hidden",
            "overflow-y": "hidden"
        });

    };

    this.unMask = function () {
        $("#" + this.deckId).remove();
        $("#" + this.boxId).remove();
        $(document.body).css({
            "overflow-x": this.oldOverflowX,
            "overflow-y": this.oldOverflowY
        });
    };

    // 关闭详情窗口
    this.close = function () {
        $("#" + this.id).remove();
        $("#" + this.deckId).remove();
        if (!Tools.isNull(this.ondestory))
            this.ondestory(this.userparam, this.id);
        $(document.body).css({
            "overflow-x": this.oldOverflowX,
            "overflow-y": this.oldOverflowY
        });
    };

    // 使窗口居中
    this.adjustLocation = function () {
        var obox = document.getElementById(this.id);
        if (obox != null && obox.style.display != "none") {
            var w = $(obox).width();
            var h = $(obox).height();
            var oLeft, oTop;

            if (parent.window.innerWidth) {
                oLeft = parent.window.pageXOffset + (parent.window.innerWidth - w) / 2 + "px";
                oTop = parent.window.pageYOffset + (parent.window.innerHeight - h) / 2 + "px";
            } else {
                var dde = document.documentElement;
                oLeft = dde.scrollLeft + (dde.offsetWidth - w) / 2 + "px";
                oTop = dde.scrollTop + (dde.offsetHeight - h) / 2 + "px";
            }

            obox.style.left = oLeft;
            obox.style.top = oTop;
        }
    };
};