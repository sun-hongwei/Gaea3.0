package com.wh.system.tools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Units;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.Borders;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.TextAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHeaderFooter;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.apache.poi.xwpf.usermodel.XWPFStyles;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.impl.xb.xmlschema.SpaceAttribute;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDecimalNumber;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFldChar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFonts;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHeight;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTInd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTShd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSignedTwipsMeasure;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSpacing;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTString;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyle;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTabStop;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTabs;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGrid;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGridCol;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTextScale;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTrPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTVerticalJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STEm;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STFldCharType;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHeightRule;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHighlightColor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STLineSpacingRule;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STPageOrientation;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STShd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STStyleType;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTabJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STVerticalAlignRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STVerticalJc;

import com.wh.control.EditorEnvironment; 

public class WordDocument extends XWPFDocument{

	File file;
	
	public static class HeaderInfo{
		public Object object;
		public ParagraphAlignment alignment = ParagraphAlignment.CENTER;
		
		public HeaderInfo(){}
		
		public String toObjectString() {
			return (object == null ? "" : (object instanceof File ? "" : object.toString()));
		}
		
		protected void init(String headerinfoString) {
			if (headerinfoString == null || headerinfoString.isEmpty())
				return;
			
			int index = headerinfoString.indexOf(",");
			
			object = headerinfoString.substring(index + 1);
			alignment = getAlign(headerinfoString.substring(0, index));
		}
		
		public static HeaderInfo fromString(String headerinfoString, Class<? extends HeaderInfo> c) throws Exception{

			Constructor<? extends HeaderInfo> constructor =  c.getDeclaredConstructor();
			HeaderInfo headerInfo = constructor.newInstance();
			headerInfo.init(headerinfoString);
			return headerInfo;
		}
		
		public HeaderInfo(Object obj, ParagraphAlignment alignment){
			this.object = obj;
			this.alignment = alignment;
		}
		
		public String toString(){
			return getAlign(alignment) + "," + (object == null ? "" : object.toString());
		}
		
		public static ParagraphAlignment getAlign(String value) {
			if (value.compareToIgnoreCase("right") == 0)
				return ParagraphAlignment.RIGHT;
			else if (value.compareToIgnoreCase("left") == 0)
				return ParagraphAlignment.LEFT;
			else {
				return ParagraphAlignment.CENTER;
			}
		}
		
		public static String getAlign(ParagraphAlignment align) {
			switch (align) {
			case RIGHT:
				return "right";
			case LEFT:
				return "left";
			default:
				return "center";
			}
		}
		
	}
	
	public static class PageNumberHeaderInfo extends HeaderInfo{
		
		public String prefix;
		public String suxfix;
		
		public static final String KEY = "[页码]";
		
		public PageNumberHeaderInfo(){}
		
		protected void init(String headerinfoString) {
			super.init(headerinfoString);
			init(object.toString(), alignment);
		}

		public String toObjectString() {
			return (prefix == null ? "" : prefix) + KEY + (suxfix == null ? "" : suxfix);
		}
		
		public PageNumberHeaderInfo(String value, ParagraphAlignment alignment){
			init(value, alignment);
		}
		
		public void init(String value, ParagraphAlignment alignment){
			int index = value.indexOf(KEY);
			if (index != -1){
				prefix = value.substring(0, index);
				suxfix = value.substring(index + KEY.length());
			}
			this.alignment = alignment;
			object = toObjectString();
		}
		
		public PageNumberHeaderInfo(String prefix, String suxfix, ParagraphAlignment alignment){
			this.prefix = prefix;
			this.suxfix = suxfix;
			this.alignment = alignment;
			object = toObjectString();
		}
		
		public String toString(){
			return getAlign(alignment) + "," + toObjectString();
		}

		public static boolean is(String value){
			return value.indexOf(KEY) != -1;
		}
	}
	
	public static class Info{
		public enum Type{
			itImage, itText, itTable
		}
		
		public Type type = Type.itText;
		
		XWPFParagraph obj;
		XWPFTable table;
		public XWPFRun getRun(){
			List<XWPFRun> runs = obj.getRuns();
			if (runs.size() == 0){
		        obj.createRun();
			}
			
			return obj.getRuns().get(0);
		}
		
		public Info(){}
		public Info(XWPFParagraph obj){
			this.obj = obj;
		}
	}
	
	protected Info title;
	
	protected List<Info> infos = new ArrayList<>();
	
	public WordDocument(){
		super();
		setPage(A4);
	}
	
	protected int maxLevel = 10;
	
	public static final Dimension Letter = new Dimension(612, 792);
	public static final Dimension LetterSmall = new Dimension(612, 792);
	public static final Dimension Tabloid = new Dimension(792, 1224);
	public static final Dimension Ledger = new Dimension(1224, 792);
	public static final Dimension Legal = new Dimension(612, 1008);
	public static final Dimension Statement = new Dimension(396, 612);
	public static final Dimension Eecutive = new Dimension(540, 720);
	public static final Dimension A0 = new Dimension(2384, 3371);
	public static final Dimension A1 = new Dimension(1685, 2384);
	public static final Dimension A2 = new Dimension(1190, 1684);
	public static final Dimension A3 = new Dimension(842, 1190);
	public static final Dimension A4 = new Dimension(595, 842);
	public static final Dimension A4Small = new Dimension(595, 842);
	public static final Dimension A5 = new Dimension(420, 595);
	public static final Dimension B4 = new Dimension(729, 1032);
	public static final Dimension B5 = new Dimension(516, 729);
	public static final Dimension Folio = new Dimension(612, 936);
	public static final Dimension Quarto = new Dimension(610, 780);
	
	public void setPage(Dimension size){
		setPage(size.width * 20, size.height * 20, STPageOrientation.PORTRAIT);
	}
	
