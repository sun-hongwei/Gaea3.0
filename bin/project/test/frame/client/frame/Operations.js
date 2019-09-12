var GlobalMainUIInfo = undefined;
var GlobalUIInfos = {};
var Global_Current_UIInfo = undefined;
var Global_Dialog_UIInfo = undefined;
var Global_Report_Infos = {};
var Global_Scripts = {};
var Global_Script_Error_Alert = false;
var global_fail_msg_key = undefined;

/***
 * 清除所有缓冲的ui信息，包括主ui信息
 */
function globalClearOperationInfos() {
    GlobalMainUIInfo = undefined;
    GlobalUIInfos = {};
}

/***
 * 获取指定的ui信息，如果ui信息已经存在则直接返回
 * @param uiName 要获取的ui的name，同设计器的模块关系图中的模块name属性
 */
function globalGetUIInfo(uiName) {
    var uiInfo = undefined;

    if (Tools.isNull(uiName)) {
        if (Tools.isNull(GlobalMainUIInfo)) {
            GlobalMainUIInfo = Tools.getMainUIInfo();
            initUIInfoFrameControlHash(GlobalMainUIInfo);
        }
        return GlobalMainUIInfo;
    }

    if (!Tools.isNull(GlobalUIInfos[uiName])) {
        uiInfo = GlobalUIInfos[uiName];
    } else {
        uiInfo = Tools.getUIInfo(uiName);
        if (!Tools.isNull(uiInfo)) {
            initUIInfoFrameControlHash(uiInfo);
            GlobalUIInfos[uiName] = uiInfo;
        }
    }

    return uiInfo;
}

/**
 * 装载并执行页面的初始脚本（以uiInfo.name + "_start"命名的方法）
 * @param {*} uiInfo 页面描述文件
 * @param {*} tabname tab标签
 * @param {*} paramdata 初始化参数，会在调用该页面的init方法时传入
 */
function execGlobalScript(uiInfo, tabname, paramdata) {
    var name = uiInfo.workflow.name;
    var info = Global_Scripts[name];
    if (Tools.isNull(info) || !info.loaded) {
        Global_Scripts[name] = {
            uiInfo: uiInfo,
            initdata: paramdata,
            tabname: tabname,
            loaded: false
        };

        var url = "./client/userjs/" + name + ".js";
        $.getScript(url)
            .done(function (script, textStatus) {
                console.log(textStatus);
            })
            .fail(function (jqxhr, settings, exception) {
                if (Global_Script_Error_Alert)
                    alert("stack:" + exception.stack + "\nstate:" + settings + "\njqxhr:" + JSON.stringify(jqxhr));
                //$( "div.log" ).text( "Triggered ajaxError handler." );
            });
    } else {
        info.initdata = paramdata;
        Tools.dynamicCallFunction(eval(name + "_start"), [info.uiInfo, info.initdata]);
    }
}

/**
 * 获取页面的执行脚本文件
 * @param {string} name 脚本名称，必须与gaea设计器的modelnode的name相同
 * @return {string} 返回脚本内容
 */
function getGlobalScriptInfo(name) {
    var info = Global_Scripts[name];
    if (!Tools.isNull(info))
        info.loaded = true;
    return info;
}

/**
 * 获取报表模板的描述信息
 * @param {string} name 报表模板的name
 * @return {json} 描述报表的呈现信息
 */
function globalGetReportInfo(name) {
    if (Tools.isNull(Global_Report_Infos[name])) {
        var reportData = Tools.getReportInfo(name);
        if (Tools.isNull(reportData))
            return undefined;

        Global_Report_Infos[name] = reportData;
    }

    return Global_Report_Infos[name];
}

/**
 * 获取控件对应的MagicDiv对象，每个frame控件都对应唯一MagicDiv对象
 * @param {object} control 通过getFrameControl...系列函数返回的对象
 * @return {object} MagicDiv对象
 */
function getFrameMagicDiv(control) {
    if (Tools.isNull(control.options))
        return undefined;
    else
        return control.options.magicdiv;
}

/**
 * 
 * @param {object} control  通过getFrameControl...系列函数返回的对象
 * @return {object} 返回frame的内部管理对象，比如ListViewControl、DataGridControl等
 */
function getFrameControl(control) {
    if (Tools.isNull(control.options))
        return undefined;
    else if (!Tools.isNull(control.options.frameobj))
        return control.options.frameobj;

    if (Tools.isNull(control.options))
        return undefined;
    else if (Tools.isNull(control.options.magicdiv))
        return undefined;
    else if (Tools.isNull(control.options.magicdiv.control))
        return undefined;
    else if (Tools.isNull(control.options.magicdiv.control.options))
        return undefined;
    else {
        return control.options.magicdiv.control.options.frameobj;
    }
}

/**
 * 重置ui的数据源，并重新绘制界面上所有关联数据源的组件
 * @param {UIInfo} uiInfo 界面描述信息
 * @param {json} params 数据源的参数信息
 */
function resetDataSourceForm(uiInfo, params) {
    var cf = new ControlFactory();
    cf.uiInfo = uiInfo;
    cf.initDataSource(params);
}

