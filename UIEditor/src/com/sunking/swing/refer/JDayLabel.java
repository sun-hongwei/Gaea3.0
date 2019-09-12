package com.sunking.swing.refer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import com.sunking.swing.OpenSwingUtil;


/**
 * <p>Title: JDatePicker</p>
 * <p>Description:JDayLable ��ѡ�����ڹ��ܵ�JLabel </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author <a href="mailto:sunkingxie@hotmail.com"'>Sunking</a>
 * @version 1.0
 */


public class JDayLabel extends JLabel{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static ImageIcon todayIcon =
        OpenSwingUtil.getOpenSwingImage("today.gif", new ImageIcon());

    Date date = null;
    ImageIcon currentIcon = null;

    /**
     * ���ڸ�ʽ��TODAY/TIP�ã�
     */
    final SimpleDateFormat dateFormat
        = new SimpleDateFormat("yyyy/MM/dd");
    /**
     * �ո�ʽ
     */
    final SimpleDateFormat dayFormat = new SimpleDateFormat("d");

    public JDayLabel(Date date){
        this(date, true);
    }

    public JDayLabel(Date date, boolean isSmallLabel){
        setPreferredSize(new Dimension(40, 20));
        setToolTipText(dateFormat.format(date));
        this.date = date;
        if(isSmallLabel){
            setHorizontalAlignment(JLabel.CENTER);
            setText(dayFormat.format(date));
            Date d = new Date();
            if(dateFormat.format(date).equals(dateFormat.format(d))){
                currentIcon = todayIcon;
            }
        } else{
            setText("Today:" + dateFormat.format(new Date()));
            setIcon(todayIcon);
            setHorizontalAlignment(JLabel.LEFT);
        }
    }

    public Date getDate(){
        return date;
    }
    public void setDate(Date date){
        this.date = date;
    }
    public void paint(Graphics g){
        super.paint(g);
        if(currentIcon != null && isEnabled()){
            int x = (this.getWidth() - currentIcon.getIconWidth()) / 2;
            int y = (this.getHeight() - currentIcon.getIconHeight()) / 2;
            currentIcon.paintIcon(this, g, x, y);
        }
    }
}