	protected CTSectPr getCTSectPr() {
		CTSectPr ctSectPr = getDocument().getBody().getSectPr();
		if (ctSectPr == null)
			ctSectPr = getDocument().getBody().addNewSectPr();
		
		return ctSectPr;
	}
	
	public void setPage(Integer width, Integer height, STPageOrientation.Enum orientation){
		CTSectPr ctSectPr = getCTSectPr();
		
		CTPageSz pageSize = ctSectPr.getPgSz();
		if (pageSize == null)
			pageSize = ctSectPr.addNewPgSz();
		
		if (width != null && width > 0)
			pageSize.setW(BigInteger.valueOf(width));
		if (height != null && height > 0)
			pageSize.setH(BigInteger.valueOf(height));
		if (orientation != null)
			pageSize.setOrient(orientation);
		setPageMargin();
	}

	public void setPageMargin(){
		setPageMargin(720L, 1440L, 720L, 1440L);
	}
	
	public void setPageMargin(long left, long top, long right, long bottom){
		CTSectPr ctSectPr = getCTSectPr();
		CTPageMar pageMar = ctSectPr.getPgMar();  
		if (pageMar == null)
			pageMar = ctSectPr.addNewPgMar();  
		pageMar.setLeft(BigInteger.valueOf(left));  
		pageMar.setTop(BigInteger.valueOf(top));  
		pageMar.setRight(BigInteger.valueOf(right));  
		pageMar.setBottom(BigInteger.valueOf(bottom));
	}
	
	public long[] getPageMargin(){
		long[] ls = new long[4];
		
		CTSectPr ctSectPr = getCTSectPr();
		CTPageMar pageMar = ctSectPr.getPgMar();  
		if (pageMar == null)
			return null;
		
		ls[0] = pageMar.getLeft().longValue();
		ls[1] = pageMar.getTop().longValue();
		ls[2] = pageMar.getRight().longValue();
		ls[3] = pageMar.getBottom().longValue();
		
		return ls;
	}
	
	public Dimension getPageSize(){
		if (getDocument().getBody().getSectPr() == null){
			setPage(A4);
		}
		
		CTPageSz pageSize = getDocument().getBody().getSectPr().getPgSz();
		return new Dimension(pageSize.getW().intValue(), pageSize.getH().intValue());
	}
	
	public Dimension getTwipPageSize(){
		if (getDocument().getBody().getSectPr() == null){
			setPage(A4);
		}
		
		CTPageSz pageSize = getDocument().getBody().getSectPr().getPgSz();
		return new Dimension(pageSize.getW().intValue(), pageSize.getH().intValue());
	}
	
	public double twipToPoints(double twip){
		return twip / 20;
	}
	
	public double pointsToTwip(double points){
		return points * 20;
	}
	
	public double charsToTwip(int charcount){
		int emu = Units.charactersToEMU(charcount);
		double points = Units.toPoints(emu);
		int twip = (int) pointsToTwip(points);
		return twip;
	}
	
	public Dimension getPointPageSize(){
		Dimension size = getTwipPageSize();
		size.width = size.width * 20;
		size.height = size.height * 20;
		return size;
	}
	
	public Dimension getInchPageSize(){
		Dimension size = getTwipPageSize();
		size.width = size.width / 1440;
		size.height = size.height / 1440;
		return size;
	}
		
	public void setMaxLevel(int maxLevel){
		this.maxLevel = maxLevel;
	}
	
	public void setFile(File file){
		this.file = file;
	}
	
	protected Info addInfo() {
		Info info = new Info();
		infos.add(info);
		return info;
	}
	
	public Info addTitle(String text){
		this.title = addParagraph(true, "标题 1", 1, text);
		return this.title;
	}
	
	public Info addParagraph(String text) {
		return addParagraph("标题 5", 2, text);
	}
	
	public Info addParagraph(int headingLevel, String text) {
		return addParagraph("标题 5", headingLevel, text);
	}
	
	public Info addParagraph(String strStyleId, int headingLevel, String text) {
		return addParagraph(false, strStyleId, headingLevel, text);
	}
	
	protected Info addParagraph(Boolean isTitle, String strStyleId, int headingLevel, String text) {
		if (headingLevel > maxLevel - 1){
			EditorEnvironment.showMessage("段落不能大于：" + String.valueOf(maxLevel) + "!");
			return null;
		}
		
		if (this.title == null && !isTitle){
			EditorEnvironment.showMessage("请先添加标题！");
			return null;
		}
		
		if (!isTitle){
			if (headingLevel < 2){
				EditorEnvironment.showMessage("段落级别必须大于1！");
				return null;
			}
		}
		
		Info info = addInfo();
		
		info.obj = createParagraph();
		
//		addStyle(strStyleId, headingLevel);
		
		info.obj.setStyle(strStyleId);
		
		if (text != null)
			setText(info, text);
		
		return info;
	}
	
	public Info addText(String text){
		return addParagraph("正文", maxLevel - 1, text);
	}
	
	public Info addTable(List<String> columns, HashMap<String, Object> row, float[] widths, boolean border){
		List<HashMap<String, Object>> rows = new ArrayList<>();
		rows.add(row);
		return addTable(columns, rows, widths, border);
	}
	
	/**
	 * @Description: 得到单元格第一个Paragraph
	 */
	protected XWPFParagraph getCellFirstParagraph(XWPFTableCell cell) {
		XWPFParagraph p;
		if (cell.getParagraphs() != null && cell.getParagraphs().size() > 0) {
			p = cell.getParagraphs().get(0);
		} else {
			p = cell.addParagraph();
		}
		return p;
	}

