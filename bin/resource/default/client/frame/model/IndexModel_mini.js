function IndexModel() {
    this.global_tabcontrol = undefined;

    this.layout = undefined;
    this.topLayout = undefined;
    this.bottomLayout = undefined;
    this.leftLayout = undefined;
    this.rightLayout = undefined;
    this.mainLayout = undefined;

    this.data = undefined;

    this.layout_id = "parentLayout";

    this.Event = {
        OnItemClick: undefined,
    }

    this.getValue = function(value, defaultValue) {
        if (Tools.isNull(value))
            if (Tools.isNull(defaultValue))
                return "";
            else
                return defaultValue
        else
            return value;
    }

    this.setField = function(fieldname, value, defaultValue) {
        return (Tools.isNull(value) ? (defaultValue == undefined ? '' : ' ' + fieldname + '="' + defaultValue + '"') : ' ' + fieldname + '="' + value + '"');
    }

    this.writeDefault = function(dw, needTopRegion, needBottomRegion, needLeftRegion, needRightRegion, needTabPage) {
        var header = { header: {}, content: {} };
        if (!Tools.isNull(needTopRegion) && needTopRegion) {
            header.header.topRegion = {};
        }
        if (!Tools.isNull(needBottomRegion) && needBottomRegion) {
            header.header.bottomRegion = {};
        }
        if (!Tools.isNull(needLeftRegion) && needLeftRegion) {
            header.header.leftRegion = {};
        }
        if (!Tools.isNull(needRightRegion) && needRightRegion) {
            header.header.rightRegion = {};
        }
        if (!Tools.isNull(needTabPage) && needTabPage) {
            header.header.tabPage = {};
        }
        this.write(dw, header);
    }

    this.hideRegion = function(regionName) {
        var layout = mini.get("parentLayout");
        layout.updateRegion(regionName, { visible: false });
    }

    this.showRegion = function(regionName) {
        var layout = mini.get("parentLayout");
        layout.updateRegion(regionName, { visible: true });
    }

    this.updateRegionValue = function(regionName, attrName, attrValue) {
        var layout = mini.get("parentLayout");
        layout.updateRegion(regionName, { attrName: attrValue });
    }

    this.updateRegionHeight = function(regionName, value) {
        var layout = mini.get("parentLayout");
        layout.updateRegion(regionName, { height: value });
    }

    this.updateRegionWidth = function(regionName, value) {
        var layout = mini.get("parentLayout");
        layout.updateRegion(regionName, { width: value });
    }

    this.hideToolbar = function(name) {
        var element = getFrameControlByName(name).options.element;
        var height = $(element).height();

        var layout = mini.get("parentLayout");
        var headerLayout = layout.getRegion("north");
        layout.updateRegion("north", { height: headerLayout.height - height });
        $(element).hide();
    }

    this.showToolbar = function(name) {
        var element = getFrameControlByName(name).options.element;
        var height = $(element).height();

        var layout = mini.get("parentLayout");
        var headerLayout = layout.getRegion("north");
        layout.updateRegion("north", { height: headerLayout.height + height });
        $(element).show();
    }

    this.setVarRegions = function() {
        this.layout = mini.get(this.layout_id);
        this.topLayout = this.layout.getRegionBodyEl("north");
        this.bottomLayout = this.layout.getRegionBodyEl("south");
        this.leftLayout = this.layout.getRegionBodyEl("west");
        this.rightLayout = this.layout.getRegionBodyEl("east");
        this.mainLayout = this.layout.getRegionBodyEl("center");
    }

    this.hides = function() {
        this.hideRegion("north");
        this.hideRegion("south");
        this.hideRegion("west");
        this.hideRegion("east");
    }

    this.write = function(dw, data) {
        var hasTopRegion = !Tools.isNull(data.header.topRegion);
        var hasBottomRegion = !Tools.isNull(data.header.bottomRegion);
        var hasLeftRegion = !Tools.isNull(data.header.leftRegion);
        var hasRightRegion = !Tools.isNull(data.header.rightRegion);
        var hasTabPage = !Tools.isNull(data.header.tabPage);

        var headerString = '<div class="mini-layout"' +
            this.setField('id', this.layout_id) +
            this.setField('style', data.style, ";border:0px;width:100%;height:100%") +
            '>';

        var header = data.header.topRegion;
        if (Tools.isNull(header)) {
            header = {};
        }
        headerString += '<div region="north"' +
            this.setField('id', header.id, "topLayout") +
            this.setField('height', header.height, "85px") +
            // this.setField('width', header.width, "100%") +
            this.setField('style', "border:0px;overflow:hidden;") +
            this.setField('bodyStyle', header.style, "border:0px;overflow:hidden;") +
            this.setField('showHeader', header.showHeader, "false") +
            this.setField('showCollapseButton', header.showCollapseButton, "false") +
            this.setField('showSplit', header.showSplit, "false") +
            this.setField('showCloseButton', header.showCloseButton, "false") +
            this.setField('allowResize', header.allowResize, "false") +
            '>';
        headerString += '</div>';

        header = data.header.bottomRegion;
        if (Tools.isNull(header)) {
            header = {};
        }
        headerString += '<div region="south"' +
            this.setField('id', header.id, "bottomLayout") +
            this.setField('height', header.height, "30px") +
            this.setField('width', header.width, "100%") +
            this.setField('style', "border:0px;overflow:hidden;") +
            this.setField('bodyStyle', header.style, ";border:0px;overflow:hidden;") +
            this.setField('showHeader', header.showHeader, "false") +
            this.setField('showCollapseButton', header.showCollapseButton, "false") +
            this.setField('showSplit', header.showSplit, "false") +
            this.setField('showCloseButton', header.showCloseButton, "false") +
            this.setField('allowResize', header.allowResize, "false") +
            '>';
        headerString += '</div>';

        header = data.header.leftRegion;
        if (Tools.isNull(header)) {
            header = {};
        }

        headerString += '<div region="west"' +
            this.setField('id', header.id, "leftLayout") +
            this.setField('width', header.width, "200px") +
            // this.setField('width', header.height, "100%") +
            this.setField('style', "border:0px;overflow:hidden;") +
            this.setField('bodyStyle', header.style, "border:0px;overflow:hidden;") +
            this.setField('showHeader', header.showHeader, "false") +
            this.setField('showCollapseButton', header.showCollapseButton, "false") +
            this.setField('showSplit', header.showSplit, "false") +
            this.setField('showCloseButton', header.showCloseButton, "false") +
            this.setField('allowResize', header.allowResize, "false") +
            '>';
        headerString += '</div>';

        header = data.header.rightRegion;
        if (Tools.isNull(header)) {
            header = {};
        }
        headerString += '<div region="east"' +
            this.setField('id', header.id, "rightLayout") +
            this.setField('width', header.width, "150px") +
            // this.setField('width', header.height, "100%") +
            this.setField('style', "border:0px;overflow:hidden;") +
            this.setField('bodyStyle', header.style, "border:0px;overflow:hidden;") +
            this.setField('showHeader', header.showHeader, "false") +
            this.setField('showCollapseButton', header.showCollapseButton, "false") +
            this.setField('showSplit', header.showSplit, "false") +
            this.setField('showCloseButton', header.showCloseButton, "false") +
            this.setField('allowResize', header.allowResize, "false") +
            '>';
        headerString += '</div>';

        header = data.header.content;
        if (Tools.isNull(header)) {
            header = {};
        }
        headerString += '<div region="center"' +
            this.setField('id', header.id, "mainLayout") +
            this.setField('style', header.style, "border:0px;overflow:hidden;") +
            this.setField('bodyStyle', header.style, "border:0px;overflow:hidden;") +
            this.setField('showHeader', header.showHeader, "false") +
            this.setField('showCollapseButton', header.showCollapseButton, "false") +
            this.setField('showSplit', header.showSplit, "false") +
            this.setField('showCloseButton', header.showCloseButton, "false") +
            this.setField('allowResize', header.allowResize, "false") +
            '>';
        headerString += '</div>';

        headerString += '</div>';

        dw.write(data, headerString);

        mini.parse();

        this.setVarRegions();

        if (!hasTopRegion) {
            this.hideRegion("north");
        }

        if (!hasBottomRegion) {
            this.hideRegion("south");
        }

        if (!hasLeftRegion) {
            this.hideRegion("west");
        }

        if (!hasRightRegion) {
            this.hideRegion("east");
        }

        this.data = data;

        if (hasTabPage) {
            this.global_tabcontrol = new TabControl();
            this.global_tabcontrol.Event.onActiveChanged = GlobalFrameManger.onMainActiveChanged;
            this.global_tabcontrol.write(new DocumentWriter(), { tabParent: this.mainLayout, id: "mainPage" });
        }
    }
}