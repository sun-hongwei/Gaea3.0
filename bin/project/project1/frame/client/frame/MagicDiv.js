function magicSetOnClick(div, name, callback) {
    if (Tools.isNull(div))
        var control = mini.getByName(name);
    else
        var control = mini.getByName(name, div);
    if (Tools.isNull(control)) {
        if (Tools.isNull(div))
            $("[name='" + name + "']").click(function (e) {
                callback(e.currentTarget); //"defaultClickEvent('${control}')";这种写法需要在html中定义
            });
        else
            $(div).find("[name='" + name + "']").click(function (e) {
                callback(e.currentTarget); //"defaultClickEvent('${control}')";这种写法需要在html中定义
            });
    } else {
        control.on("click", callback);
    }
}

function magicSetOnKeyup(callback) {
    if (document.addEventListener) {
        document.addEventListener("keyup", callback, true);
    } else {
        document.attachEvent("onkeyup", fnKeyup);
    }
}

function defaultValueChangedEvent(e) {
    var data;
    var uiData;
    var doEvent;
    var magicDiv;

    if (Tools.isNull(e.sender)) {
        controldata = e.options["userdata"];
        uiData = e.options["uidata"];
        magicDiv = e.options["magicdiv"];
    } else {
        controldata = e.sender.options["userdata"];
        uiData = e.sender.options["uidata"];
        magicDiv = e.sender.options["magicdiv"];
    }

    if (!Tools.isNull(magicDiv.Event.OnValueChanged)) {
        magicDiv.Event.OnValueChanged(magicDiv, controldata, uiData)
    }
}

function defaultClickEvent(e) {
    var data;
    var uiData;
    var doEvent;
    var magicDiv;

    if (Tools.isNull(e.sender)) {
        data = e.options["userdata"];
        uiData = e.options["uidata"];
        doEvent = e.options["proc"];
        magicDiv = e.options["magicdiv"];
    } else {
        data = e.sender.options["userdata"];
        uiData = e.sender.options["uidata"];
        doEvent = e.sender.options["proc"];
        magicDiv = e.sender.options["magicdiv"];
    }

    if (!Tools.isNull(data.data.needdisabled) && data.data.needdisabled)
        return;

    var needRunFlowDo = true;

    var jumpParams = magicDiv.getJumpParams();

    try {
        if (data.data.validateForm) {
            if (!Tools.validateForm(uiData)) {
                alert("您当前窗口有输入项目填写错误！");
                return;
            }
        }

        var allowId = data.data.jumpID;
        if (Tools.isNull(data.data.jumpID))
            allowId = data.data.attachID;
        if (!Tools.isNull(allowId)){
            var allow = Tools.dynamicCallFunctionByName(uiData.workflow.name + "_allowJump", [uiData, data, allowId]);
            if (!Tools.isNull(allow) && !allow){
                return;
            }
        }
        
        if (Tools.isNull(data.data.jumpID)) {
            if (!Tools.isNull(magicDiv.Event.onJumped)) {
                magicDiv.Event.onJumped(e, uiData, uiData.workflow.title, jumpParams);
                return;
            }
        }

        var finalEvent = Tools.isNull(magicDiv.Event.onJumped) ? GlobalFrameManger.Event.onInitUI : magicDiv.Event.onJumped;
        for (i = 0; i < uiData.output.length; i++) {
            input = uiData.output[i];
            if (input.id == data.data.jumpID) {
                if (!Tools.isNull(magicDiv.Event.onClick)) {
                    if (!magicDiv.Event.onClick(e, uiData, input.id, input.title, jumpParams))
                        return;
                }
                doEvent(input.title, input.name, finalEvent, jumpParams);
                return;
            }
        }

        if (!Tools.isNull(data.data.attachID)) {
            if (!Tools.isNull(magicDiv.Event.onClick)) {
                if (!magicDiv.Event.onClick(e, uiData, data.data.attachID, uiData.workflow.title, jumpParams))
                    return;
            }
            var title = Tools.isNull(data.data.title) ? null : data.data.title;
            if (Tools.isNull(title)) {
                var uiInfo = globalGetUIInfo(data.data.attachID);
                title = uiInfo.workflow.title;
            }
            doEvent(title, data.data.attachID, finalEvent, jumpParams);
            return;
        }

        if (!Tools.isNull(magicDiv.Event.onClick)) {
            magicDiv.Event.onClick(e, uiData, "", uiData.workflow.title, jumpParams);
        } else if (!(Tools.isNull(data.data.scrollControl))) {
            magicDiv.scroll();
            needRunFlowDo = false;
        }

    } catch (e) {
        console.log(e);
        needRunFlowDo = false;
        alert(e);
    } finally {
        if (!needRunFlowDo)
            return;

        magicDiv.fireDataSourceAction();
        magicDiv.saveDataSourceAction(data.data.saveRowType, data.data.forceRowType);
        magicDiv.fireRunflowAction();
        magicDiv.fireCloseDialogAction();
    }
}