	protected XWPFRun getOrAddParagraphFirstRun(XWPFParagraph p, boolean isInsert, boolean isNewLine) {
		XWPFRun pRun = null;
		if (isInsert) {
			pRun = p.createRun();
		} else {
			if (p.getRuns() != null && p.getRuns().size() > 0) {
				pRun = p.getRuns().get(0);
			} else {
				pRun = p.createRun();
			}
		}
		if (isNewLine) {
			pRun.addBreak();
		}
		return pRun;
	}
  
	/**
	 * @Description: 得到XWPFRun的CTRPr
	 */
	protected CTRPr getRunCTRPr(XWPFParagraph p, XWPFRun pRun) {
		CTRPr pRpr = null;
		if (pRun.getCTR() != null) {
			pRpr = pRun.getCTR().getRPr();
			if (pRpr == null) {
				pRpr = pRun.getCTR().addNewRPr();
			}
		} else {
			pRpr = p.getCTP().addNewR().addNewRPr();
		}
		return pRpr;
	}

	protected void setText(XWPFRun pRun, String content){
		if (content != null && !content.isEmpty()) {
			// pRun.setText(content);
			if (content.contains("\n")) {// System.properties("line.separator")
				String[] lines = content.split("\n");
				pRun.setText(lines[0], 0); // set first line into XWPFRun
				for (int i = 1; i < lines.length; i++) {
					// add break and insert new text
					pRun.addBreak();
					pRun.setText(lines[i]);
				}
			} else {
				pRun.setText(content, 0);
			}
		}
	}
	
	/**
	 * 设置字符间距位置
	 * @param position
	 *            :字符间距位置：>0提升 <0降低=磅值*2 如3磅=6
	 * @param div
	 *            :字符间距间距 >0加宽 <0紧缩 =磅值*20 如2磅=40
	 * @param indent
	 *            :字符间距缩进 <100 缩
	 */

	public void setCharLocation(Info info, Integer position, Integer div, int indent){
		if (info == null || info.obj == null)
			return;
		
		if (position != null)
			info.getRun().setTextPosition(position);
		CTRPr pRpr = getRunCTRPr(info.obj, info.getRun());
		if (div != null){
			CTSignedTwipsMeasure ctSTwipsMeasure = pRpr.isSetSpacing() ? pRpr.getSpacing() : pRpr.addNewSpacing();
			ctSTwipsMeasure.setVal(new BigInteger(String.valueOf(div)));
		}
		if (indent > 0) {
			CTTextScale paramCTTextScale = pRpr.isSetW() ? pRpr.getW() : pRpr.addNewW();
			paramCTTextScale.setVal(indent);
		}
	}
	
	protected void setCellValue(XWPFTableCell cell, String text) {
		XWPFParagraph p = getCellFirstParagraph(cell);  
		XWPFRun pRun = getOrAddParagraphFirstRun(p, false, false);
		setAlign(new Info(p), ParagraphAlignment.LEFT);
        setText(pRun, text);  
	}
	
	/**
	 * @Description: 得到CTTrPr,不存在则新建
	 */
	protected CTTrPr getRowCTTrPr(XWPFTableRow row) {
		CTRow ctRow = row.getCtRow();
		CTTrPr trPr = ctRow.isSetTrPr() ? ctRow.getTrPr() : ctRow.addNewTrPr();
		return trPr;
	}

	/**
	 * @Description: 设置行高
	 */
	protected void setRowHeight(XWPFTableRow row, int hight, STHeightRule.Enum heigthEnum) {
		CTTrPr trPr = getRowCTTrPr(row);
		CTHeight trHeight;
		if (trPr.getTrHeightList() != null && trPr.getTrHeightList().size() > 0) {
			trHeight = trPr.getTrHeightList().get(0);
		} else {
			trHeight = trPr.addNewTrHeight();
		}

		if (heigthEnum == null || (heigthEnum != null && heigthEnum == STHeightRule.AT_LEAST)){
			trHeight.setVal(BigInteger.valueOf(hight));
		}else {
			trHeight.setHRule(heigthEnum);
		}
	}
    
	public Info addTable(List<String> columns, List<HashMap<String, Object>> rows, float[] widths, boolean border){
        XWPFTable infoTable = createTable(rows == null ? 1 : rows.size() + 1, columns.size());  
        return addTable(infoTable, columns, rows, widths, border);
	}
	
	public Info addTable(XWPFTable infoTable, List<String> columns, List<HashMap<String, Object>> rows, float[] widths, boolean border){
        Info info = addInfo();
        info.table = infoTable;
        
        //去表格边框 
        if (!border)
        	infoTable.getCTTbl().getTblPr().unsetTblBorders();
        else
        	setTableBorders(info, STBorder.SINGLE, "4", "auto", "0");  
        setTableWidthAndHAlign(info, null, STJc.CENTER);  
        setTableCellMargin(info, 0, 108, 0, 108);
        
        BigInteger[] colWidths = null;
        //列宽自动分割 
        if (widths == null || widths.length == 0){
            CTTblWidth infoTableWidth = infoTable.getCTTbl().addNewTblPr().addNewTblW();  
            infoTableWidth.setType(STTblWidth.DXA);  
            infoTableWidth.setW(BigInteger.valueOf(9072));  
        }else{
        	colWidths = setTableGridCol(info, widths);
        }
        
        //表格第一行  
        XWPFTableRow tableColumns = infoTable.getRow(0); 
//        setRowHeight(tableColumns, 2 * 256, STHeightRule.AT_LEAST);
        for (int i = 0; i < columns.size(); i++) {
        	XWPFTableCell cell = tableColumns.getCell(i);
        	if (cell == null)
        		cell = tableColumns.addNewTableCell();
        	if (colWidths != null)
        		setCellWidthAndVAlign(cell, colWidths[i].intValue(), STTblWidth.DXA, STVerticalJc.CENTER); 
            setCellValue(cell, columns.get(i));  
		}
  
        if (rows != null){
        	int index = 1;
            for (HashMap<String, Object> rowdata : rows) {
                XWPFTableRow row = infoTable.getRow(index++);
//                setRowHeight(tableColumns, 4 * 256, STHeightRule.AT_LEAST);
				for (int j = 0; j < columns.size(); j++) {
					String name = columns.get(j);
					XWPFTableCell cell = row.getCell(j);
		        	if (colWidths != null)
		        		setCellWidthAndVAlign(cell, colWidths[j].intValue(), STTblWidth.DXA, STVerticalJc.CENTER); 
					if (rowdata.containsKey(name)){
						Object vObject = rowdata.get(name);
						if (vObject != null)
							setCellValue(cell, vObject.toString());
						else
							setCellValue(cell, "");
					}
				}
			}
        }
        
        return info;
	}
	