/**
 * 重置控件的数据源，并重新绘制此组件
 * @param {object} control 通过getFrameControl...系列函数返回的对象
 * @param {json} params 数据源的参数信息
 */
function resetDataSourceControl(control, params,callback) {
    var frameControl = getFrameControl(control);
    if (!Tools.isNull(frameControl.initDataSource)) {
        frameControl.initDataSource(params,callback);
    }
}

/**
 * 抛出由msg指定内容的frame格式异常
 * @param {string} msg 
 */
function throwFrameFailMessage(msg) {
    if (Tools.isNull(global_fail_msg_key))
        global_fail_msg_key = Tools.guid();
    throw global_fail_msg_key + (Tools.isNull(msg) ? "" : ":" + msg);
}

/**
 * 检查此消息是否是失败消息
 * @param {string} msg 通过throwFrameFailMessage返回的消息
 * @return {bool} true是失败消息，其他不是
 */
function checkFrameFailMessage(msg) {
    if (Tools.isNull(global_fail_msg_key))
        global_fail_msg_key = Tools.guid();
    return msg.indexOf(global_fail_msg_key) != -1;
}

/**
 * 清除div上的所有子控件
 * @param {UIInfo} uiInfo div所在ui的描述信息
 * @param {element} div 要清除的div，此div为document文档中的div节点
 */
function emptyFrameDiv(uiInfo, div) {
    delete div.uiname;
    delete div.uiid;
    $(div).empty();

    if (!Tools.isNull(uiInfo)) {
        if (Tools.isNull(uiInfo.nameHash))
            return;

        var divInfo = uiInfo.nameHash[div.name];
        if (!Tools.isNull(divInfo))
            initDivDataForUIInfo(uiInfo, div.element, divInfo)
    }
}

/**
 * 获取uiInfo包含的uiName
 * @param {json} uiInfo 界面描述文件
 * @return {string} 返回uiName
 */
function getFrameUIName(uiInfo) {
    return uiInfo.workflow.name;
}

/**
 * 获取uiInfo包含的运行流程名称
 * @param {json} uiInfo 界面描述文件
 * @return {string} 返回运行流程名称
 */
function getFrameRunFlowName(uiInfo) {
    if (Tools.isNull(uiInfo.workflow.runTaskName))
        return undefined;

    return uiInfo.workflow.runTaskName.key;
}

/**
 * 获取uiInfo描述的界面的最外层div节点，一般用于css样式设置
 * @param {UIInfo} uiInfo ui描述信息
 */
function getFrameFormId(uiInfo) {
    return uiInfo.workflow.id + "div";
}

function getFrameControlById(uiInfo, id) {
    if (Tools.isNull(uiInfo.idHash))
        getFrameControlByName(uiInfo, id);
    if (Tools.isNull(uiInfo.idHash[id]))
        return undefined;
    var element = uiInfo.idHash[id].element;
    if (Tools.isNull(element))
        return element;
    
    return getFrameControlByName(uiInfo, element.data.name);
}

function internalGetFrameControlByName(uiInfo, name) {
    var formId = getFrameFormId(uiInfo);
    var formDiv = $("#" + formId)[0];
    var control = mini.getByName(name, formDiv);
    if (Tools.isNull(control)) {
        control = $(formDiv).find("[name='" + name + "']")[0];
    }

    return control;
}

/**
 * 获取指定父控件上的子控件对象
 * @param {string} controlName 要获取的控件name，可以为null，为null时controlID必须不为null
 * @param {string} controlID 要获取的控件id，可以为null，为null时controlName必须不为null
 * @param {object} parentControl 父控件对象，通过getFrameControl...系列函数返回的对象
 * @return {object} 返回控件对象，对于miniui组件，返回mini对象，其他为document的节点对象
 */
function getFrameControlByParent(controlName, controlID, parentControl) {
    var control = mini.getByName(controlName, parentControl);
    if (Tools.isNull(control)) {
        control = $(parentControl).find("#" + controlID)[0];
    }
    return control;
}

/**
 * 更新listview的数据项目到uiInfo，仅内部使用
 */
refreshFrameListView = function (uiInfo, header, data) {
    if (Tools.isNull(uiInfo.nameHash) || Tools.isNull(uiInfo.idHash))
        return;

    var datas = {
        data: header
    };
    datas.data.data = data;
    var elementInfo = {
        element: datas,
        childs: {
            types: [],
            id: {},
            name: {},
            rows: [],
            ui: {}
        }
    };
    initListViewDataForUIInfo(datas, elementInfo);
    uiInfo.nameHash[header.name] = elementInfo;
    uiInfo.idHash[header.id] = elementInfo;
}

/**
 * 初始化listview的内部对象信息到uiInfo，仅内部使用
 * @param {object} element 
 * @param {object} elementInfo 
 */
