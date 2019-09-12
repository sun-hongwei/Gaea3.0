package com.sunking.swing.print.content;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.print.PageFormat;
import java.util.Vector;

/**
 * <p>Title: OpenSwing</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author <a href="mailto:sunkingxie@hotmail.com">SunKing</a>
 * @version 1.0
 */
public class PrintTabString
    implements PrintContentInterface {
    String printString[];
    int xWidth[];

    /**
     * ��һ���ı���xWidth�����ַ���s;
     * @param s
     * @param xWidth
     */
    public PrintTabString(String s[], int xWidth[]) {
        if (s.length != xWidth.length) {
            System.err.println("\u53C2\u6570\u6784\u9020\u9519\u8BEF!");
        }
        this.printString = s;
        this.xWidth = xWidth;
    }

    public void draw(Graphics g, Point p, PageFormat pf, int startPos) {
        FontMetrics fm = g.getFontMetrics();
        int pw = (int) (pf.getImageableWidth() - startPos);

        /**
         * ���������ʵ��ÿ���ĳ���
         */
        int len[] = new int[xWidth.length];
        int totalLen = 0;
        for (int i = 0; i < xWidth.length; i++) {
            totalLen += xWidth[i];
        }
        int everyLen = (pw - len.length * 5) / totalLen; //5�Ǹ���λ�м�ļ��
        for (int i = 0; i < len.length; i++) {
            len[i] = everyLen * xWidth[i];
        }

        int currentY = p.y; //��ס��ǰ��������λ��
        int maxY = p.y;
        for (int i = 0; i < printString.length; i++) {
            maxY = Math.max(maxY, p.y);
            p.y = currentY;
            String fs[] = fenjie(printString[i], fm, len[i]);
            for (int j = 0; j < fs.length; j++) {
                g.drawString(fs[j], p.x, p.y);
                p.y += fm.getHeight();
            }
            p.x += len[i] + 5; //5�Ǹ���λ�м�ļ��
        }
        p.y = maxY - fm.getHeight();
    }

    /**
     * ��ָ������,���ַ����ֽ�ɶ���
     */
    String[] fenjie(String s, FontMetrics fm, int len) {
        if (s == null)return new String[] {
            ""};
        if (fm.stringWidth(s) <= len)return new String[] {
            s};
        Vector<String> v = new Vector<>();
        while (true) {
            if (s == null)break;
            if (fm.stringWidth(s) <= len) {
                v.add(s);
                break;
            }
            for (int i = 0; i < s.length(); i++) {
                String sub = s.substring(0, i + 1);
                if (sub.indexOf("\n") != -1) {
                    v.add(s.substring(0, i));
                    s = s.substring(i + 1);
                }
                else if (fm.stringWidth(sub) >= len) {
                    v.add(s.substring(0, i));
                    s = s.substring(i);
                    break;
                }
                else if (sub.length() == s.length()) {
                    v.add(s);
                    s = null;
                    break;
                }
            }
        }
        String ss[] = new String[v.size()];
        v.copyInto(ss);
        return ss;
    }
}