	/**
	 * @Description: 设置单元格Margin
	 */
	public void setTableCellMargin(Info info, int top, int left, int bottom, int right) {
		if (info == null || info.table == null)
			return;
		
		XWPFTable table = info.table;
		table.setCellMargins(top, left, bottom, right);
	}
    
	/**
	 * @Description: 设置表格总宽度与水平对齐方式
	 */
	public void setTableWidthAndHAlign(Info info, Integer width, STJc.Enum enumValue) {
		if (info == null || info.table == null)
			return;
		
		XWPFTable table = info.table;
		CTTblPr tblPr = getTableCTTblPr(table);
		CTTblWidth tblWidth = tblPr.isSetTblW() ? tblPr.getTblW() : tblPr.addNewTblW();
		if (enumValue != null) {
			CTJc cTJc = tblPr.addNewJc();
			cTJc.setVal(enumValue);
		}
		
		if (width == null){
			long[] margin = getPageMargin();
			width = (int) (getPageSize().width - margin[0] - margin[2] - 4* 20);
		}
		
		tblWidth.setW(BigInteger.valueOf(width));
		tblWidth.setType(STTblWidth.DXA);
	}

	/**
	 * 
	 * @Description: 得到Cell的CTTcPr,不存在则新建
	 */
	protected CTTcPr getCellCTTcPr(XWPFTableCell cell) {
		CTTc cttc = cell.getCTTc();
		CTTcPr tcPr = cttc.isSetTcPr() ? cttc.getTcPr() : cttc.addNewTcPr();
		return tcPr;
	}

	/**
	 * @Description: 得到Table的CTTblPr,不存在则新建
	 */
	protected CTTblPr getTableCTTblPr(XWPFTable table) {
		CTTbl ttbl = table.getCTTbl();
		CTTblPr tblPr = ttbl.getTblPr() == null ? ttbl.addNewTblPr() : ttbl.getTblPr();
		return tblPr;
	}

	/**
	 * @Description: 设置Table的边框
	 */
	public void setTableBorders(Info info, STBorder.Enum borderType, String size, String color, String space) {
		if (info == null || info.table == null)
			return;

		XWPFTable table = info.table;
		CTTblPr tblPr = getTableCTTblPr(table);
		CTTblBorders borders = tblPr.isSetTblBorders() ? tblPr.getTblBorders() : tblPr.addNewTblBorders();
		CTBorder hBorder = borders.isSetInsideH() ? borders.getInsideH() : borders.addNewInsideH();
		hBorder.setVal(borderType);
		hBorder.setSz(new BigInteger(size));
		hBorder.setColor(color);
		hBorder.setSpace(new BigInteger(space));

		CTBorder vBorder = borders.isSetInsideV() ? borders.getInsideV() : borders.addNewInsideV();
		vBorder.setVal(borderType);
		vBorder.setSz(new BigInteger(size));
		vBorder.setColor(color);
		vBorder.setSpace(new BigInteger(space));

		CTBorder lBorder = borders.isSetLeft() ? borders.getLeft() : borders.addNewLeft();
		lBorder.setVal(borderType);
		lBorder.setSz(new BigInteger(size));
		lBorder.setColor(color);
		lBorder.setSpace(new BigInteger(space));

		CTBorder rBorder = borders.isSetRight() ? borders.getRight() : borders.addNewRight();
		rBorder.setVal(borderType);
		rBorder.setSz(new BigInteger(size));
		rBorder.setColor(color);
		rBorder.setSpace(new BigInteger(space));

		CTBorder tBorder = borders.isSetTop() ? borders.getTop() : borders.addNewTop();
		tBorder.setVal(borderType);
		tBorder.setSz(new BigInteger(size));
		tBorder.setColor(color);
		tBorder.setSpace(new BigInteger(space));

		CTBorder bBorder = borders.isSetBottom() ? borders.getBottom() : borders.addNewBottom();
		bBorder.setVal(borderType);
		bBorder.setSz(new BigInteger(size));
		bBorder.setColor(color);
		bBorder.setSpace(new BigInteger(space));
	}

	/**
	 * @Description: 设置列宽和垂直对齐方式
	 */
	public void setCellWidthAndVAlign(XWPFTableCell cell, Integer width, STTblWidth.Enum typeEnum,
			STVerticalJc.Enum vAlign) {
		CTTcPr tcPr = getCellCTTcPr(cell);
		CTTblWidth tcw = tcPr.isSetTcW() ? tcPr.getTcW() : tcPr.addNewTcW();
		if (width != null) {
			tcw.setW(BigInteger.valueOf(width));
		}
		if (typeEnum != null) {
			tcw.setType(typeEnum);
		}
		if (vAlign != null) {
			CTVerticalJc vJc = tcPr.isSetVAlign() ? tcPr.getVAlign() : tcPr.addNewVAlign();
			vJc.setVal(vAlign);
		}
	}