function initListViewDataForUIInfo(element, elementInfo) {
    if (Tools.isNull(element.data.data))
        return;

    var json = Tools.isObject(element.data.data) ? element.data.data : JSON.parse(element.data.data);
    for (var index = 0; index < json.length; index++) {
        var rows = json[index];
        var row = {};
        for (var index1 = 0; index1 < rows.length; index1++) {
            var rowData = rows[index1];
            row[rowData.id] = rowData;
        }
        elementInfo.childs.rows.push(row);
    }

    if (elementInfo.childs.rows.length > 0) {
        elementInfo.childs.types.push("rows");
    }
}

/**
 * 初始报表的内部信息到uiInfo，仅内部使用
 * @param {object} element 
 * @param {object} elementInfo 
 */
function initReportDataForUIInfo(element, elementInfo) {
    if (Tools.isNull(element.data.cells))
        return;

    for (var index = 0; index < element.data.cells.length; index++) {
        var cell = element.data.cells[index];
        var data = cell.editor.data;
        elementInfo.childs.name[data.name] = data;
        elementInfo.childs.id[data.id] = data;
    }
    if (Object.keys(elementInfo.childs.id).length > 0) {
        elementInfo.childs.types.push("id");
    }
    if (Object.keys(elementInfo.childs.name).length > 0) {
        elementInfo.childs.types.push("name");
    }

}

/**
 * 初始div的内部信息到uiInfo，仅内部使用
 * @param {UIInfo} uiInfo 
 * @param {object} element 
 * @param {object} elementInfo 
 */
function initDivDataForUIInfo(uiInfo, element, elementInfo) {
    var div = internalGetFrameControlByName(uiInfo, element.data.name);
    if (!Tools.isNull(div)) {
        if (!Tools.isNull(div.uiname)) {
            if (!Tools.isNull(elementInfo.childs.ui.info)) {
                if (elementInfo.childs.ui.name == div.uiname)
                    return;
            }

            var childUIInfo = globalGetUIInfo(div.uiname);
            if (!Tools.isNull(childUIInfo)) {
                initUIInfoFrameControlHash(childUIInfo);
                elementInfo.childs.ui.info = childUIInfo;
                elementInfo.childs.ui.name = div.uiname;
                if (elementInfo.childs.types.indexOf("ui") == -1)
                    elementInfo.childs.types.push("ui");
            }
        } else {
            var index = elementInfo.childs.types.indexOf("ui");
            if (index != -1)
                elementInfo.childs.types.splice(index, 1);
        }
    }
}

/**
 * 初始子窗体信息到uiInfo，仅内部使用
 * @param {object} element 
 * @param {object} elementInfo 
 */
function initSubDataForUIInfo(element, elementInfo) {
    if (!Tools.isNull(element.data.uiInfo)) {
        if (!Tools.isNull(elementInfo.childs.ui.info)) {
            if (elementInfo.childs.ui.name == element.data.uiInfo)
                return;
        }
        elementInfo.childs.ui.name = element.data.uiInfo;
        var childUIInfo = globalGetUIInfo(elementInfo.childs.ui.name);
        if (!Tools.isNull(childUIInfo)) {
            initUIInfoFrameControlHash(childUIInfo);
            elementInfo.childs.ui.info = childUIInfo;
            elementInfo.childs.types.push("ui");
        }
    } else {
        delete elementInfo.childs.ui.info;
        var index = elementInfo.childs.types.indexOf("ui");
        if (index != -1)
            elementInfo.childs.types.splice(index, 1);
    }
}

/**
 * 初始ui信息到uiInfo中，仅内部使用
 * @param {UIInfo} uiInfo 
 */
function initUIInfoFrameControlHash(uiInfo) {
    if (Tools.isNull(uiInfo.nameHash)) {
        uiInfo.divs = [];
        uiInfo.subs = [];
        uiInfo.nameHash = {};
        uiInfo.idHash = {};
        uiInfo.buttons = {};
        uiInfo.labels = {};
        uiInfo.images = {};
        uiInfo.menus = {};
        uiInfo.trees = {};
        uiInfo.toolbars = {};
        uiInfo.mainTrees = {};

        var cc = new ControlCommon();
        uiInfo.data.forEach(function (element) {
            var elementInfo = {
                element: element,
                childs: {
                    types: [],
                    id: {},
                    name: {},
                    rows: [],
                    ui: {}
                }
            };
            switch (element.data.type) {
                case cc.Report_Name:
                    initReportDataForUIInfo(element, elementInfo);
                    break;
                case cc.ListView_Name:
                    initListViewDataForUIInfo(element, elementInfo);
                    break;
                case cc.Div_Name:
                    uiInfo.divs.push(elementInfo);
                    break;
                case cc.Sub_Name:
                    uiInfo.subs.push(elementInfo);
                    break;
                case cc.Label_Name:
                    uiInfo.labels[element.data.name] = elementInfo;
                    break;
                case cc.Tree_Name:
                    uiInfo.trees[element.data.name] = elementInfo;
                    break;
                case cc.Button_Name:
                    uiInfo.buttons[element.data.name] = elementInfo;
                    break;
                case cc.Image_Name:
                    uiInfo.images[element.data.name] = elementInfo;
                    break;
                case cc.MainMenu_Name:
                    uiInfo.menus[element.data.name] = elementInfo;
                    break;
                case cc.MainTree_Name:
                    uiInfo.mainTrees[element.data.name] = elementInfo;
                    break;
                case cc.Toolbar_Name:
                    uiInfo.toolbars[element.data.name] = elementInfo;
                    break;
                default:
                    break;
            }
            uiInfo.nameHash[element.data.name] = elementInfo;
            uiInfo.idHash[element.data.id] = elementInfo;
        });
    }
    for (var index = 0; index < uiInfo.divs.length; index++) {
        var elementInfo = uiInfo.divs[index];
        initDivDataForUIInfo(uiInfo, elementInfo.element, elementInfo);
    }
    for (var index = 0; index < uiInfo.subs.length; index++) {
        var elementInfo = uiInfo.subs[index];
        initSubDataForUIInfo(elementInfo.element, elementInfo);
    }
}

