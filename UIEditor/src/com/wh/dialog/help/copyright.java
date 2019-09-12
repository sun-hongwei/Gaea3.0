package com.wh.dialog.help;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;

public class copyright extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			copyright dialog = new copyright();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public copyright() {
		setBounds(100, 100, 808, 636);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JScrollPane scrollPane = new JScrollPane();
			contentPanel.add(scrollPane, BorderLayout.CENTER);
			{
				JTextPane textPane = new JTextPane();
				textPane.setText("Gaea系统 软件最终用户许可协议\r\n重要须知━请认真阅读：本《最终用户许可协议》（以下称《协议》）是您与 [Gaea]系统之间有关[Gaea]系统软件产品的法律协议。 \r\n本“软件产品”包括计算机软件，并可能包括相关媒体、印刷材料和“联机”或电子文档（“软件产品”）。本“软件产品”还包括对Gaea系统提供给您的原“软件产品”的任何更新和补充资料。任何与本“软件产品”一同提供给您的并与单独一份最终用户许可证相关的软件产品是根据那份许可协议中的条款而授予您。您一旦安装、复制、下载、访问或以其它方式使用“软件产品”，即表示您同意接受本《协议》各项条款的约束。如您不同意本《协议》中的条款，请不要安装或使用“软件产品”；但您可将其退回原购买处，并获得全额退款。\r\n软件产品许可证\r\n本“软件产品”受袒护著作权法及国际著作权条约和其它知识产权法和条约的保护。本“软件产品”只许可使用，而不出售。\r\n1．许可证的授予。本《协议》授予您下列权利：．应用软件。您可在单一一台计算机上安装、使用、访问、显示、运行或以其它方 \r\n式互相作用于（“运行”）本“软件产品” （或适用于同一操作系统的任何前版本）的一份副本。运行“软件产品”的计算机的主要用户可以制作另一份副本，仅供在其在安装到公司其他电脑管理注册后的同一项目之用。\r\n．储存／网络用途。您还可以在您公司的其它计算机上运行“软件产品”但仅限于注册时所添之项目，您必须为增加的每个项目获得一份许可证。\r\n．保留权利。未明示授予的一切其它权利均为王岩个人所有。\r\n2．其它权利和限制的说明。 \r\n．试用版本。仅限于试用，如需正式使用，必须注册成为正式版。\r\n．组件的分隔。本“软件产品”是作为单一产品而被授予使用许可的。您不得将 \r\n其组成部分分开在多台计算机上使用。\r\n．商标。本《协议》不授予您有关任何Gaea系统商标或服务商标的任何 \r\n权利。 \r\n．出租。不得出租、租赁或出借本“软件产品”。\r\n．支持服务。我可能为您提供 \r\n与“软件产品”有关的支持服务（“支持服务”）。支持服务的使用受用户手册、“联机”文档和/或其它提供的材料中所述的各项政策和计划的制约。提供给您作为支持服务的一部份的任何附加软件代码应被视为本“软件产品”的一部分，并须符合本《协议》中的各项条款和条件。\r\n．软件转让。本\"软件产品\"的第一被许可人不可以对本《协议》及“软件产品”直接或间接向任何用户作转让。");
				scrollPane.setViewportView(textPane);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("确认");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
	}

}