	/**
	 * @Description: 跨列合并
	 */
	public void mergeCellsHorizontal(Info info, int row, int fromCell, int toCell) {
		if (info == null || info.table == null)
			return;

		XWPFTable table = info.table;

		for (int cellIndex = fromCell; cellIndex <= toCell; cellIndex++) {
			XWPFTableCell cell = table.getRow(row).getCell(cellIndex);
			if (cellIndex == fromCell) {
				// The first merged cell is set with RESTART merge value
				getCellCTTcPr(cell).addNewHMerge().setVal(STMerge.RESTART);
			} else {
				// Cells which join (merge) the first one,are set with CONTINUE
				getCellCTTcPr(cell).addNewHMerge().setVal(STMerge.CONTINUE);
			}
		}
	}

	public BigInteger[] setTableGridCol(Info info, float[] colWidths) {
		Dimension size = getPageSize();
		BigInteger[] widths = new BigInteger[colWidths.length];
		for (int i = 0; i < widths.length; i++) {
			widths[i] = BigInteger.valueOf((int) (size.width * colWidths[i]));
		}
		return setTableGridCol(info, widths);
	}

	public BigInteger getWidthForPage(float percent) {
		Dimension size = getPageSize();
		return BigInteger.valueOf((int) (size.width * percent));
	}
	
	public BigInteger getCharSizeForPage(float percent) {
		Dimension size = getPageSize();
		return BigInteger.valueOf((int) (size.width * percent));
	}
	
	public BigInteger getHeightForPage(float percent) {
		Dimension size = getPageSize();
		return BigInteger.valueOf((int) (size.height * percent));
	}
	
	/**
	 * @Description: 设置表格列宽
	 */
	public BigInteger[]  setTableGridCol(Info info, int[] colWidths) {
		BigInteger[] colWidths_big = new BigInteger[colWidths.length];
		for (int j = 0, len = colWidths.length; j < len; j++) {
			colWidths_big[j] = BigInteger.valueOf(colWidths[j]);
		}

		return setTableGridCol(info, colWidths_big);
	}

	public BigInteger[]  setTableGridCol(Info info, BigInteger[] colWidths) {
		if (info == null || info.table == null)
			return colWidths;

		XWPFTable table = info.table;

		CTTbl ttbl = table.getCTTbl();
		CTTblGrid tblGrid = ttbl.getTblGrid() != null ? ttbl.getTblGrid() : ttbl.addNewTblGrid();
		for (int j = 0, len = colWidths.length; j < len; j++) {
			CTTblGridCol gridCol = tblGrid.addNewGridCol();
			gridCol.setW(colWidths[j]);
		}
		
		return colWidths;
	}
 
	public Info addImage(File file, String text) throws Exception{
		return addImage(file, text, null);
	}
	
	public Info addImage(File file, String text, Dimension size) throws Exception{
        return addImage(null, file, text, size);
	}
	
	public Info addImage(Info info, File imageFile, String text, Dimension size) throws Exception{
		return addImage(info, imageFile, text, size, ParagraphAlignment.CENTER);
	}
	
	public Info addImage(Info info, File imageFile, String text, Dimension size, ParagraphAlignment alignment) throws Exception{
		String imgFile = imageFile.getAbsolutePath();
		int format;
				
		if (imgFile.endsWith(".emf")) format = XWPFDocument.PICTURE_TYPE_EMF;
	    else if (imgFile.endsWith(".wmf")) format = XWPFDocument.PICTURE_TYPE_WMF;
	    else if (imgFile.endsWith(".pict")) format = XWPFDocument.PICTURE_TYPE_PICT;
	    else if (imgFile.endsWith(".jpeg") || imgFile.endsWith(".jpg")) format = XWPFDocument.PICTURE_TYPE_JPEG;
	    else if (imgFile.endsWith(".png")) format = XWPFDocument.PICTURE_TYPE_PNG;
	    else if (imgFile.endsWith(".dib")) format = XWPFDocument.PICTURE_TYPE_DIB;
	    else if (imgFile.endsWith(".gif")) format = XWPFDocument.PICTURE_TYPE_GIF;
	    else if (imgFile.endsWith(".tiff")) format = XWPFDocument.PICTURE_TYPE_TIFF;
	    else if (imgFile.endsWith(".eps")) format = XWPFDocument.PICTURE_TYPE_EPS;
	    else if (imgFile.endsWith(".bmp")) format = XWPFDocument.PICTURE_TYPE_BMP;
	    else if (imgFile.endsWith(".wpg")) format = XWPFDocument.PICTURE_TYPE_WPG;
	    else {
	        EditorEnvironment.showMessage("文件格式不支持: 仅支持 emf|wmf|pict|jpeg|png|dib|gif|tiff|eps|bmp|wpg");
	        return null;
	    }

		return addImage(info, new FileInputStream(imgFile), text, size, format, alignment);
	}
	
	public Info addImage(BufferedImage image, String text, Dimension size) throws Exception{
		byte[] imageData = ImageUtils.imageToBytes(image);
		return addImage(null, new ByteArrayInputStream(imageData), text, size, XWPFDocument.PICTURE_TYPE_JPEG);
	}
	
	public Info addImage(byte[] imageData, String text, Dimension size, int format) throws Exception{
		return addImage(null, new ByteArrayInputStream(imageData), text, size, format);
	}
	
	public Info addImage(Info info, InputStream stream, String text, Dimension size, int format) throws Exception{
		return addImage(info, stream, text, size, format, ParagraphAlignment.CENTER);
	}
	