/**
 * 获取div的子控件
 * @param {*} divs 要查找的divinfo， 通过getFrameControlBy...系列函数返回 
 * @param {*} controlName 要查找控件的name，如果控件为listview，report类型则可以指定childControlName、rowindex
 * @param {*} childControlName 要查找在controlName指定的控件中的子控件name（报表）或者时列id（listview、datagrid）
 * @param {*} rowindex 对于listview、datagrid为行索引，以0开始，其他类型控件忽略
 * @return {object} 返回控件对象，对于miniui组件，返回mini对象，其他为document的节点对象
 */
function getFrameControlByDiv(divs, controlName, childControlName, rowindex) {
    for (var index = 0; index < divs.length; index++) {
        var subElementInfo = divs[index];
        if (Tools.isNull(subElementInfo.childs.ui.info))
            continue;

        var result = getFrameControlByName(subElementInfo.childs.ui.info, controlName, childControlName, rowindex);
        if (!Tools.isNull(result))
            return result;
    }
    return undefined;
}

/**
 * 获取左侧导航的UIInfo
 * @description 如果当前页面没有使用frame，那么返回undefined
 */
function getFrameLeftRegionUIInfo() {
    return globalGetUIInfo("left_region");
}

/**
 * 获取上侧导航的UIInfo
 */
function getFrameTopRegionUIInfo() {
    return globalGetUIInfo("top_region");
}

/**
 * 获取下侧导航的UIInfo
 */
function getFrameBottomRegionUIInfo() {
    return globalGetUIInfo("bottom_region");
}

/**
 * 获取右侧侧导航的UIInfo
 */
function getFrameRightRegionUIInfo() {
    return globalGetUIInfo("right_region");
}

/**
 * 根据控件name获取frame的控制对象（非element对象）
 * @param {UIInfo} uiInfo 要获取控件所在的界面定义信息
 * @param {string} controlName 控件name
 * @return {object} frame的控制对象
 */
function getFrameControlObjectByName(uiInfo, controlName) {
    var div = getFrameControlByName(uiInfo, controlName);
    return getFrameControl(div);
}
/**
 * 查找左侧导航上的控件
 * @param {*} 参数见 getFrameControlByDiv
 * @description 如果当前页面没有使用frame，那么返回undefined
 * @return {object} 返回控件对象，对于miniui组件，返回mini对象，其他为document的节点对象
 */
function getFrameLeftRegionControlByName(controlName, childControlName, rowindex) {
    var uiInfo = getFrameLeftRegionUIInfo();
    return getFrameControlByName(uiInfo, controlName, childControlName, rowindex);
}

/**
 * 查找上侧导航上的控件
 * @param {*} 参数见 getFrameControlByDiv
 * @return {object} 返回控件对象，对于miniui组件，返回mini对象，其他为document的节点对象
 */
function getFrameTopRegionControlByName(controlName, childControlName, rowindex) {
    var uiInfo = getFrameTopRegionUIInfo();
    return getFrameControlByName(uiInfo, controlName, childControlName, rowindex);
}

/**
 * 查找下侧导航上的控件
 * @param {*} 参数见 getFrameControlByDiv
 * @return {object} 返回控件对象，对于miniui组件，返回mini对象，其他为document的节点对象
 */
function getFrameBottomRegionControlByName(controlName, childControlName, rowindex) {
    var uiInfo = getFrameBottomRegionUIInfo();
    return getFrameControlByName(uiInfo, controlName, childControlName, rowindex);
}

/**
 * 查找右侧侧导航上的控件
 * @param {*} 参数见 getFrameControlByDiv
 * @return {object} 返回控件对象，对于miniui组件，返回的时mini对象，其他为document的节点对象
 */
function getFrameRightRegionControlByName(controlName, childControlName, rowindex) {
    var uiInfo = getFrameRightRegionUIInfo();
    return getFrameControlByName(uiInfo, controlName, childControlName, rowindex);
}

/**
 * 查找页面上的控件
 * @param {*} uiInfo 要查找的页面的描述信息
 * @param {*} 其他参数见 getFrameControlByDiv
 * @return {object} 返回控件对象，对于miniui组件，返回mini对象，其他为document的节点对象
 */
