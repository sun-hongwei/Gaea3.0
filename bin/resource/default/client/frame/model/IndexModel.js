function IndexModel() {
    this.global_tabcontrol = undefined;
    this.needTabPage = false;

    this.model = {
        top: {
            visible: true,
            maxleft: true,
            maxright: true
        },
        bottom: {
            visible: true,
            maxleft: true,
            maxright: true
        },
        left: {
            visible: true,
            maxtop: false,
            maxbottom: false
        },
        right: {
            visible: true,
            maxtop: false,
            maxbottom: false
        },
    };

    this.layout = undefined;
    this.topLayout = undefined;
    this.bottomLayout = undefined;
    this.leftLayout = undefined;
    this.rightLayout = undefined;
    this.mainLayout = undefined;
    this.toolbarLayout = undefined;

    this.layout_id = "parentLayout";
    this.layout_top_id = "topLayout";
    this.layout_bottom_id = "bottomLayout";
    this.layout_left_id = "leftLayout";
    this.layout_right_id = "rightLayout";
    this.layout_main_id = "mainLayout";
    this.layout_tabpage_id = "mainPage";

    this.Event = {
        OnItemClick: undefined,
    }

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

    this.defaultRect = {};
    this.copyRect = function (rect) {
        return {
            x: rect.x,
            y: rect.y,
            width: rect.width,
            height: rect.height
        };
    }

    this.getRegionRect = function (regionName) {
        var width = $(document.body).width();
        var height = $(document.body).height();
        var r = {
            x: Tools.convetPXToValue(width, this.getRegionCssValue(regionName, "left"), 0),
            y: Tools.convetPXToValue(height, this.getRegionCssValue(regionName, "top"), 0),
            width: Tools.convetPXToValue(width, this.getRegionCssValue(regionName, "width"), 0),
            height: Tools.convetPXToValue(height, this.getRegionCssValue(regionName, "height"), 0)
        };

        return r;
    }

    this.resetRegion = function (hasTop, hasBottom, hasLeft, hasRight) {

        var container = {
            width: $(this.layout).width(),
            height: $(this.layout).height()
        };
        var top = this.getRegionRect("north");
        var left = this.getRegionRect("west");
        var right = this.getRegionRect("east");
        var bottom = this.getRegionRect("south");
        var main = {};

        // this.writeChild();

        if (hasLeft || hasRight) {
            if (this.model.left.maxtop || this.model.right.maxtop || this.model.left.maxbottom || this.model.right.maxbottom) {
                var decLeftHeight = (hasTop && !this.model.left.maxtop ? top.height : 0) + (hasBottom && !this.model.left.maxbottom ? bottom.height : 0);
                var decRightHeight = (hasTop && !this.model.right.maxtop ? top.height : 0) + (hasBottom && !this.model.right.maxbottom ? bottom.height : 0);
                var decTopWidth = (hasLeft && this.model.left.maxtop ? left.width : 0) + (hasRight && this.model.right.maxtop ? right.width : 0);
                var decBottomWidth = (hasLeft && this.model.left.maxbottom ? left.width : 0) + (hasRight && this.model.right.maxbottom ? right.width : 0);

                if (hasLeft) {
                    left.y = 0;
                    left.height = container.height - decLeftHeight;
                }
                if (hasRight) {
                    right.y = 0;
                    right.x = container.width - right.width;
                    right.height = container.height - decRightHeight;
                }
                if (hasTop) {
                    top.x = left.width;
                    top.width = container.width - decTopWidth;
                }
                if (hasBottom) {
                    bottom.x = left.width;
                    bottom.y = container.height - bottom.height;
                    bottom.width = container.width - decBottomWidth;
                }

            } else {
                left.y = hasTop ? top.height : 0;
                left.height = container.height - (hasTop ? top.height : 0) - (hasBottom ? bottom.height : 0);

                right.y = left.y;
                right.x = container.width - right.width;
                right.height = left.height;

                top.x = 0;
                top.width = container.width;

                bottom.x = 0;
                bottom.y = container.height - bottom.height;
                bottom.width = top.width;

            }
        }

        var toolbar = {
            x: 0,
            y: 0,
            width: 0,
            height: 0
        };

        if (!Tools.isNull(this.toolbarLayout)) {
            toolbar = {
                x: hasLeft ? left.width : 0,
                y: hasTop ? top.height : 0,
                width: container.width - (hasLeft ? left.width : 0) - (hasRight ? right.width : 0),
                height: $(this.toolbarLayout).height()
            };
        }

        main.x = hasLeft ? left.width : 0;
        main.y = (hasTop ? top.height : 0) + toolbar.height;
        main.width = container.width - (hasLeft ? left.width : 0) - (hasRight ? right.width : 0);
        main.height = container.height - (hasTop ? top.height : 0) - (hasBottom ? bottom.height : 0) - toolbar.height;

        this.updateRegion("north", {
            left: top.x,
            top: top.y,
            width: hasTop ? top.width : 0,
            height: hasTop ? top.height : 0
        });
        this.updateRegion("south", {
            left: bottom.x,
            top: bottom.y,
            width: hasBottom ? bottom.width : 0,
            height: hasBottom ? bottom.height : 0
        });
        this.updateRegion("west", {
            left: left.x,
            top: left.y,
            width: hasLeft ? left.width : 0,
            height: hasLeft ? left.height : 0
        });
        this.updateRegion("east", {
            left: right.x,
            top: right.y,
            width: hasRight ? right.width : 0,
            height: hasRight ? right.height : 0
        });
        this.updateRegion("center", {
            left: main.x,
            top: main.y,
            width: main.width,
            height: main.height
        });

        if (!Tools.isNull(this.toolbarLayout)) {
            $(this.toolbarLayout).css("left", toolbar.x);
            $(this.toolbarLayout).css("top", toolbar.y);
            $(this.toolbarLayout).css("width", toolbar.width);
            $(this.toolbarLayout).css("height", toolbar.height);
        }
    }

    this.getRegionCssValue = function (regionName, attrName) {
        var regionSelector = $("#" + this.layout_id);
        var region = regionSelector.children("[region='" + regionName + "']");
        return region.css(attrName);
    }

    this.RegionVisible = function (regionName) {
        var display = this.getRegionCssValue(regionName, "display");
        return !(!Tools.isNull(display) && display == "none");
    }

    this.updateRegion = function (regionName, attrs) {
        var regionSelector = $("#" + this.layout_id).children("[region='" + regionName + "']");
        for (var key in attrs) {
            switch (key) {
                case "visible":
                    if (attrs[key])
                        regionSelector.show();
                    else
                        regionSelector.hide();
                    break;
                case "width":
                    regionSelector.width(attrs[key]);
                    break;
                case "height":
                    regionSelector.height(attrs[key]);
                    break;
                case "left":
                case "top":
                default:
                    regionSelector.css(key, attrs[key]);
                    break;
            }
        }

    }

    this.hideRegion = function (regionName) {
        this.updateRegion(regionName, {
            visible: false
        });
    }

    this.showRegion = function (regionName) {
        this.updateRegion(regionName, {
            visible: true
        });
    }

    this.updateRegionHeight = function (regionName, value) {
        this.updateRegion(regionName, {
            height: value
        });
    }

    this.updateRegionWidth = function (regionName, value) {
        this.updateRegion(regionName, {
            width: value
        });
    }

    this.hideToolbar = function (name) {
        var element = getFrameControlByName(name).options.element;

        $(element).hide();

        this.toolbarLayout = undefined;

        this.updateSize();
    }

    this.showToolbar = function (name) {
        var element = getFrameControlByName(name).options.element;
        $(element).show();
        this.toolbarLayout = element;
        this.updateSize();
    }

    this.setVarRegions = function () {
        var regionSelector = $("#" + this.layout_id);
        this.layout = regionSelector[0];
        this.topLayout = regionSelector.children("[region='north']")[0];
        this.bottomLayout = regionSelector.children("[region='south']")[0];
        this.leftLayout = regionSelector.children("[region='west']")[0];
        this.rightLayout = regionSelector.children("[region='east']")[0];
        this.mainLayout = regionSelector.children("[region='center']")[0];
    }

    this.hides = function () {
        this.hideRegion("north");
        this.hideRegion("south");
        this.hideRegion("west");
        this.hideRegion("east");
    }

    this.writeChild = function () {
        var div = $("#" + this.layout_id)[0];
        $(div).empty();
        this.writeRegion(div);
    }

    this.writeRegion = function (div) {
        var width = $(document.body).width();
        var height = $(document.body).height();

        var cf = new ControlFactory();
        cf.createCheckTreeInfos(getFrameTopRegionUIInfo());
        var topSize = cf.computeFormSize(getFrameTopRegionUIInfo(), 1, 1);
        cf.createCheckTreeInfos(getFrameBottomRegionUIInfo());
        var bottomSize = cf.computeFormSize(getFrameBottomRegionUIInfo(), 1, 1);
        cf.createCheckTreeInfos(getFrameLeftRegionUIInfo());
        var leftSize = cf.computeFormSize(getFrameLeftRegionUIInfo(), 1, 1);
        cf.createCheckTreeInfos(getFrameRightRegionUIInfo());
        var rightSize = cf.computeFormSize(getFrameRightRegionUIInfo(), 1, 1);
        var topHeight = topSize.divHeight;
        var bottomHeight = bottomSize.divHeight;
        var leftWidth = leftSize.divWidth;
        var rightWidth = rightSize.divWidth;

        var headerString = '<div region="north"' +
            this.setField('id', this.layout_top_id) +
            this.setField('style', "left:0;top:0;width:100%;height:" + topHeight + "px;position:absolute;border:0px;overflow:hidden;") +
            '>';
        headerString += '</div>';

        headerString += '<div region="south"' +
            this.setField('id', this.layout_bottom_id) +
            this.setField('style', "left:0;top:" + (height - bottomHeight) + "px;width:100%;height:" + bottomHeight +
                "px;position:absolute;border:0px;overflow:hidden;") +
            '>';
        headerString += '</div>';

        headerString += '<div region="west"' +
            this.setField('id', this.layout_left_id) +
            this.setField('style', "left:0;top:" + topHeight + "px;width:" + leftWidth + "px;height:" + (height - topHeight - bottomHeight) +
                "px;position:absolute;border:0px;overflow:hidden;") +
            '>';
        headerString += '</div>';

        headerString += '<div region="east"' +
            this.setField('id', this.layout_right_id) +
            this.setField('style', "left:" + (width - rightWidth) + "px;top:" + topHeight + "px;width:" + rightWidth + "px;height:" +
                (height - topHeight - bottomHeight) + "px;position:absolute;border:0px;overflow:hidden;") +
            '>';
        headerString += '</div>';

        headerString += '<div region="center"' +
            this.setField('id', this.layout_main_id) +
            this.setField('style', "left:" + leftWidth + "px;top:" + topHeight + "px;width:" + (width - leftWidth - rightWidth) +
                "px;height:" + (height - topHeight - bottomHeight) + "px;position:absolute;border:0px;overflow:hidden;") +
            '>';
        headerString += '</div>';

        dw.write({
            tabParent: div
        }, headerString);

        this.setVarRegions();

        if (this.needTabPage) {
            this.global_tabcontrol = new TabControl();
            this.global_tabcontrol.Event.onActiveChanged = GlobalFrameManger.onMainActiveChanged;
            this.global_tabcontrol.write(new DocumentWriter(), {
                tabParent: this.mainLayout,
                id: this.layout_tabpage_id
            });
            mini.parse();
        }
    }

    this.write = function (dw, needTabPage) {
        this.needTabPage = needTabPage;
        var headerString = '<div' +
            this.setField('id', this.layout_id) +
            this.setField('style', "position:relative;border:0px;width:100%;height:100%;overflow:hidden") +
            '>';

        headerString += '</div>';

        dw.write({}, headerString);

        this.writeChild();
    }
}