	public Info addImage(Info info, InputStream stream, String text, Dimension size, int format, ParagraphAlignment alignment) throws Exception{
		if (info == null)
			info = addInfo();
		
		if (info.obj == null){
			info.obj = createParagraph();
			info.getRun();
		}
		
		setParagraphAlignInfo(info.obj, alignment, TextAlignment.CENTER);

		byte[] data = IOUtils.toByteArray(stream);
		BufferedImage image = ImageUtils.bytesToImage(data);
		if (size == null){
			size = new Dimension(image.getWidth(), image.getHeight());
		}else{
			boolean needResize = false;
			if (size.width > image.getWidth())
				size.width = image.getWidth();
			else{
				needResize = true;
			}
			if (size.height > image.getHeight())
				size.height = image.getHeight();
			else{
				needResize = true;
			}
			
			if (needResize){
				int width = image.getWidth();
				int height = image.getHeight();
				
				float div = (float)width / height;
				
				while (width > size.width || height > size.height){
					if (width > height){
						width -= 10;
						height = (int) (width / div);
					}else{
						height -= 10;
						width = (int) (height * div);
					}
				}
				
				size.width = width;
				size.height = height;
			}
		}

		info.getRun().addPicture(new ByteArrayInputStream(data), format, text, Units.toEMU(size.width), Units.toEMU(size.height));
		
	    return info;
	}
    
    protected void addStyle(String strStyleId, int headingLevel) {
	    CTStyle ctStyle = CTStyle.Factory.newInstance();
	    ctStyle.setStyleId(strStyleId);

	    CTString styleName = CTString.Factory.newInstance();
	    styleName.setVal(strStyleId);
	    ctStyle.setName(styleName);

	    CTDecimalNumber indentNumber = CTDecimalNumber.Factory.newInstance();
	    indentNumber.setVal(BigInteger.valueOf(headingLevel));

	    // lower number > style is more prominent in the formats bar
	    ctStyle.setUiPriority(indentNumber);

	    CTOnOff onoffnull = CTOnOff.Factory.newInstance();
	    ctStyle.setUnhideWhenUsed(onoffnull);

	    // style shows up in the formats bar
	    ctStyle.setQFormat(onoffnull);

	    // style defines a heading of the given level
	    CTPPr ppr = CTPPr.Factory.newInstance();
	    ppr.setOutlineLvl(indentNumber);
	    ctStyle.setPPr(ppr);

	    XWPFStyle style = new XWPFStyle(ctStyle);

	    // is a null op if already defined
	    XWPFStyles styles = createStyles();

	    style.setType(STStyleType.PARAGRAPH);
	    styles.addStyle(style);

	}
	
	public void addNewLine(){
		Info info = addInfo();
		info.obj = createParagraph();
	}
	
	public void addSpacePage(){
		Info info = addInfo();
		info.obj = createParagraph();
		
		info.getRun().addBreak(BreakType.PAGE);
	}
	
	public void setText(Info info, String text){
		if (info.obj == null)
			return;
		
		setText(info.getRun(), text);
	}
	
	public void setAlign(Info info, ParagraphAlignment alignment){
		if (info.obj == null)
			return;
		
        info.obj.setAlignment(alignment);  
	}

	public void setFontColor(Info info, Color color){
		if (info.obj == null)
			return;
		
        XWPFRun run = info.getRun();
        String colorString = Integer.toHexString(color.getRed());
        colorString += Integer.toHexString(color.getGreen());
        colorString += Integer.toHexString(color.getBlue());
        run.setColor(colorString);  
	}

	public void setColor(Info info, Color color){
		setColor(info, color, STShd.CLEAR);
	}
	
	public void setColor(Info info, Color color, STShd.Enum stShd){
		if (info.obj == null)
			return;
		
        XWPFRun run = info.getRun();
        String colorString = Integer.toHexString(color.getRed());
        colorString += Integer.toHexString(color.getGreen());
        colorString += Integer.toHexString(color.getBlue());
        CTShd cTShd = run.getCTR().addNewRPr().addNewShd();  
        cTShd.setVal(stShd);  
        cTShd.setFill(colorString);  
	}

	public void setFont(Info info, Font font){
		if (info.obj == null)
			return;
		
        XWPFRun run = info.getRun();
        run.setFontFamily(font.getFontName());
        run.setBold(font.isBold());
        run.setItalic(font.isItalic());
        run.setFontSize(font.getSize());
	}
	
	public void setUnderline(Info info, boolean b){
		if (b)
			setUnderline(info, UnderlinePatterns.SINGLE);
		else
			setUnderline(info, UnderlinePatterns.NONE);
	}
	
	public void setUnderline(Info info, UnderlinePatterns underlinePatterns){
		if (info.obj == null)
			return;
		
        XWPFRun run = info.getRun();
        run.setUnderline(underlinePatterns);
	}
	
	public void setStrikeline(Info info, boolean b){
		if (info.obj == null)
			return;
		
        XWPFRun run = info.getRun();
        run.setStrikeThrough(b);
	}
	
	public void setDoubleStrikeline(Info info, boolean b){
		if (info.obj == null)
			return;
		
        XWPFRun run = info.getRun();
        run.setDoubleStrikethrough(b);
	}
	
	public void setHighlight(Info info, boolean b){
		setHighlight(info, b ? STHighlightColor.DARK_BLUE : STHighlightColor.NONE);
	}
	
	public void setHighlight(Info info, STHighlightColor.Enum style){
		if (info.obj == null)
			return;
		
        XWPFRun run = info.getRun();
        run.getCTR().addNewRPr().addNewHighlight().setVal(style);
	}
	
	public void setEmboss(Info info, boolean b){
		if (info.obj == null)
			return;
		
        XWPFRun run = info.getRun();
        run.getCTR().addNewRPr().addNewEmboss().setVal(b ? STOnOff.TRUE : STOnOff.FALSE);
	}
	