function getFrameControlByName(uiInfo, controlName, childControlName, rowindex) {
    var elementInfo = uiInfo.nameHash[controlName];
    if (Tools.isNull(elementInfo)) {
        var result = getFrameControlByDiv(uiInfo.divs, controlName, childControlName, rowindex);
        if (Tools.isNull(result))
            result = getFrameControlByDiv(uiInfo.subs, controlName, childControlName, rowindex);
        return result;
    }

    var control = internalGetFrameControlByName(uiInfo, elementInfo.element.data.name);
    if (Tools.isNull(control))
        return undefined;

    if (!Tools.isNull(childControlName)) {
        var cc = new ControlCommon();
        switch (elementInfo.element.data.type) {
            case cc.Toolbar_Name:
                return control.options.toolbarcontrol.getControl(childControlName);
        }
        for (var index = 0; index < elementInfo.childs.types.length; index++) {
            var typename = elementInfo.childs.types[index];
            var childid = undefined;
            var childname = undefined;
            switch (typename) {
                case "name":
                    var child = elementInfo.childs.name[childControlName];
                    if (!Tools.isNull(child)) {
                        childname = child.name;
                        childid = child.id;
                    }
                    break;
                case "rows":
                    if (Tools.isNull(rowindex))
                        rowindex = 0;
                    if (rowindex < 0 || rowindex > elementInfo.childs.rows.length - 1)
                        return undefined;

                    var child = elementInfo.childs.rows[rowindex];
                    if (!Tools.isNull(child)) {
                        childname = child.name;
                        childid = child.id;
                    }
                    break;
            }

            if (!Tools.isNull(childid)) {
                control = getFrameControlByParent(childname, childid, control);
                break;
            }
        }
    }
    return control;
}

/***
 * 获取listview组件上单元格类型为报表的对象
 * @param {UIInfo} uiInfo 要获取控件所在的界面定义信息
 * @param {string} listViewId listview的id
 * @param {int} row 要获取的报表控件所在的行索引，开始索引为0
 * @param {string} column 要获取的报表控件所在的列id
 * 
 * @return {object} 返回报表控件对象(ReportControl)
 */
function getListViewReportControl(uiInfo, listViewId, row, column) {
    var listview = getFrameControlByName(uiInfo, listViewId);
    return getFrameControlValue(listview, column, row);
}

/***
 * 获取listview组件上单元格类型为报表，此报表中的控件值
 * 
 * @param {UIInfo} uiInfo 要获取控件所在的界面定义信息
 * @param {string} listViewId listview的id
 * @param {int} row 要获取的报表控件所在的行索引，开始索引为0
 * @param {string} column 要获取的报表控件所在的列id
 * @param {string} rid 要获取值的报表的控件id
 * 
 * @return {object} 返回rid指定的控件的值
 */
function getListViewReportControlValue(uiInfo, listViewId, row, column, rid) {
    var report = getListViewReportControl(uiInfo, listViewId, row, column);
    var reportSubControlId = "b8891134-cb31-4395-983e-10ff7b7ce9d3"; //报表中的控件name
    var value = getFrameControlValue(report, rid);
    return value;
}

/***
 * 获取指定组件的子控件值
 * @param {object} control 由getFrameControlBy...函数返回的对象，不可以为null
 * @param {string} column 对于listview、grid、report组件为列id，其他组件忽略
 * @param {int} row 对于listview、grid组件为行号，从0开始，其他组件忽略
 * 
 * @return {object} 返回control对象的子控件值；对于listview组件，如果column指定的列为report类型，那么返回report对象
 */
function getFrameControlValue(control, column, row) {
    if (Tools.isNull(control))
        return undefined;

    if (Tools.isNull(control.options.report))
        return control.options.magicdiv.getControlValue(column, row);
    else {
        var report = control.options.report;
        if (!Tools.isNull(column)) {
            control = report.getCellDivByName(column);
        } else {
            return control.options.text;
        }
        var type;
        if (Tools.isNull(control)) {
            control = report.getCellDivById(column);
            type = report.items[column].editor.type;
        } else {
            type = report.nameItems[column].editor.type;
        }
        return report.getCellValueByControl(control, type);

    }
}

/***
 * 设置指定组件的值
 * @param {object} control 由getFrameControlBy...函数返回的对象，不可以为null
 * @param {object} value 要设置的值
 * @param {string} column 对于listview、grid、report组件为列id，其他组件忽略
 * @param {int} row 对于listview、grid组件为行号，从0开始，其他组件忽略
 */
function setFrameControlValue(control, value, column, row) {
    if (Tools.isNull(control))
        return undefined;

    if (Tools.isNull(control.options.report))
        return control.options.magicdiv.setControlValue(value, column, row);
    else {
        var report = control.options.report;
        if (!Tools.isNull(column)) {
            var type;
            var control = report.getCellDivByName(column);
            if (Tools.isNull(control)) {
                control = report.getCellDivById(column);
                type = report.items[column].editor.type;
            } else {
                type = report.nameItems[column].editor.type;
            }
            report.setCellValueByControl(control, value, type);
        } else {
            report.setValue(value);
        }
    }
}

/***
 * 获取控件值的字符形式
 * 参数同getFrameControlValue
 * 
 * @return {string} 返回控件值的字符形式
 */
