function GlobalReportOnHrefClick(e) {
    var element = global_GetReportElement($(e));
    var report = element.options["report"];
    var id = element.options["id"];
    var name = element.options["name"];

    var jumpid = element.options.hasOwnProperty("jumpid") ? element.options.jumpid : undefined;
    var attachid = element.options.hasOwnProperty("attachid") ? element.options.attachid : undefined;
    var isJump = !Tools.isNull(jumpid) || !Tools.isNull(attachid);
    if (!isJump)
        if (Tools.isNull(report.Event.onReportClick))
            return;
    if (isJump) {
        var uiName = attachid;
        if (!Tools.isNull(jumpid))
            uiName = jumpIdToUIName(report.uiInfo, jumpid);
        if (!Tools.isNull(uiName))
            jumpUIForName("", uiName);
    } else {
        report.Event.onReportClick(report, element, id, name);
    }
}

function Report_GlobalOnKeyEsc(event) {
    var control = document.activeElement;
    if (Tools.isNull(control))
        return;
    switch (event.keyCode) {
        case 27:
        case 96:
            if (Tools.isNull(control.options))
                return;
            var report = control.options["report"];
            report.updateEditorValue();
            break;
    }
}

function ReportGlobalOnClick(e) {
    if (Tools.isNull(e.options)) {
        return;
    }

    var reportControl = e.options["report"];
    var data = e.options["data"];
    if (!Tools.isNull(data.editor.data.isHref) && data.editor.data.isHref) {
        reportControl.updateEditorValue();
        return;
    }

    var allHeader = e.options["userdata"];

    var divWidth = e.options["divWidth"];
    var divHeight = e.options["divHeight"];
    reportControl.createCell(allHeader, data, reportControl.uiInfo, divWidth, divHeight, true);
}