	public void setImprint(Info info, boolean b){
		if (info.obj == null)
			return;
		
        XWPFRun run = info.getRun();
        run.getCTR().addNewRPr().addNewImprint().setVal(b ? STOnOff.TRUE : STOnOff.FALSE);
	}
	
	public void setShadow(Info info, boolean b){
		if (info.obj == null)
			return;
		
        XWPFRun run = info.getRun();
        run.getCTR().addNewRPr().addNewShadow().setVal(b ? STOnOff.TRUE : STOnOff.FALSE);
	}
	
	public void setVanish(Info info, boolean b){
		if (info.obj == null)
			return;
		
        XWPFRun run = info.getRun();
        run.getCTR().addNewRPr().addNewVanish().setVal(b ? STOnOff.TRUE : STOnOff.FALSE);
	}
	
	public void setOutline(Info info, boolean b){
		if (info.obj == null)
			return;
		
        XWPFRun run = info.getRun();
        run.getCTR().addNewRPr().addNewOutline().setVal(b ? STOnOff.TRUE : STOnOff.FALSE);
	}
	
	public void setEM(Info info, STEm.Enum style){
		if (info.obj == null)
			return;
		
        XWPFRun run = info.getRun();
        run.getCTR().addNewRPr().addNewEm().setVal(style);
	}
	
	public void setVertAlign(Info info, STVerticalAlignRun.Enum verticalAlign){
		if (info.obj == null)
			return;
		
        XWPFRun run = info.getRun();
        run.getCTR().addNewRPr().addNewVertAlign().setVal(verticalAlign);
	}
	
	public CTPPr getParagraphCTPPr(XWPFParagraph p) {
		CTPPr pPPr = null;
		if (p.getCTP() != null) {
			if (p.getCTP().getPPr() != null) {
				pPPr = p.getCTP().getPPr();
			} else {
				pPPr = p.getCTP().addNewPPr();
			}
		}
		return pPPr;
	}
	
	/**
	 * @Description: 设置段落间距信息,一行=100  一磅=20
	 */
	public void setParagraphSpacingInfo(Info info, boolean isSpace,
			String before, String after, String beforeLines, String afterLines,
			boolean isLine, String line, STLineSpacingRule.Enum lineValue) {
		
		if (info.obj == null)
			return;
		
		CTPPr pPPr = getParagraphCTPPr(info.obj);
		CTSpacing pSpacing = pPPr.getSpacing() != null ? pPPr.getSpacing()
				: pPPr.addNewSpacing();
		if (isSpace) {
			// 段前磅数
			if (before != null) {
				pSpacing.setBefore(new BigInteger(before));
			}
			// 段后磅数
			if (after != null) {
				pSpacing.setAfter(new BigInteger(after));
			}
			// 段前行数
			if (beforeLines != null) {
				pSpacing.setBeforeLines(new BigInteger(beforeLines));
			}
			// 段后行数
			if (afterLines != null) {
				pSpacing.setAfterLines(new BigInteger(afterLines));
			}
		}
		// 间距
		if (isLine) {
			if (line != null) {
				pSpacing.setLine(new BigInteger(line));
			}
			if (lineValue != null) {
				pSpacing.setLineRule(lineValue);
			}
		}
	}

	// 设置段落缩进信息 1厘米≈567
	public void setParagraphIndInfo(Info info, String firstLine,
			String firstLineChar, String hanging, String hangingChar,
			String right, String rigthChar, String left, String leftChar) {
		
		if (info.obj == null)
			return;
		
		CTPPr pPPr = getParagraphCTPPr(info.obj);
		CTInd pInd = pPPr.getInd() != null ? pPPr.getInd() : pPPr.addNewInd();
		if (firstLine != null) {
			pInd.setFirstLine(new BigInteger(firstLine));
		}
		if (firstLineChar != null) {
			pInd.setFirstLineChars(new BigInteger(firstLineChar));
		}
		if (hanging != null) {
			pInd.setHanging(new BigInteger(hanging));
		}
		if (hangingChar != null) {
			pInd.setHangingChars(new BigInteger(hangingChar));
		}
		if (left != null) {
			pInd.setLeft(new BigInteger(left));
		}
		if (leftChar != null) {
			pInd.setLeftChars(new BigInteger(leftChar));
		}
		if (right != null) {
			pInd.setRight(new BigInteger(right));
		}
		if (rigthChar != null) {
			pInd.setRightChars(new BigInteger(rigthChar));
		}
	}

	// 设置段落边框
	public void setParagraphBorders(XWPFParagraph p, Borders lborder,
			Borders tBorders, Borders rBorders, Borders bBorders,
			Borders btborders) {
		if (lborder != null) {
			p.setBorderLeft(lborder);
		}
		if (tBorders != null) {
			p.setBorderTop(tBorders);
		}
		if (rBorders != null) {
			p.setBorderRight(rBorders);
		}
		if (bBorders != null) {
			p.setBorderBottom(bBorders);
		}
		if (btborders != null) {
			p.setBorderBetween(btborders);
		}
	}

	/**
	 * @Description: 设置段落对齐
	 */
	public void setParagraphAlignInfo(XWPFParagraph p,
			ParagraphAlignment pAlign, TextAlignment valign) {
		if (pAlign != null) {
			p.setAlignment(pAlign);
		}
		if (valign != null) {
			p.setVerticalAlignment(valign);
		}
	}

	private void setXWPFRunStyle(XWPFRun r1,Font font) {
        r1.setFontSize(font.getSize());
        CTRPr rpr = r1.getCTR().isSetRPr() ? r1.getCTR().getRPr() : r1.getCTR().addNewRPr();
        CTFonts fonts = rpr.isSetRFonts() ? rpr.getRFonts() : rpr.addNewRFonts();
        fonts.setAscii(font.getName());
        fonts.setEastAsia(font.getName());
        fonts.setHAnsi(font.getName());
	}
	