function getFrameControlText(control, column, row) {
    if (Tools.isNull(control))
        return undefined;

    return control.options.magicdiv.getControlValue(column, row, true);
}

/***
 * 设置控件的text值
 * 
 * 参数同setFrameControlValue
 */
function setFrameControlText(control, value, column, row) {
    if (Tools.isNull(control))
        return undefined;

    return control.options.magicdiv.setControlValue(value, column, row, true);
}

function DatasetRowState() {
    this.Update_State = "modified";
    this.Insert_State = "added";
    this.Delete_State = "removed";
    this.None_State = "";
    addState = function (row) {
        this.setRowState(row, this.Insert_State);
    }

    delState = function (row) {
        this.setRowState(row, this.Delete_State);
    }

    updateState = function (row) {
        this.setRowState(row, this.Update_State);
    }

    removeState = function (row) {
        this.setRowState(row, this.None_State);
    }

    setRowState = function (row, state) {
        if (state == this.None_State) {
            row["_state"] = "";
        } else
            row["_state"] = state;
    }
}

/***
 * 根据ui信息创建web界面
 * @param {UIInfo} uiInfo 要建立的界面描述信息
 * @param {element} div 界面的父div
 * @param {bool} notAdjuse 是否调整界面位置，true不调整，其他调整
 * @param {bool} transparent 界面背景是否透明，true透明，其他不透明
 * 
 * @return {object} 返回表示界面的ControlFactory对象
 */
function createUIForUIInfo(uiInfo, div, notAdjust, transparent) {
    if (Tools.isNull(uiInfo))
        return;

    var oldColor = uiInfo.page.color;

    if (!Tools.isNull(transparent) && transparent) {
        delete uiInfo.page.color;
    }
    var dw = new DocumentWriter();
    var name = uiInfo.workflow.id + "_layout";
    var divStr = "<div style=';border:0px;width:100%;height:100%;" +
        (transparent ? "" : "background:" + Tools.convetColor(uiInfo.page.color)) +
        "' id = '" + name + "'></div>";
    if (Tools.isNull(div))
        document.write(divStr);
    else {
        emptyFrameDiv(uiInfo, div);
        div.uiid = uiInfo.workflow.id;
        div.uiname = uiInfo.workflow.name;
        var header = {
            tabParent: div
        };
        dw.write(header, divStr);
    }
    var bodyEl = document.getElementById(name);
    cf = new ControlFactory();
    var width = bodyEl.offsetWidth;
    var height = bodyEl.offsetHeight;
    uiInfo.tabParent = bodyEl;
    var control = cf.write(dw, uiInfo, width, height, jumpUIForName);
    uiInfo.page.color = oldColor;
    return control;
}

/**
 * 根据模块关系图中的模块name建立界面
 * @param {string} uiName 模块name
 * @param 其他参数参见createUIForUIInfo
 * @return 返回同createUIForUIInfo
 */
function createUIForName(uiName, div, notAdjust, transparent) {
    var uiInfo = globalGetUIInfo(uiName);
    return createUIForUIInfo(uiInfo, div, notAdjust, transparent);
}

/**
 * 根据模块关系图中的模块name建立界面
 * @param {string} uiName 模块name
 * @param tabName 界面的显示名称，如果使用此界面所在的模块节点使用frame，则为标签页的显示内容
 * @return 返回同createUIForUIInfo
 */
function createUITabForUIName(tabName, uiName) {
    var uiInfo = globalGetUIInfo(uiName);
    return createUITabForUIInfo(tabName, uiInfo);
}

/**
 * 根据ui信息创建web界面
 * @param uiInfo 要建立的界面描述信息
 * @param tabName 界面的显示名称，如果使用此界面所在的模块节点使用frame，则为标签页的显示内容
 * @return 返回同createUIForUIInfo
 */
function createUITabForUIInfo(tabName, uiInfo) {
    $(document.body).css("overflow", "hidden");

    GlobalFrameManger.initFrame(uiInfo);
    tn = "tab" + tabName;
    var bodyEl = GlobalFrameManger.getMainLayout(uiInfo, tabName);
    if (Tools.isNull(bodyEl))
        return undefined;

    if (uiInfo.workflow.useTab != "true") {
        $(bodyEl).empty();
    }
    cf = new ControlFactory();
    var width = bodyEl.clientWidth;
    var height = bodyEl.clientHeight;
    uiInfo.tabParent = bodyEl;
    var dw = new DocumentWriter();
    var control = cf.write(dw, uiInfo, width, height, jumpUIForName);
    $(bodyEl).css("overflow-x", uiInfo.workflow.showHorizontalScrollBar ? "auto" : "hidden");
    $(bodyEl).css("overflow-y", uiInfo.workflow.showVerticalScrollBar ? "auto" : "hidden");
    return control;
}

function getFrameContentWindowSize() {
    var width = $(document.body).width();
    var height = $(document.body).height();

    return {
        width: width,
        height: height
    };
}

/**
 * 转换uiid到uiname
 * @param {UIInfo} uiInfo 包含jumpid的界面信息 
 * @param {string} jumpid 要转换的界面id
 * @return {string} 返回对应的界面name，如果未找到返回undefined
 */
