var GlobalFrameManger = {
    Event: {
        onMenuClick: undefined,
        onMainActiveChanged: undefined,
        onToolbarClick: undefined,
        onInitUI: undefined,
        onTreeNodeClick: undefined,
    },
    useTopRegion: false,
    useBottomRegion: false,
    useRightRegion: false,
    useLeftRegion: false,
    useTab: false,
    globalModel: new IndexModel(),

    hasControls: false,
    needUpdateModel: true,

    getCurTabLayout: function () {
        if (Tools.isNull(GlobalFrameManger.globalModel.global_tabcontrol)) {
            return undefined;
        }

        var bodyEl = undefined;
        bodyEl = GlobalFrameManger.globalModel.global_tabcontrol.getBodyForCurrent({
            showCloseButton: "true"
        }, false);
        return bodyEl;
    },

    getMainLayout: function (uiInfo, tabName) {
        if (uiInfo.workflow.useTab == "true") {
            if (Tools.isNull(GlobalFrameManger.globalModel.global_tabcontrol)) {
                $(GlobalFrameManger.globalModel.mainLayout).empty();
                return GlobalFrameManger.globalModel.mainLayout;
            }
            var bodyEl = undefined;
            if (!Tools.isNull(uiInfo.workflow.useCurrentTab) && uiInfo.workflow.useCurrentTab == "true") {
                bodyEl = GlobalFrameManger.globalModel.global_tabcontrol.getBodyForCurrent({
                    name: uiInfo.workflow.name,
                    title: tabName,
                    showCloseButton: "true"
                });
                $(bodyEl).empty();
            } else {
                tabInfo = GlobalFrameManger.globalModel.global_tabcontrol.addTab({
                    name: uiInfo.workflow.name,
                    title: tabName,
                    showCloseButton: uiInfo.workflow.allowClose,
                });

                uiTab = tabInfo.tab;

                if (!tabInfo.exists) {
                    bodyEl = GlobalFrameManger.globalModel.global_tabcontrol.getBodyForTab(uiTab);
                } else
                    return undefined;
            }
            return bodyEl;
        } else {
            return GlobalFrameManger.globalModel.mainLayout
        }

    },

    updateToolbar: function (uiInfo) {
        var useToolbar = !Tools.isNull(uiInfo.workflow.useToolbar) && uiInfo.workflow.useToolbar == "true";
        if (useToolbar && !Tools.isNull(uiInfo.toolbar)) {
            var toolbar = undefined;
            $("#toolbarLayout").empty();

            var itemdata = [];

            var cc = new ControlCommon();
            uiInfo.toolbar.forEach(function (element) {
                var etype = cc.convertType(element.type);
                if (etype != cc.SEPARATOR_TYPE) {
                    itemdata.push({
                        id: "",
                        name: "",
                        value: "1",
                        type: cc.SEPARATOR_TYPE
                    });
                }
                itemdata.push({
                    id: element.id,
                    name: element.id,
                    type: etype,
                    iconCls: element.iconCls,
                    value: element.text,
                    onclick: "GlobalFrameManger.onToolbarClick(e)"
                });
            }, this);

            // [
            //     { id:"addbutton", type:GlobalFrameManger.globalModel.global_toolbar.BUTTON_TYPE, iconCls:"icon-add", title:"增加", onclick:"GlobalFrameManger.onToolbarClick(e)"},		
            //     { id:"editbutton", type:GlobalFrameManger.globalModel.global_toolbar.BUTTON_TYPE, iconCls:"icon-edit", title:"编辑", onclick:"GlobalFrameManger.onToolbarClick(e)"},		
            //     { id:"delbutton", type:GlobalFrameManger.globalModel.global_toolbar.BUTTON_TYPE, iconCls:"icon-remove", title:"删除", onclick:"GlobalFrameManger.onToolbarClick(e)"},		
            //     { type:GlobalFrameManger.globalModel.global_toolbar.SEPARATOR_TYPE},		
            //     { id:"queryinput", type:GlobalFrameManger.globalModel.global_toolbar.TEXTBOX_TYPE},		
            //     { id:"querybutton", type:GlobalFrameManger.globalModel.global_toolbar.BUTTON_TYPE, plain:"true", title:"查询", onclick:"onquery(e)"},		
            // ]

            var toolbardata = {
                parentLayout: GlobalFrameManger.globalModel.toolbarLayout,
                id: "maintoolbar",
                items: itemdata
            };
            GlobalFrameManger.globalModel.global_toolbar.write(dw, toolbardata);
            GlobalFrameManger.hasToolbarcontrol = true;
            GlobalFrameManger.globalModel.showToolbar();
        } else
            GlobalFrameManger.globalModel.hideToolbar();

    },

    createRegion: function (useTopRegion, useBottomRegion, useLeftRegion, useRightRegion) {
        $(GlobalFrameManger.globalModel.topLayout).empty();
        $(GlobalFrameManger.globalModel.bottomLayout).empty();
        $(GlobalFrameManger.globalModel.leftLayout).empty();
        $(GlobalFrameManger.globalModel.rightLayout).empty();
        if (useTopRegion) {
            var rect = createUIForName("top_region", GlobalFrameManger.globalModel.topLayout, true);
            if (!Tools.isNull(rect)) {
                GlobalFrameManger.globalModel.updateRegionHeight("north", rect.height);
            }
        }

        if (useBottomRegion) {
            rect = createUIForName("bottom_region", GlobalFrameManger.globalModel.bottomLayout, true);
            if (!Tools.isNull(rect)) {
                GlobalFrameManger.globalModel.updateRegionHeight("south", rect.height);
            }
        }

        if (useLeftRegion) {
            rect = createUIForName("left_region", GlobalFrameManger.globalModel.leftLayout, true);
            if (!Tools.isNull(rect)) {
                GlobalFrameManger.globalModel.updateRegionWidth("west", rect.width);
            }
        }

        if (useRightRegion) {
            rect = createUIForName("right_region", GlobalFrameManger.globalModel.rightLayout, true);
            if (!Tools.isNull(rect)) {
                GlobalFrameManger.globalModel.updateRegionWidth("east", rect.width);
            }
        }
    },

    initFrame: function (uiInfo) {
        dw = new DocumentWriter();
        var useTopRegion = !Tools.isNull(uiInfo.workflow.useTopRegion) && uiInfo.workflow.useTopRegion == "true";
        var useBottomRegion = !Tools.isNull(uiInfo.workflow.useBottomRegion) && uiInfo.workflow.useBottomRegion == "true";
        var useLeftRegion = !Tools.isNull(uiInfo.workflow.useLeftRegion) && uiInfo.workflow.useLeftRegion == "true";
        var useRightRegion = !Tools.isNull(uiInfo.workflow.useRightRegion) && uiInfo.workflow.useRightRegion == "true";

        var needReload = GlobalFrameManger.useTopRegion != useTopRegion ||
            GlobalFrameManger.useBottomRegion != useBottomRegion ||
            GlobalFrameManger.useLeftRegion != useLeftRegion ||
            GlobalFrameManger.useRightRegion != useRightRegion;
        if (!this.hasControls || needReload) {
            var useTab = !Tools.isNull(uiInfo.workflow.useTab) && uiInfo.workflow.useTab == "true";
            GlobalFrameManger.globalModel.write(dw, useTab);

            if (this.needUpdateModel) {
                this.needUpdateModel = false;
                GlobalFrameManger.globalModel.model.left.maxtop = uiInfo.workflow.leftRegionTopMax == "true";
                GlobalFrameManger.globalModel.model.left.maxbottom = uiInfo.workflow.leftRegionBottomMax == "true";
                GlobalFrameManger.globalModel.model.right.maxtop = uiInfo.workflow.rightRegionTopMax == "true";
                GlobalFrameManger.globalModel.model.right.maxbottom = uiInfo.workflow.rightRegionBottomMax == "true";
            }

            GlobalFrameManger.globalModel.resetRegion(useTopRegion, useBottomRegion, useLeftRegion, useRightRegion);

            GlobalFrameManger.globalModel.topLayout.style.backgroundColor = Tools.convetColor(uiInfo.page.color);
            GlobalFrameManger.globalModel.bottomLayout.style.backgroundColor = Tools.convetColor(uiInfo.page.color);
            GlobalFrameManger.globalModel.leftLayout.style.backgroundColor = Tools.convetColor(uiInfo.page.color);
            GlobalFrameManger.globalModel.rightLayout.style.backgroundColor = Tools.convetColor(uiInfo.page.color);

            GlobalFrameManger.createRegion(useTopRegion, useBottomRegion, useLeftRegion, useRightRegion);

            if (!Tools.isNull(GlobalFrameManger.globalModel.global_tabcontrol)) {
                if (uiInfo.workflow.useTab == false) {
                    $(GlobalFrameManger.globalModel.global_tabcontrol).hide();
                } else {
                    $(GlobalFrameManger.globalModel.global_tabcontrol).show();
                }

            }
            GlobalFrameManger.useTopRegion = useTopRegion;
            GlobalFrameManger.useBottomRegion = useBottomRegion;
            GlobalFrameManger.useLeftRegion = useLeftRegion;
            GlobalFrameManger.useRightRegion = useRightRegion;

            this.hasControls = true;
        }

    },

    onMainActiveChanged: function (tabName) {
        var tabs = mini.get("mainPage");
        var curTab = tabs.getActiveTab();
        var control = tabs.getTabBodyEl(curTab);
        if (!Tools.isNull(control.children[0].options)) {
            Global_Current_UIInfo = control.children[0].options["uidata"];
            //GlobalFrameManger.updateToolbar(Global_Current_UIInfo);
        }
        if (!Tools.isNull(GlobalFrameManger.Event.onMainActiveChanged))
            GlobalFrameManger.Event.onMainActiveChanged(tabName);
    },

    onToolbarClick: function (e) {
        if (!Tools.isNull(GlobalFrameManger.Event.onToolbarClick))
            GlobalFrameManger.Event.onToolbarClick(e);
    }
}