	public Info addHeader(HeaderInfo[] datas) throws Exception{
		return addHeader(datas, null);
	}
	
	public Info addHeader(HeaderInfo[] datas, Font font) throws Exception{
		return addHeaderOrFooter(datas, HeaderType.htHeader, font);
    }
	
	public Info addFooter(HeaderInfo[] datas) throws Exception{
		return addFooter(datas, null);
	}
	
	public Info addFooter(HeaderInfo[] datas, Font font) throws Exception{
		return addHeaderOrFooter(datas, HeaderType.htFooter, font);
	}
	
	public enum HeaderType{
		htHeader, htFooter
	}
	
	public Info addHeaderOrFooter(HeaderInfo[] datas, HeaderType headerType, Font font) throws Exception{
		if (font == null){
			font = new Font("微软雅黑", 0, 9);
		}
		
		
		XWPFHeaderFooter header =  null;
		switch (headerType) {
		case htFooter:
			header = createFooter(HeaderFooterType.DEFAULT);
			break;
		case htHeader:
			header = createHeader(HeaderFooterType.DEFAULT);
			break;
		}
        XWPFParagraph paragraph = header.createParagraph();
        
        Info info = addInfo();
        info.obj = paragraph;
        setFont(info, font);
        
		paragraph.setAlignment(ParagraphAlignment.LEFT);
		paragraph.setVerticalAlignment(TextAlignment.CENTER);
		switch (headerType) {
		case htFooter:
			paragraph.setBorderTop(Borders.THICK);
			break;
		case htHeader:
			paragraph.setBorderBottom(Borders.THICK);
			paragraph.setSpacingBeforeLines((int) charsToTwip(1));
			break;
		}
		
		CTTabs tabs = paragraph.getCTP().getPPr().getTabs();
		
		if (tabs == null)
			tabs = paragraph.getCTP().getPPr().addNewTabs();

		TreeMap<Integer, HeaderInfo> sorts = new TreeMap<>();
		Dimension size = getTwipPageSize();
		for (HeaderInfo headerInfo : datas) {

			switch (headerInfo.alignment) {
			case LEFT:
				sorts.put(0, headerInfo);
				break;
			case RIGHT:
				sorts.put(2, headerInfo);
				break;
			default:
				sorts.put(1, headerInfo);
				break;
			}
		}
		
		for (HeaderInfo headerInfo : sorts.values()) {

			if (headerInfo.alignment != ParagraphAlignment.LEFT){
				String value = headerInfo.toObjectString();
				if (PageNumberHeaderInfo.is(value)){
					value = value.replace(PageNumberHeaderInfo.KEY, "100/100");
				}
				int width = 0;
				int twip = (int) charsToTwip(value.length());
				
				CTTabStop tabStop = tabs.addNewTab();
				switch (headerInfo.alignment) {
				case RIGHT:
					tabStop.setVal(STTabJc.RIGHT);
					width = (int) (size.width - twip - charsToTwip(5));
					break;
				default:
					tabStop.setVal(STTabJc.CENTER);
					width = size.width / 2 - twip / 2;
					break;
				}
		        tabStop.setPos(BigInteger.valueOf(width));
		        paragraph.createRun().addTab();
			}
			
			XWPFRun run = null;
			if (headerInfo instanceof PageNumberHeaderInfo) {
				PageNumberHeaderInfo pni = (PageNumberHeaderInfo) headerInfo;

				/*
				 * 生成页码 页码右对齐
				 */
				if (pni.prefix != null && !pni.prefix.isEmpty()){
					 run = paragraph.createRun();
					run.setText(pni.prefix);
					setXWPFRunStyle(run, font);
				}

				run = paragraph.createRun();
				CTFldChar fldChar = run.getCTR().addNewFldChar();
				fldChar.setFldCharType(STFldCharType.Enum.forString("begin"));

				run = paragraph.createRun();
				CTText ctText = run.getCTR().addNewInstrText();
				ctText.setStringValue("PAGE  \\* MERGEFORMAT");
				ctText.setSpace(SpaceAttribute.Space.Enum.forString("preserve"));
				setXWPFRunStyle(run, font);

				fldChar = run.getCTR().addNewFldChar();
				fldChar.setFldCharType(STFldCharType.Enum.forString("end"));

				run = paragraph.createRun();
				run.setText("/");
				setXWPFRunStyle(run, font);

				run = paragraph.createRun();
				fldChar = run.getCTR().addNewFldChar();
				fldChar.setFldCharType(STFldCharType.Enum.forString("begin"));

				run = paragraph.createRun();
				ctText = run.getCTR().addNewInstrText();
				ctText.setStringValue("NUMPAGES  \\* MERGEFORMAT ");
				ctText.setSpace(SpaceAttribute.Space.Enum.forString("preserve"));
				setXWPFRunStyle(run, font);

				fldChar = run.getCTR().addNewFldChar();
				fldChar.setFldCharType(STFldCharType.Enum.forString("end"));

				if (pni.suxfix != null && !pni.suxfix.isEmpty()){
					run = paragraph.createRun();
					run.setText(pni.suxfix);
					setXWPFRunStyle(run, font);
				}	
			} else {
				if (headerInfo.object instanceof File) {
					addImage(info, (File)headerInfo.object, headerInfo.toObjectString(), null, info.obj.getAlignment());
				} else {
					run = paragraph.createRun();
					setText(run, headerInfo.toObjectString());
					setXWPFRunStyle(run, font);
				}
			}
		}

        return info;
	}
	
	public void save() throws FileNotFoundException, IOException{
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		try(FileOutputStream stream = new FileOutputStream(file)){			
			write(stream);
		}catch (Exception e) {
		}
	}
}