function jumpIdToUIName(uiInfo, jumpid) {
    for (i = 0; i < uiInfo.output.length; i++) {
        input = uiInfo.output[i];
        if (input.id == jumpid) {
            return input.name;
        }
    }
    return undefined;
}

/**
 * 根据模块关系图中的模块name完成界面建立及跳转，调用完毕会设置当前页为此界面
 * @param {string} uiName 模块name
 * @param {string} tabName 界面的显示名称，如果使用此界面所在的模块节点使用frame，则为标签页的显示内容
 * @param {callback} callback 当此界面完成建立后调用，回调格式：function(uiInfo, tabName, param)
 * @param {object} param 用户的自定义参数，当调用callback时回传
 * @return {bool} 成功建立返回true，其他返回false
 */
function jumpUIForName(tabName, uiName, callback, param) {
    var uiInfo = globalGetUIInfo(uiName);
    return jumpUIForUIInfo(tabName, uiInfo, callback, param);
}

function setJumpWindowParams(uiData, params){
    if (Tools.isNull(params))
        return;

    for (var index = 0; index < uiData.data.length; index++) {
        var header = uiData.data[index].data;
        if (Tools.isNull(header.inputId))
            continue;

        if (Tools.isNull(params[header.inputId]))
            continue;

        var value = params[header.inputId];
        if (Tools.isNull(value))
            continue;

        var control = getFrameControlByName(uiData, header.name);
        setFrameControlValue(control, value);
    }

}

/**
 * 根据模块关系图中的模块name完成界面建立及跳转，调用完毕会设置当前页为此界面
 * @param {UIInfo} uiInfo 要建立的界面描述信息
 * @param 其他参数同jumpUIForName(tabName, uiName, callback, param)
 * @return 返回同jumpUIForName(tabName, uiName, callback, param)
 */
function jumpUIForUIInfo(tabName, uiInfo, callback, param) {
    if (Tools.isNull(uiInfo))
        return false;

    if (Tools.isNull(tabName))
        tabName = uiInfo.workflow.title;

    if (!Tools.isNull(uiInfo.workflow.useDialog) && uiInfo.workflow.useDialog == "true") {
        GlobalDialog.showFrameDialog(uiInfo.workflow.name, tabName, undefined, undefined, param);
    } else {
        var needReload = (!Tools.isNull(Global_Current_UIInfo) && uiInfo.workflow.useFrame != Global_Current_UIInfo.workflow.useFrame);
        if (!needReload) {
            var needLoad = GlobalCookies.getCookie("needLoad");
            if (Tools.isNull(needLoad) || needLoad != "true")
                if (uiInfo.workflow.useFrame != "true")
                    needReload = true;
        }
        Global_Current_UIInfo = uiInfo;
        if (needReload) {
            GlobalCookies.setCookie("params", JSON.stringify(param));
            loadNewPage(tabName, uiInfo.workflow.name);
            return true;
        }else{
            var saveJumpParams = GlobalCookies.getCookie("params");
            GlobalCookies.removeCookie("params");
            if (!Tools.isNull(saveJumpParams)){
                var jumpParams = JSON.parse(saveJumpParams);
                if (Tools.isNull(param))
                    param = jumpParams;
                else{
                    for (const key in jumpParams) {
                        param[key] = jumpParams[key];
                    }    
                }
            }
        }

        GlobalCookies.removeCookie("needLoad");

        document.body.style.backgroundColor = Tools.convetColor(uiInfo.page.color);
        if (uiInfo.workflow.useFrame != "true") {
            createUIForUIInfo(uiInfo);
        } else {
            createUITabForUIInfo(tabName, uiInfo);
        }
    }

    setJumpWindowParams(uiInfo, param);

    initPage(uiInfo, tabName, param);
    // if (!Tools.isNull(callback)) {
    //     callback(uiInfo, tabName, param);
    // } else {
    //     if (!Tools.isNull(GlobalFrameManger.Event.onInitUI)) {
    //         GlobalFrameManger.Event.onInitUI(uiInfo, tabName, param)
    //     }
    // }

    return true;
}

/**
 * 从cookie中建立窗体，仅内部使用
 * @param {callback} callback 
 */
function loadPageFromCookie(callback) {
    var tabName = GlobalCookies.getCookie("tabName");
    var id = GlobalCookies.getCookie("jumpid");
    var value = GlobalCookies.getCookie("jumpdata");
    var data = null;
    if (!Tools.isNull(value))
        data = JSON.parse(value);
    jumpUIForName(tabName, id, callback, data);
    GlobalCookies.removeCookie("jumpid");
    GlobalCookies.removeCookie("tabName");
    GlobalCookies.removeCookie("jumpdata");
}

/**
 * 建立新窗体，并设置cookie信息，仅内部使用
 * @param {string} tabName 
 * @param {string} jumpid 
 * @param {string} param 
 */
function loadNewPage(tabName, jumpid, param) {
    GlobalCookies.setCookie("tabName", tabName);
    GlobalCookies.setCookie("jumpid", jumpid);
    if (!Tools.isNull(param)) {
        var data = JSON.stringify(param);
        GlobalCookies.setCookie("jumpdata", data);
    }
    GlobalCookies.setCookie("needLoad", "true");
    window.location.reload();
}

