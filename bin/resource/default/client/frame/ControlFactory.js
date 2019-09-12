function ControlFactory() {
    this.charts = []
    this.toolbars = []
    this.divs = []
    this.lookups = []
    this.uiInfo
    this.timers = []
    this.controlCommon = new ControlCommon()

    this.relocationToolbars = function (uiInfo) {
        for (var index = 0; index < this.toolbars.length; index++) {
            var element = this.toolbars[index]
            var toolbar = element.options.toolbarcontrol
            var json = toolbar.data
            if (Tools.isNull(json.attachControlName))
                continue
            var parent = getFrameControlByName(uiInfo, json.attachControlName)
            if (Tools.isNull(parent))
                continue
            parent = $('#' + parent.options.element.id + 'div')[0]

            element = $('#' + element.id + 'div')[0]
            var left = $(parent).position().left
            var top = 0
            $(element).width($(parent).width())
            switch (json.attatchWay) {
                case 'awTop':
                    top = $(parent).position().top - $(element).height() - 1 - json.attatchSpace
                    break
                case 'awBottom':
                    top = $(parent).position().top + $(parent).height() + 1 + json.attatchSpace
                    break
            }

            $(element).css('left', left)
            $(element).css('top', top)
        }
    }

    this.getValue = function (value, defaultValue) {
        if (Tools.isNull(value))
            if (Tools.isNull(defaultValue))
                return ''
        else
            return defaultValue
        else
            return value
    }

    this.setField = function (fieldname, value, defaultValue) {
        return (Tools.isNull(value) ? (defaultValue == undefined ? '' : ' ' +
            fieldname + '="' + defaultValue + '"') : ' ' + fieldname + '="' + value + '"')
    }

    this.setTabs = function (controls) {
        var sortdata = []
        controls.forEach(function (element) {
            // var elements = document.querySelectorAll("*")

            // [].forEach.call(elements, function(element) {
            //     element.tabindex = -1
            // })

            var header = element.options['userdata'].data
            switch (header.type) {
                case this.controlCommon.CHECKBOX_TYPE:
                case this.controlCommon.DATE_TYPE:
                case this.controlCommon.TIME_TYPE:
                case this.controlCommon.COMBOBOX_TYPE:
                case this.controlCommon.COMBOBOXTREE_TYPE:
                case this.controlCommon.INT_TYPE:
                case this.controlCommon.PASSWORD_TYPE:
                case this.controlCommon.TEXTAREA_TYPE:
                case this.controlCommon.BUTTON_TYPE:
                case this.controlCommon.TEXTBOX_TYPE:
                    // case this.controlCommon.RADIOBUTTONS_TYPE:
                    // case this.controlCommon.LISTBOX_TYPE:
                    // case this.controlCommon.GRID_TYPE:
                    // case this.controlCommon.TREE_TYPE:
                    if (header.tabindex > 0)
                        sortdata.push(element)
                    break
            }
        }, this)

        sortdata = sortdata.sort(function (a, b) {
            var aindex = a.options['tabindex']
            var bindex = b.options['tabindex']

            var isA = !Tools.isNull(aindex)
            var isB = !Tools.isNull(bindex)
            if (isA && isB)
                return aindex - bindex
            else {
                if (isA) {
                    return aindex
                } else if (isB)
                    return bindex
                else
                    return 1
            }
        })

        var index = 1
        sortdata.forEach(function (element) {
            element.options['element'].tabindex = index++
        }, this)
    }

    this.directGetControlByName = function (dw, header, uiData, typename, needDiv, zOrder, parent, divWidth, divHeight) {
        var typeid = this.controlCommon.convertType(typename)
        return this.directGetControlByID(dw, header, uiData, typeid, needDiv, zOrder, parent, divWidth, divHeight)
    }

    this.directGetControlByID = function (dw, header, uiData, typeid, needDiv, zOrder, parent, divWidth, divHeight) {
        header.type = typeid
        header.zOrder = zOrder
        var prepareControl = this.createColumn(dw, divWidth, divHeight, header, uiData, parent, needDiv)
        if (header.prepare)
            return prepareControl
        else
            return getFrameControlByName(uiData, header.name)
    }

    this.getControl = function (dw, divWidth, divHeight, data, uiData, parent, userObj, proc) {
        // try{
        var prepareControl = this.directGetControlByName(dw, data.data, uiData, data.typename, true, data.zOrder, parent, divWidth, divHeight)
        if (data.data.prepare)
            return prepareControl

        var subWidth = 0
        var subHeight = 0
        var controlWidth = 0
        var controlHeight = 0

        this.id = data.data.id
        var control = mini.get(data.data.id)
        var isMiniUI = true
        if (Tools.isNull(control)) {
            control = $('#' + data.data.id)[0]
            isMiniUI = false
        }

        if (Tools.isNull(control))
            return undefined

        var resizeControl = control
        if (isMiniUI) {
            resizeControl = control.getEl()
        }

        controlWidth = $(resizeControl).width()
        controlHeight = $(resizeControl).height()

        switch (data.data.type) {
            case this.COMBOBOX_TYPE:
            case this.COMBOBOXTREE_TYPE:
                $(resizeControl).css('margin-top', 12)
                break
            default: {
                if (controlWidth > 0 && controlHeight > 0) {
                    subWidth = controlWidth - Tools.convetPXToValue(divWidth, data.data.width, 0)
                    subHeight = controlHeight - Tools.convetPXToValue(divHeight, data.data.height, 0)
                    $(resizeControl).css('margin-top', -subHeight / 2)
                    // $(resizeControl).css("margin-left", -subWidth / 2)
                }
            }
        }
    }

    this.maxwidth = 0
    this.maxheight = 0

    this.minLeft = 0
    this.minTop = 0
    this.controls = []
    this.allControls = []

    this.createControl = function (item, dw, divWidth, divHeight, data, div, newTabProc, needFixPosition) {
        var magicDiv = new MagicDiv()
        item.data.prepare = false
        var control = magicDiv.getControl(dw, divWidth, divHeight, item, data, div, this, newTabProc)
        if (!Tools.isNull(control)) {
            if (control.options['userdata'].data.width != '100%') {
                var cx = control.options['x'] + control.options['width']
                if (cx > this.maxwidth)
                    this.maxwidth = cx

                if (control.options['x'] > 0 && control.options['x'] < this.minLeft)
                    this.minLeft = control.options['x']
            }

            if (control.options['userdata'].data.height != '100%') {
                var cy = control.options['y'] + control.options['height']
                if (cy > this.maxheight)
                    this.maxheight = cy

                if (control.options['y'] > 0 && control.options['y'] < this.minTop)
                    this.minTop = control.options['y']
            }

            if (needFixPosition)
                this.controls.push(control)
            this.allControls.push(control)

            if (control.options['userdata'].data.type == this.controlCommon.Toolbar_Type) {
                this.toolbars.push($('#' + control.options['userdata'].data.id)[0])
            }

            if (control.options['userdata'].data.type == this.controlCommon.Div_Type) {
                this.divs.push(control)
            }

            if (control.options['userdata'].data.type == this.controlCommon.Chart_Type) {
                this.charts.push(control)
            }

            if (control.options['userdata'].data.type == this.controlCommon.COMBOBOX_TYPE) {
                if (control.options['userdata'].data.mode == "lookup") {
                    this.lookups.push(control)
                }
            }

        }
        return control
    }

    this.createTreeDiv = function (dw, controlInfo, treeNodes, controls, divWidth, divHeight, data, div, newTabProc, needFixPosition) {
        var id = controlInfo.id
        var treeNode = treeNodes[id]
        if (!Tools.isNull(treeNode['offx'])) {
            controlInfo.data.left_old = controlInfo.data.left
            controlInfo.data.left = treeNode['offx'] + 'px'
            controlInfo.data.top_old = controlInfo.data.top
            controlInfo.data.top = treeNode['offy'] + 'px'
        }
        var curDiv = this.createControl(controlInfo, dw, divWidth, divHeight, data, div, newTabProc, needFixPosition)

        switch (controlInfo.data.type) {
            case this.controlCommon.Div_Type:
                break
            default:
                return curDiv
        }

        if (curDiv.type) {
            curDiv = curDiv.getEl();
        }
        var childs = treeNode.childs
        var width = $(curDiv).width()
        var height = $(curDiv).height()
        for (var index = 0; index < childs.length; index++) {
            var childInfo = controls[childs[index]]
            if (Tools.isNull(childInfo))
                continue
            this.createTreeDiv(dw, childInfo, treeNodes, controls, width, height, data, curDiv, newTabProc, false)
        }

        return curDiv
    }

    this.createTreeDivs = function (dw, treeNodes, controls, roots, divWidth, divHeight, data, div, newTabProc) {
        for (var index = 0; index < roots.length; index++) {
            var root = roots[index]
            this.createTreeDiv(dw, controls[root], treeNodes, controls, divWidth, divHeight, data, div, newTabProc, true)
        }
    }

    this.getDataSourceId = function () {
        var datasource = uiInfo.page.datasource
        if (Tools.isNull(datasource))
            return [];

        var result = [];
        for (const dsid in datasource) {
            result.push(dsid);
        }

        return result;
    }

    this.initDataSource = function (params) {
        var uiInfo = this.uiInfo;
        var datasource = uiInfo.page.datasource
        if (Tools.isNull(datasource))
            return

        var cc = new ControlCommon()
        var controlInfos = []
        for (var index = 0; index < uiInfo.data.length; index++) {
            info = uiInfo.data[index]

            if (info.data.type == cc.REPORT_TYPE) {
                if (!uiInfo.workflow.useReportDataSource)
                    continue;
            }
            if (!Tools.isNull(info.data.dataSource) &&
                !Tools.isNull(info.data.field) &&
                !Tools.isNull(datasource[info.data.dataSource]))
                controlInfos.push(info.data)
        }

        GlobalDataSources.loadData(datasource, controlInfos, function (controlId, controlName, value) {
            var control = getFrameControlByName(uiInfo, controlName)
            if (!Tools.isNull(control)) {
                setFrameControlValue(control, value)
            }
        }, params)
    }

    this.checkNoCompute = function (element) {
        if (element.xAlign == "alRight" ||
            element.xAlign == "alCenter" ||
            element.yAlign == "alBottom" ||
            element.nocompute ||
            element.yAlign == "alCenter") {
            return true;
        } else
            return false;
    }

    this.createCheckTreeInfo = function (uiInfo, node, notCompute) {
        if (!uiInfo.allDrawInfoMap.hasOwnProperty(node.id))
            return;

        var element = uiInfo.allDrawInfoMap[node.id].data;
        if (!notCompute)
            notCompute = this.checkNoCompute(element);
        if (notCompute) {
            uiInfo.noComputes[element.id] = element;
        }
        if (!node.hasOwnProperty("childs") || node.childs.length == 0)
            return;

        var childs = node.childs;
        for (var index = 0; index < childs.length; index++) {
            var childInfo = uiInfo.treeLayouts[childs[index]]
            if (Tools.isNull(childInfo))
                continue;
            this.createCheckTreeInfo(uiInfo, childInfo, notCompute);
        }
    }

    this.createCheckTreeInfos = function (uiInfo) {
        uiInfo.treeLayouts = {};
        uiInfo.noComputes = {};
        uiInfo.allControlInfoMap = {};
        uiInfo.allDrawInfoMap = {};
        for (var index = 0; index < uiInfo.data.length; index++) {
            var element = uiInfo.data[index].data;

            uiInfo.allDrawInfoMap[uiInfo.data[index].id] = uiInfo.data[index];
            uiInfo.allControlInfoMap[element.id] = element;
        }
        for (var index = 0; index < uiInfo.tree.nodes.length; index++) {
            var node = uiInfo.tree.nodes[index];
            uiInfo.treeLayouts[node.id] = node;
        }

        for (var index = 0; index < uiInfo.tree.roots.length; index++) {
            var id = uiInfo.tree.roots[index];
            this.createCheckTreeInfo(uiInfo, uiInfo.treeLayouts[id], false);
        }
    }

    this.computeSize = function (uiInfo, datas, ignorePercentage, maxWidth, maxHeight, divWidth, divHeight) {
        var percentageElements = []
        var result = {
            width: maxWidth,
            height: maxHeight
        }
        datas.forEach(function (element) {
            var width = 0
            var height = 0
            var left = 0
            var top = 0

            var useCompute = false
            if (ignorePercentage) {
                useCompute = Tools.isPercentageValue(element.data.width) ||
                    Tools.isPercentageValue(element.data.height) ||
                    Tools.isPercentageValue(element.data.left) ||
                    Tools.isPercentageValue(element.data.top)
            }
            if (!useCompute) {
                if (this.checkNoCompute(element.data))
                    return;

                width = Tools.convetPXToValue(divWidth, element.data.width, 0, ignorePercentage)
                height = Tools.convetPXToValue(divHeight, element.data.height, 0, ignorePercentage)
                left = Tools.convetPXToValue(divWidth, element.data.left, 0, ignorePercentage)
                top = Tools.convetPXToValue(divHeight, element.data.top, 0, ignorePercentage)
                width += left
                height += top

                if (width > result.width)
                    result.width = width
                if (height > result.height)
                    result.height = height
            } else {
                if (ignorePercentage)
                    percentageElements.push(element)
            }
        }, this)

        return {
            elements: percentageElements,
            width: result.width,
            height: result.height
        }
    }

    this.computeFormSize = function (uiInfo, width, height) {
        if (Tools.isNull(width) || width <= 0)
            width = uiInfo.page.size.width
        if (Tools.isNull(height) || height <= 0)
            height = uiInfo.page.size.height

        var ignorePercentage = true
        var dialogAutoSize = !uiInfo.workflow.hasOwnProperty('dialogAutoSize') || uiInfo.workflow.dialogAutoSize
        var isDialog = !Tools.isNull(uiInfo.workflow) && uiInfo.workflow.useDialog == 'true'
        if (isDialog) {
            ignorePercentage = dialogAutoSize
        }

        var maxWidth = !ignorePercentage ? width : 0
        var maxHeight = !ignorePercentage ? height : 0

        var sizeInfo = this.computeSize(uiInfo, uiInfo.data, ignorePercentage, maxWidth, maxHeight, width, height)
        var percentageElements = sizeInfo.elements
        if (ignorePercentage) {
            maxWidth = ((!isDialog || (isDialog && !dialogAutoSize)) && width > sizeInfo.width) ? width : sizeInfo.width
            maxHeight = ((!isDialog || (isDialog && !dialogAutoSize)) && height > sizeInfo.height) ? height : sizeInfo.height
            sizeInfo = this.computeSize(uiInfo, percentageElements, false, maxWidth, maxHeight, width, height)
        }

        var formWidth = sizeInfo.width
        var formHeight = sizeInfo.height

        var size = {
            width: width,
            height: height
        };
        if (uiInfo.workflow.useFrame != "true" || isDialog) {
            size = getFrameContentWindowSize()
        }
        var decWidth = Tools.getScrollbarWidth()
        var decHeight = Tools.getScrollbarWidth()
        if (isDialog) {
            size.height -= GlobalDialogDefine.titleHeight
        }

        var hasVScrollBar = formHeight - 1 > size.height && uiInfo.workflow.showVerticalScrollBar
        var hasHScrollBar = formWidth - 1 > size.width && uiInfo.workflow.showHorizontalScrollBar

        if (hasVScrollBar && hasHScrollBar) {
            if (isDialog) {
                if (dialogAutoSize) {
                    if (formWidth - 1 > size.width) {
                        formWidth -= decWidth;
                    } else
                        formWidth += decWidth;

                    if (formHeight - 1 > size.height)
                        formHeight -= decHeight;
                    else
                        formHeight += decHeight;
                } else {
                    formWidth -= decWidth
                    formHeight -= decHeight
                }
            } else {
                formWidth += decWidth
            }
        } else if (hasVScrollBar) {
            formWidth -= decWidth
        } else if (hasHScrollBar) {
            formHeight -= decHeight
        }

        return {
            divWidth: formWidth,
            divHeight: formHeight,
            hasHScrollBar: hasHScrollBar,
            hasVScrollBar: hasVScrollBar
        }
    }

    this.write = function (dw, data, width, height, newTabProc, onParentResize) {
        this.createCheckTreeInfos(data);
        var id = data.workflow.id
        var name = data.workflow.name
        var headerString = '<div style="margin:0;position:relative;padding:0;width:100%;height:100%'
        if (!Tools.isNull(data.page.border) && data.page.border == 'true') {
            headerString += ';border:1px solid #FF0000'
        } else
            headerString += ';border:0px'

        if (!Tools.isNull(data.page.color))
            headerString += ';background:' + Tools.convetColor(data.page.color)

        var divID = id + 'div'
        headerString += '"' +
            this.setField('id', divID) +
            this.setField('name', name) +
            '>'
        headerString += '</div>'
        dw.write(data, headerString)

        var div = $('#' + divID)[0]
        if (div == undefined)
            return false

        var isDialog = data.workflow.useDialog == 'true'
        var sizeInfo = this.computeFormSize(data, width, height)
        var scrollDiv = data.tabParent
        var dialogAutoSize = data.workflow.dialogAutoSize

        $(div).width(sizeInfo.divWidth)
        $(div).height(sizeInfo.divHeight)

        this.divWidth = sizeInfo.divWidth
        this.divHeight = sizeInfo.divHeight
        var vspace = 0
        var hspace = 0
        this.maxwidth = 0
        this.maxheight = 0
        this.minLeft = this.divWidth
        this.minTop = this.divHeight

        if (isDialog && dialogAutoSize) {
            width = sizeInfo.divWidth
            height = sizeInfo.divHeight
        }

        $(div).css('overflow', 'hidden')
        $(scrollDiv).css('overflow-x', data.workflow.showHorizontalScrollBar ? 'auto' : 'hidden')
        $(scrollDiv).css('overflow-y', data.workflow.showVerticalScrollBar ? 'auto' : 'hidden')

        div.options = {}
        div.options['uidata'] = data

        if (!Tools.isNull(data.buttonrole)) {
            data.buttonRule = {}
            for (var key in data.buttonrole) {
                var tmps = key.split('\.')
                if (tmps[0] == data.workflow.id) {
                    data.buttonRule[tmps[1]] = data.buttonrole[key]
                }
            }
        }

        var treeNodes = {}
        for (var index = 0; index < data.tree.nodes.length; index++) {
            var treeNode = data.tree.nodes[index]
            treeNodes[treeNode.id] = treeNode
        }
        data.tree['treeNodes'] = treeNodes

        var controls = {}
        for (var index = 0; index < data.data.length; index++) {
            var control = data.data[index]
            controls[control.id] = control
        }
        data.controls = controls
        var roots = data.tree.roots
        for (i = 0; i < data.data.length; i++) {
            var item = data.data[i]
            if (!Tools.isNull(treeNodes[item.id]))
                continue

            if (item.data.type == this.controlCommon.Timer_Name) {
                this.timers.push(item);
                continue;
            }

            this.createControl(item, dw, width, height, data, div, newTabProc, true)
        }

        this.createTreeDivs(dw, treeNodes, controls, roots, width, height, data, div, newTabProc)

        var pageWidth = Tools.convetPXToValue(0, data.page.size.width, 0);
        var pageHeight = Tools.convetPXToValue(0, data.page.size.height, 0);
        this.controls.forEach(function (control) {
            var controlDiv = control.options['div']
            if (data.workflow.showHorizontalScrollBar && control.options['userdata'].data.width == "100%") {
                $(controlDiv).width(this.divWidth);
            }
            if (data.workflow.showVerticalScrollBar && control.options['userdata'].data.height == "100%") {
                $(controlDiv).height(this.divHeight);
            }

            if (control.options['userdata'].data.xAlign == "alRight") {
                var size = Tools.convetPXToValue(width, control.options['userdata'].data.left, 0)
                $(controlDiv).css('left', (width - (pageWidth - size)) + "px");
            } else if (control.options['userdata'].data.xAlign == "alCenter") {
                var size = Tools.convetPXToValue(width, control.options['userdata'].data.width, 0)
                $(controlDiv).css('left', ((width - size) / 2) + "px");
            }

            if (control.options['userdata'].data.yAlign == "alBottom") {
                var size = Tools.convetPXToValue(height, control.options['userdata'].data.top, 0)
                $(controlDiv).css('top', (height - (pageHeight - size)) + "px");
            } else if (control.options['userdata'].data.yAlign == "alCenter") {
                var size = Tools.convetPXToValue(height, control.options['userdata'].data.height, 0)
                $(controlDiv).css('top', ((height - size) / 2) + "px");
            }
        }, this)

        if (data.page.autoCenter) {
            var i = 0

            vspace = (height - (this.maxheight - this.minTop)) / 2
            hspace = (width - (this.maxwidth - this.minLeft)) / 2

            if (vspace < 0)
                vspace = 0

            if (hspace < 0)
                hspace = 0

            Tools.moveElements(this.controls, hspace, vspace, this.minLeft, this.minTop, this.maxwidth, this.maxheight)
        }

        this.setTabs(this.controls)

        mini.parse()
        this.relocationToolbars(data)

        if (this.charts.length > 0)
            for (var index = 0; index < this.charts.length; index++) {
                var element = this.charts[index]
                element.options.control.refresh()
            }

        this.divs.forEach(function (div) {
            var control = new DivControl()
            control.init(div)
        }, this)

        GlobalSessionObject.checkButtonRoles(data, function (control, elementInfo, isButton) {
            if (!isButton) {
                var isHref = elementInfo.element.data.isHref;
                if (Tools.isNull(isHref) || !isHref)
                    return;
            }
            removeFrameControlByName(data, control.name);
        })

        GlobalSessionObject.checkMainMenuRole(data)
        GlobalSessionObject.checkMainNavRole(data)

        this.uiInfo = data;
        if (Tools.isNull(data.workflow.useFormDataSource) || data.workflow.useFormDataSource)
            this.initDataSource()

        if (!Tools.isNull(onParentResize)) {
            onParentResize(div, sizeInfo)
        }

        for (i = 0; i < data.data.length; i++) {
            var controlInfo = data.data[i].data;
            if (controlInfo.autoLoginFire && controlInfo.hasOwnProperty(onLogin)) {
                GlobalUserLogin.registerNotify(controlInfo.name, controlInfo.onLogin);
            }
        }

        var uiInfo = data;
        this.lookups.forEach(function (lookup) {
            var data = lookup.options['userdata'].data;
            var popupDiv = getFrameControlById(uiInfo, data.popup);
            popupDiv.setShowHeader(false);
            popupDiv.setShowToolbar(false);
            popupDiv.setShowFooter(false);
            popupDiv.setShowFooter(false);
            $(popupDiv.getEl()).css({
                left: "0px",
                top: "0px",
                overflow: "hidden"
            });

            lookup.setPopup(data.popup);
            lookup.setGrid(data.grid);
            lookup.setAlwaysView(true);
            lookup.setShowPopupOnClick(true);
            // lookup.setPopupWidth("auto");
            // lookup.setPopupHeight("auto");
            lookup.setPopupWidth($(popupDiv.getEl()).width());
            lookup.setPopupHeight($(popupDiv.getEl()).height());

            $(lookup.getPopup().getEl()).css({
                overflow: "hidden"
            });
        }, this)

        this.timers.forEach(function (timer) {

        });

        reInitUIInfoFrameControlHash(uiInfo);
        return {
            x: hspace,
            y: vspace,
            width: $(div).width(),
            height: $(div).height()
        }
    }
}