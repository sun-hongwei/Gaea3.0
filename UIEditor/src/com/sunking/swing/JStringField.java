package com.sunking.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.io.Serializable;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

//import java.awt.event.*;
/**
 * <p>Title: OpenSwing</p>
 * <p>Description: JStringField ���ƿ����볤�ȵ����������</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author <a href="mailto:sunkingxie@hotmail.com">SunKing</a>
 * @version 1.0
 */
public class JStringField
    extends JTextField
    implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JStringField() {
        this(255);
    }

    public JStringField(int MaxLen) {
        setPreferredSize(new Dimension(100, 25));
        setDocument(new StringDocument(MaxLen));
    }

    public static void main(String[] args){
        JFrame frame = OpenSwingUtil.createDemoFrame("JStringField Demo");
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(new JStringField(10), BorderLayout.CENTER);
        frame.setVisible(true);
    }
}

class StringDocument
    extends PlainDocument {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int MaxLength;
    public StringDocument(int MaxLen) {
        MaxLength = MaxLen;
    }

    public void insertString(int offset, String s, AttributeSet attributeSet) throws
        BadLocationException {
        int sLen = s.length();
        int conLen = getLength();
        if ( ( (sLen + conLen) > MaxLength) || s.equals("\'") || s.equals("\"")) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        super.insertString(offset, s, null);
    }
}