/**
 * 获取类型为ListViewControl的选择器对象包含的Document的Element对象
 * @param {JQuerySelector} selector jquery的选择器
 * @return {element} 如果selector是一个ListViewControl对象包含的Div的选择器对象，那么返回此div的Element对象，其他返回undefined
 */
function global_GetListViewCellElement(selector) {
    if (selector.length == 0)
        return undefined;
    if (Tools.isNull(selector[0].options) ||
        Tools.isNull(selector[0].options["listview"]) ||
        Tools.isNull(selector[0].options["row"]) ||
        Tools.isNull(selector[0].options["header"])
    ) {
        if (!Tools.isNull(selector.parent()))
            return global_GetListViewCellElement(selector.parent());
        else
            return undefined;
    }

    return selector[0];
}

/**
 * 获取类型为ReportControl的选择器对象包含的Document的Element对象
 * @param {JQuerySelector} selector jquery的选择器
 * @return {element} 如果selector是一个ReportControl对象包含的Div的选择器对象，那么返回此div的Element对象，其他返回undefined
 */
function global_GetReportElement(selector) {
    if (selector.length == 0)
        return undefined;
    if (Tools.isNull(selector[0].options) || Tools.isNull(selector[0].options["report"])) {
        if (!Tools.isNull(selector.parent()))
            return global_GetReportElement(selector.parent());
        else
            return undefined;
    }

    return selector[0];
}

/**
 * 获取document的element对象包含frame对象的element
 * 检查此element及其子集，并返回第一个包含frame对象信息的element
 * @param {element} element 要获取的element对象
 * @return {element} 如果找到则返回包含frame对象信息的element，否则返回undefined
 */
function global_GetOptionsElement(element) {
    return global_GetJQueryOptionsElement($(element));
}

/**
 * 获取选择器对象包含frame对象的element
 * 检查此此选择器包含的element及其子集，并返回第一个包含frame对象信息的element
 * @param {selector} selector 要获取的选择器对象
 * @return {element} 如果找到则返回包含frame对象信息的element，否则返回undefined
 */
function global_GetJQueryOptionsElement(selector) {
    if (selector.length == 0)
        return undefined;

    if (Tools.isNull(selector[0].options)) {
        if (!Tools.isNull(selector.parent()))
            return global_GetReportObject(selector.parent());
        else
            return undefined;
    }

    return selector[0];
}

/**
 * 移除控件，并更新界面层，此方法与$().remove()的区别在于，前者会刷新界面的关联关闭，比如折叠以及关联组效果等
 * @param {UIInfo} uiInfo 控件所在的ui的描述信息
 * @param {String} controlName 要移除的控件名称
 */
function removeFrameControlByName(uiInfo, controlName) {
    var control = getFrameControlByName(uiInfo, controlName);
    removeFrameControl(control);
}

/**
 * 移除控件，并更新界面层，此方法与$().remove()的区别在于，前者会刷新界面的关联关闭，比如折叠以及关联组效果等
 * @param {Object} control 通过getFrameControlByName函数返回的对象
 */
function removeFrameControl(control) {
    if (Tools.isNull(control))
        return;

    if (Tools.isNull(control.options))
        return

    if (Tools.isNull(control.options.magicdiv))
        return

    control.options.magicdiv.removeSelf()
}

/**
 * 获取此uiInfo关联的界面中设置autoRunFlowTasks或autoRunFlowHistoryTasks的表格对象
 * @param {UIInfo} uiInfo 界面描述信息
 * @return 返回getFrameControlByName返回的对象
 */
function getFrameRunFlowDataGrid(uiInfo) {
    var cm = new ControlCommon();
    for (var index = 0; index < uiInfo.data.length; index++) {
        var element = uiInfo.data[index].data;
        if (element.type == cm.GRID_TYPE) {
            if (element.autoRunFlowTasks)
                return getFrameControlByName(uiInfo, element.name);
            else if (element.autoRunFlowHistoryTasks) {
                return getFrameControlByName(uiInfo, element.name);
            }
        }
    }

    return undefined;
}

/**
 * 获取单击事件中的被点击对象
 * @param {UIInfo} uiInfo 界面描述信息
 * @param {object} e OnClick事件中的第一个参数
 * @return 返回getFrameControlByName返回的对象
 */
function getFrameControlByClickEventObject(uiInfo, e) {
    if (Tools.isNull(e.sender)) {
        return getFrameControlByName(uiInfo, e.options["userdata"].data.name)
    } else {
        return getFrameControlByName(uiInfo, e.sender.options["userdata"].data.name)
    }
}

/**
 * 获取单击事件中被点击对象的控制对象
 * @param {object} e OnClick事件中的第一个参数
 * @return 返回MagicDiv对象
 */
function getFrameMagicControlByClickEventObject(e) {
    if (Tools.isNull(e.sender)) {
        return e.options["magicdiv"]
    } else
        return e.sender.options["magicdiv"]
}