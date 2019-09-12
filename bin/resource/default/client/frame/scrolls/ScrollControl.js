/*
**    Anderson Ferminiano
**    contato@andersonferminiano.com -- feel free to contact me for bugs or new implementations.
**    jQuery ScrollPagination
**    28th/March/2011
**    http://andersonferminiano.com/jqueryscrollpagination/
**    You may use this script for free, but keep my credits.
**    Thank you.
*/

(function( $ ){
    $.fn.scrollPagination = function(options) {
        var opts = $.extend({}, $.fn.scrollPagination.defaults, options || {});
        var target = opts.scrollTarget;
        if (target == null) {
            target = obj;
        }
        opts.scrollTarget = target;
        return this.each(function() {
            $.fn.scrollPagination.init($(this), opts);
        });
    
    };
    
    $.fn.stopScrollPagination = function() {
        return this.each(function() {
            $(this).attr('scrollPagination', 'disabled');
        });
    
    };
    
    $.fn.postRequest = function(opts) {
        opts.allowPost = false;
        $.ajax({
            type: 'POST',
            url: opts.contentPage,
            data: opts.contentData,
            beforeSend:function(){
                if( opts.beforeSend != null ){
                    opts.beforeSend();
                }
            },
            success: function(data) {
                opts.loader(data);
                opts.allowPost = true;
                /*
                var objectsRendered = $(obj).children('[rel!=loaded]');

                if (opts.afterLoad != null) {
                    opts.afterLoad(objectsRendered);
                }
                */
            },
            error : function(){
                opts.error(opts);
                opts.allowPost = true;
            },
            dataType: opts.dataType
        });
    }
    
    $.fn.scrollPagination.loadContent = function(obj, opts) {
        if (opts.allowPost != undefined && !opts.allowPost)
            return;

        if (opts == undefined)
            opts.allowPost = true;
        var target = opts.scrollTarget;
        var mayLoadContent = -$(obj).offset().top + opts.heightOffset >= $(obj).height() - $(target).height();
        if (!mayLoadContent){
            if (Tools.isNull(opts.first)){
                opts.first = true;
                mayLoadContent = true;
            }
        }
        if (mayLoadContent) {
            if (opts.beforeLoad != null) {
                opts.beforeLoad(opts);
            }
            $(obj).children().attr('rel', 'loaded');
            $.fn.postRequest(opts);
        }
    
    };
    
    $.fn.scrollPagination.onScrollEvent = function(obj, opts, event){
        if ($(obj).attr('scrollPagination') == 'enabled') {
            $.fn.scrollPagination.loadContent(obj, opts);
        } else {
            event.stopPropagation();
        }
    }

    $.fn.scrollPagination.init = function(obj, opts) {
        var target = opts.scrollTarget;
        $(obj).attr('scrollPagination', 'enabled');
    
        $(target).off("mousewheel");
        $(target).off("scroll");
        $(target).on("mousewheel", function(event) {
            $.fn.scrollPagination.onScrollEvent(obj, opts, event);
        });
        $(target).on("scroll", function(event) {
            $.fn.scrollPagination.onScrollEvent(obj, opts, event);
        });
    
        $.fn.scrollPagination.loadContent(obj, opts);
    
    };
    
    $.fn.scrollPagination.defaults = {
        'contentPage': null,
        'contentData': {},
        'beforeLoad': null,
        'afterLoad': null,
        'scrollTarget': null,
        'heightOffset': 0,
        //Add        
        'dataType': null,
        'beforeSend':null,
        'loader': function(data) {}
    };     

})( jQuery );