function MagicDiv() {
    this.Event = {
        onClick: undefined, //onClick(magicDiv, uiData, jumpid, jumptitle)
        onJumped: undefined, //onJumped(magicDiv, uiData, title)
        OnValueChanged: undefined,
    }

    this.id = undefined;
    this.name = undefined;
    this.uiData = undefined;
    this.controlCommon = new ControlCommon();
    this.control = undefined;
    this.div = undefined;
    this.parent = undefined;
    this.header = undefined;
    this.isMini = false;

    this.genUIParams = function () {
        var uiData = this.uiData;
        var dsParams = {};
        for (var index = 0; index < uiData.data.length; index++) {
            var header = uiData.data[index].data;
            if (Tools.isNull(header.dataSourceParams))
                continue;

            var control = getFrameControlByName(uiData, header.name);
            for (var j = 0; j < header.dataSourceParams.length; j++) {
                var element = header.dataSourceParams[j];
                var dsid = element.dsId;
                var params;
                if (Tools.isNull(dsParams[dsid])) {
                    params = {};
                    dsParams[dsid] = params;
                } else {
                    params = dsParams[dsid];
                }

                var value = getFrameControlValue(control);
                params[element.param] = Tools.isNull(value) ? element.default : value;
            }
        }

        return dsParams;
    }

    this.getJumpParams = function () {
        var uiData = this.uiData;
        var result = {};
        for (var index = 0; index < uiData.data.length; index++) {
            var header = uiData.data[index].data;
            if (Tools.isNull(header.outputId))
                continue;

            var control = getFrameControlByName(uiData, header.name);
            var value = getFrameControlValue(control);

            result[header.outputId] = value;
        }

        return result;
    }

    this.genUISaveData = function (rowState, forceRowState) {
        var uiData = this.uiData;
        var result = {};
        var state = uiData.workflow.form_data_source_row_state;
        if (Tools.isNull(state))
            state = Tools.isNull(rowState) ? "added" : rowState;

        for (var index = 0; index < uiData.data.length; index++) {
            var header = uiData.data[index].data;
            var dsid = header.dataSource;
            if (Tools.isNull(dsid) && Tools.isNull())
                continue;

            var control = getFrameControlByName(uiData, header.name);
            var value = getFrameControlValue(control);
            if (Tools.isNull(header.field)) {
                if (forceRowState) {
                    for (var index1 = 0; index1 < value.length; index1++) {
                        var element = value[index1];
                        element["_state"] = state;
                    }
                }
                result[dsid] = value;
            } else {
                var values = {};
                if (Tools.isNull(result[dsid])) {
                    values["_state"] = state;
                    result[dsid] = values;
                } else {
                    values = result[dsid];
                }

                values[header.field] = Tools.isObject(value) ? JSON.stringify(value) : value;
            }
        }

        return result;
    }

    this.fireDataSourceAction = function () {
        var uiData = this.uiData;
        var data = this.header;

        if (!Tools.isNull(data.fireDataSource)) {
            var dsParams = this.genUIParams();

            for (var index = 0; index < data.fireDataSource.length; index++) {
                var element = data.fireDataSource[index];
                var control = getFrameControlByName(uiData, element.name);
                resetDataSourceControl(control, dsParams);
            }
        }
    }

    this.getPopupControl = function (div, name) {
        var control = mini.getByName(name, div);
        if (Tools.isNull(control))
            control = $(div).find("[name='" + name + "']")[0];
        return control;
    }

    this.genCloseDialogData = function (div, name) {
        var uiData = this.uiData;
        for (var index = 0; index < uiData.data.length; index++) {
            var header = uiData.data[index].data;
            if (header.name != name)
                continue;

            var control = this.getPopupControl(div, header.name);
            if (header.typename == this.controlCommon.Grid_Name) {
                return undefined;
                // var grid = control;
                // var value = grid.getSelected();
                // if (!Tools.isNull(value)) {
                //     var valueField = header.selectIdField;
                //     var textField = header.selectTextField;
                //     return {
                //         id: value[valueField],
                //         text: value[textField]
                //     }
                // } else {
                //     return {};
                // }
            } else {
                var value = getFrameControlValue(control);
                return {
                    id: value,
                    text: value
                };
            }
        }
    }

    this.fireCloseDialogAction = function () {
        var uiData = this.uiData;
        var data = this.header;

        if (!Tools.isNull(data.closeDialogId)) {
            var info = uiData.nameHash[data.closeDialogId];
            var control = getFrameControlByName(uiData, data.closeDialogId);
            var div = control.getPopup().getEl();
            var selectData = this.genCloseDialogData(div, info.element.data.grid);
            if (!Tools.isNull(selectData))
                setFrameControlValue(control, selectData);
            control.hidePopup();
        }
    }

    this.saveDataSourceAction = function (rowState, forceRowState) {
        var uiData = this.uiData;
        var data = this.header;

        if (!Tools.isNull(data.saveDataSource)) {
            var saveData = this.genUISaveData(rowState, forceRowState);

            for (var index = 0; index < data.saveDataSource.length; index++) {
                var dsid = data.saveDataSource[index];
                if (!Tools.isNull(saveData[dsid])) {
                    GlobalDataSources.saveData(dsid, saveData[dsid], function (isok) {
                        if (isok) {
                            alert("保存成功！");
                        } else {
                            alert("保存失败，请检查您的网络配置及数据输入是否合法！")
                        }
                    });
                }
            }
        }
    }

    this.fireRunflowAction = function () {
        var uiData = this.uiData;
        var data = this.header;

        if (Tools.isNull(data.decideValue) || Tools.isNull(uiData.workflow.runTaskName))
            return;
        var groups = GlobalSessionObject.getGroupRole();
        var decides = JSON.parse(data.decideValue);
        for (var roleid in decides) {
            if (!Tools.isNull(groups[roleid])) {
                var state = decides[roleid];
                var uiName = uiData.workflow.name;
                var taskInfo = Tools.dynamicCallFunctionByName(uiName + "_getRunFlowTaskInfo", [uiData, data, state]);
                if (Tools.isNull(taskInfo) || Tools.isNull(taskInfo.taskid)) {
                    var control = getFrameRunFlowDataGrid(uiData);
                    if (Tools.isNull(control))
                        return;
                    var dgc = getFrameControl(control);
                    if (Tools.isNull(dgc))
                        return;

                    var dg = new DataGrid(dgc.header.name, uiData);
                    var row = dg.getRow();
                    if (Tools.isNull(row)) {
                        alert("请先选择一个任务！");
                        return;
                    }

                    taskInfo = {
                        memo: "",
                        taskid: row["taskid"],
                    }
                }

                var memo = Tools.isNull(taskInfo.memo) ? "auto action" : taskInfo.memo;
                var scheduler = new Scheduler();
                var taskid = taskInfo.taskid;
                var userid = GlobalSessionObject.getUserId();

                var info = scheduler.action(userid, uiName, taskid,
                    uiData.workflow.runTaskName.key, state, memo, GlobalSessionObject.getGroupRole());
                if (info.ret) {
                    control = getFrameControlByName(uiData, "task");
                    setFrameControlValue(control, []);
                    Tools.dynamicCallFunctionByName(uiName + "_actionExecuted", [uiData, data, info.data]);
                } else
                    alert("装载任务失败：" + info.data);
            }

        }

    }

    this.getHtmlControl = function () {
        return getFrameControlByParent(this.name, this.id, this.parent);
    };

    this.getValue = function (value, defaultValue) {
        if (Tools.isNull(value))
            if (Tools.isNull(defaultValue))
                return "";
            else
                return defaultValue
        else
            return value;
    }

    this.setField = function (fieldname, value, defaultValue) {
        return (Tools.isNull(value) ? (defaultValue == undefined ? '' : ' ' + fieldname + '="' + defaultValue + '"') : ' ' + fieldname + '="' + value + '"');
    }

    this.getDivElement = function (header) {
        return $(header.tabParent).find("#" + header.id + "div")[0]
    }

    this.getControlValue = function (column, row, isText) {
        var control = this.getHtmlControl();
        switch (control.options["userdata"].data.type) {
            case this.controlCommon.DATE_TYPE:
            case this.controlCommon.TIME_TYPE:
            case this.controlCommon.CHECKBOX_TYPE:
            case this.controlCommon.INT_TYPE:
            case this.controlCommon.PASSWORD_TYPE:
            case this.controlCommon.TEXTAREA_TYPE:
            case this.controlCommon.LISTBOX_TYPE:
            case this.controlCommon.RADIOBUTTONS_TYPE:
            case this.controlCommon.TEXTBOX_TYPE:
            case this.controlCommon.TREE_TYPE:
            case this.controlCommon.COMBOBOX_TYPE:
                return control.getValue();
            case this.controlCommon.LABEL_TYPE:
                if (Tools.isNull(control.options.label) || control.options.label.isLabel)
                    return control.innerText;
                else
                    return control.options.label.getValue();
            case this.controlCommon.IMAGE_TYPE:
                return control.src;
            case this.controlCommon.GRID_TYPE: {
                if (!Tools.isNull(row) && !Tools.isNull(column)) {
                    var dg = new DataGrid(control.name, this.uiData);
                    return dg.getValue(column, row);
                } else if (!Tools.isNull(row)) {
                    var dg = new DataGrid(control.name, this.uiData);
                    return dg.getValue(undefined, row);
                } else if (!Tools.isNull(column)) {
                    var dg = new DataGrid(control.name, this.uiData);
                    return dg.getColumnValues();
                } else
                    return control.getData();
            }
            case this.controlCommon.BUTTON_TYPE:
            case this.controlCommon.SCROLLBAR_TYPE:
                return undefined;
            case this.controlCommon.ListView_Type:
                var listview = control.options.listview;
                return listview.getCellValue(row, column, isText);
            case this.controlCommon.REPORT_TYPE:
                var report = control.report;
                var control = report.getCellDivByName(column);
                return report.getCellValue(control, report.nameItems[column].editor.type);
        }
        return undefined;
    }

    this.setControlValue = function (value, column, row, isText) {
        var control = this.getHtmlControl();
        switch (control.options["userdata"].data.type) {
            case this.controlCommon.DATE_TYPE:
            case this.controlCommon.TIME_TYPE:
            case this.controlCommon.CHECKBOX_TYPE:
            case this.controlCommon.INT_TYPE:
            case this.controlCommon.PASSWORD_TYPE:
            case this.controlCommon.TEXTAREA_TYPE:
            case this.controlCommon.LISTBOX_TYPE:
            case this.controlCommon.RADIOBUTTONS_TYPE:
            case this.controlCommon.TEXTBOX_TYPE:
            case this.controlCommon.TREE_TYPE:
            case this.controlCommon.COMBOBOX_TYPE:
                control.setValue(value);
                break;
            case this.controlCommon.LABEL_TYPE:
                if (Tools.isNull(control.options.label) || control.options.label.isLabel)
                    $(control).text(value);
                else
                    control.options.label.setValue(value);
                break;
            case this.controlCommon.IMAGE_TYPE:
                control.src = value;
                break;
            case this.controlCommon.GRID_TYPE: {
                if (!Tools.isNull(row) && !Tools.isNull(column)) {
                    var dg = new DataGrid(control.name, this.uiData);
                    dg.setValue(column, row, value);
                } else if (!Tools.isNull(row)) {
                    var dg = new DataGrid(control.name, this.uiData);
                    dg.setValue(undefined, row, value);
                } else if (!Tools.isNull(column)) {
                    var dg = new DataGrid(control.name, this.uiData);
                    dg.setColumnValues(column, value);
                } else
                    control.setData(value);
                break;
            }
            case this.controlCommon.BUTTON_TYPE:
            case this.controlCommon.SCROLLBAR_TYPE:
                return;
            case this.controlCommon.ListView_Type:
                var listview = control.options.listview;
                if (Tools.isNull(row) && Tools.isNull(column)) {
                    listview.setData(value);
                } else if (!Tools.isNull(row) && !Tools.isNull(column)) {
                    listview.setValue(row, column, value, isText);
                } else if (!Tools.isNull(row)) {
                    listview.setRowData(value, row);
                }
                break;
            case this.controlCommon.REPORT_TYPE:
                var report = control.report;
                if (!Tools.isNull(column)) {
                    var control = report.getCellDivByName(column);
                    report.setCellValue(control, value, report.nameItems[column].editor.type);
                    break;
                } else {
                    report.setValue(value);
                }
        }
    }

    this.setCommonAttr = function (header, styleClass, enabled) {
        var enabledVar = header.enabled;
        if (!Tools.isNull(enabled)) {
            enabledVar = enabled ? undefined : "false";
        }

        var str = this.setField('id', header.id) +
            this.setField('name', header.name) +
            this.setField('typecode', header.type) +
            this.setField('typename', header.typename) +
            this.setField('required', header.required) +
            this.setField('enabled', enabledVar) +
            this.setField('class', Tools.insertString(" ", header.styleClass, styleClass));

        return str;
    }

    this.setTextEditorCommonAttr = function (header, divWidth, divHeight, styleClass, setSize, enabled) {
        var readonlyVar = header.readonly;
        if (!Tools.isNull(readonlyVar) && readonlyVar) {
            readonlyVar = "true";
        } else
            readonlyVar = undefined;


        var str = this.setCommonAttr(header, styleClass, enabled) +
            this.setField('readonly', readonlyVar) +
            this.setField('value', header.value) +
            this.setField("emptyText", header.emptyText);
        if (!Tools.isNull(setSize) && setSize) {
            this.setField('width', Tools.convetPX(divWidth, header.width)) +
                this.setField('height', Tools.convetPX(divHeight, header.height));
        }
        return str;
    }

    this.scroll = function () {
        var div = GlobalFrameManger.getCurTabLayout();
        var main;
        if (Tools.isNull(div)) {
            main = $("#" + this.uiData.workflow.id + "_layout");
        } else {
            main = $(div);
        }
        var top = main.scrollTop();


        var scrollToContainer = getFrameControlByName(this.uiData, this.header.scrollControl);
        var id = scrollToContainer.options.magicdiv.header.id;

        var parent = this.header.scrollDiv;

        var selector;
        if (scrollToContainer.options.userdata.data.type != this.controlCommon.Div_Type)
            selector = $("#" + id + "div")[0];
        else
            selector = $("#" + id)[0];
        selector.scrollIntoView();
        if (!Tools.isNull(parent)) {
            main.scrollTop(top);
        }
    }

    this.createColumn = function (dw, divWidth, divHeight, header, uiInfo, parent, needDiv) {
        var str = '';
        if (!Tools.isNull(parent)) {
            if (Tools.isNull(header.parent))
                header.tabParent = parent;
        }

        var fontstyle = "";
        switch (header.fontStyle) {
            case 2:
                fontstyle = "italic";
                break;
            case 3:
                fontstyle = "oblique";
                break;
            default:
            case 0:
                fontstyle = "normal";
                break;

        }

        var borderStyle = header.border ? "" : "border:0px";

        var fontweight = header.fontStyle == "1" || header.fontStyle == "3" ? "bold" : "normal";

        var styleHeight = Tools.convetPX(divHeight, header.height);
        var styleWidth = Tools.convetPX(divWidth, header.width);
        var textFontHeight = "line-height:" + styleHeight;
        var fontsize = Tools.convetFontSize(header.fontSize);
        var textcolor = Tools.convetColor(header.textColor);

        var textFontStyleNoHeightColor = ";font-family:'" + header.fontName + "';font-style:" +
            fontstyle + ";font-weight:" + fontweight + ";font-size:" + fontsize;

        var textFontStyleNoHeight = textFontStyleNoHeightColor +
            ";color:" + textcolor;
        var textFontStyle = textFontHeight + textFontStyleNoHeight;

        var left = Tools.convetPXToValue(divWidth, header.left, 0);
        var top = Tools.convetPXToValue(divHeight, header.top, 0);
        var right = Tools.convetPXToValue(divWidth, header.right, 0);
        var bottom = Tools.convetPXToValue(divHeight, header.bottom, 0);

        if (!Tools.isNull(header.right)) {
            left = divWidth - right;
        }

        if (!Tools.isNull(header.bottom)) {
            top = divHeight - bottom;
        }

        var heightStyle = "";
        var inputStyle = ";height:" + styleHeight;
        switch (header.type) {
            case this.controlCommon.DATE_TYPE: {
                if (header.viewType == "日历") {
                    heightStyle = ";height:" + styleHeight;
                    break;
                }

            }
            case this.controlCommon.TEXTBOX_TYPE:
            case this.controlCommon.TIME_TYPE:
            case this.controlCommon.COMBOBOXTREE_TYPE:
            case this.controlCommon.COMBOBOX_TYPE:
            case this.controlCommon.BUTTON_TYPE:
                inputStyle += ";" + textFontStyle;
                borderStyle += ";height:" + styleHeight;
                break;
            default:
                heightStyle = ";height:" + styleHeight;
                break;
        }
        var style = "position:absolute;left:" + left + "px;top:" + top + "px;width:" +
            styleWidth + heightStyle + ";z-index:" + header.zOrder;

        switch (header.type) {
            case this.controlCommon.IMAGE_TYPE:
                if (header.scaleMode == "smCenterInRectangle")
                    style += ";text-align:center";
                break;

        }

        if (!Tools.isNull(header.backgroundColor)) {
            style += ";background:" + Tools.convetColor(header.backgroundColor);
        }

        var divStr = "<div" +
            this.setField("id", header.id + "div") +
            this.setField("class", header.divClass) +
            this.setField("style", style + ";border:0px") +
            ">";

        switch (header.type) {
            case this.controlCommon.Div_Type:
                divStr = "";
                break;
        }
        var url = undefined;
        if (!Tools.isNull(header.codeTableName)) {
            url = (Tools.isNull(header.codeTableName) ? undefined : ("./Services/CodeServices.php?code=" + Tools.EncodeUrl(header.codeTableName)));
        }
        if (Tools.isNull(url) && !Tools.isNull(header.url))
            url = Tools.EncodeUrl(header.url);

        var labelControl = undefined;

        switch (header.type) {
            case this.controlCommon.Div_Type:
                needDiv = false;
                var dc = new DivControl();
                str = dc.getHtml(header, style);
                break;
            case this.controlCommon.Sub_Type:
                str = '<div ' +
                    this.setCommonAttr(header) +
                    this.setField('style', Tools.addString(header.style, ";width:100%;height:100%;" + (header.border ? "border:1px solid #d3d3d3;" : "border:0px;") +
                        (header.showScrollbar ? "overflow:auto" : "overflow:hidden"))) +
                    '>' +
                    this.getValue(header.title) +
                    '</div>';
                dw.write(header, divStr + str + "</div>");
                var div = $(parent).find("#" + header.id)[0];
                if (!Tools.isNull(header.uiInfo))
                    createUIForName(header.uiInfo, div, false, header.transparent);

                if (Tools.isNull(div.options))
                    div.options = {};
                div.options["magicdiv"] = this;
                this.control = div;

                return;
            case this.controlCommon.MainTree_Type:
                dw.write(header, divStr + "</div>");
                var outlookTree = new TreeControl();
                outlookTree.Event.OnGetData = function (tree, id, nodeid) {
                    return Tools.getMainTreeInfo();
                };

                outlookTree.Event.OnNodeClick = function (tc, tree, node, isLeaf) {
                    if (!Tools.isNull(GlobalFrameManger.Event.onTreeNodeClick))
                        GlobalFrameManger.Event.onTreeNodeClick(tc, tree, node);
                    if (!Tools.isNull(node.jumpid)) {
                        jumpUIForName(node.text, node.jumpid)
                    }
                };

                header.tabParent = this.getDivElement(header);
                header.class = Tools.insertString(" ", header.styleClass, "mini-outlooktree");
                header.idField = "id";
                header.textField = "name";
                header.parentField = "pid";
                outlookTree.write(dw, header, divWidth, divHeight);
                outlookTree.load();

                if (Tools.isNull(outlookTree.options))
                    outlookTree.options = {};
                outlookTree.options["magicdiv"] = this;
                this.control = outlookTree;

                return;
            case this.controlCommon.MainMenu_Type:
                dw.write(header, divStr + "</div>");
                var parentLayout = this.getDivElement(header);
                delete header.parent;
                delete header.tabParent;

                var menuIteminfo = Tools.getMainMenuInfo();
                var menudata = {
                    parentLayout: parentLayout,
                    id: "mainmenu",
                    idField: "id",
                    textField: "text",
                    parentField: "pid",
                    border: header.border,
                    "class": header.styleClass,
                    menuitems: menuIteminfo
                };
                var menu = new MenuControl();
                header.id = menudata.id;
                menudata.parentLayout.id = header.id + "div";
                menu.write(dw, menudata);
                menu.Event.OnItemClick = function (mc, menu, item, isLeaf) {
                    var hashMenuData = menu.options["menuitemdata"];
                    var menudata = hashMenuData[item.id];
                    if (!Tools.isNull(GlobalFrameManger.Event.onMenuClick)) {
                        if (!GlobalFrameManger.Event.onMenuClick(menudata.jumpid, menudata))
                            return;
                    }
                    if (!Tools.isNull(menudata.jumpid))
                        jumpUIForName(undefined, menudata.jumpid, GlobalFrameManger.Event.onInitUI);
                };

                if (Tools.isNull(menu.options))
                    menu.options = {};
                menu.options["magicdiv"] = this;
                this.control = menu;

                return;
            case this.controlCommon.Toolbar_Type:
                var control = new ToolBarControl();
                dw.write(header, divStr + "</div>");

                if (Tools.isNull(header.parent))
                    header.tabParent = this.getDivElement(header);

                control.load(dw, header, uiInfo);

                if (Tools.isNull(control.options))
                    control.options = {};
                control.options["magicdiv"] = this;
                this.control = control;
                return;
            case this.controlCommon.ListView_Type:
                var control = new ListViewControl();
                dw.write(header, divStr + "</div>");

                if (Tools.isNull(header.parent))
                    header.tabParent = this.getDivElement(header);

                control.write(dw, uiInfo, header, divWidth, divHeight);

                if (Tools.isNull(control.options))
                    control.options = {};
                control.options["magicdiv"] = this;
                this.control = control;

                return;
            case this.controlCommon.Upload_Type:
                var control = new UploadControl();
                dw.write(header, divStr + "</div>");

                if (Tools.isNull(header.parent))
                    header.tabParent = this.getDivElement(header);

                control.write(dw, header, divWidth, divHeight);
                if (Tools.isNull(control.options))
                    control.options = {};
                control.options["magicdiv"] = this;
                this.control = control;
                return;
            case this.controlCommon.REPORT_TYPE:
                var control = new ReportControlEx();
                dw.write(header, divStr + "</div>");

                if (Tools.isNull(header.parent))
                    header.tabParent = this.getDivElement(header);

                control.write(dw, header, uiInfo, divWidth, divHeight);
                if (Tools.isNull(control.options))
                    control.options = {};
                control.options["magicdiv"] = this;
                this.control = control;
                return;
            case this.controlCommon.CHECKBOX_TYPE:
                str = '<input ' +
                    this.setTextEditorCommonAttr(header, divWidth, divHeight, "mini-checkbox", false) +
                    this.setField('style', Tools.addString(header.style, ";width:100%;height:100%;" + textFontStyle)) +
                    this.setField('trueValue', header.trueValue, 1) +
                    this.setField('falseValue', header.falseValue, 0) +
                    this.setField('value', header.value) +
                    '>' +
                    this.getValue(header.title) +
                    '</input>';
                break;
            case this.controlCommon.LABEL_TYPE:
                var control = new LabelControl();
                dw.write(header, divStr + "</div>");

                if (Tools.isNull(header.parent))
                    header.tabParent = this.getDivElement(header);

                control.write(dw, header, uiInfo, styleWidth, styleHeight, textFontStyle, fontstyle, fontsize, textcolor, fontweight);
                if (Tools.isNull(control.options))
                    control.options = {};
                control.options["magicdiv"] = this;
                this.control = control;
                return;
            case this.controlCommon.ProgressBar_Type:
                str = "<progress" +
                    this.setCommonAttr(header, undefined, false) +
                    this.setField('style', Tools.addString(header.style, ";width:100%;height:100%;")) +
                    this.setField('value', header.start) +
                    this.setField('max', header.size) +
                    ">" +
                    header.value + "</progress>";
                break;
            case this.controlCommon.DATE_TYPE:
                var control = new DateControl();
                dw.write(header, divStr + "</div>");

                if (Tools.isNull(header.parent))
                    header.tabParent = this.getDivElement(header);

                control.write(dw, header, uiInfo, divWidth, divHeight, inputStyle, borderStyle, textFontStyle);
                if (Tools.isNull(control.options))
                    control.options = {};
                control.options["magicdiv"] = this;
                this.control = control;

                return;
            case this.controlCommon.TIME_TYPE:
                str = '<input' +
                    this.setTextEditorCommonAttr(header, divWidth, divHeight, "mini-timespinner", false) +
                    this.setField('style', Tools.addString(header.style, ";width:100%;height:100%;" + textFontStyle)) +
                    this.setField('format', header.format, "HH:mm") +
                    this.setField('inputStyle', inputStyle) +
                    this.setField('borderStyle', borderStyle) + '>' +
                    // this.getValue(header.value, header.title) +
                    '</input>';
                break;
            case this.controlCommon.COMBOBOX_TYPE:
                var classname = (Tools.isNull(header.mode) || header.mode == "combobox") ? "mini-combobox" : "mini-lookup";
                if (classname == "mini-lookup") {
                    str = '<input' +
                        this.setCommonAttr(header, classname) +
                        this.setField('style', Tools.addString(header.style, ";width:100%;height:100%;" + textFontStyle)) +
                        this.setField('multiSelect', header.multiSelect) +
                        this.setField('valueField', header.valueField) +
                        this.setField('textField', header.textField) +
                        this.setField('inputStyle', inputStyle) +
                        this.setField('borderStyle', borderStyle);
                    // this.setField('popupWidth', "auto");
                } else {
                    str = '<input' +
                        this.setTextEditorCommonAttr(header, divWidth, divHeight,
                            classname, false) +
                        this.setField('url', url) +
                        this.setField('style', Tools.addString(header.style, ";width:100%;height:100%;" + textFontStyle)) +
                        this.setField('allowInput', header.allowEdit, "false") +
                        this.setField('valueFromSelect', header.valueFromSelect, "false") +
                        this.setField('showNullItem', header.showNullItem, "true") +
                        this.setField('pinyinField', header.pinyinField) +
                        this.setField('valueField', header.valueField) +
                        this.setField('textField', header.textField) +
                        this.setField('multiSelect', header.multiSelect) +
                        this.setField('inputStyle', inputStyle) +
                        this.setField('borderStyle', borderStyle);
                }

                str += '/>';
                break;
            case this.controlCommon.COMBOBOXTREE_TYPE:
                str = '<input' +
                    this.setTextEditorCommonAttr(header, divWidth, divHeight, "mini-treeselect", false) +
                    this.setField('url', url) +
                    this.setField('style', Tools.addString(header.style, ";width:100%;height:100%;" + textFontStyle)) +
                    this.setField('showRadioButton', header.showRadioButton, "true") +
                    this.setField('showFolderCheckBox', header.showFolderCheckBox, "false") +
                    this.setField('allowInput', header.allowEdit, "false") +
                    this.setField('virtualScroll', header.virtualScroll, "false") +
                    this.setField('pinyinField', header.pinyinField) +
                    this.setField('showTreeIcon', header.showTreeIcon, "true") +
                    this.setField('showTreeLines', header.showTreeLines, "true") +
                    this.setField('autoCheckParent', header.autoCheckParent, "false") +
                    this.setField('expandOnLoad', header.expandOnLoad, "true") +
                    this.setField('valueFromSelect', header.valueFromSelect, "true") +
                    this.setField('parentField', Tools.isNull(header.codeTableName) ? header.parentField : "PCode") +
                    this.setField('valueField', Tools.isNull(header.codeTableName) ? header.valueField : "Code") +
                    this.setField('textField', Tools.isNull(header.codeTableName) ? header.textField : "CodeMean") +
                    this.setField('multiSelect', header.multiSelect) +
                    this.setField('inputStyle', inputStyle) +
                    this.setField('borderStyle', borderStyle) + '/>';
                break;
            case this.controlCommon.IMAGE_TYPE:
                var imagestyle = "width:100%;height:100%";
                switch (header.scaleMode) {
                    case "smHeight":
                        imagestyle = "height:100%;";
                        break;
                    case "smWidth":
                        imagestyle = "width:100%;";
                        break;
                    case "smCenterInRectangle":
                        imagestyle = "max-width: 100%;max-height: 100%;";
                        break;
                }

                str = '<img' +
                    this.setCommonAttr(header) +
                    this.setField('style', Tools.addString(header.style, imagestyle)) +
                    this.setField('src', header.value) +
                    this.setField('alt', header.alt, "下载中。。。") +
                    // this.setField('width', Tools.convetPX(divWidth, header.width)) +
                    // this.setField('height', Tools.convetPX(divHeight, header.height)) +
                    '/>';
                if ((Tools.isNull(header.needdisabled) || !header.needdisabled) && !Tools.isNull(header.isHref) && header.isHref) {
                    if (!Tools.isNull(header.isHref) && header.isHref) {
                        if (!Tools.isNull(header.href)) {
                            str = "<a" +
                                // this.setField("style", textFontStyle) +
                                this.setField('href', header.href) +
                                this.setField('download', header.download) +
                                this.setField('target', header.target);
                            '>' +
                                str +
                                '</a>';
                        } else {
                            str = "<a" +
                                // this.setField("style", textFontStyle) +
                                this.setField('href', "javascript:void(0);") +
                                '>' +
                                str +
                                '</a>';

                        }
                    }

                }

                break;
            case this.controlCommon.INT_TYPE:
                str = '<input' +
                    this.setTextEditorCommonAttr(header, divWidth, divHeight, "mini-spinner", false) +
                    this.setField('style', Tools.addString(header.style, ";width:100%;height:100%;" + textFontStyle)) +
                    this.setField('minValue', header.minValue, 0) +
                    this.setField('maxValue', header.maxValue, 100) +
                    this.setField('format', header.format) +
                    this.setField('decimalPlaces', header.decimalPlaces) +
                    this.setField('inputStyle', inputStyle) +
                    this.setField('borderStyle', borderStyle) +
                    '>' +
                    '</input>';
                break;
            case this.controlCommon.PASSWORD_TYPE:
                str = '<input' +
                    this.setTextEditorCommonAttr(header, divWidth, divHeight, "mini-password", false) +
                    this.setField('style', Tools.addString(header.style, ";width:100%;height:100%;" + textFontStyle)) +
                    '>' +
                    // this.getValue(header.value, header.title) +
                    '</input>';
                break;
            case this.controlCommon.TEXTAREA_TYPE:
                if (needDiv) {
                    str = '<textarea' +
                        this.setTextEditorCommonAttr(header, divWidth, divHeight, "mini-textarea", false) +
                        this.setField('style', ";width:100%;height:100%;box-sizing:border-box;") +
                        this.setField('inputStyle', Tools.addString(header.style, ";line-height:normal;box-sizing:border-box;" + textFontStyleNoHeight)) +
                        '>' +
                        this.getValue(header.value, header.title) +
                        '</textarea>';
                } else
                    str = '<textarea' +
                        this.setTextEditorCommonAttr(header, divWidth, divHeight, "mini-textarea", false, true) +
                        this.setField('style', ";width:100%;height:100%;box-sizing:border-box;") +
                        this.setField('inputStyle', Tools.addString(header.style, ";line-height:normal;box-sizing:border-box;" + textFontStyleNoHeight)) +
                        '>' +
                        this.getValue(header.value, header.title) +
                        '</textarea>';
                break;
            case this.controlCommon.RADIOBUTTONS_TYPE:
                dw.write(header, divStr + "</div>");
                var control = new RadioButtonListControl();
                if (Tools.isNull(header.parent))
                    header.tabParent = this.getDivElement(header);

                control.write(dw, header, textFontStyle);
                if (Tools.isNull(control.options))
                    control.options = {};
                control.options["magicdiv"] = this;
                this.control = control;
                return control;
            case this.controlCommon.LISTBOX_TYPE:
                str = '<input' +
                    this.setTextEditorCommonAttr(header, divWidth, divHeight, "mini-listbox", false) +
                    this.setField('style', Tools.addString(header.style, ";width:100%;height:100%;" + textFontStyleNoHeight)) +
                    this.setField('showNullItem', header.showNullItem, "false") +
                    this.setField('valueField', header.valueField) +
                    this.setField('textField', header.textField) +
                    '>' +
                    // this.getValue(header.value, header.title) +
                    '</input>';
                break;
            case this.controlCommon.SEPARATOR_TYPE:
                str = '<span  class="separator">' +
                    '</span>';
                break;
            case this.controlCommon.BUTTON_TYPE:
                var classname = (Tools.isNull(header.mode) || header.mode == "button") ? "mini-button" : "mini-buttonedit";
                str = '<a' +
                    this.setCommonAttr(header, classname) +
                    this.setField('style',
                        Tools.addString(header.style, ";" + textFontStyleNoHeightColor +
                            (needDiv ? "" : ";" + this.setField('z-index', header.zOrder)))) +
                    this.setField('iconCls', header.iconCls) +
                    this.setField('img', Tools.isNull(header.img) ? undefined : "image/" + header.img) +
                    this.setField('plain', header.plain) +
                    this.setField('width', Tools.convetPX(divWidth, header.width)) +
                    // this.setField('height', Tools.convetPX(divHeight, header.height)) +
                    // this.setField('borderStyle', borderStyle) +
                    '>' +
                    this.getValue(header.value, header.title) +
                    '</a>';
                break;
            case this.controlCommon.Chart_Type:
                dw.write(header, divStr + "</div>");
                var control = new ChartControl();
                if (Tools.isNull(header.parent))
                    header.tabParent = this.getDivElement(header);

                control.write(dw, header);
                if (Tools.isNull(control.options))
                    control.options = {};
                control.options["magicdiv"] = this;
                this.control = control;
                return control;
            case this.controlCommon.GRID_TYPE:
                dw.write(header, divStr + "</div>");

                dgControl = new DataGridControl();
                if (Tools.isNull(header.parent))
                    header.tabParent = this.getDivElement(header);

                if (!Tools.isNull(header.OnSumCell)) {
                    dgControl.Event.OnSumCell = header.OnSumCell;
                    delete header.OnSumCell;
                }
                if (!Tools.isNull(header.onSelectCompare)) {
                    dgControl.Event.onSelectCompare = header.onSelectCompare;
                    delete header.onSelectCompare;
                }
                if (!Tools.isNull(header.onDeselectCompare)) {
                    dgControl.Event.onDeselectCompare = header.onDeselectCompare;
                    delete header.onDeselectCompare;
                }
                if (!Tools.isNull(header.OnChangeCellStyle)) {
                    dgControl.Event.OnChangeCellStyle = header.OnChangeCellStyle;
                    delete header.OnChangeCellStyle;
                }

                initData = header.initData;
                delete header.initData;

                dgControl.write(dw, uiInfo, header);
                if (!Tools.isNull(initData)) {
                    dgControl.init(initData.hint, initData.command, initData.data);
                }

                if (Tools.isNull(dgControl.options))
                    dgControl.options = {};
                dgControl.options["magicdiv"] = this;
                this.control = dgControl;

                return;
            case this.controlCommon.TREE_TYPE:
                treeControl = new TreeControl();
                dw.write(header, divStr + "</div>");

                dgControl = new DataGridControl();
                if (Tools.isNull(header.parent))
                    header.tabParent = this.getDivElement(header);

                if (!Tools.isNull(header.OnNodeClick)) {
                    treeControl.Event.OnNodeClick = header.OnNodeClick;
                    delete header.OnNodeClick;
                }

                onGetLoadParam = header.OnGetLoadParam;
                inited = header.inited;
                if (!Tools.isNull(header.inited))
                    delete header.inited;

                if (!Tools.isNull(header.OnGetLoadParam)) {
                    treeControl.Event.OnGetLoadParam = header.OnGetLoadParam;
                    delete header.OnGetLoadParam;
                }

                var classname = header.classname;
                header.class = classname;
                delete header.classname;

                treeControl.write(dw, header, divWidth, divHeight);

                header.classname = classname;

                if ((!Tools.isNull(onGetLoadParam) || !Tools.isNull(header.value)) && inited) {
                    if (!Tools.isNull(header.value)) {
                        var treedata = $.parseJSON(header.value);
                        treeControl.loadFromData(treedata);
                    } else
                        treeControl.load();
                }

                if (Tools.isNull(treeControl.options))
                    treeControl.options = {};
                treeControl.options["magicdiv"] = this;
                this.control = treeControl;

                return;
            case this.controlCommon.SCROLLBAR_TYPE:
                scrollControl = new ScrollBarControl();
                dw.write(header, divStr + "</div>");

                if (Tools.isNull(header.parent))
                    header.tabParent = this.getDivElement(header);

                inited = header.inited;
                if (!Tools.isNull(header.inited))
                    delete header.inited;

                if (!Tools.isNull(header.onGetData)) {
                    var code = header.onGetData.replace(/\t/g, " ");
                    var getDatafun = eval("(" + code + ")");
                    header.onGetData = getDatafun;
                }


                scrollControl.write(dw, header);

                if (!Tools.isNull(inited) && inited)
                    $(function () {
                        scrollControl.load();
                    })

                if (Tools.isNull(scrollControl.options))
                    scrollControl.options = {};
                scrollControl.options["magicdiv"] = this;
                this.control = scrollControl;

                return;
            case this.controlCommon.TEXTBOX_TYPE:
            default:
                if (needDiv) {
                    //compare:name;
                    var Vtype = header.vtype
                    if (Vtype) {
                        var vtype = header.vtype.split("compare");
                        var max = header.vtype.split("compareMax");
                        if (max.length > 1) {
                            vtype = max;
                            max = true;
                        }
                        if (vtype.length > 1) {
                            var k = vtype[1].indexOf(";");
                            vtype = vtype[1].substr(1, k - 1);
                            var vtypeJson = {};
                            vtypeJson.compare = vtype;
                            vtypeJson.name = "compare-" + vtype;
                            CustomVTypeExecutor.setSpecialData(vtypeJson);
                        }
                    }
                    str = '<input' +
                        this.setTextEditorCommonAttr(header, divWidth, divHeight, "mini-textbox", false) +
                        this.setField('style', Tools.addString(header.style, ";width:100%;height:100%;" + (needDiv ? textFontStyle : ""))) +
                        this.setField('vtype', header.vtype) +
                        this.setField('inputStyle', inputStyle) +
                        this.setField('borderStyle', borderStyle);
                        if (vtypeJson) {
                        if (max) {
                            str +=this.setField('onvalidation',  "compareMax_fun(e,'"+vtypeJson.name+"')");
                        }else{
                            str +=this.setField('onvalidation',  "compare_fun(e,'"+vtypeJson.name+"')");
                        }}
                    str += '>' +
                        // this.getValue(header.value, header.title) +
                        '</input>';
                } else
                    str = '<input ' +
                        this.setTextEditorCommonAttr(header, divWidth, divHeight, undefined, false) +
                        this.setField('style', ";width:100%;height:100%;" + textFontStyle) +
                        '/>';
                break;
        }

        if (str != undefined) {
            if (needDiv)
                dw.write(header, divStr + str + "</div>");
            else
                dw.write(header, str);

            mini.parse();

            switch (header.type) {
                case this.controlCommon.BUTTON_TYPE:
                    var control = getFrameControlByParent(header.name, header.id, header.tabParent);
                    control.setWidth(styleWidth);
                    var selector = $(control.getEl()).find(".mini-button-inner");
                    var lineHeight = Tools.convetPXToValue(0, styleHeight);
                    // var padtop = selector.css("padding-top").replace("px", "") / 1;
                    // var padbottom = selector.css("padding-bottom").replace("px", "") / 1;
                    // lineHeight -= padtop + padbottom;
                    var lineheightString = {
                        "line-height": lineHeight + "px"
                    };
                    selector.css(lineheightString);
                    $(control.getEl()).find(".mini-button-text").css(lineheightString);

                    break;
                case this.controlCommon.DATE_TYPE:
                    if (header.viewType == "日历")
                        break;
                case this.controlCommon.TIME_TYPE:
                case this.controlCommon.INT_TYPE:
                    var control = getFrameControlByParent(header.name, header.id, header.tabParent);
                    if (!Tools.isNull(control._buttonsEl)) {
                        var selector = $(control._buttonsEl);
                        var parentHeight = Tools.convetPXToValue(divHeight, styleHeight);
                        selector.css("top", (parentHeight - selector.height()) / 2);
                    }
                    break;
                case this.controlCommon.COMBOBOX_TYPE:
                case this.controlCommon.COMBOBOXTREE_TYPE:
                    var control = getFrameControlByParent(header.name, header.id, header.tabParent);
                    if (!Tools.isNull(control._buttonEl)) {
                        $(control._buttonEl).css("height", styleHeight);
                        $(control._buttonEl).height($(control._buttonEl).height() - 5);
                    }
                    if ((!Tools.isNull(control.data) && control.data.length > 0) || Tools.isNull(header.data))
                        return;

                    var data = JSON.parse(header.data);
                    if (header.type == this.controlCommon.COMBOBOX_TYPE) {
                        if (!Tools.isNull(control.setData))
                            control.setData(data);
                    } else
                        control.loadList(data, header.valueField, header.parentField);
                    if (!Tools.isNull(header.value))
                        control.setValue(header.value);
                    break;
                case this.controlCommon.RADIOBUTTONS_TYPE:
                case this.controlCommon.LISTBOX_TYPE:
                    if (!Tools.isNull(header.data)) {
                        try {
                            var control = getFrameControlByParent(header.name, header.id, header.tabParent);
                            var items = $.parseJSON(header.data);
                            control.loadData(items);
                        } catch (e) {

                        }
                    }
                    break;

            }
        }

        var control = getFrameControlByParent(header.name, header.id, header.tabParent);
        if (!Tools.isNull(control)) {
            if (Tools.isNull(control.name)) {
                control.name = header.name;
            }
        }
    }

    this.directGetControlByName = function (dw, header, uiData, typename, needDiv, zOrder, parent, divWidth, divHeight) {
        var typeid = this.controlCommon.convertType(typename);
        header.typename = typename;
        return this.directGetControlByID(dw, header, uiData, typeid, needDiv, zOrder, parent, divWidth, divHeight)
    }

    this.directGetControlByID = function (dw, header, uiData, typeid, needDiv, zOrder, parent, divWidth, divHeight) {
        header.type = typeid;
        header.zOrder = zOrder;
        this.parent = parent;
        var prepareControl = this.createColumn(dw, divWidth, divHeight, header, uiData, parent, needDiv);
        if (header.prepare)
            return prepareControl;
        else
            return getFrameControlByParent(header.name, header.id, this.parent);
    }

    this.relocation = function (replaceMode) {
        if (Tools.isNull(this.header.placeIndex))
            return;

        var placeMode = this.header.placeInType;
        if (Tools.isNull(placeMode) || placeMode == "ptNone")
            return;

        if (Tools.isNull(replaceMode))
            replaceMode = true;

        var placeAlign = [];
        if (!Tools.isNull(this.header.placeAlign)) {
            placeAlign = JSON.parse(this.header.placeAlign);
        }
        if (placeMode == "ptAlign" && Tools.isNull(placeAlign))
            return;

        sorts = {};

        for (i = 0; i < this.uiData.data.length; i++) {
            var header = this.uiData.data[i].data;
            if (header.placeGroup == this.header.placeGroup) {
                if (!Tools.isNull(header.placeIndex)) {
                    sorts[header.placeIndex] = header;
                }
            }
        }

        var keys = Object.keys(sorts);
        keys.sort(function (a, b) {
            return a - b;
        });

        var prevRect = undefined;

        var prevControl;
        for (var index = 0; index < keys.length; index++) {
            var key = keys[index];
            var header = sorts[key];
            var control = getFrameControlByName(this.uiData, header.name);
            if (Tools.isNull(control))
                continue;

            var placeMode = header.placeInType;

            if (key <= this.header.placeIndex) {
                var prev = control.options.div;

                var offset = $(prev).offset();
                prevRect = {
                    left: offset.left,
                    top: offset.top
                };
                prevRect.size = {
                    width: $(prev).width(),
                    height: $(prev).height()
                };
                prevControl = control;
                continue;
            }

            var cur = control.options.div;
            var curpos = $(cur).offset();
            var curRect = {
                left: curpos.left,
                top: curpos.top
            };

            var alignSpaceLeft = 0;
            var alignSpaceTop = 0;
            if (!Tools.isNull(header.alignSpace)) {
                alignSpaceLeft = header.alignSpace;
                alignSpaceTop = header.alignSpace;
            }

            if (!Tools.isNull(header.keepAlignSpace) && header.keepAlignSpace) {
                alignSpaceTop += control.options.y - prevControl.options.y - prevControl.options.height;
                alignSpaceLeft += control.options.x - prevControl.options.x - prevControl.options.width;
            }

            curRect.size = {
                width: $(cur).width(),
                height: $(cur).height()
            };
            switch (placeMode) {
                case "ptReplace":
                    curpos.left = prevRect.left;
                    curpos.top = prevRect.top;
                    break;
                case "ptAlign":
                    var placeAlign = [];
                    if (!Tools.isNull(header.placeAlign)) {
                        placeAlign = JSON.parse(header.placeAlign);
                    }
                    if (Tools.isNull(placeAlign))
                        continue;

                    for (var j = 0; j < placeAlign.length; j++) {
                        var align = placeAlign[j];
                        if (replaceMode) {
                            switch (align) {
                                case "alTop":
                                    curpos.top = prevRect.top + alignSpaceTop;
                                    break;
                                case "alBottom":
                                    curpos.top = prevRect.top + prevRect.size.height + alignSpaceTop;
                                    break;
                                case "alLeft":
                                    curpos.left = prevRect.left + alignSpaceLeft;
                                    break;
                                case "alRight":
                                    curpos.left = prevRect.left + prevRect.size.width + alignSpaceLeft;
                                    break;
                            }
                        } else {
                            switch (align) {
                                case "alTop":
                                case "alBottom":
                                    curpos.top = prevRect.top + prevRect.size.height + alignSpaceTop;
                                    break;
                                case "alLeft":
                                case "alRight":
                                    curpos.left = prevRect.left + prevRect.size.width + alignSpaceLeft;
                                    break;
                            }

                        }
                    }

                    break;
            }
            $(cur).offset({
                left: curpos.left,
                top: curpos.top
            });
            prevRect = curRect;
            if (!replaceMode) {
                prevRect.left = curpos.left;
                prevRect.top = curpos.top;
            }
            prevControl = control;
        }
    }

    this.removeSelf = function () {
        if (Tools.isNull(this.control))
            return;

        this.relocation();
        if (this.isMini) {
            this.control.destroy();
            this.control = undefined;
        } else {
            switch (this.header.type) {
                case this.controlCommon.Div_Type:
                    $(this.div).remove();
                    break;
                default:
                    $(this.control).parent().remove();
                    break;
            }
        }

    }

    this.getControl = function (dw, divWidth, divHeight, data, uiData, parent, userObj, proc) {
        // try{
        var prepareControl = this.directGetControlByName(dw, data.data, uiData, data.typename, true, data.zOrder, parent, divWidth, divHeight)
        if (data.data.prepare)
            return prepareControl;

        var subWidth = 0;
        var subHeight = 0;
        var controlWidth = 0;
        var controlHeight = 0;

        this.id = data.data.id;
        this.name = data.data.name;
        this.uiData = uiData;
        this.header = data.data;
        this.parent = parent;

        // mini.parse();
        var control = mini.getByName(data.data.name, parent);
        var isMiniUI = true;
        if (Tools.isNull(control)) {
            control = $(parent).find("#" + data.data.id)[0];
            isMiniUI = false;
        }

        if (Tools.isNull(control))
            return undefined;

        var resizeControl = control;
        if (isMiniUI) {
            resizeControl = control.getEl();
        }

        controlWidth = $(resizeControl).width();
        controlHeight = $(resizeControl).height();

        switch (data.data.type) {
            case this.controlCommon.BUTTON_TYPE:
                break;
            default: {
                if (controlWidth > 0 && controlHeight > 0) {
                    subWidth = controlWidth - Tools.convetPXToValue(divWidth, data.data.width, 0);
                    subHeight = controlHeight - Tools.convetPXToValue(divHeight, data.data.height, 0);
                    if (subHeight > 0)
                        $(resizeControl).css("margin-top", -subHeight / 2);
                    // $(resizeControl).css("margin-left", -subWidth / 2);
                }
            }
        }

        if (Tools.isNull(control.options))
            control.options = {};
        control.options["magicdiv"] = this;
        control.options["controltype"] = isMiniUI ? "miniui" : "element";
        control.options["workflow"] = userObj;
        control.options["userdata"] = data;
        control.options["uidata"] = uiData;
        control.options["proc"] = proc;
        control.options["element"] = resizeControl;
        this.control = control;

        var div;
        switch (data.data.type) {
            case this.controlCommon.Div_Type:
                div = $(parent).find("#" + data.data.id)[0];
                break;
            default:
                data.data.tabParent = parent;
                div = this.getDivElement(data.data);
                break;
        }

        this.div = div;

        control.options["x"] = div.offsetLeft;
        control.options["y"] = div.offsetTop;
        control.options["width"] = $(div).width();
        control.options["height"] = $(div).height();
        control.options["div"] = div;
        if (Tools.isNull(data.data.tabindex))
            data.data.tabindex = -1;
        control.options["tabindex"] = data.data.tabindex;
        control.options["type"] = data.data.type;

        switch (data.data.type) {
            case this.controlCommon.LABEL_TYPE:
            case this.controlCommon.IMAGE_TYPE:
                if (Tools.isNull(data.data.isHref) || !data.data.isHref)
                    break;
            case this.controlCommon.BUTTON_TYPE:
                if (data.data.mode == "edit") {
                    var button = mini.getByName(data.data.name, div);
                    button.on("buttonclick", defaultClickEvent);
                    button.on("closeclick", defaultClickEvent);
                } else
                    magicSetOnClick(div, data.data.name, defaultClickEvent);
                break;
        }

        this.isMini = isMiniUI;
        if (isMiniUI) {
            switch (data.data.type) {
                case this.controlCommon.CHECKBOX_TYPE:
                case this.controlCommon.DATE_TYPE:
                case this.controlCommon.TEXTBOX_TYPE:
                case this.controlCommon.COMBOBOX_TYPE:
                case this.controlCommon.DATE_TIME:
                case this.controlCommon.COMBOBOXTREE_TYPE:
                case this.controlCommon.TEXTAREA_TYPE:
                case this.controlCommon.RADIOBUTTONS_TYPE:
                case this.controlCommon.LISTBOX_TYPE:
                case this.controlCommon.PASSWORD_TYPE:
                case this.controlCommon.TIME_TYPE:
                    control.on("valuechanged", defaultValueChangedEvent);
                    break;
            }
        }

        if ((!Tools.isNull(data.data.enabled) && data.data.enabled == "false") || (!Tools.isNull(data.data.needdisabled) && data.data.needdisabled)) {
            if (isMiniUI) {
                control.setEnabled(false);
            } else {
                switch (data.data.type) {
                    case this.controlCommon.LABEL_TYPE:
                        $(control).css("color", "#dedede");
                        break;
                    case this.controlCommon.IMAGE_TYPE:
                        break;
                }
                $(control).attr("disabled", "disabled");
            }
            control.options.needdisabled = true;
        }

        if (isMiniUI) {
            var realControl = $(parent).find("#" + data.data.id)[0];
            if (!Tools.isNull(realControl)) {
                realControl.options = control.options;
            }
        }
        return control;
        // }catch(e){
        //     console.log(e);
        //     return undefined;
        // }
    }

}