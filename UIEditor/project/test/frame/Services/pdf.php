<?php
// 查看网页url
if (isset($_REQUEST['url']) && ! empty($_REQUEST['url'])) {
    $url = $_REQUEST['url'];
} else {
    header('content-type:text/html; charset=utf-8');
    exit('查看页面地址为空！');
}
// 临时生成bat文件名
if (isset($_REQUEST['bat_filename']) && ! empty($_REQUEST['bat_filename'])) {
    $bat_filename = $_REQUEST['bat_filename'];
} else {
    header('content-type:text/html; charset=utf-8');
    exit('bat文件名为空！');
}
// 生成pdf文件名
if (isset($_REQUEST['pdf_filename']) && ! empty($_REQUEST['pdf_filename'])) {
    $pdf_filename = $_REQUEST['pdf_filename'];
} else {
    header('content-type:text/html; charset=utf-8');
    exit('pdf文件名为空！');
}
// 生成文件类型 1 - 在线查看 2 - 下载
$type = isset($_REQUEST['type']) && ! empty($_REQUEST['type']) ? $_REQUEST['type'] : 1;

// 执行cmd命令
$content = "cd ./topdf/bin\r\nwkhtmltopdf.exe {$url} ../../file/{$pdf_filename}";
// 生成临时bat文件
file_put_contents($bat_filename, $content);
// 执行bat文件
exec($bat_filename);

// 在线查看pdf  
function read_pdf ($file)
{
    
    if (strtolower(substr(strrchr($file, '.'), 1)) != 'pdf') {
        echo '文件格式不对.';
        return;
    }
    
    if (! file_exists($file)) {
        echo '文件不存在';
        return;
    }
    
    header('Content-type: application/pdf');
    header('filename=' . $file);
    readfile($file);
}

// 下载pdf 
function down_pdf ($file)
{
    $fp = fopen($file, "r");
    header("Content-Type: application/octet-stream");
    header("Accept-Ranges: bytes");
    header("Accept-Length: " . filesize($file));
    header("Content-Disposition: attachment; filename=" . basename($file));
    echo fread($fp, filesize($file));
    fclose($fp);
}
if (2 == $type) {
    down_pdf("./file/{$pdf_filename}");
} elseif (1 == $type) {
    read_pdf("./file/{$pdf_filename}");
}

// 删除临时bat文件
unlink($bat_filename);