function ReportControlEx() {
    this.Event = {
        /**
         * 函数原型：function(report, element, id, name)
         * @param report 控件所在的报表对象
         * @param element 触发事件的原始DOM元素
         * @param id 此元素对应报表控件中的组件id
         * @param name 此元素对应报表控件中的组件name
         */
        onReportClick: undefined,
    };
    this.items = {};
    this.nameItems = {};
    this.cells;
    this.radios = [];
    this.divs = [];
    this.rows;
    this.cols;
    this.uiInfo;
    this.controlCommon = new ControlCommon();
    this.editor = undefined;
    this.editControlID = undefined;
    this.controlType = undefined;
    this.id = undefined;
    this.name = undefined;

    this.header;
    this.div;

    this.exportToExcel = function (filename, sheetname) {
        var info = {};
        for (var index = 0; index < this.cells.length; index++) {
            var cell = this.cells[index];
            cell.value = this.getCellValue(cell.editor.id, cell.editor.type);
        }
        info.cells = this.cells;
        info.rows = this.rows;
        info.cols = this.cols;
        info.name = this.name;
        info.id = this.id;
        info.filename = filename;
        info.sheetName = sheetname;

        Tools.simpleTomcatSubmit("/report/service/excel_export", {
            info: JSON.stringify(info)
        }, function (data) {
            if (data.ret == 0) {
                var url = Tools.getTomcatUri() + data.data;
                Tools.openSaveFileSelector(info.name, url);
            } else {
                alert("导出报表失败！");
            }
        });
    }

    this.getFieldValue = function (value, defaultValue) {
        if (Tools.isNull(value))
            if (Tools.isNull(defaultValue))
                return "";
            else
                return defaultValue
        else
            return value;
    }

    this.listviews = [];
    this.grids = [];
    this.hrefs = [];
    this.trees = [];
    this.scrolls = [];
    this.uploads = [];
    this.charts = [];

    this.loadScrollbar = function (data) {
        var header = data.data;
        var scrollControl = new ScrollBarControl();

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
    }

    this.loadHref = function (data) {
        var control = this.getCellDivById(data.id);
        if (Tools.isNull(control))
            return;
        if (Tools.isNull(control.options))
            control.options = {};
        control.options["header"] = data.header;
        control.options["text"] = data.text;
        control.options["report"] = this;
        control.options["jumpid"] = data.header.jumpID;
        control.options["attachid"] = data.header.attachID;

    }

    this.loadListView = function (data) {
        var div = this.getControlByID(data.id);
        if (Tools.isNull(div))
            return;
        var control = new ListViewControl();
        control.init(data, this.uiInfo, $(div).parent()[0]);
    }

    this.loadChart = function (data) {
        var div = this.getControlByID(data.id);
        if (Tools.isNull(div))
            return;
        var control = new ChartControl();
        control.init(this.uiInfo, div, data, true);
    }

    this.loadRadio = function (data) {
        var div = this.getControlByID(data.id);
        if (Tools.isNull(div))
            return;
        data.tabParent = div;
        data.radiocontrol.init(data, data.textFontStyle);
    }

    this.loadDiv = function (data) {
        var div = this.getControlByID(data.id);
        if (Tools.isNull(div))
            return;
        var control = new DivControl();
        control.initReport(data, this.uiInfo, $(div).parent()[0]);
    }

    this.switchToImage = function () {
        for (var index = 0; index < this.charts.length; index++) {
            var element = this.charts[index];
            var div = this.getCellDivById(element.id);
            var chart = div.options.control;
            chart.switchToImage();
        }

        for (var index = 0; index < this.divs.length; index++) {
            var element = this.divs[index];
            var div = this.getCellDivById(element.id);
            var divControl = div.options.control;
            divControl.switchToImage();
        }
    }

    this.getFrameControl = function (div) {
        return div.options.control;
    }

    this.getCellFrameControl = function (name) {
        var div = this.getCellDivByName(name);
        return this.getFrameControl(div);
    }

    this.switchImageToControl = function () {
        for (var index = 0; index < this.charts.length; index++) {
            var element = this.charts[index];
            var div = this.getCellDivById(element.id);
            var chart = div.options.control;
            chart.switchToChart();
        }

        for (var index = 0; index < this.divs.length; index++) {
            var element = this.divs[index];
            var div = this.getCellDivById(element.id);
            var divControl = div.options.control;
            divControl.switchToDiv();
        }

    }

    this.loadUpload = function (data) {
        var div = this.getControlByID(data.id);
        if (Tools.isNull(div))
            return;
        var control = new UploadControl();
        control.init(data, $(div).parent()[0]);
    }

    this.loadTree = function (data) {
        var header = data.data;
        var treeControl = new TreeControl();
        if (!Tools.isNull(header.OnNodeClick)) {
            treeControl.Event.OnNodeClick = header.OnNodeClick;
            delete header.OnNodeClick;
        }

        var onGetLoadParam = header.OnGetLoadParam;

        if (!Tools.isNull(header.OnGetLoadParam)) {
            treeControl.Event.OnGetLoadParam = header.OnGetLoadParam;
            delete header.OnGetLoadParam;
        }

        header.class = header.classname;
        delete header.classname;

        treeControl.init(header);

    }

    this.loadGrid = function (data) {
        var header = data.data;
        var dgControl = new DataGridControl();
        if (Tools.isNull(header.parent))
            header.tabParent = this.getCellDivById(data.id + "div");

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

        dgControl.load(header);

        if (!Tools.isNull(initData)) {
            dgControl.init(initData.hint, initData.command, initData.data);
        }

        dgControl.grid.borderStyle += ";border:0px";
    }

    this.setField = function (fieldname, value, defaultValue) {
        return (Tools.isNull(value) ? (defaultValue == undefined ? '' : ' ' + fieldname + '="' + defaultValue + '"') : ' ' + fieldname + '="' + value + '"');
    }

    this.createImageCell = function (header, divstyle, divWidth, divHeight) {

        var spanStr = '<span style="height:100%;display:inline-block;vertical-align:middle"></span>';
        var str = '<img' +
            this.setField('id', header.id) +
            this.setField('name', header.name) +
            this.setField('style', Tools.addString(header.style, "border:0;vertical-align:middle;max-width:95%;max-height:95%;")) +
            this.setField('class', header.styleClass) +
            this.setField('src', "image/" + header.value) +
            this.setField('alt', header.alt, "下载中。。。") +
            '/>';
        if (!Tools.isNull(header.isHref) && header.isHref) {
            if (Tools.isNull(header.href))
                str = "<a id='" + header.id + "_a' href='javascript:void(0)' onclick='GlobalReportOnHrefClick(this)'>" + str + '</a>';
            else {
                str = "<a id='" + header.id + "_a'" +
                    this.setField('href', header.href) +
                    this.setField('download', header.download) +
                    ">" + str + '</a>';

            }
        }
        return "<div style='table-layout:fixed;width:100%;height:100%'>" + spanStr + str + "</div>";
    }

    this.createProgressBarCell = function (header, textFontStyle, divWidth, divHeight) {
        var str = '<div ' +
            this.setField('id', header.id) +
            this.setField('name', header.name) +
            this.setField('style', Tools.addString(header.style,
                "position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);width:" +
                Tools.convetPX(divWidth, header.reportWidth) +
                ";line-height:" +
                Tools.convetPX(divHeight, header.reportHeight) + ";" + textFontStyle)) +
            this.setField('class', Tools.insertString(" ", header.styleClass, "mini-progressbar")) +
            this.setField('value', header.start) +
            // this.setField('max', header.size) +
            '>' +
            'abc' +
            '</div>';

        return str;
    }
    this.createCheckBoxCell = function (header, textFontStyle, divWidth) {
        var str = '<span style="height:100%;display:inline-block;vertical-align:middle"></span><input ' +
            this.setField('id', header.id) +
            this.setField('name', header.name) +
            this.setField('style', Tools.addString(header.style, ";width:100%;height:100%;")) +
            this.setField('class', Tools.insertString(" ", header.styleClass, "mini-checkbox")) +
            this.setField('trueValue', header.trueValue, 1) +
            this.setField('falseValue', header.falseValue, 0) +
            this.setField('required', header.required, "false") +
            this.setField('width', Tools.convetPX(divWidth, header.width)) +
            this.setField('height', header.height) +
            this.setField('onclick', header.onclick) +
            this.setField('value', header.value) +
            this.setField('onvaluechanged', header.onvaluechanged) +
            '>' +
            // this.getFieldValue(header.title) +
            '</input>';

        return str;
    }

    this.createRadioBoxCell = function (header, textFontStyle, divWidth) {
        var control = new RadioButtonListControl();
        header.radiocontrol = control;
        return control.getHtml(header);
    }

    this.getReportMiniControl = function (name) {
        if (Tools.isNull(this.div))
            return null;

        return mini.getByName(name, this.div);
    }

    this.getCellDivById = function (id) {
        if (Tools.isNull(this.div))
            return null;

        var control = $(this.div).find("#" + id)[0];

        return control;
    }

    this.getCellDivByName = function (name) {
        if (Tools.isNull(this.div))
            return null;

        return $(this.div).find("[name='" + name + "']")[0];
    }

    this.createEditorCell = function (div, data, typeid, divWidth, divHeight) {
        switch (data.editor.type) {
            case this.controlCommon.DATE_TYPE:
            case this.controlCommon.TIME_TYPE:
            case this.controlCommon.TEXTBOX_TYPE:
            case this.controlCommon.COMBOBOX_TYPE:
            case this.controlCommon.TEXTAREA_TYPE:
            case this.controlCommon.COMBOBOXTREE_TYPE:
                var magicDiv = new MagicDiv();
                data.editor.data.tabParent = data.editor.tabParent;
                var control = magicDiv.directGetControlByID(new DocumentWriter(), data.editor.data, null, typeid, false, 99999, div, divWidth, divHeight);
                if (Tools.isNull(control.options))
                    control.options = {};
                control.options.report = this;
                if (!Tools.isNull(control.getTextEl)) {
                    var el = control.getTextEl();
                    if (Tools.isNull(el.options))
                        el.options = {};
                    el.options.report = this;
                }
                return control;
            case this.controlCommon.LABEL_TYPE:
            case this.controlCommon.LISTBOX_TYPE:
            case this.controlCommon.IMAGE_TYPE:
            case this.controlCommon.BUTTON_TYPE:
            case this.controlCommon.GRID_TYPE:
            case this.controlCommon.TREE_TYPE:
            case this.controlCommon.SCROLLBAR_TYPE:
            case this.controlCommon.CHECKBOX_TYPE:
            default:
                return undefined;
        }
    }

    this.setDataSource = function (dataSource, rowIndex) {
        var row = dataSource.dataset[rowIndex];
        this.setDataSourceRow(row);
    }

    this.setDataSourceRow = function (row) {
        for (var id in this.items) {
            var item = this.items[id];
            if (Tools.isNull(item.editor.data.field))
                continue;

            var field = item.editor.data.field.field;
            if (row.hasOwnProperty(field))
                this.setCellValue(id, row[field], item.editor.type);
        };
    }

    this.setValue = function (data) {
        for (var id in this.items) {
            var item = this.items[id];
            if (Tools.isNull(item.editor.data.field))
                continue;
            var field = item.editor.data.field.field;
            if (data.hasOwnProperty(field))
                this.setCellValue(id, data[field], item.editor.type);
        };
    }

    this.getValue = function () {
        var result = {};
        for (var id in this.items) {
            var item = this.items[id];
            result[id] = this.getCellValue(id, item.editor.type);
        };
        return result;
    }

    this.getCellValue = function (id, controlType) {
        var control = this.getCellDivById(id);
        return this.getCellValueByControl(control, controlType);
    }

    this.getControlByName = function (name) {
        return $(this.div).find("[name='" + name + "']")[0];
    }

    this.getControlByID = function (id) {
        var div = this.getCellDivById(id);
        return div;
    }

    this.divToMiniObject = function (control) {
        if (!Tools.isNull(control)) {
            var name = control.name;
            if (Tools.isNull(name)) {
                if (!Tools.isNull(control.options))
                    name = control.options.name;
            }
            if (!Tools.isNull(name)) {
                var miniControl = mini.getByName(name, this.div);
                if (!Tools.isNull(miniControl))
                    control = miniControl;
            }
        }

        return control;
    }

    this.getCellValueByControl = function (control, controlType) {
        if (!Tools.isNull(this.editor)) {
            this.updateEditorValue();
        }

        if (Tools.isNull(controlType)) {
            controlType = this.items[control.id].editor.type;
        }

        switch (controlType) {
            case this.controlCommon.Chart_Type:
                return control.options.control.getImageUrl();
            case this.controlCommon.COMBOBOX_TYPE:
            case this.controlCommon.COMBOBOXTREE_TYPE:
            case this.controlCommon.BUTTON_TYPE:
            case this.controlCommon.DATE_TYPE:
            case this.controlCommon.LABEL_TYPE:
            case this.controlCommon.TIME_TYPE:
            case this.controlCommon.TEXTBOX_TYPE:
                data = control.options["data"]
                return data.editor.data.value;
            case this.controlCommon.RADIOBUTTONS_TYPE:
            case this.controlCommon.CHECKBOX_TYPE:
                return this.divToMiniObject(control).getValue();
            case this.controlCommon.ProgressBar_Type:
                return control.value;
            case this.controlCommon.IMAGE_TYPE:
                return control.src;
            case this.controlCommon.TEXTAREA_TYPE:
            case this.controlCommon.LISTBOX_TYPE:
                var rows = control.rows.length;
                var datas = [];
                for (var index = 0; index < rows; index++) {
                    var rowControl = this.getCellDivById(control.id + index);
                    datas.push(rowControl.innerText);
                }
                return datas.join("\n");
            case this.controlCommon.GRID_TYPE:
                return control.getData();
            case this.controlCommon.TREE_TYPE:
                var tc = control.options["treecontrol"];
                return tc.getData();
                // return tc.getCascadeSelectValues(tc.getRoot());
            default:
                return undefined;
        }

    }

    this.setCellValue = function (id, value, controlType) {
        var control = this.getCellDivById(id);
        this.setCellValueByControl(control, value, controlType);
    }

    this.setCellValueByControl = function (control, value, controlType) {

        if (Tools.isNull(controlType)) {
            controlType = this.items[control.id].editor.type;
        }

        switch (controlType) {
            case this.controlCommon.IMAGE_TYPE:
                control.src = value;
                break;
            case this.controlCommon.ProgressBar_Type:
                control.value = value;
                break;
            case this.controlCommon.CHECKBOX_TYPE:
            case this.controlCommon.RADIOBUTTONS_TYPE:
                this.divToMiniObject(control).setValue(value);
                break;
            case this.controlCommon.DATE_TYPE:
            case this.controlCommon.TIME_TYPE:
            case this.controlCommon.LABEL_TYPE:
            case this.controlCommon.TEXTBOX_TYPE:
            case this.controlCommon.COMBOBOXTREE_TYPE:
            case this.controlCommon.COMBOBOX_TYPE:
            case this.controlCommon.TEXTAREA_TYPE:
            case this.controlCommon.LISTBOX_TYPE:
                var header = control.options["data"];
                var divWidth = control.options["divWidth"];
                var divHeight = control.options["divHeight"];
                var allHeader = control.options["userdata"];

                switch (controlType) {
                    case this.controlCommon.LISTBOX_TYPE:
                        header.editor.data.data = value;
                        break;
                    default:
                        header.editor.data.value = value;
                        break;
                }
                var id = control.id;

                $(this.getCellDivById(id + "div")).remove(); //_layout
                var cellString = this.createCell(allHeader, header, this.uiInfo, divWidth, divHeight, false);
                var dw = new DocumentWriter();
                allHeader.tabParent = this.div;
                dw.write(allHeader, cellString);
                this.initControl(header.editor.data.name, allHeader, divWidth, divHeight);
                this.loadHref({
                    "id": id + "_a",
                    "text": header.editor.data.value,
                    "header": header.editor.data
                });
                break;
            case this.controlCommon.GRID_TYPE:
                return control.setData(value);
            case this.controlCommon.TREE_TYPE:
                control.loadList(value.data, value.idField, value.parentField);
            case this.controlCommon.BUTTON_TYPE:
            default:
                return undefined;
        }

    }

    this.getEditorValue = function () {
        if (Tools.isNull(this.editor) || Tools.isNull(this.controlType))
            return undefined;

        switch (this.controlType) {
            case this.controlCommon.DATE_TYPE:
            case this.controlCommon.TIME_TYPE:
                return this.editor.text;
            case this.controlCommon.TEXTBOX_TYPE:
            case this.controlCommon.TEXTAREA_TYPE:
            case this.controlCommon.COMBOBOXTREE_TYPE:
            case this.controlCommon.COMBOBOX_TYPE:
                return this.editor.value;
                // case this.controlCommon.TEXTAREA_TYPE:
                //     if (Tools.isNull(this.editor.value))
                //         return [];
                //     else {
                //         return this.editor.value.split("\n");
                //     }
            case this.controlCommon.LABEL_TYPE:
            case this.controlCommon.LISTBOX_TYPE:
            case this.controlCommon.IMAGE_TYPE:
            case this.controlCommon.BUTTON_TYPE:
            case this.controlCommon.GRID_TYPE:
            case this.controlCommon.TREE_TYPE:
            case this.controlCommon.SCROLLBAR_TYPE:
            case this.controlCommon.CHECKBOX_TYPE:
            default:
                return undefined;
        }
    }

    this.setEditorValue = function (value) {
        if (Tools.isNull(this.editor) || Tools.isNull(this.controlType))
            return undefined;

        switch (this.controlType) {
            case this.controlCommon.DATE_TYPE:
            case this.controlCommon.TIME_TYPE:
            case this.controlCommon.TEXTBOX_TYPE:
            case this.controlCommon.TEXTAREA_TYPE:
                $(this.editor).val(value);
                break;
            case this.controlCommon.COMBOBOXTREE_TYPE:
            case this.controlCommon.COMBOBOX_TYPE:
                this.editor.setValue(value);
                break;
                // case this.controlCommon.TEXTAREA_TYPE:
                //     if (Tools.isNull(this.editor.value))
                //         return [];
                //     else {
                //         return this.editor.value.split("\n");
                //     }
            case this.controlCommon.LABEL_TYPE:
            case this.controlCommon.LISTBOX_TYPE:
            case this.controlCommon.IMAGE_TYPE:
            case this.controlCommon.BUTTON_TYPE:
            case this.controlCommon.GRID_TYPE:
            case this.controlCommon.TREE_TYPE:
            case this.controlCommon.SCROLLBAR_TYPE:
            case this.controlCommon.CHECKBOX_TYPE:
            default:
                return undefined;
        }
    }

    this.updateEditorValue = function () {
        if (Tools.isNull(this.editControlID) || Tools.isNull(this.editor) || Tools.isNull(this.controlType))
            return false;

        var value = this.getEditorValue();
        $(this.editor).remove();
        // if (Tools.isNull(this.editor.destroy))
        //     $(this.editor).remove();
        // else
        //     this.editor.destroy();

        this.setCellValue(this.editControlID, value, this.controlType);

        this.editor = undefined;
        this.editControlID = undefined;
        this.controlType = undefined;

        return true;
    }

    this.initControl = function (name, header, divWidth, divHeight) {
        var data = this.nameItems[name];
        var id = data.editor.data.id;
        var control = this.getCellDivById(id);
        if (Tools.isNull(control.options))
            control.options = {};
        control.options["id"] = id;
        control.options["name"] = name;
        control.options["userdata"] = header;
        control.options["data"] = data;
        control.options["divWidth"] = divWidth;
        control.options["divHeight"] = divHeight;
        control.options["report"] = this;

        if (Tools.isNull(data))
            return;

        control = this.getCellDivById(id + "_layout");
        if (Tools.isNull(control))
            return;

        if (Tools.isNull(control.options))
            control.options = {};
        control.options["id"] = id;
        control.options["name"] = name;
        control.options["userdata"] = header;
        control.options["data"] = this.items[id];
        control.options["divWidth"] = divWidth;
        control.options["divHeight"] = divHeight;
        control.options["report"] = this;

        switch (data.editor.type) {
            case this.controlCommon.DATE_TYPE:
            case this.controlCommon.TIME_TYPE:
            case this.controlCommon.TEXTBOX_TYPE:
            case this.controlCommon.COMBOBOX_TYPE:
            case this.controlCommon.COMBOBOXTREE_TYPE:
            case this.controlCommon.TEXTAREA_TYPE:
                break;
            default:
                return;
        }

        if (data.editor.data.allowEdit) {
            magicSetOnClick(header.tabParent, name, ReportGlobalOnClick);
            magicSetOnClick(header.tabParent, name + "_layout", ReportGlobalOnClick);
        }
    }

    this.getControlValue = function (data, type) {
        var text = (Tools.isNull(data.value) ? "" : data.value);
        switch (type) {
            case this.controlCommon.DATE_TYPE:
            case this.controlCommon.TIME_TYPE:
                text = mini.formatDate(data.value, data.format);
                break;
        }
        return text;
    }

    this.getHrefHeader = function (data) {
        var href = "href='javascript:void(0)' onclick='GlobalReportOnHrefClick(this)'";
        if (!Tools.isNull(data.href)) {
            href = this.setField('href', data.href) +
                this.setField('download', data.download) +
                this.setField('target', data.target);
        }
        return href;
    }

    this.setStyle = function (data) {
        var classStr = "";
        if (!Tools.isNull(data.styleClass)) {
            classStr = "class='" + data.styleClass + "'";
        }
        if (!Tools.isNull(data.style)) {
            classStr += " style='" + data.style + "'";
        }

        if (!Tools.isNull(classStr))
            classStr = " " + classStr + " ";

        return classStr;
    }

    this.right = undefined;
    this.bottom = undefined;

    this.getBorderStyle = function (cellHeader) {
        var borderStyle = "border:0px";
        var allHeader = this.header;
        if (allHeader.border) {
            var lineColor = "#d3d3d3";
            if (!Tools.isNull(allHeader.lineColor))
                lineColor = Tools.convetColor(allHeader.lineColor);
            borderStyle = "border:1px solid " + lineColor + ";";

            if (!Tools.isNull(cellHeader)) {
                if (cellHeader.startCol == 0) {
                    borderStyle += "border-left: none;";
                }

                if (cellHeader.startRow == 0) {
                    borderStyle += "border-top: none;";
                }
                if (cellHeader.endCol == this.cols.length) {
                    borderStyle += "border-right: none;";
                }

                if (cellHeader.endRow == this.rows.length) {
                    borderStyle += "border-bottom: none;";
                }
            }
        }

        return borderStyle;
    }

    this.createCell = function (allHeader, cellHeader, uiInfo, divWidth, divHeight, isEditor) {
        var left = allHeader.border ? 1 : 0;
        for (var i = 0; i < cellHeader.startCol; i++) {
            left += this.cols[i].width;
        }
        var top = allHeader.border ? 1 : 0;
        for (var i = 0; i < cellHeader.startRow; i++) {
            top += this.rows[i].height;
        }
        var width = 0;
        for (var i = cellHeader.startCol; i < cellHeader.endCol; i++) {
            width += this.cols[i].width;
        }
        var height = 0;
        for (var i = cellHeader.startRow; i < cellHeader.endRow; i++) {
            height += this.rows[i].height;
        }

        var borderStyle = this.getBorderStyle(cellHeader);
        if (allHeader.border) {
            if (cellHeader.startCol == 0) {
                left++;
            }

            if (cellHeader.startRow == 0) {
                top++;
            }

            if (cellHeader.endCol == this.cols.length) {
                width -= 2;
            }

            if (cellHeader.endRow == this.rows.length) {
                height -= 2;
            }
        }

        switch (cellHeader.editor.type) {
            case this.controlCommon.CHECKBOX_TYPE:
            case this.controlCommon.IMAGE_TYPE:
            case this.controlCommon.LISTBOX_TYPE:
            case this.controlCommon.ListView_Type:
                // width--;
                // height--;
                break;
        }

        cellHeader.editor.data.left = left;
        cellHeader.editor.data.top = top;
        cellHeader.editor.data.width = width;
        cellHeader.editor.data.height = height;

        var data = cellHeader.editor.data;

        var backgroundColor = "";
        if (!Tools.isNull(allHeader.backgroundColor))
            backgroundColor = "background:" + Tools.convetColor(allHeader.backgroundColor) + ";";


        var style = "table-layout:fixed;overflow:hidden;" + borderStyle + backgroundColor + "position:absolute;left:" +
            left + "px;top:" + top + "px;width:" + width + "px;height:" + height + "px";

        var data = cellHeader.editor.data;
        var fontstyle = "";
        switch (data.fontStyle) {
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

        var fontweight = data.fontStyle == "1" || data.fontStyle == "3" ? "bold" : "normal";

        var textColor = Tools.convetColor(data.textColor);
        var textFontStyle = "word-wrap:break-word;font-family:'" + data.fontName + "';font-style:" + fontstyle + ";font-weight:" + fontweight + ";font-size:" + Tools.convetFontSize(data.fontSize) +
            ";color:" + textColor;

        var textFontCenterStyle = "line-height:" + height + "px;" + (Tools.isNull(data.style) ? "text-align:center" : data.style);

        var allStyles = "";
        switch (cellHeader.editor.type) {
            case this.controlCommon.CHECKBOX_TYPE:
            case this.controlCommon.IMAGE_TYPE:
                allStyles = style + ";text-align:center";
                break;
                // case this.controlCommon.ProgressBar_Type:
                //     allStyles = style + ";text-align:center";
                //     break;
            case this.controlCommon.TEXTAREA_TYPE:
            case this.controlCommon.LISTBOX_TYPE:
            default:
                allStyles = style + ";margin:0px;display:table;" + textFontStyle;
                // allStyles = style + ";" + textFontStyle + ";" + textFontCenterStyle;
                break;
        }

        var editor_id = data.id;
        var editor_name = data.name;

        if (isEditor) {
            if (!Tools.isNull(this.editor)) {
                this.updateEditorValue();
            }
            var div = this.getCellDivById(editor_id + "div");
            var control = this.getCellDivById(editor_id);
            var allHeader = control.options["userdata"];

            var value = this.getCellValue(editor_id, cellHeader.editor.type);

            var tableid = editor_id + "_layout";
            // if (Tools.isNull(mini.get(tableid))) {
            switch (cellHeader.editor.type) {
                case this.controlCommon.DATE_TYPE:
                case this.controlCommon.LABEL_TYPE:
                case this.controlCommon.TIME_TYPE:
                case this.controlCommon.TEXTBOX_TYPE:
                case this.controlCommon.COMBOBOX_TYPE:
                case this.controlCommon.COMBOBOXTREE_TYPE:
                case this.controlCommon.TEXTAREA_TYPE:
                case this.controlCommon.LISTBOX_TYPE:
                    this.getCellDivById(tableid).style.display = 'none';
                    break;
                default:
                    control.style.display = 'none';
                    break;
            }
            // }
            cellHeader.editor.width = width;
            cellHeader.editor.height = height;

            var oldid = cellHeader.editor.data.id;
            cellHeader.editor.data.id = oldid + "_editor";
            this.editor = this.createEditorCell(div, cellHeader, cellHeader.editor.type, divWidth, divHeight);
            magicSetOnKeyup(Report_GlobalOnKeyEsc);
            this.editor.options = {};
            this.editor.options["data"] = cellHeader;
            this.editor.options["report"] = this;

            cellHeader.editor.data.id = oldid;
            this.controlType = cellHeader.editor.type;
            this.editControlID = cellHeader.editor.data.id;

            this.setEditorValue(value);


            if (Tools.isNull(this.getReportMiniControl(editor_name)))
                this.editor.focus();
            else
                this.editor.getEl().focus();
            var el = document.activeElement;
            if (Tools.isNull(el.options))
                el.options = {};
            el.options["report"] = this;

            return;
        }

        var colorStyle = "";
        if (!data.useHrefColor) {
            colorStyle += ";color:" + textColor;
        }

        var cellString = "<div" +
            this.setField("style", allStyles) +
            this.setField('id', editor_id + "div") +
            this.setField('name', cellHeader.editor.name + "div") +
            ">";

        var padding = 5;

        var tableStyle = "table-layout:fixed; word-break: break-all; word-wrap: break-word;"; //"position:absolute;left:0px;top:0px;width:" + width + "px;height:" + height + "px;";

        var href = this.getHrefHeader(data);
        switch (cellHeader.editor.type) {
            case this.controlCommon.COMBOBOX_TYPE:
            case this.controlCommon.COMBOBOXTREE_TYPE:
                cellString += '<div id="' + editor_id + '_layout" name="' + editor_name + '_layout" style="display:table-cell;border:0px; vertical-align:middle"><table id="' +
                    editor_id +
                    '_table" align="' + (Tools.isNull(data.align) ? "center" : data.align) + '" border="0" cellpadding="0" cellspacing="0">';
                var text = "";
                if (!Tools.isNull(data.value) && !Tools.isNull(data.data)) {
                    var valueJson = JSON.parse(data.data);
                    valueJson.forEach(function (element) {
                        if (element[data.valueField] == data.value) {
                            text = element[data.textField];
                        }
                    }, this);
                } else if (!Tools.isNull(data.value)) {
                    text = data.value;
                }

                if (!Tools.isNull(data.isHref) && data.isHref) {
                    cellString += "<tr><td style='word-break: keep-all;white-space:nowrap;'><a id='" + editor_id + "_a' " + href + " style='" + colorStyle + "'" + "><span id='" +
                        editor_id + "' name='" + editor_name + "'" + this.setStyle(data) + ">" + text + "</span></td></tr>";
                    this.hrefs.push({
                        "id": editor_id + "_a",
                        "text": text,
                        "header": data
                    });
                } else {
                    cellString += "<tr><td><span id='" + editor_id + "' name='" + editor_name + "'" + this.setStyle(data) + ">" +
                        text + "</span></td></tr>";
                }
                cellString += "</table></div>";
                break;
            case this.controlCommon.DATE_TYPE:
            case this.controlCommon.TIME_TYPE:
            case this.controlCommon.LABEL_TYPE:
            case this.controlCommon.TEXTBOX_TYPE:
                var text = this.getControlValue(data, cellHeader.editor.type);

                cellString += '<div id="' + editor_id + '_layout" name="' + editor_name + '_layout" style="display:table-cell;border:0px; vertical-align:middle"><table id="' +
                    editor_id +
                    '_table" style="' + tableStyle + data.style +
                    '" align="' + (Tools.isNull(data.align) ? "center" : data.align) + '" border="0" cellpadding="0" cellspacing="0">';
                if (!Tools.isNull(data.isHref) && data.isHref) {

                    cellString += "<tr><td style='word-break: keep-all;white-space:nowrap;'><a id='" + editor_id + "_a' " + href + " style='" + colorStyle + "'" + "><span id='" + editor_id +
                        "' name='" + editor_name + "'" + this.setStyle(data) + ">" + text + "</span></td></tr>";
                    this.hrefs.push({
                        "id": editor_id + "_a",
                        "text": data.value,
                        "header": data
                    });
                } else {
                    cellString += "<tr><td style='word-break: keep-all;white-space:nowrap;'><span id='" + editor_id + "' name='" + editor_name + "'" + this.setStyle(data) + ">" +
                        (Tools.isNull(data.value) ? "" : data.value) + "</span></td></tr>";
                }
                cellString += "</table></div>";
                break;
            case this.controlCommon.ProgressBar_Type:
                cellString += this.createProgressBarCell(data, textFontStyle, divWidth, divHeight); //this.createCheckBoxCell(data.value == "true" ? "icon_box-checked.png" : "icon_box-empty.png");
                break;
            case this.controlCommon.CHECKBOX_TYPE:
                cellString += this.createCheckBoxCell(data, textFontStyle, divWidth); //this.createCheckBoxCell(data.value == "true" ? "icon_box-checked.png" : "icon_box-empty.png");
                break;
            case this.controlCommon.RADIOBUTTONS_TYPE:
                cellString += this.createRadioBoxCell(data, textFontStyle, divWidth); //this.createCheckBoxCell(data.value == "true" ? "icon_box-checked.png" : "icon_box-empty.png");
                data.textFontStyle = textFontStyle;
                this.radios.push(data);
                break;
            case this.controlCommon.IMAGE_TYPE:
                cellString += this.createImageCell(data, "width:" + width + "px;height:" + height + "px", divWidth, divHeight);
                if (!Tools.isNull(data.isHref) && data.isHref)
                    this.hrefs.push({
                        "id": editor_id + "_a",
                        "text": data.value,
                        "header": data
                    });
                break;
            case this.controlCommon.TEXTAREA_TYPE:
                cellString += '<div id="' + editor_id + '_layout" name="' + editor_name + '_layout" style="table-layout:fixed;display:table-cell; border:0px; vertical-align:middle;"><table id="' + editor_id + '" name="' +
                    editor_name + '" style="' + tableStyle + data.style +
                    '" align="' + (Tools.isNull(data.align) ? "center" : data.align) + '" border="0" cellpadding="0" cellspacing="0">';
                if (!Tools.isNull(data.value)) {
                    var strs = new Array();
                    strs = data.value.split("\n");
                    for (var i = 0; i < strs.length; i++) {
                        if (!Tools.isNull(data.isHref) && data.isHref) {
                            var id = editor_id + "_a" + i;
                            cellString += "<tr style='line-height:normal'><td style='word-break:keep-all;white-space:nowrap;'><a id='" + id + "' " + href + " style='" + colorStyle + "'" + "><span id='" + editor_id + i + "' style='" + (Tools.isNull(data.style) ? "" : data.style) + "'>" + strs[i] + "</span></td></tr>";
                            this.hrefs.push({
                                "id": id,
                                "text": strs[i],
                                "header": data
                            });
                        } else {
                            cellString += "<tr style='line-height:normal'><td style='word-break:keep-all;white-space:nowrap;'><span  id='" + editor_id + i + "'" + this.setStyle(data) + ">" + strs[i] + "</span></td></tr>";
                        }
                    }
                }
                cellString += "</table></div>";
                break;
            case this.controlCommon.LISTBOX_TYPE:
                if (!Tools.isNull(data.data)) {
                    var json = data.data;
                    if (!Tools.isObject(json))
                        json = JSON.parse(json);
                    if (!Tools.isNull(json)) {
                        cellString += '<div id="' + editor_id + '_layout" name="' + editor_name + '_layout" style="' + tableStyle + 'display:table-cell; border:0px; vertical-align:middle;"><table id="' +
                            editor_id + '" name="' + editor_name + '" align="' + (Tools.isNull(data.align) ? "center" : data.align) +
                            '" border="0" cellpadding="0" cellspacing="0">';
                        for (var i = 0; i < json.length; i++) {
                            if (!Tools.isNull(data.isHref) && data.isHref) {
                                href = this.getHrefHeader(json[i]);
                                var id = editor_id + "_a" + i;
                                cellString += "<tr><td style='word-break: keep-all;white-space:nowrap;'><a id='" + id + "' " + href + " style='" + colorStyle + "'" + "><span  id='" + editor_id + i + "'" + this.setStyle(data) + ">" + json[i].text + "</span></td></tr>";
                                this.hrefs.push({
                                    "id": id,
                                    "text": json[i].text,
                                    "header": data
                                });
                            } else {
                                cellString += "<tr>" + "<td><span id='" + editor_id + i + "'" + this.setStyle(data) + ">" + json[i].text + "</span></td></tr>";
                            }
                        }
                        cellString += "</table></div>";
                    }
                }
                break;
            case this.controlCommon.BUTTON_TYPE:
                cellString += '<div id="' + editor_id + '_layout" name="' + editor_name + '_layout" style="display:table-cell; border:0px; vertical-align:middle"><table id="' +
                    editor_id + '_table" style="' + tableStyle + data.style +
                    '" align="' + (Tools.isNull(data.align) ? "center" : data.align) + '" border="0" cellpadding="0" cellspacing="0">';
                cellString += "<tr><td style='word-break: keep-all;white-space:nowrap;'><a id='" + editor_id + "_a' " + href + "><span id='" + editor_id + "' name='" + editor_name + "'" + this.setStyle(data) + ">" + (Tools.isNull(data.value) ? "" : data.value) + "</span></td></tr>";
                this.hrefs.push({
                    "id": editor_id + "_a",
                    "text": data.value,
                    "header": data
                });
                cellString += "</table></div>";
                break;
            case this.controlCommon.Chart_Type:
                var chart = new ChartControl();
                var chartheader = chart.getHtml(data);
                this.charts.push(data);

                cellString += "<div" +
                    this.setField("style", "position:absolute;left:" +
                        (padding) + "px;top:" + (padding) + "px;width:" + (width - padding * 2) + "px;height:" + (height - padding * 2) + "px") +
                    this.setField('id', editor_id + "layout") +
                    this.setField('name', cellHeader.editor.name + "layout") +
                    ">";
                cellString += chartheader;
                cellString += "</div>";
                break;
            case this.controlCommon.ListView_Type:
                var listview = new ListViewControl();
                var listviewheader = listview.getHtml(data);
                this.listviews.push(data);

                cellString += "<div" +
                    this.setField("style", "position:absolute;left:" +
                        (padding) + "px;top:" + (padding) + "px;width:" + (width - padding * 2) + "px;height:" + (height - padding * 2) + "px") +
                    this.setField('id', editor_id + "layout") +
                    this.setField('name', cellHeader.editor.name + "layout") +
                    ">";
                cellString += listviewheader;
                cellString += "</div>";
                break;
            case this.controlCommon.Upload_Type:
                var upload = new UploadControl();
                var uploadheader = upload.getHtml(data);
                this.uploads.push(data);
                cellString += "<div" +
                    this.setField("style", "position:absolute;left:" +
                        (padding) + "px;top:" + (padding) + "px;width:" + (width - padding * 2) + "px;height:" + (height - padding * 2) + "px") +
                    this.setField('id', editor_id + "layout") +
                    this.setField('name', cellHeader.editor.name + "layout") +
                    ">";
                cellString += uploadheader;
                cellString += "</div>";

                break;
            case this.controlCommon.GRID_TYPE:
                var grid = new DataGridControl();
                try {
                    var tmp = JSON.parse(data.header).header;
                    data.header = tmp;
                } catch (error) {}
                data.header.width = width - 5;
                data.header.height = height - 5;
                var gridheader = grid.getHtml(data);
                var griddata = {
                    "id": editor_id,
                    "name": editor_name,
                    "data": data
                };
                this.grids.push(griddata);
                cellString += gridheader;
                break;
            case this.controlCommon.TREE_TYPE:
                var tree = new TreeControl();
                data.width = width - 1;
                data.height = height - 1;
                var treeheader = tree.getHtml(data, divWidth, divHeight);
                var treedata = {
                    "id": editor_id,
                    "name": editor_name,
                    "data": data
                };
                this.trees.push(treedata);
                cellString += treeheader;
                break;
            case this.controlCommon.Div_Type:
                var div = new DivControl();
                data.width = width - 1;
                data.height = height - 1;
                var divHeader = div.getHtml(data, undefined, "100%", "100%", false);
                var divdata = {
                    "id": editor_id,
                    "name": editor_name,
                    "data": data
                };
                this.divs.push(divdata);
                cellString += divHeader;
                break;
            case this.controlCommon.SCROLLBAR_TYPE:
                data.width = width - 1;
                data.height = height - 1;
                var scrollControl = new ScrollBarControl();
                var scrollheader = scrollControl.getHtml(data);
                var scrolldata = {
                    "id": editor_id,
                    "name": editor_name,
                    "data": data
                };
                this.scrolls.push(scrolldata);
                cellString += scrollheader;
                break;

        }

        cellString += "</div>";

        return cellString;
    }

    this.resetRowColumn = function (div) {
        var width = $(div).width();
        var height = $(div).height();

        var rowHeight = 0;
        var rows = {};
        for (var index = 0; index < this.rows.length; index++) {
            var element = this.rows[index];
            rowHeight += element.height;
        }
        for (var index = 0; index < this.rows.length; index++) {
            var element = this.rows[index];
            rows[index] = element.height / rowHeight;
        }

        var colWidth = 0;
        var cols = {};
        for (var index = 0; index < this.cols.length; index++) {
            var element = this.cols[index];
            colWidth += element.width;
        }
        for (var index = 0; index < this.cols.length; index++) {
            var element = this.cols[index];
            cols[index] = element.width / colWidth;
        }

        for (var index = 0; index < this.rows.length; index++) {
            this.rows[index].height = Math.floor(height * rows[index]);
        }
        for (var index = 0; index < this.cols.length; index++) {
            this.cols[index].width = Math.floor(width * cols[index]);
        }
    }

    this.getDataSourceId = function () {
        if (Tools.isNull(header.datasource))
            return [];

        var result = [];
        for (const dsid in header.datasource) {
            result.push(dsid);
        }

        return result;
    }

    this.initDataSource = function (params) {
        var header = this.header;

        var datasource = header.datasource;

        if (Tools.isNull(datasource))
            return;

        var controlInfos = [];
        for (var index = 0; index < header.cells.length; index++) {
            info = header.cells[index].editor;
            if (!Tools.isNull(info.data.dataSource) &&
                !Tools.isNull(info.data.field) &&
                !Tools.isNull(datasource[info.data.dataSource]))
                controlInfos.push(info.data);
        }

        var report = this;
        GlobalDataSources.loadData(datasource, controlInfos,
            function (controlId, controlName, value) {
                report.setCellValue(controlId, value);
            }, params);

    }

    this.write = function (dw, globalheader, uiInfo, divWidth, divHeight, colWidths, rowHeights) {

        var v = JSON.stringify(globalheader);
        var header = JSON.parse(v);
        header.tabParent = globalheader.tabParent;

        this.header = header;

        var borderStyle = this.getBorderStyle();

        var headerString = '<div style="table-layout:fixed;width:100%;height:100%;' + borderStyle + '"' +
            this.setField('id', header.id) +
            this.setField('name', header.name) +
            this.setField('typecode', header.type) +
            this.setField('typename', header.typename) +
            this.setField('class', header.styleClass) +
            '>';

        this.rows = header.rows;
        this.cols = header.cols;
        this.uiInfo = uiInfo;
        this.id = header.id;
        this.name = header.name;
        this.cells = header.cells;

        if (Tools.isNull(colWidths) || Tools.isNull(rowHeights))
            this.resetRowColumn(header.tabParent);
        else {
            this.cols = colWidths;
            this.rows = rowHeights;
        }

        for (var i = 0; i < header.cells.length; i++) {
            header.cells[i].typename = header.cells[i].editor.typename;
            header.cells[i].editor.type = this.controlCommon.convertType(header.cells[i].editor.typename);
            headerString += this.createCell(header, header.cells[i], uiInfo, divWidth, divHeight);
            this.items[header.cells[i].editor.data.id] = header.cells[i];
            this.nameItems[header.cells[i].editor.data.name] = header.cells[i];
        }

        headerString += '</div>';

        dw.write(header, headerString);

        mini.parse();

        var div = $(header.tabParent).find("#" + header.id)[0];

        this.div = div;

        div.options = {};
        div.options.report = this;
        div.report = this;
        div.options.frameobj = this;
        for (var key in this.nameItems) {
            this.initControl(key, header, divWidth, divHeight);
        };

        this.grids.forEach(function (element) {
            this.loadGrid(element);
        }, this);

        this.trees.forEach(function (element) {
            this.loadTree(element);
        }, this);

        this.hrefs.forEach(function (element) {
            this.loadHref(element);
        }, this);

        this.scrolls.forEach(function (element) {
            this.loadScrollbar(element);
        }, this);

        this.listviews.forEach(function (element) {
            this.loadListView(element);
        }, this);

        this.uploads.forEach(function (element) {
            this.loadUpload(element);
        }, this);

        this.charts.forEach(function (element) {
            this.loadChart(element);
        }, this);

        this.divs.forEach(function (element) {
            this.loadDiv(element);
        }, this);

        this.radios.forEach(function (element) {
            this.loadRadio(element);
        }, this);

        var $selector = $(div).parent();
        $selector.width($selector.width() + 1);
        $selector.height($selector.height() + 1);

        if (this.header.autoInitDataSource)
            this.initDataSource();

    }

    this.print = function () {
        this.switchToImage();
        Tools.printDiv($(this.div).parent(), false, Tools.getDpiScaleFromUIInfo(this.uiInfo));
        this.switchImageToControl();
    }

}